package com.example.ue_tracker.generator;

import com.example.ue_tracker.store.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

class GenerationServiceTest extends AbstractPostgresTest {

    @Autowired GenerationService gen;
    @Autowired JdbcTemplate jdbc;

    @Test
    void generatesUniqueImsisEachWithRandomEventCount() {
        int uniqueImsis = 5;
        GenerationService.Result r = gen.generate(uniqueImsis);

        assertEquals(uniqueImsis, r.uniqueImsis());
        // each IMSI has between MIN and MAX events
        assertTrue(r.totalEvents() >= (long) uniqueImsis * GenerationService.MIN_EVENTS);
        assertTrue(r.totalEvents() <= (long) uniqueImsis * GenerationService.MAX_EVENTS);

        // latest table = one row per IMSI; history = all events (both models)
        assertEquals(uniqueImsis, (long) jdbc.queryForObject("SELECT count(*) FROM ue_events", Long.class));
        assertEquals(r.totalEvents(), (long) jdbc.queryForObject("SELECT count(*) FROM ue_events_history", Long.class));
        assertEquals(uniqueImsis,
                (long) jdbc.queryForObject("SELECT count(DISTINCT imsi_or_supi) FROM ue_events_history", Long.class));
        assertEquals(r.totalEvents(), (long) jdbc.queryForObject("SELECT count(*) FROM cqrs_write_history", Long.class));
    }
}
