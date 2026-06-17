package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

@Component
public class NormalEventStore implements EventStore {

    private final CopySupport copy;
    private final SeekQuery seek;
    private final JdbcTemplate jdbc;

    NormalEventStore(CopySupport copy, SeekQuery seek, JdbcTemplate jdbc) {
        this.copy = copy; this.seek = seek; this.jdbc = jdbc;
    }

    @Override public EventModel model() { return EventModel.NORMAL; }

    @Override
    public void clear() {
        jdbc.execute("TRUNCATE ue_events, ue_events_history");
    }

    @Override
    public long copyIn(List<UeEvent> chunk) {
        return copy.withStaging(chunk, conn -> {
            try (Statement st = conn.createStatement()) {
                st.execute(EventSql.insertHistoryFromStaging("ue_events_history"));
                st.execute(EventSql.upsertLatestFromStaging("ue_events"));
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
