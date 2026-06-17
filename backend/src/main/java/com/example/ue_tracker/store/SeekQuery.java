package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.adapter.UeEventAdapter;
import com.example.ue_tracker.model.PaginationStrategy;
import com.example.ue_tracker.pagination.CursorCodec;
import com.example.ue_tracker.pagination.PageCursor;
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
        return strategy == PaginationStrategy.OFFSET
                ? offsetPage(table, imsi, history, tieKey, page, size)
                : keysetPage(table, imsi, history, tieKey, cursorToken, size);
    }

    private PageResult offsetPage(String table, String imsi, boolean history,
                                  String tieKey, int page, int size) {
        String sql = "SELECT * FROM " + table + (history ? " WHERE imsi_or_supi = ?" : "")
                + " ORDER BY updated_at DESC, " + tieKey + " DESC OFFSET ? LIMIT ?";
        List<Row> rows = run(sql, history, ps -> {
            int i = 1;
            if (history) ps.setString(i++, imsi);
            ps.setInt(i++, page * size);
            ps.setInt(i, size + 1);
        });
        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);
        long total = estimate(table);
        int totalPages = (int) Math.max(1, Math.ceil((double) total / size));
        return new PageResult(events(rows), total, totalPages, page, "", hasNext);
    }

    private PageResult keysetPage(String table, String imsi, boolean history,
                                  String tieKey, String cursorToken, int size) {
        PageCursor cur = codec.decode(cursorToken);
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table).append(" WHERE 1=1");
        if (history) sql.append(" AND imsi_or_supi = ?");
        if (cur != null) sql.append(" AND (updated_at, ").append(tieKey).append(") < (?, ?)");
        sql.append(" ORDER BY updated_at DESC, ").append(tieKey).append(" DESC LIMIT ?");

        List<Row> rows = run(sql.toString(), history, ps -> {
            int i = 1;
            if (history) ps.setString(i++, imsi);
            if (cur != null) {
                ps.setTimestamp(i++, Timestamp.from(cur.updatedAt()));
                if (history) ps.setLong(i++, Long.parseLong(cur.tie())); else ps.setString(i++, cur.tie());
            }
            ps.setInt(i, size + 1);
        });
        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);
        String next = "";
        if (hasNext && !rows.isEmpty()) {
            Row last = rows.get(rows.size() - 1);
            String tie = history ? String.valueOf(last.id()) : last.event().getImsiOrSupi();
            next = codec.encode(Instant.parse(last.event().getUpdatedAt()), tie);
        }
        return new PageResult(events(rows), 0, 0, 0, next, hasNext);
    }

    /** A fetched row plus its history id (0 for latest tables, which have no id). */
    private record Row(UeEvent event, long id) {}

    @FunctionalInterface
    private interface PsSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<Row> run(String sql, boolean history, PsSetter setter) {
        List<Row> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Row(adapter.fromRow(rs), history ? rs.getLong("id") : 0L));
                }
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    private static List<UeEvent> events(List<Row> rows) {
        List<UeEvent> out = new ArrayList<>(rows.size());
        for (Row r : rows) out.add(r.event());
        return out;
    }

    private long estimate(String table) {
        Long e = jdbc.queryForObject(
                "SELECT reltuples::bigint FROM pg_class WHERE relname = ?", Long.class, table);
        return e == null || e < 0 ? 0 : e;
    }
}
