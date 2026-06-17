package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.model.EventModel;

import java.util.List;

public interface EventStore {
    EventModel model();

    /** Bulk-load a chunk; returns DB-side elapsed millis for this chunk. */
    long copyIn(List<UeEvent> chunk);

    /** Remove all rows owned by this model. */
    void clear();

    /** Latest event per IMSI, optionally filtered (IMSI/MSISDN/RAT), OFFSET-paginated. */
    PageResult getLatest(String filter, int page, int size);

    /** Full history for one IMSI, OFFSET-paginated. */
    PageResult getHistory(String imsi, int page, int size);
}
