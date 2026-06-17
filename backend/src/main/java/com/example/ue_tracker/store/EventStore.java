package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;
import com.example.ue_tracker.model.PaginationStrategy;

import java.util.List;

public interface EventStore {
    EventModel model();

    /** Bulk-load a chunk; returns DB-side elapsed millis for this chunk. */
    long copyIn(List<UeEvent> chunk);

    PageResult getLatest(PaginationStrategy strategy, int page, String cursor, int size);

    PageResult getHistory(String imsi, PaginationStrategy strategy, int page, String cursor, int size);
}
