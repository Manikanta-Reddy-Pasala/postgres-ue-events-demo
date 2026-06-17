package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import com.example.ue_tracker.model.PaginationStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NormalEventStoreTest extends AbstractPostgresTest {

    @Autowired NormalEventStore store;
    @Autowired EventFactory factory;

    @Test
    void copyInLoadsHistoryAndLatestThenPaginatesBothWays() {
        List<UeEvent> batch = factory.randomBatch(500);
        long ms = store.copyIn(batch);
        assertTrue(ms >= 0);

        PageResult offset = store.getLatest(PaginationStrategy.OFFSET, 0, null, 50);
        assertEquals(50, offset.events().size());

        PageResult k1 = store.getLatest(PaginationStrategy.KEYSET, 0, null, 50);
        assertEquals(50, k1.events().size());
        assertTrue(k1.hasNext());
        assertFalse(k1.nextCursor().isBlank());
        PageResult k2 = store.getLatest(PaginationStrategy.KEYSET, 0, k1.nextCursor(), 50);
        assertFalse(k2.events().isEmpty());
        UeEvent lastOfP1 = k1.events().get(49);
        UeEvent firstOfP2 = k2.events().get(0);
        assertNotEquals(lastOfP1.getImsiOrSupi(), firstOfP2.getImsiOrSupi());

        String imsi = batch.get(0).getImsiOrSupi();
        PageResult hist = store.getHistory(imsi, PaginationStrategy.KEYSET, 0, null, 50);
        assertFalse(hist.events().isEmpty());
        assertTrue(hist.events().stream().allMatch(e -> e.getImsiOrSupi().equals(imsi)));
    }

    @Test
    void clearEmptiesBothTables() {
        store.copyIn(factory.randomBatch(100));
        store.clear();
        assertTrue(store.getLatest(PaginationStrategy.OFFSET, 0, null, 50).events().isEmpty());
    }
}
