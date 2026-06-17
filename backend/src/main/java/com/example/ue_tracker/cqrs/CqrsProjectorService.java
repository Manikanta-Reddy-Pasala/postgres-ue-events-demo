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
                "SELECT seq, write_history_id FROM cqrs_outbox ORDER BY seq " +
                "FOR UPDATE SKIP LOCKED LIMIT ?", batch);
        if (claimed.isEmpty()) return 0;

        // values are DB-generated bigints, not user input -> safe to inline
        String ids = claimed.stream().map(r -> String.valueOf(r.get("write_history_id")))
                .collect(Collectors.joining(","));
        String seqs = claimed.stream().map(r -> String.valueOf(r.get("seq")))
                .collect(Collectors.joining(","));

        String cols = CopySupport.COLS;
        jdbc.update("INSERT INTO cqrs_read_history (" + cols + ") " +
                "SELECT " + cols + " FROM cqrs_write_history WHERE id IN (" + ids + ")");
        jdbc.update("INSERT INTO cqrs_read_latest (" + cols + ") " +
                "SELECT DISTINCT ON (imsi_or_supi) " + cols + " FROM cqrs_write_history " +
                "WHERE id IN (" + ids + ") ORDER BY imsi_or_supi, updated_at DESC " +
                "ON CONFLICT (imsi_or_supi) DO UPDATE SET " +
                "updated_at = EXCLUDED.updated_at, action_taken = EXCLUDED.action_taken, " +
                "rat = EXCLUDED.rat, rssi = EXCLUDED.rssi, provider_name = EXCLUDED.provider_name, " +
                "country_name = EXCLUDED.country_name, msisdn = EXCLUDED.msisdn, " +
                "distance_in_meters = EXCLUDED.distance_in_meters " +
                "WHERE EXCLUDED.updated_at >= cqrs_read_latest.updated_at");
        jdbc.update("DELETE FROM cqrs_outbox WHERE seq IN (" + seqs + ")");
        return claimed.size();
    }

    public long backlog() {
        Long n = jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class);
        return n == null ? 0 : n;
    }
}
