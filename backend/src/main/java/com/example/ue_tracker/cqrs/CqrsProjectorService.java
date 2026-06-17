package com.example.ue_tracker.cqrs;

import com.example.ue_tracker.store.EventSql;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Drains the CQRS outbox into the read tables. Uses {@code FOR UPDATE SKIP LOCKED}
 * so concurrent drains never double-project. Scheduled, but also callable directly.
 *
 * <p>A {@link ReentrantLock} serializes draining against exclusive operations like a
 * TRUNCATE (clear): the drain takes the lock per batch and releases it between batches,
 * so {@link #runExclusive} (used by clear) gets in within one batch instead of waiting
 * for the whole backlog — and the two never deadlock on the CQRS tables.
 */
@Service
public class CqrsProjectorService {

    private final JdbcTemplate jdbc;
    private final ReentrantLock lock = new ReentrantLock();
    private final java.util.concurrent.atomic.AtomicLong projectedRows = new java.util.concurrent.atomic.AtomicLong();
    @Value("${benchmark.projector.batch-size:2000}") int batchSize;

    public CqrsProjectorService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Total rows projected into the CQRS read tables since startup (writes that hit the read side). */
    public long projectedRows() { return projectedRows.get(); }

    @Scheduled(fixedDelayString = "${benchmark.projector.fixed-delay:1000}")
    public void scheduledDrain() {
        while (true) {
            // yield immediately if an exclusive op (clear) is waiting, else this loop's
            // non-fair re-acquire would starve it for the whole backlog
            if (lock.hasQueuedThreads() || !lock.tryLock()) return;
            int n;
            try { n = drainOnce(batchSize); } finally { lock.unlock(); }
            if (n == 0) return;
        }
    }

    /** Run {@code action} with draining paused, so TRUNCATE etc. can't deadlock the projector. */
    public void runExclusive(Runnable action) {
        lock.lock();
        try { action.run(); } finally { lock.unlock(); }
    }

    /** Drains up to {@code batch} outbox rows; returns number projected. */
    @Transactional
    public int drainOnce(int batch) {
        List<Map<String, Object>> claimed = jdbc.queryForList(
                "SELECT seq FROM cqrs_outbox ORDER BY seq " +
                "FOR UPDATE SKIP LOCKED LIMIT ?", batch);
        if (claimed.isEmpty()) return 0;

        // seqs are DB-generated bigints, not user input -> safe to inline
        String seqs = claimed.stream().map(r -> String.valueOf(r.get("seq")))
                .collect(Collectors.joining(","));

        // Join write_history on the full PK (imsi_or_supi, id) so the lookup uses the
        // partition key -> partition pruning + PK index (NOT a seq scan over all partitions).
        String cols = EventSql.COLS;
        String whCols = EventSql.prefixedCols("wh.");
        String joined =
                "FROM cqrs_outbox o " +
                "JOIN cqrs_write_history wh ON wh.imsi_or_supi = o.imsi_or_supi AND wh.id = o.write_history_id " +
                "WHERE o.seq IN (" + seqs + ")";

        jdbc.update("INSERT INTO cqrs_read_history (" + cols + ") SELECT " + whCols + " " + joined);
        jdbc.update("INSERT INTO cqrs_read_latest (" + cols + ") " +
                "SELECT DISTINCT ON (wh.imsi_or_supi) " + whCols + " " + joined +
                " ORDER BY wh.imsi_or_supi, wh.updated_at DESC " +
                EventSql.onConflictUpdate("cqrs_read_latest"));
        jdbc.update("DELETE FROM cqrs_outbox WHERE seq IN (" + seqs + ")");
        projectedRows.addAndGet(claimed.size());
        return claimed.size();
    }

    public long backlog() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class);
        return n == null ? 0 : n;
    }
}
