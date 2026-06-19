package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;

import java.util.List;

public interface EventStore {

    /** Row counts for this model: distinct UEs (latest) and total events (history). */
    record Stats(long uniqueImsis, long totalEvents) {}

    EventModel model();

    /** Distinct-IMSI count (latest table) and total event count (history table). */
    Stats stats();

    /** Bulk-load a chunk; returns DB-side elapsed millis for this chunk. */
    long copyIn(List<UeEvent> chunk);

    /** Remove all rows owned by this model. */
    void clear();

    /** Latest event per IMSI, optionally filtered (ANDed fields), sorted by updated_at, OFFSET-paginated. */
    PageResult getLatest(EventQuery.Filters filters, boolean ascending, int page, int size);

    /** Full history for one IMSI, sorted by updated_at, OFFSET-paginated. */
    PageResult getHistory(String imsi, boolean ascending, int page, int size);
}
