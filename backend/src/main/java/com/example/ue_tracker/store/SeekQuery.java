package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.adapter.UeEventAdapter;
import com.example.ue_tracker.model.PaginationStrategy;
import com.example.ue_tracker.pagination.CursorCodec;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared OFFSET / KEYSET pagination over any event table. The seek tiebreaker is
 * {@code id} for history tables (imsi != null) and {@code imsi_or_supi} for latest tables.
 */
@Component
public class SeekQuery {

    private final DataSource ds;
    private final JdbcTemplate jdbc;
    private final UeEventAdapter adapter;
    private final CursorCodec codec;

    SeekQuery(DataSource ds, JdbcTemplate jdbc, UeEventAdapter adapter, CursorCodec codec) {
        this.ds = ds; this.jdbc = jdbc; this.adapter = adapter; this.codec = codec;
    }

    public PageResult query(String table, String imsi, PaginationStrategy strategy,
                            int page, String cursorToken, int size) {
        boolean history = imsi != null;
        String tieKey = history ? "id" : "imsi_or_supi";

        if (strategy == PaginationStrategy.OFFSET) {
            String sql = "SELECT * FROM " + table + (history ? " WHERE imsi_or_supi = ?" : "")
                    + " ORDER BY updated_at DESC, " + tieKey + " DESC OFFSET ? LIMIT ?";
            long[] lastId = {0};
            List<UeEvent> rows = run(sql, ps -> {
                int i = 1;
                if (history) ps.setString(i++, imsi);
                ps.setInt(i++, page * size);
                ps.setInt(i, size + 1);
            }, lastId);
            boolean hasNext = rows.size() > size;
            if (hasNext) rows = rows.subList(0, size);
            long total = estimate(table);
            int totalPages = (int) Math.max(1, Math.ceil((double) total / size));
            return new PageResult(rows, total, totalPages, page, "", hasNext);
        }

        // KEYSET
        Instant curTs = null; long curId = 0; String curImsi = null; boolean hasCursor = false;
        String[] parts = codec.decodeLatest(cursorToken); // [micros, tie]
        if (parts != null) {
            long micros = Long.parseLong(parts[0]);
            curTs = Instant.ofEpochSecond(micros / 1_000_000L, (micros % 1_000_000L) * 1_000L);
            if (history) curId = Long.parseLong(parts[1]); else curImsi = parts[1];
            hasCursor = true;
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table).append(" WHERE 1=1");
        if (history) sql.append(" AND imsi_or_supi = ?");
        if (hasCursor) sql.append(" AND (updated_at, ").append(tieKey).append(") < (?, ?)");
        sql.append(" ORDER BY updated_at DESC, ").append(tieKey).append(" DESC LIMIT ?");

        final Instant cts = curTs; final long cid = curId; final String cimsi = curImsi; final boolean hc = hasCursor;
        long[] lastId = {0};
        List<UeEvent> rows = run(sql.toString(), ps -> {
            int i = 1;
            if (history) ps.setString(i++, imsi);
            if (hc) {
                ps.setTimestamp(i++, Timestamp.from(cts));
                if (history) ps.setLong(i++, cid); else ps.setString(i++, cimsi);
            }
            ps.setInt(i, size + 1);
        }, lastId);

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);
        String next = "";
        if (hasNext && !rows.isEmpty()) {
            UeEvent last = rows.get(rows.size() - 1);
            String tie = history ? String.valueOf(lastId[0]) : last.getImsiOrSupi();
            next = codec.encodeLatest(Instant.parse(last.getUpdatedAt()), tie);
        }
        return new PageResult(rows, 0, 0, 0, next, hasNext);
    }

    @FunctionalInterface
    private interface PsSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<UeEvent> run(String sql, PsSetter setter, long[] lastId) {
        List<UeEvent> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasIdCol = hasColumn(rs, "id");
                while (rs.next()) {
                    out.add(adapter.fromRow(rs));
                    if (hasIdCol) lastId[0] = rs.getLong("id");
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    private static boolean hasColumn(ResultSet rs, String name) throws SQLException {
        var md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (name.equalsIgnoreCase(md.getColumnLabel(i))) return true;
        }
        return false;
    }

    private long estimate(String table) {
        Long e = jdbc.queryForObject(
                "SELECT reltuples::bigint FROM pg_class WHERE relname = ?", Long.class, table);
        return e == null || e < 0 ? 0 : e;
    }
}
