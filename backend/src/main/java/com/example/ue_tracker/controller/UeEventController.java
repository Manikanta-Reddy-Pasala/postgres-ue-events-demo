package com.example.ue_tracker.controller;

import com.example.ue.proto.UeEventPageResponse;
import com.example.ue_tracker.cqrs.CqrsProjectorService;
import com.example.ue_tracker.generator.GenerationService;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;
import com.example.ue_tracker.store.EventStore;
import com.example.ue_tracker.store.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UeEventController {

    private final Map<EventModel, EventStore> stores = new EnumMap<>(EventModel.class);
    private final GenerationService generation;
    private final CqrsProjectorService projector;

    public UeEventController(List<EventStore> storeList, GenerationService generation,
                             CqrsProjectorService projector) {
        storeList.forEach(s -> stores.put(s.model(), s));
        this.generation = generation;
        this.projector = projector;
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestParam(defaultValue = "1000") int count) {
        GenerationService.Result r = generation.generate(count);
        return Map.of("count", r.count(), "normalMs", r.normalMs(), "cqrsWriteMs", r.cqrsWriteMs());
    }

    @PostMapping("/clear")
    public Map<String, Object> clear() {
        stores.values().forEach(EventStore::clear);
        return Map.of("cleared", true);
    }

    @GetMapping("/projection/status")
    public Map<String, Object> projectionStatus() {
        return Map.of("outboxBacklog", projector.backlog());
    }

    @GetMapping(value = "/events/latest", produces = "application/x-protobuf")
    public UeEventPageResponse latest(@RequestParam EventModel model,
                                      @RequestParam PaginationStrategy strategy,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(required = false) String cursor,
                                      @RequestParam(defaultValue = "50") int size) {
        long start = System.nanoTime();
        PageResult r = stores.get(model).getLatest(strategy, page, cursor, size);
        return toProto(r, System.nanoTime() - start);
    }

    @GetMapping(value = "/events/{imsi}/history", produces = "application/x-protobuf")
    public UeEventPageResponse history(@PathVariable String imsi,
                                       @RequestParam EventModel model,
                                       @RequestParam PaginationStrategy strategy,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(required = false) String cursor,
                                       @RequestParam(defaultValue = "50") int size) {
        long start = System.nanoTime();
        PageResult r = stores.get(model).getHistory(imsi, strategy, page, cursor, size);
        return toProto(r, System.nanoTime() - start);
    }

    private UeEventPageResponse toProto(PageResult r, long elapsedNanos) {
        return UeEventPageResponse.newBuilder()
                .addAllEvents(r.events())
                .setTotalPages(r.totalPages())
                .setTotalElements(r.totalElements())
                .setCurrentPage(r.currentPage())
                .setQueryTimeMs(elapsedNanos / 1_000_000L)
                .setNextCursor(r.nextCursor() == null ? "" : r.nextCursor())
                .setHasNext(r.hasNext())
                .build();
    }
}
