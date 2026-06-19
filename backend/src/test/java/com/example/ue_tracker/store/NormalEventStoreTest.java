package com.example.ue_tracker.store;

import com.example.ue.proto.UeEvent;
import com.example.ue_tracker.generator.EventFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
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

        PageResult p0 = store.getLatest(EventQuery.Filters.NONE, false, 0, 50);
        assertEquals(50, p0.events().size());
        assertTrue(p0.totalElements() > 0);
        assertTrue(p0.totalPages() >= 1);

        String imsi = batch.get(0).getImsiOrSupi();
        PageResult hist = store.getHistory(imsi, false, 0, 50);
        assertFalse(hist.events().isEmpty());
        assertTrue(hist.events().stream().allMatch(e -> e.getImsiOrSupi().equals(imsi)));
    }

    @Test
    void filterMatchesImsiMsisdnOrRat() {
        store.copyIn(factory.randomBatch(500));
        // every generated row has plmn RAT in {RAT_2G..RAT_5G}; filter by a RAT substring
        PageResult byRat = store.getLatest(
                new EventQuery.Filters(null, null, "RAT_5G", null, null), false, 0, 50);
        assertTrue(byRat.events().stream().allMatch(e -> e.getRat().name().contains("RAT_5G"))
                || byRat.events().isEmpty());

        // filter by a known IMSI prefix returns only matching rows
        PageResult byImsi = store.getLatest(
                new EventQuery.Filters("4240214786734", null, null, null, null), false, 0, 50);
        assertTrue(byImsi.events().stream().allMatch(e -> e.getImsiOrSupi().contains("4240214786734")));
    }

    @Test
    void multipleFiltersAndTogetherAndSortAscending() {
        store.copyIn(factory.randomBatch(500));
        // IMSI + RAT ANDed: every returned row must satisfy both predicates
        PageResult both = store.getLatest(
                new EventQuery.Filters("42", null, "RAT_", null, null), false, 0, 50);
        assertTrue(both.events().stream().allMatch(
                e -> e.getImsiOrSupi().contains("42") && e.getRat().name().contains("RAT_")));

        // ascending sort: updated_at non-decreasing across the page
        PageResult asc = store.getLatest(EventQuery.Filters.NONE, true, 0, 50);
        for (int i = 1; i < asc.events().size(); i++) {
            assertTrue(Instant.parse(asc.events().get(i).getUpdatedAt())
                    .compareTo(Instant.parse(asc.events().get(i - 1).getUpdatedAt())) >= 0);
        }
    }

    @Test
    void statsReportsUniqueImsisAndTotalEvents() {
        store.copyIn(factory.randomBatch(500));
        EventStore.Stats s = store.stats();
        assertEquals(500, s.totalEvents());
        assertTrue(s.uniqueImsis() > 0 && s.uniqueImsis() <= 500);
    }

    @Test
    void clearEmptiesBothTables() {
        store.copyIn(factory.randomBatch(100));
        store.clear();
        assertTrue(store.getLatest(EventQuery.Filters.NONE, false, 0, 50).events().isEmpty());
    }
}
