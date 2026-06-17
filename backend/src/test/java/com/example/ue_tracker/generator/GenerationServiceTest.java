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
    void generatesIntoBothModelsAndReportsTimes() {
        GenerationService.Result r = gen.generate(1200);
        assertEquals(1200, r.count());
        assertTrue(r.normalMs() >= 0);
        assertTrue(r.cqrsWriteMs() >= 0);
        assertEquals(1200L, jdbc.queryForObject("SELECT count(*) FROM ue_events_history", Long.class));
        assertEquals(1200L, jdbc.queryForObject("SELECT count(*) FROM cqrs_write_history", Long.class));
    }
}
