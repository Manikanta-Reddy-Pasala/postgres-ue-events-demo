package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;

import java.util.List;

public record PageResult(
        List<UeEvent> events,
        long totalElements,   // estimated for OFFSET; 0 for KEYSET
        int totalPages,       // estimated for OFFSET; 0 for KEYSET
        int currentPage,      // OFFSET only
        String nextCursor,    // KEYSET only; empty otherwise
        boolean hasNext
) {}
