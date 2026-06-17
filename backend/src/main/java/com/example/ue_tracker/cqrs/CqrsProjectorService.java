package com.example.ue_tracker.cqrs;

import com.example.ue_tracker.store.EventSql;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Drains the CQRS outbox (on the PRIMARY db) into the read tables (on a SEPARATE read db).
 * Because the two sides are different databases, projection reads the source rows from primary
 * and inserts them into the read db in Java (no cross-db SQL join). Idempotent via PK conflict,
 * so the at-least-once delete-after-insert ordering can't create duplicates.
 *
 * <p>A {@link ReentrantLock} serializes draining against exclusive ops (clear): the drain takes
 * the lock per batch and yields the moment an exclusive op is queued.
 */
@Service
public class CqrsProjectorService {

    private final JdbcTemplate primaryJdbc;
    private final JdbcTemplate readJdbc;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicLong projectedRows = new AtomicLong();
    @Value("${benchmark.projector.batch-size:2000}") int batchSize;

    private static final String[] COL_ARR = EventSql.COLS.split(",");

    public CqrsProjectorService(JdbcTemplate primaryJdbc,
                                @Qualifier("readJdbcTemplate") JdbcTemplate readJdbc) {
        this.primaryJdbc = primaryJdbc;
        this.readJdbc = readJdbc;
    }

    /** Total rows projected into the CQRS read db since startup (writes that hit the read side). */
    public long projectedRows() { return projectedRows.get(); }

    @Scheduled(fixedDelayString = "${benchmark.projector.fixed-delay:1000}")
    public void scheduledDrain() {
        while (true) {
            if (lock.hasQueuedThreads() || !lock.tryLock()) return;
            int n;
            try { n = drainOnce(batchSize); } finally { lock.unlock(); }
            if (n == 0) return;
        }
    }

    /** Run {@code action} with draining paused (e.g. clear). */
    public void runExclusive(Runnable action) {
        lock.lock();
        try { action.run(); } finally { lock.unlock(); }
    }

    /** Drains up to {@code batch} outbox rows from primary into the read db; returns number projected. */
    public int drainOnce(int batch) {
        List<Map<String, Object>> claimed = primaryJdbc.queryForList(
                "SELECT seq FROM cqrs_outbox ORDER BY seq LIMIT ?", batch);
        if (claimed.isEmpty()) return 0;
        String seqs = claimed.stream().map(r -> String.valueOf(r.get("seq"))).collect(Collectors.joining(","));

        // fetch the source rows (write_history) for the claimed outbox entries, on primary
        List<Map<String, Object>> rows = primaryJdbc.queryForList(
                "SELECT wh.id, " + EventSql.prefixedCols("wh.") +
                " FROM cqrs_outbox o JOIN cqrs_write_history wh " +
                "  ON wh.imsi_or_supi = o.imsi_or_supi AND wh.id = o.write_history_id " +
                "WHERE o.seq IN (" + seqs + ")");

        // write into the read db (idempotent), then drop the outbox entries on primary
        if (!rows.isEmpty()) {
            readJdbc.batchUpdate(historyInsertSql(), rowArgs(rows, true));
            // dedup per IMSI (keep newest) so the rewritten multi-row upsert never hits the
            // same ON CONFLICT key twice ("cannot affect row a second time")
            readJdbc.batchUpdate(latestUpsertSql(), latestArgs(rows));
        }
        primaryJdbc.update("DELETE FROM cqrs_outbox WHERE seq IN (" + seqs + ")");
        projectedRows.addAndGet(claimed.size());
        return claimed.size();
    }

    public long backlog() {
        Long n = primaryJdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class);
        return n == null ? 0 : n;
    }

    private static String historyInsertSql() {
        return "INSERT INTO cqrs_read_history (id," + EventSql.COLS + ") VALUES ("
                + placeholders(COL_ARR.length + 1) + ") ON CONFLICT (imsi_or_supi, id) DO NOTHING";
    }

    private static String latestUpsertSql() {
        return "INSERT INTO cqrs_read_latest (" + EventSql.COLS + ") VALUES ("
                + placeholders(COL_ARR.length) + ") " + EventSql.onConflictUpdate("cqrs_read_latest");
    }

    private static List<Object[]> rowArgs(List<Map<String, Object>> rows, boolean withId) {
        List<Object[]> out = new ArrayList<>(rows.size());
        for (Map<String, Object> r : rows) out.add(toArgs(r, withId));
        return out;
    }

    /** One row per IMSI (the newest), so the rewritten multi-row upsert has no duplicate keys. */
    private static List<Object[]> latestArgs(List<Map<String, Object>> rows) {
        Map<String, Map<String, Object>> newest = new java.util.LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            String imsi = (String) r.get("imsi_or_supi");
            Map<String, Object> ex = newest.get(imsi);
            if (ex == null || compareUpdatedAt(r, ex) >= 0) newest.put(imsi, r);
        }
        List<Object[]> out = new ArrayList<>(newest.size());
        for (Map<String, Object> r : newest.values()) out.add(toArgs(r, false));
        return out;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compareUpdatedAt(Map<String, Object> a, Map<String, Object> b) {
        Object ua = a.get("updated_at"), ub = b.get("updated_at");
        if (ua instanceof Comparable && ub != null) return ((Comparable) ua).compareTo(ub);
        return 0;
    }

    private static Object[] toArgs(Map<String, Object> r, boolean withId) {
        Object[] args = new Object[withId ? COL_ARR.length + 1 : COL_ARR.length];
        int i = 0;
        if (withId) args[i++] = r.get("id");
        for (String c : COL_ARR) args[i++] = r.get(c);
        return args;
    }

    private static String placeholders(int n) {
        return String.join(",", java.util.Collections.nCopies(n, "?"));
    }
}
