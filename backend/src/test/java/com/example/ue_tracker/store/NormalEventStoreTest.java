package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NormalEventStoreTest extends AbstractPostgresTest {

    @Autowired NormalEventStore store;
    @Autowired EventFactory factory;

    @Test
    void copyInLoadsHistoryAndLatestThenPaginates() {
        List<UeEvent> batch = factory.randomBatch(500);
        long ms = store.copyIn(batch);
        assertTrue(ms >= 0);

        PageResult p0 = store.getLatest(null, 0, 50);
        assertEquals(50, p0.events().size());
        assertTrue(p0.totalElements() > 0);
        assertTrue(p0.totalPages() >= 1);

        String imsi = batch.get(0).getImsiOrSupi();
        PageResult hist = store.getHistory(imsi, 0, 50);
        assertFalse(hist.events().isEmpty());
        assertTrue(hist.events().stream().allMatch(e -> e.getImsiOrSupi().equals(imsi)));
    }

    @Test
    void filterMatchesImsiMsisdnOrRat() {
        store.copyIn(factory.randomBatch(500));
        // every generated row has plmn RAT in {RAT_2G..RAT_5G}; filter by a RAT substring
        PageResult byRat = store.getLatest("RAT_5G", 0, 50);
        assertTrue(byRat.events().stream().allMatch(e -> e.getRat().name().contains("RAT_5G"))
                || byRat.events().isEmpty());

        // filter by a known IMSI prefix returns only matching rows
        PageResult byImsi = store.getLatest("4240214786734", 0, 50);
        assertTrue(byImsi.events().stream().allMatch(e -> e.getImsiOrSupi().contains("4240214786734")));
    }

    @Test
    void clearEmptiesBothTables() {
        store.copyIn(factory.randomBatch(100));
        store.clear();
        assertTrue(store.getLatest(null, 0, 50).events().isEmpty());
    }
}
