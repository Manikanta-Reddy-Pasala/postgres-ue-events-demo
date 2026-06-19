package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.adapter.UeEventAdapter;

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
 *   <li>Latest tables (imsi == null): zero or more field filters, ANDed together
 *       (IMSI / MSISDN / RAT / provider / country), each an {@code ILIKE} substring match.</li>
 *   <li>History tables (imsi != null): rows for that one IMSI.</li>
 * </ul>
 * Results sort by {@code updated_at} in the requested direction (tie-broken by the table key).
 * Counts are exact {@code count(*)} with the same predicate — cheap here because the latest
 * table is bounded by IMSI cardinality and history is always IMSI-filtered.
 */
public class EventQuery {

    /** Optional per-field filters for the latest table; null/blank fields are ignored, present ones ANDed. */
    public record Filters(String imsi, String msisdn, String rat, String provider, String country) {
        public static final Filters NONE = new Filters(null, null, null, null, null);
    }

    private final DataSource ds;
    private final UeEventAdapter adapter;

    public EventQuery(DataSource ds, UeEventAdapter adapter) {
        this.ds = ds; this.adapter = adapter;
    }

    public PageResult page(String table, String imsi, Filters filters, boolean ascending, int page, int size) {
        boolean history = imsi != null;
        String tieKey = history ? "id" : "imsi_or_supi";

        List<String> clauses = new ArrayList<>();
        List<String> binds = new ArrayList<>();
        if (history) { clauses.add("imsi_or_supi = ?"); binds.add(imsi); }
        if (!history && filters != null) {
            addLike(clauses, binds, "imsi_or_supi", filters.imsi());
            addLike(clauses, binds, "msisdn", filters.msisdn());
            addLike(clauses, binds, "rat", filters.rat());
            addLike(clauses, binds, "provider_name", filters.provider());
            addLike(clauses, binds, "country_name", filters.country());
        }
        String where = clauses.isEmpty() ? "" : " WHERE " + String.join(" AND ", clauses);
        String dir = ascending ? "ASC" : "DESC";

        long total = count("SELECT count(*) FROM " + table + where, ps -> bind(ps, binds));

        String sql = "SELECT * FROM " + table + where
                + " ORDER BY updated_at " + dir + ", " + tieKey + " " + dir + " OFFSET ? LIMIT ?";
        List<UeEvent> rows = run(sql, ps -> {
            int i = bind(ps, binds);
            ps.setInt(i++, page * size);
            ps.setInt(i, size);
        });

        int totalPages = (int) Math.max(1, Math.ceil((double) total / size));
        return new PageResult(rows, total, totalPages, page);
    }

    /** Append {@code col ILIKE %val%} (and its bind) only when {@code val} is non-blank. */
    private static void addLike(List<String> clauses, List<String> binds, String col, String val) {
        if (val != null && !val.isBlank()) {
            clauses.add(col + " ILIKE ?");
            binds.add("%" + val.trim() + "%");
        }
    }

    /** Bind every filter param in order; returns the next 1-based index for trailing params. */
    private static int bind(PreparedStatement ps, List<String> binds) throws SQLException {
        int i = 1;
        for (String b : binds) ps.setString(i++, b);
        return i;
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
