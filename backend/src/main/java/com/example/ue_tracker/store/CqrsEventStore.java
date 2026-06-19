package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.List;

/**
 * Write side (cqrs_write_*, outbox) lives on the PRIMARY db; read side (cqrs_read_*) lives on a
 * SEPARATE read db. Writes go to primary; reads come from the read db (isolated from the write load).
 */
@Component
public class CqrsEventStore implements EventStore {

    private final CopySupport copy;                 // primary datasource
    private final EventQuery readQuery;             // read datasource
    private final JdbcTemplate primaryJdbc;
    private final JdbcTemplate readJdbc;

    CqrsEventStore(CopySupport copy,
                   @Qualifier("readEventQuery") EventQuery readQuery,
                   JdbcTemplate primaryJdbc,
                   @Qualifier("readJdbcTemplate") JdbcTemplate readJdbc) {
        this.copy = copy; this.readQuery = readQuery;
        this.primaryJdbc = primaryJdbc; this.readJdbc = readJdbc;
    }

    @Override public EventModel model() { return EventModel.CQRS; }

    @Override
    public void clear() {
        primaryJdbc.execute("TRUNCATE cqrs_write_latest, cqrs_write_history, cqrs_outbox");
        readJdbc.execute("TRUNCATE cqrs_read_latest, cqrs_read_history");
    }

    @Override
    public Stats stats() {
        Long uniq = readJdbc.queryForObject("SELECT count(*) FROM cqrs_read_latest", Long.class);
        Long tot = readJdbc.queryForObject("SELECT count(*) FROM cqrs_read_history", Long.class);
        return new Stats(uniq == null ? 0 : uniq, tot == null ? 0 : tot);
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
    public PageResult getLatest(EventQuery.Filters filters, boolean ascending, int page, int size) {
        return readQuery.page("cqrs_read_latest", null, filters, ascending, page, size);
    }

    @Override
    public PageResult getHistory(String imsi, boolean ascending, int page, int size) {
        return readQuery.page("cqrs_read_history", imsi, null, ascending, page, size);
    }
}
