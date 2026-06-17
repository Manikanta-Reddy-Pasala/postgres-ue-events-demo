package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

@Component
public class CqrsEventStore implements EventStore {

    private final CopySupport copy;
    private final SeekQuery seek;

    CqrsEventStore(CopySupport copy, SeekQuery seek) { this.copy = copy; this.seek = seek; }

    @Override public EventModel model() { return EventModel.CQRS; }

    @Override
    public long copyIn(List<UeEvent> chunk) {
        return copy.withStaging(chunk, conn -> {
            try (Statement st = conn.createStatement()) {
                // insert into write history, capturing generated ids into a temp table
                st.execute("CREATE TEMP TABLE IF NOT EXISTS new_ids (id bigint, imsi_or_supi text) ON COMMIT DROP");
                st.execute("WITH ins AS (" +
                        "INSERT INTO cqrs_write_history (" + CopySupport.COLS + ") " +
                        "SELECT " + CopySupport.COLS + " FROM staging_events RETURNING id, imsi_or_supi) " +
                        "INSERT INTO new_ids (id, imsi_or_supi) SELECT id, imsi_or_supi FROM ins");
                // upsert write latest (latest wins)
                st.execute("""
                    INSERT INTO cqrs_write_latest (%s)
                    SELECT DISTINCT ON (imsi_or_supi) %s FROM staging_events
                    ORDER BY imsi_or_supi, updated_at DESC
                    ON CONFLICT (imsi_or_supi) DO UPDATE SET
                      updated_at = EXCLUDED.updated_at, action_taken = EXCLUDED.action_taken,
                      rat = EXCLUDED.rat, rssi = EXCLUDED.rssi, provider_name = EXCLUDED.provider_name,
                      country_name = EXCLUDED.country_name, msisdn = EXCLUDED.msisdn,
                      distance_in_meters = EXCLUDED.distance_in_meters
                    WHERE EXCLUDED.updated_at >= cqrs_write_latest.updated_at
                    """.formatted(CopySupport.COLS, CopySupport.COLS));
                // one outbox row per written history row
                st.execute("INSERT INTO cqrs_outbox (imsi_or_supi, write_history_id) " +
                        "SELECT imsi_or_supi, id FROM new_ids");
            }
        });
    }

    @Override
    public PageResult getLatest(PaginationStrategy strategy, int page, String cursor, int size) {
        return seek.query("cqrs_read_latest", null, strategy, page, cursor, size);
    }

    @Override
    public PageResult getHistory(String imsi, PaginationStrategy strategy, int page, String cursor, int size) {
        return seek.query("cqrs_read_history", imsi, strategy, page, cursor, size);
    }
}
