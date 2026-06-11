package com.example.ue_tracker.controller;

import com.example.ue.proto.UeEventPageResponse;
import com.example.ue_tracker.service.UeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class UeEventController {

    private final UeEventService service;

    @GetMapping(value = "/latest", produces = "application/x-protobuf")
    public UeEventPageResponse getLatestEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return service.getLatestEvents(page, size);
    }

    @GetMapping(value = "/{imsiOrSupi}/history", produces = "application/x-protobuf")
    public UeEventPageResponse getHistory(
            @PathVariable String imsiOrSupi,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return service.getHistory(imsiOrSupi, page, size);
    }
}
