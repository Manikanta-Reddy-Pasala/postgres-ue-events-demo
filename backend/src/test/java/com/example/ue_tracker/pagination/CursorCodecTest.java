package com.example.ue_tracker.pagination;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CursorCodecTest {

    private final CursorCodec codec = new CursorCodec();

    @Test
    void roundTripsUpdatedAtAndTie() {
        Instant ts = Instant.parse("2026-06-17T10:15:30.123456Z");
        PageCursor decoded = codec.decode(codec.encode(ts, "9876543210"));
        assertEquals(ts, decoded.updatedAt());
        assertEquals("9876543210", decoded.tie());
    }

    @Test
    void roundTripsStringTiebreaker() {
        Instant ts = Instant.parse("2026-06-17T10:15:30.123456Z");
        PageCursor decoded = codec.decode(codec.encode(ts, "424021478673415"));
        assertEquals("424021478673415", decoded.tie());
    }

    @Test
    void decodesNullOrBlankAsNull() {
        assertNull(codec.decode(null));
        assertNull(codec.decode(""));
    }
}
