package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;

import java.util.List;

public record PageResult(
        List<UeEvent> events,
        long totalElements,
        int totalPages,
        int currentPage
) {}
