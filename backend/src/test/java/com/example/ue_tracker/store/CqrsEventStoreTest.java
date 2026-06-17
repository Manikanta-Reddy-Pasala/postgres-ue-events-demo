package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CqrsEventStoreTest extends AbstractPostgresTest {

    @Autowired CqrsEventStore store;
    @Autowired EventFactory factory;
    @Autowired JdbcTemplate jdbc;

    @Test
    void writePathFillsWriteTablesAndOutboxButNotReadTables() {
        List<UeEvent> batch = factory.randomBatch(300);
        store.copyIn(batch);

        Long writeHist = jdbc.queryForObject("SELECT count(*) FROM cqrs_write_history", Long.class);
        Long outbox = jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class);
        // read tables live on the separate read db; projector hasn't run
        Long readHist = readJdbcTemplate.queryForObject("SELECT count(*) FROM cqrs_read_history", Long.class);

        assertEquals(300L, writeHist);
        assertEquals(300L, outbox);
        assertEquals(0L, readHist);
    }
}
