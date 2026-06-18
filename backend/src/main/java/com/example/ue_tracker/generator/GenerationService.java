package com.example.ue_tracker.generator;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.store.CqrsEventStore;
import com.example.ue_tracker.store.NormalEventStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates {@code uniqueImsis} distinct UEs, each with a random number of history events
 * in [{@link #MIN_EVENTS}, {@link #MAX_EVENTS}]. Work is split across threads (each owns a
 * disjoint IMSI range and its own COPY connection) so generation scales with cores; events
 * stream through COPY in chunks so large runs don't hold everything in memory.
 */
@Service
public class GenerationService {

    public static final int MIN_EVENTS = 100;
    public static final int MAX_EVENTS = 1000;

    private final EventFactory factory;
    private final NormalEventStore normal;
    private final CqrsEventStore cqrs;
    @Value("${benchmark.generation.chunk-size:50000}") int chunkSize;
    @Value("${benchmark.generation.threads:4}") int threads;

    public GenerationService(EventFactory factory, NormalEventStore normal, CqrsEventStore cqrs) {
        this.factory = factory; this.normal = normal; this.cqrs = cqrs;
    }

    public record Result(long uniqueImsis, long totalEvents, long normalMs, long cqrsWriteMs) {}

    public Result generate(int uniqueImsis) {
        int nThreads = Math.max(1, Math.min(threads, uniqueImsis));
        // Space events SECONDS apart (not ms) so an IMSI's history shows visibly distinct
        // updated_at values — toLocaleString in the UI only renders to second resolution, so
        // ms-spaced events all collapsed onto the same displayed timestamp.
        Instant base = Instant.now().minusSeconds((long) uniqueImsis * MAX_EVENTS + 1);

        AtomicLong total = new AtomicLong(), normalMs = new AtomicLong(), cqrsMs = new AtomicLong();
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < nThreads; t++) {
            final long from = (long) uniqueImsis * t / nThreads;
            final long to = (long) uniqueImsis * (t + 1) / nThreads;
            futures.add(pool.submit(() -> generateRange(from, to, base, total, normalMs, cqrsMs)));
        }
        try {
            for (Future<?> f : futures) f.get();
        } catch (Exception e) {
            throw new RuntimeException("generation failed", e);
        } finally {
            pool.shutdownNow();
        }
        return new Result(uniqueImsis, total.get(), normalMs.get(), cqrsMs.get());
    }

    private void generateRange(long from, long to, Instant base,
                               AtomicLong total, AtomicLong normalMs, AtomicLong cqrsMs) {
        List<UeEvent> buf = new ArrayList<>(chunkSize);
        for (long k = from; k < to; k++) {
            String imsi = factory.imsiAt(k);
            int events = MIN_EVENTS + ThreadLocalRandom.current().nextInt(MAX_EVENTS - MIN_EVENTS + 1);
            for (int j = 0; j < events; j++) {
                buf.add(factory.eventFor(imsi, base.plusSeconds(k * MAX_EVENTS + j)));
                if (buf.size() >= chunkSize) flush(buf, normalMs, cqrsMs, total);
            }
        }
        if (!buf.isEmpty()) flush(buf, normalMs, cqrsMs, total);
    }

    private void flush(List<UeEvent> buf, AtomicLong normalMs, AtomicLong cqrsMs, AtomicLong total) {
        normalMs.addAndGet(normal.copyIn(buf));
        cqrsMs.addAndGet(cqrs.copyIn(buf));
        total.addAndGet(buf.size());
        buf.clear();
    }
}
