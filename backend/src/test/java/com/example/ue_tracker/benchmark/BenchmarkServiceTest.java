package com.example.ue_tracker.benchmark;

import com.example.ue_tracker.store.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkServiceTest extends AbstractPostgresTest {

    @Autowired BenchmarkService benchmark;

    @Test
    void runsReadsForBothModelsUnderLoad() {
        BenchmarkService.Result r = benchmark.run(700);
        assertEquals(700, r.durationMs());
        assertNotNull(r.normal());
        assertNotNull(r.cqrs());
        assertTrue(r.normal().samples() > 0, "should sample NORMAL reads");
        assertTrue(r.cqrs().samples() > 0, "should sample CQRS reads");
        assertTrue(r.normal().avgMs() >= 0 && r.cqrs().avgMs() >= 0);
    }
}
