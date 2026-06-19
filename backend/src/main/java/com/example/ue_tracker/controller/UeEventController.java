package com.example.ue_tracker.controller;

import com.example.ue.proto.UeEventPageResponse;
import com.example.ue_tracker.benchmark.BenchmarkService;
import com.example.ue_tracker.cqrs.CqrsProjectorService;
import com.example.ue_tracker.generator.GenerationService;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.store.EventQuery;
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
    private final BenchmarkService benchmark;

    public UeEventController(List<EventStore> storeList, GenerationService generation,
                             CqrsProjectorService projector, BenchmarkService benchmark) {
        storeList.forEach(s -> stores.put(s.model(), s));
        this.generation = generation;
        this.projector = projector;
        this.benchmark = benchmark;
    }

    @PostMapping("/benchmark")
    public Map<String, Object> benchmark(@RequestParam(defaultValue = "4000") int durationMs) {
        BenchmarkService.Result r = benchmark.run(Math.max(500, Math.min(durationMs, 15000)));
        return Map.of("durationMs", r.durationMs(),
                "normalRead", r.normalRead(), "cqrsRead", r.cqrsRead(),
                "normalWrite", r.normalWrite(), "cqrsWrite", r.cqrsWrite());
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestParam(defaultValue = "1000") int count) {
        GenerationService.Result r = generation.generate(count);
        return Map.of("uniqueImsis", r.uniqueImsis(), "totalEvents", r.totalEvents(),
                "normalMs", r.normalMs(), "cqrsWriteMs", r.cqrsWriteMs());
    }

    @PostMapping("/clear")
    public Map<String, Object> clear() {
        // pause the CQRS projector while truncating so the TRUNCATE can't deadlock with a drain
        projector.runExclusive(() -> stores.values().forEach(EventStore::clear));
        return Map.of("cleared", true);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(@RequestParam EventModel model) {
        EventStore.Stats s = stores.get(model).stats();
        return Map.of("uniqueImsis", s.uniqueImsis(), "totalEvents", s.totalEvents());
    }

    @GetMapping("/projection/status")
    public Map<String, Object> projectionStatus() {
        return Map.of("outboxBacklog", projector.backlog());
    }

    @GetMapping(value = "/events/latest", produces = "application/x-protobuf")
    public UeEventPageResponse latest(@RequestParam EventModel model,
                                      @RequestParam(required = false) String q,
                                      @RequestParam(required = false) String imsi,
                                      @RequestParam(required = false) String msisdn,
                                      @RequestParam(required = false) String rat,
                                      @RequestParam(required = false) String provider,
                                      @RequestParam(required = false) String country,
                                      @RequestParam(defaultValue = "desc") String sort,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        // `q` (legacy single box) maps onto IMSI; per-field params AND together with it.
        EventQuery.Filters filters = new EventQuery.Filters(
                firstNonBlank(imsi, q), msisdn, rat, provider, country);
        long start = System.nanoTime();
        PageResult r = stores.get(model).getLatest(filters, isAscending(sort), page, size);
        return toProto(r, System.nanoTime() - start);
    }

    @GetMapping(value = "/events/{imsi}/history", produces = "application/x-protobuf")
    public UeEventPageResponse history(@PathVariable String imsi,
                                       @RequestParam EventModel model,
                                       @RequestParam(defaultValue = "desc") String sort,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size) {
        long start = System.nanoTime();
        PageResult r = stores.get(model).getHistory(imsi, isAscending(sort), page, size);
        return toProto(r, System.nanoTime() - start);
    }

    private static boolean isAscending(String sort) { return "asc".equalsIgnoreCase(sort); }

    private static String firstNonBlank(String a, String b) {
        return (a != null && !a.isBlank()) ? a : b;
    }

    private UeEventPageResponse toProto(PageResult r, long elapsedNanos) {
        return UeEventPageResponse.newBuilder()
                .addAllEvents(r.events())
                .setTotalPages(r.totalPages())
                .setTotalElements(r.totalElements())
                .setCurrentPage(r.currentPage())
                .setQueryTimeMs(elapsedNanos / 1_000_000L)
                .build();
    }
}
