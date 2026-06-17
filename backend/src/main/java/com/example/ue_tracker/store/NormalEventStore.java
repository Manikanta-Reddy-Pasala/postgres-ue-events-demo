package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

@Component
public class NormalEventStore implements EventStore {

    private final CopySupport copy;
    private final EventQuery query;
    private final JdbcTemplate jdbc;

    NormalEventStore(CopySupport copy, EventQuery query, JdbcTemplate jdbc) {
        this.copy = copy; this.query = query; this.jdbc = jdbc;
    }

    @Override public EventModel model() { return EventModel.NORMAL; }

    @Override
    public void clear() {
        jdbc.execute("TRUNCATE ue_events, ue_events_history");
    }

    @Override
    public Stats stats() {
        Long uniq = jdbc.queryForObject("SELECT count(*) FROM ue_events", Long.class);
        Long tot = jdbc.queryForObject("SELECT count(*) FROM ue_events_history", Long.class);
        return new Stats(uniq == null ? 0 : uniq, tot == null ? 0 : tot);
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
    public PageResult getLatest(String filter, int page, int size) {
        return query.page("ue_events", null, filter, page, size);
    }

    @Override
    public PageResult getHistory(String imsi, int page, int size) {
        return query.page("ue_events_history", imsi, null, page, size);
    }
}
