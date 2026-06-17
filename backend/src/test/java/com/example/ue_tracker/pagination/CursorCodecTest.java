package com.example.ue_tracker.pagination;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CursorCodecTest {

    private final CursorCodec codec = new CursorCodec();

    @Test
    void roundTripsUpdatedAtAndId() {
        Instant ts = Instant.parse("2026-06-17T10:15:30.123456Z");
        String encoded = codec.encode(new Cursor(ts, 9876543210L));
        Cursor decoded = codec.decode(encoded);
        assertEquals(ts, decoded.updatedAt());
        assertEquals(9876543210L, decoded.id());
    }

    @Test
    void decodesNullOrBlankAsNull() {
        assertNull(codec.decode(null));
        assertNull(codec.decode(""));
        assertNull(codec.decodeLatest(null));
        assertNull(codec.decodeLatest(""));
    }

    @Test
    void roundTripsLatestStringTiebreaker() {
        Instant ts = Instant.parse("2026-06-17T10:15:30.123456Z");
        String token = codec.encodeLatest(ts, "424021478673415");
        String[] parts = codec.decodeLatest(token);
        assertEquals("424021478673415", parts[1]);
    }
}
