package com.example.ue_tracker.generator;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.store.CqrsEventStore;
import com.example.ue_tracker.store.NormalEventStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenerationService {

    private final EventFactory factory;
    private final NormalEventStore normal;
    private final CqrsEventStore cqrs;
    @Value("${benchmark.generation.chunk-size:50000}") int chunkSize;

    public GenerationService(EventFactory factory, NormalEventStore normal, CqrsEventStore cqrs) {
        this.factory = factory; this.normal = normal; this.cqrs = cqrs;
    }

    public record Result(int count, long normalMs, long cqrsWriteMs) {}

    public Result generate(int count) {
        long normalMs = 0, cqrsMs = 0;
        int remaining = count;
        while (remaining > 0) {
            int n = Math.min(remaining, chunkSize);
            List<UeEvent> batch = factory.randomBatch(n);
            normalMs += normal.copyIn(batch);
            cqrsMs += cqrs.copyIn(batch);
            remaining -= n;
        }
        return new Result(count, normalMs, cqrsMs);
    }
}
