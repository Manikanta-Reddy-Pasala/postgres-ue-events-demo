package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

@Component
public class CqrsEventStore implements EventStore {

    private final CopySupport copy;
    private final EventQuery query;
    private final JdbcTemplate jdbc;

    CqrsEventStore(CopySupport copy, EventQuery query, JdbcTemplate jdbc) {
        this.copy = copy; this.query = query; this.jdbc = jdbc;
    }

    @Override public EventModel model() { return EventModel.CQRS; }

    @Override
    public void clear() {
        jdbc.execute("TRUNCATE cqrs_write_latest, cqrs_write_history, " +
                "cqrs_read_latest, cqrs_read_history, cqrs_outbox");
    }

    @Override
    public long copyIn(List<UeEvent> chunk) {
        return copy.withStaging(chunk, conn -> {
            try (Statement st = conn.createStatement()) {
                // insert into write history, capturing generated ids into a temp table
                st.execute("CREATE TEMP TABLE IF NOT EXISTS new_ids (id bigint, imsi_or_supi text) ON COMMIT DROP");
                st.execute("WITH ins AS (" +
                        "INSERT INTO cqrs_write_history (" + EventSql.COLS + ") " +
                        "SELECT " + EventSql.COLS + " FROM staging_events RETURNING id, imsi_or_supi) " +
                        "INSERT INTO new_ids (id, imsi_or_supi) SELECT id, imsi_or_supi FROM ins");
                // upsert write latest (latest wins)
                st.execute(EventSql.upsertLatestFromStaging("cqrs_write_latest"));
                // one outbox row per written history row
                st.execute("INSERT INTO cqrs_outbox (imsi_or_supi, write_history_id) " +
                        "SELECT imsi_or_supi, id FROM new_ids");
            }
        });
    }

    @Override
    public PageResult getLatest(String filter, int page, int size) {
        return query.page("cqrs_read_latest", null, filter, page, size);
    }

    @Override
    public PageResult getHistory(String imsi, int page, int size) {
        return query.page("cqrs_read_history", imsi, null, page, size);
    }
}
