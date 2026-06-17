package com.example.ue_tracker.cqrs;

import com.example.ue_tracker.store.CopySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Drains the CQRS outbox into the read tables. Uses {@code FOR UPDATE SKIP LOCKED}
 * so concurrent drains never double-project. Scheduled, but also callable directly.
 */
@Service
public class CqrsProjectorService {

    private final JdbcTemplate jdbc;
    @Value("${benchmark.projector.batch-size:2000}") int batchSize;

    public CqrsProjectorService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(fixedDelayString = "${benchmark.projector.fixed-delay:1000}")
    public void scheduledDrain() {
        int n;
        do { n = drainOnce(batchSize); } while (n > 0);
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
        String cols = CopySupport.COLS;
        String whCols = prefix(cols, "wh.");
        String joined =
                "FROM cqrs_outbox o " +
                "JOIN cqrs_write_history wh ON wh.imsi_or_supi = o.imsi_or_supi AND wh.id = o.write_history_id " +
                "WHERE o.seq IN (" + seqs + ")";

        jdbc.update("INSERT INTO cqrs_read_history (" + cols + ") SELECT " + whCols + " " + joined);
        jdbc.update("INSERT INTO cqrs_read_latest (" + cols + ") " +
                "SELECT DISTINCT ON (wh.imsi_or_supi) " + whCols + " " + joined +
                " ORDER BY wh.imsi_or_supi, wh.updated_at DESC " +
                "ON CONFLICT (imsi_or_supi) DO UPDATE SET " +
                "updated_at = EXCLUDED.updated_at, action_taken = EXCLUDED.action_taken, " +
                "rat = EXCLUDED.rat, rssi = EXCLUDED.rssi, provider_name = EXCLUDED.provider_name, " +
                "country_name = EXCLUDED.country_name, msisdn = EXCLUDED.msisdn, " +
                "distance_in_meters = EXCLUDED.distance_in_meters " +
                "WHERE EXCLUDED.updated_at >= cqrs_read_latest.updated_at");
        jdbc.update("DELETE FROM cqrs_outbox WHERE seq IN (" + seqs + ")");
        return claimed.size();
    }

    private static String prefix(String cols, String p) {
        StringBuilder sb = new StringBuilder();
        for (String c : cols.split(",")) {
            if (sb.length() > 0) sb.append(',');
            sb.append(p).append(c);
        }
        return sb.toString();
    }

    public long backlog() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class);
        return n == null ? 0 : n;
    }
}
