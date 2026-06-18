package com.example.ue_tracker.benchmark;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.cqrs.CqrsProjectorService;
import com.example.ue_tracker.generator.EventFactory;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.store.EventStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Measures LATEST-table read latency for both models under a background write load — the table
 * where the upsert-latest insert pattern actually contends:
 * <ul>
 *   <li>NORMAL reads {@code ue_events} — every event UPSERTs this row in place, so the table the
 *       dashboard reads is the same one the full write load churns (dead tuples, index bloat on the
 *       seek index, vacuum pressure). Tail latency (p99) is where that churn shows up.</li>
 *   <li>CQRS reads {@code cqrs_read_latest} — updated only by the projector, per-IMSI deduped, so
 *       the read-side latest table stays calm regardless of write rate.</li>
 * </ul>
 * The sampler reads the first latest page (seek, LIMIT 50) repeatedly while {@value #WRITER_THREADS}
 * writer threads/model hammer the write path — concurrent read under write. Counters are in-memory
 * (load writer + projector) so measuring the write rate adds no DB cost.
 */
@Service
public class BenchmarkService {

    private static final int WRITE_BATCH = 2000;
    private static final int WRITER_THREADS = 3;

    private final EventFactory factory;
    private final CqrsProjectorService projector;
    private final JdbcTemplate primaryJdbc;   // NORMAL tables + CQRS write side
    private final JdbcTemplate readJdbc;      // CQRS read tables (separate db)
    private final Map<EventModel, EventStore> stores = new EnumMap<>(EventModel.class);

    public BenchmarkService(EventFactory factory, CqrsProjectorService projector,
                            JdbcTemplate primaryJdbc,
                            @org.springframework.beans.factory.annotation.Qualifier("readJdbcTemplate") JdbcTemplate readJdbc,
                            List<EventStore> storeList) {
        this.factory = factory;
        this.projector = projector;
        this.primaryJdbc = primaryJdbc;
        this.readJdbc = readJdbc;
        storeList.forEach(s -> stores.put(s.model(), s));
    }

    /**
     * Count-free first-page read of the LATEST table (seek on idx_..._seek, LIMIT 50) — the exact
     * query the dashboard's default view runs, and the one that contends with the upsert churn.
     */
    private void readLatestPage(JdbcTemplate jdbc, String table) {
        jdbc.queryForList("SELECT * FROM " + table +
                " ORDER BY updated_at DESC, imsi_or_supi DESC LIMIT 50");
    }

    public record LatencyStats(double avgMs, long p50Ms, long p95Ms, long p99Ms, long maxMs, int samples) {}
    /** Writes that landed on the table this model reads, during its window. */
    public record WriteStats(long writesToReadTable, long writesPerSec) {}
    public record Result(int durationMs,
                         LatencyStats normalRead, LatencyStats cqrsRead,
                         WriteStats normalWrite, WriteStats cqrsWrite) {}

    private record Sampled(LatencyStats read, WriteStats write) {}

    public Result run(int durationMs) {
        ExecutorService writers = Executors.newFixedThreadPool(WRITER_THREADS * 2);
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicLong normalLoadEvents = new AtomicLong(); // inserts the load makes into ue_events_history
        for (int w = 0; w < WRITER_THREADS; w++) {
            writers.submit(() -> loadLoop(running, EventModel.NORMAL, normalLoadEvents));
            writers.submit(() -> loadLoop(running, EventModel.CQRS, null));
        }
        try {
            // warm both paths so first-sample JIT/plan cost isn't attributed unfairly
            readLatestPage(primaryJdbc, "ue_events");
            readLatestPage(readJdbc, "cqrs_read_latest");
            Sampled normal = measureFor(primaryJdbc, "ue_events", normalLoadEvents::get, durationMs);
            Sampled cqrs = measureFor(readJdbc, "cqrs_read_latest", projector::projectedRows, durationMs);
            return new Result(durationMs, normal.read(), cqrs.read(), normal.write(), cqrs.write());
        } finally {
            running.set(false);
            writers.shutdown();
            try { writers.awaitTermination(15, TimeUnit.SECONDS); } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
            writers.shutdownNow();
        }
    }

    private void loadLoop(AtomicBoolean running, EventModel model, AtomicLong counter) {
        EventStore store = stores.get(model);
        while (running.get()) {
            List<UeEvent> batch = factory.randomBatch(WRITE_BATCH);
            try {
                store.copyIn(batch);
                if (counter != null) counter.addAndGet(WRITE_BATCH);
            } catch (RuntimeException ignore) { /* keep loading */ }
        }
    }

    /** Sample read latency for {@code durationMs}; {@code readTableWrites} counts writes hitting {@code readTable}. */
    private Sampled measureFor(JdbcTemplate jdbc, String readTable, LongSupplier readTableWrites, int durationMs) {
        long writesBefore = readTableWrites.getAsLong();
        List<Long> samples = new ArrayList<>();
        long start = System.nanoTime();
        long end = start + durationMs * 1_000_000L;
        while (System.nanoTime() < end) {
            long t = System.nanoTime();
            readLatestPage(jdbc, readTable);   // first latest page; write counter tracks churn on this table
            samples.add((System.nanoTime() - t) / 1_000_000L);
        }
        long wallMs = (System.nanoTime() - start) / 1_000_000L;
        long writeDelta = Math.max(0, readTableWrites.getAsLong() - writesBefore);
        long writeRate = wallMs > 0 ? writeDelta * 1000L / wallMs : 0;

        long[] ms = samples.stream().mapToLong(Long::longValue).sorted().toArray();
        LatencyStats latency = ms.length == 0
                ? new LatencyStats(0, 0, 0, 0, 0, 0)
                : new LatencyStats(round(Arrays.stream(ms).average().orElse(0)),
                        pct(ms, 50), pct(ms, 95), pct(ms, 99), ms[ms.length - 1], ms.length);
        return new Sampled(latency, new WriteStats(writeDelta, writeRate));
    }

    private static long pct(long[] sorted, int p) {
        int idx = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(idx, sorted.length - 1))];
    }

    private static double round(double v) { return Math.round(v * 10.0) / 10.0; }
}
