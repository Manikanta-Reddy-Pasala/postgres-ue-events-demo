package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

@Component
public class NormalEventStore implements EventStore {

    private final CopySupport copy;
    private final SeekQuery seek;

    NormalEventStore(CopySupport copy, SeekQuery seek) {
        this.copy = copy; this.seek = seek;
    }

    @Override public EventModel model() { return EventModel.NORMAL; }

    @Override
    public long copyIn(List<UeEvent> chunk) {
        return copy.withStaging(chunk, conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute("INSERT INTO ue_events_history (" + CopySupport.COLS + ") " +
                           "SELECT " + CopySupport.COLS + " FROM staging_events");
                st.execute("""
                    INSERT INTO ue_events (%s)
                    SELECT DISTINCT ON (imsi_or_supi) %s FROM staging_events
                    ORDER BY imsi_or_supi, updated_at DESC
                    ON CONFLICT (imsi_or_supi) DO UPDATE SET
                      updated_at = EXCLUDED.updated_at, action_taken = EXCLUDED.action_taken,
                      rat = EXCLUDED.rat, rssi = EXCLUDED.rssi, provider_name = EXCLUDED.provider_name,
                      country_name = EXCLUDED.country_name, msisdn = EXCLUDED.msisdn,
                      distance_in_meters = EXCLUDED.distance_in_meters
                    WHERE EXCLUDED.updated_at >= ue_events.updated_at
                    """.formatted(CopySupport.COLS, CopySupport.COLS));
            }
        });
    }

    @Override
    public PageResult getLatest(PaginationStrategy strategy, int page, String cursor, int size) {
        return seek.query("ue_events", null, strategy, page, cursor, size);
    }

    @Override
    public PageResult getHistory(String imsi, PaginationStrategy strategy, int page, String cursor, int size) {
        return seek.query("ue_events_history", imsi, strategy, page, cursor, size);
    }
}
