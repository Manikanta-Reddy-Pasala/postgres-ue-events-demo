package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.adapter.UeEventAdapter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * OFFSET pagination over any event table.
 * <ul>
 *   <li>Latest tables (imsi == null): optional free-text filter on IMSI / MSISDN / RAT.</li>
 *   <li>History tables (imsi != null): rows for that one IMSI.</li>
 * </ul>
 * Counts are exact {@code count(*)} with the same predicate — cheap here because the latest
 * table is bounded by IMSI cardinality and history is always IMSI-filtered.
 */
@Component
public class EventQuery {

    private final DataSource ds;
    private final UeEventAdapter adapter;

    EventQuery(DataSource ds, UeEventAdapter adapter) {
        this.ds = ds; this.adapter = adapter;
    }

    public PageResult page(String table, String imsi, String filter, int page, int size) {
        boolean history = imsi != null;
        boolean hasFilter = !history && filter != null && !filter.isBlank();
        String tieKey = history ? "id" : "imsi_or_supi";

        StringBuilder where = new StringBuilder();
        if (history) where.append(" WHERE imsi_or_supi = ?");
        if (hasFilter) where.append(" WHERE (imsi_or_supi ILIKE ? OR msisdn ILIKE ? OR rat ILIKE ?)");

        String like = hasFilter ? "%" + filter.trim() + "%" : null;

        long total = count("SELECT count(*) FROM " + table + where, ps -> {
            if (history) ps.setString(1, imsi);
            if (hasFilter) { ps.setString(1, like); ps.setString(2, like); ps.setString(3, like); }
        });

        String sql = "SELECT * FROM " + table + where
                + " ORDER BY updated_at DESC, " + tieKey + " DESC OFFSET ? LIMIT ?";
        List<UeEvent> rows = run(sql, ps -> {
            int i = 1;
            if (history) ps.setString(i++, imsi);
            if (hasFilter) { ps.setString(i++, like); ps.setString(i++, like); ps.setString(i++, like); }
            ps.setInt(i++, page * size);
            ps.setInt(i, size);
        });

        int totalPages = (int) Math.max(1, Math.ceil((double) total / size));
        return new PageResult(rows, total, totalPages, page);
    }

    @FunctionalInterface
    private interface PsSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<UeEvent> run(String sql, PsSetter setter) {
        List<UeEvent> out = new ArrayList<>();
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(adapter.fromRow(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return out;
    }

    private long count(String sql, PsSetter setter) {
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
