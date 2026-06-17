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
 * Measures read latency for both models under a background write load, plus how many writes
 * land on the table each model READS during its sampling window:
 * <ul>
 *   <li>NORMAL reads {@code ue_events_history} — written directly by the load writers, so its
 *       read-table write rate = the load rate.</li>
 *   <li>CQRS reads {@code cqrs_read_history} — written only by the projector (load writers hit
 *       the separate write tables + outbox), so its read-table write rate = projector throughput
 *       (≈0 at steady state).</li>
 * </ul>
 * Counters are in-memory (load writer + projector) so measuring them adds no DB cost.
 */
@Service
public class BenchmarkService {

    private static final int WRITE_BATCH = 2000;
    private static final int WRITER_THREADS = 3;
    // a base IMSI the load reuses heavily -> its history grows on both sides; we time reads of it
    private static final String HOT_IMSI = "424021478673415";

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

    /** Count-free page fetch (index seek, LIMIT 50) so latency reflects contention, not count(*) size. */
    private void readPage(JdbcTemplate jdbc, String table) {
        jdbc.queryForList("SELECT * FROM " + table +
                " WHERE imsi_or_supi = ? ORDER BY updated_at DESC, id DESC LIMIT 50", HOT_IMSI);
    }

    public record LatencyStats(double avgMs, long p50Ms, long p95Ms, long maxMs, int samples) {}
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
            readPage(primaryJdbc, "ue_events_history");
            readPage(readJdbc, "cqrs_read_history");
            Sampled normal = measureFor(primaryJdbc, "ue_events_history", normalLoadEvents::get, durationMs);
            Sampled cqrs = measureFor(readJdbc, "cqrs_read_history", projector::projectedRows, durationMs);
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
            readPage(jdbc, readTable);   // count-free page read of the table the write counter tracks
            samples.add((System.nanoTime() - t) / 1_000_000L);
        }
        long wallMs = (System.nanoTime() - start) / 1_000_000L;
        long writeDelta = Math.max(0, readTableWrites.getAsLong() - writesBefore);
        long writeRate = wallMs > 0 ? writeDelta * 1000L / wallMs : 0;

        long[] ms = samples.stream().mapToLong(Long::longValue).sorted().toArray();
        LatencyStats latency = ms.length == 0
                ? new LatencyStats(0, 0, 0, 0, 0)
                : new LatencyStats(round(Arrays.stream(ms).average().orElse(0)),
                        pct(ms, 50), pct(ms, 95), ms[ms.length - 1], ms.length);
        return new Sampled(latency, new WriteStats(writeDelta, writeRate));
    }

    private static long pct(long[] sorted, int p) {
        int idx = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(idx, sorted.length - 1))];
    }

    private static double round(double v) { return Math.round(v * 10.0) / 10.0; }
}
