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
        assertTrue(r.normalRead().p99Ms() >= r.normalRead().p50Ms(), "p99 >= p50");
        // NORMAL reads ue_events (latest), upserted directly by the load -> writes churn its read table
        assertTrue(r.normalWrite().writesToReadTable() > 0, "NORMAL read table should take writes");
        assertTrue(r.normalWrite().writesPerSec() >= 0 && r.cqrsWrite().writesPerSec() >= 0);
        assertTrue(r.cqrsWrite().writesToReadTable() >= 0);
    }
}
