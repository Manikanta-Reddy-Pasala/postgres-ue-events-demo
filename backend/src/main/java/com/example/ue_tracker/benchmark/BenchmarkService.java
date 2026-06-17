package com.example.ue_tracker.benchmark;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.store.EventStore;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Measures read latency for both models while a background write load runs concurrently.
 * Demonstrates CQRS read/write isolation: under load NORMAL reads contend with the write
 * storm on the same tables (and its hot latest table bloats from upsert churn), while CQRS
 * reads hit isolated read tables and stay flat.
 */
@Service
public class BenchmarkService {

    private static final int WRITE_BATCH = 2000;

    private final EventFactory factory;
    private final Map<EventModel, EventStore> stores = new EnumMap<>(EventModel.class);

    public BenchmarkService(EventFactory factory, List<EventStore> storeList) {
        this.factory = factory;
        storeList.forEach(s -> stores.put(s.model(), s));
    }

    private static final int WRITER_THREADS = 3;

    public record LatencyStats(double avgMs, long p50Ms, long p95Ms, long maxMs, int samples) {}
    public record Result(int durationMs, LatencyStats normal, LatencyStats cqrs) {}

    /**
     * Hammer writes from several threads while continuously sampling read latency for each
     * model over {@code durationMs}. Sustained load lets NORMAL's shared/upsert-churned tables
     * accumulate contention + dead-tuple bloat, which a short fixed-count run never reaches.
     */
    public Result run(int durationMs) {
        ExecutorService writers = Executors.newFixedThreadPool(WRITER_THREADS);
        AtomicBoolean running = new AtomicBoolean(true);
        for (int w = 0; w < WRITER_THREADS; w++) {
            writers.submit(() -> {
                while (running.get()) {
                    List<UeEvent> batch = factory.randomBatch(WRITE_BATCH);
                    try {
                        stores.get(EventModel.NORMAL).copyIn(batch);
                        stores.get(EventModel.CQRS).copyIn(batch);
                    } catch (RuntimeException ignore) { /* keep loading */ }
                }
            });
        }
        try {
            // warm both paths so first-sample JIT/plan cost isn't attributed unfairly
            stores.get(EventModel.NORMAL).getLatest(null, 0, 50);
            stores.get(EventModel.CQRS).getLatest(null, 0, 50);
            LatencyStats normal = measureFor(EventModel.NORMAL, durationMs);
            LatencyStats cqrs = measureFor(EventModel.CQRS, durationMs);
            return new Result(durationMs, normal, cqrs);
        } finally {
            running.set(false);
            writers.shutdown();
            try { writers.awaitTermination(15, TimeUnit.SECONDS); } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
            writers.shutdownNow();
        }
    }

    private LatencyStats measureFor(EventModel model, int durationMs) {
        EventStore store = stores.get(model);
        List<Long> samples = new java.util.ArrayList<>();
        long end = System.nanoTime() + durationMs * 1_000_000L;
        while (System.nanoTime() < end) {
            long t = System.nanoTime();
            store.getLatest(null, 0, 50);
            samples.add((System.nanoTime() - t) / 1_000_000L);
        }
        long[] ms = samples.stream().mapToLong(Long::longValue).sorted().toArray();
        if (ms.length == 0) return new LatencyStats(0, 0, 0, 0, 0);
        double avg = Arrays.stream(ms).average().orElse(0);
        return new LatencyStats(round(avg), pct(ms, 50), pct(ms, 95), ms[ms.length - 1], ms.length);
    }

    private static long pct(long[] sorted, int p) {
        int idx = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(idx, sorted.length - 1))];
    }

    private static double round(double v) { return Math.round(v * 10.0) / 10.0; }
}
