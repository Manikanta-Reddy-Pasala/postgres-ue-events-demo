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
        assertTrue(r.normalRead().samples() > 0, "should sample NORMAL reads");
        assertTrue(r.cqrsRead().samples() > 0, "should sample CQRS reads");
        assertTrue(r.normalRead().avgMs() >= 0 && r.cqrsRead().avgMs() >= 0);
        assertTrue(r.normalWrite().events() > 0, "NORMAL should have writes");
        assertTrue(r.cqrsWrite().events() > 0, "CQRS should have writes");
        assertTrue(r.normalWrite().ratePerSec() >= 0 && r.cqrsWrite().ratePerSec() >= 0);
    }
}
