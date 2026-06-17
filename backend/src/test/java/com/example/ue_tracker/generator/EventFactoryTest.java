package com.example.ue_tracker.generator;

import com.example.ue.proto.UeEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventFactoryTest {

    private final EventFactory factory = new EventFactory();

    @Test
    void buildsRequestedCountWithRequiredFields() {
        List<UeEvent> batch = factory.randomBatch(100);
        assertEquals(100, batch.size());
        for (UeEvent e : batch) {
            assertFalse(e.getImsiOrSupi().isBlank());
            assertFalse(e.getUpdatedAt().isBlank());
            assertNotNull(e.getActionTaken());
            assertNotNull(e.getRat());
        }
    }
}
