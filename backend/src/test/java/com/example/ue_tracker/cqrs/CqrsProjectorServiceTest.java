package com.example.ue_tracker.cqrs;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import com.example.ue_tracker.store.AbstractPostgresTest;
import com.example.ue_tracker.store.CqrsEventStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CqrsProjectorServiceTest extends AbstractPostgresTest {

    @Autowired CqrsEventStore store;
    @Autowired CqrsProjectorService projector;
    @Autowired EventFactory factory;
    @Autowired JdbcTemplate jdbc;

    @Test
    void drainsOutboxIntoReadTablesAndClearsBacklog() {
        List<UeEvent> batch = factory.randomBatch(250);
        store.copyIn(batch);
        assertEquals(250L, jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class));

        int drained = 0, loops = 0;
        while (jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class) > 0 && loops++ < 100) {
            drained += projector.drainOnce(2000);
        }
        assertEquals(250, drained);
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM cqrs_outbox", Long.class));
        assertEquals(250L, jdbc.queryForObject("SELECT count(*) FROM cqrs_read_history", Long.class));
        assertTrue(jdbc.queryForObject("SELECT count(*) FROM cqrs_read_latest", Long.class) > 0);
    }
}
