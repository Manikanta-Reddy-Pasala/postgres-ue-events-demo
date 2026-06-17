package com.example.ue_tracker.generator;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.store.CqrsEventStore;
import com.example.ue_tracker.store.NormalEventStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates {@code uniqueImsis} distinct UEs, each with a random number of history events
 * in [{@link #MIN_EVENTS}, {@link #MAX_EVENTS}]. Latest tables end up with one row per IMSI
 * (the newest event); history holds every event. Events stream through COPY in chunks so
 * arbitrarily large runs don't hold everything in memory.
 */
@Service
public class GenerationService {

    public static final int MIN_EVENTS = 100;
    public static final int MAX_EVENTS = 1000;

    private final EventFactory factory;
    private final NormalEventStore normal;
    private final CqrsEventStore cqrs;
    private final Random random = new Random();
    @Value("${benchmark.generation.chunk-size:50000}") int chunkSize;

    public GenerationService(EventFactory factory, NormalEventStore normal, CqrsEventStore cqrs) {
        this.factory = factory; this.normal = normal; this.cqrs = cqrs;
    }

    public record Result(long uniqueImsis, long totalEvents, long normalMs, long cqrsWriteMs) {}

    public Result generate(int uniqueImsis) {
        long total = 0, normalMs = 0, cqrsMs = 0;
        List<UeEvent> buf = new ArrayList<>(chunkSize);
        Instant base = Instant.now().minusSeconds((long) uniqueImsis * MAX_EVENTS / 1000 + 1);

        for (long k = 0; k < uniqueImsis; k++) {
            String imsi = factory.imsiAt(k);
            int events = MIN_EVENTS + random.nextInt(MAX_EVENTS - MIN_EVENTS + 1);
            for (int j = 0; j < events; j++) {
                // strictly increasing per IMSI so the last event is the "latest"
                Instant ts = base.plusMillis(k * MAX_EVENTS + j);
                buf.add(factory.eventFor(imsi, ts));
                total++;
                if (buf.size() >= chunkSize) {
                    normalMs += normal.copyIn(buf);
                    cqrsMs += cqrs.copyIn(buf);
                    buf.clear();
                }
            }
        }
        if (!buf.isEmpty()) {
            normalMs += normal.copyIn(buf);
            cqrsMs += cqrs.copyIn(buf);
        }
        return new Result(uniqueImsis, total, normalMs, cqrsMs);
    }
}
