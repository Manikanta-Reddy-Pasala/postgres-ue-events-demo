package com.example.ue_tracker.pagination;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Encodes/decodes opaque keyset cursors. Two variants:
 *  - {@link #encode}/{@link #decode} carry a numeric tiebreaker (history tables, seek key = id).
 *  - {@link #encodeLatest}/{@link #decodeLatest} carry a string tiebreaker (latest tables, seek key = imsi).
 */
@Component
public class CursorCodec {

    public String encode(Cursor c) {
        String raw = micros(c.updatedAt()) + ":" + c.id();
        return b64(raw);
    }

    public Cursor decode(String token) {
        if (token == null || token.isBlank()) return null;
        String[] p = split(token);
        long micros = Long.parseLong(p[0]);
        return new Cursor(fromMicros(micros), Long.parseLong(p[1]));
    }

    public String encodeLatest(Instant updatedAt, String tie) {
        return b64(micros(updatedAt) + ":" + tie);
    }

    /** Returns [microsString, tie] or null. */
    public String[] decodeLatest(String token) {
        if (token == null || token.isBlank()) return null;
        return split(token);
    }

    private static long micros(Instant t) {
        return t.getEpochSecond() * 1_000_000L + t.getNano() / 1_000L;
    }

    private static Instant fromMicros(long micros) {
        return Instant.ofEpochSecond(micros / 1_000_000L, (micros % 1_000_000L) * 1_000L);
    }

    private static String b64(String raw) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private static String[] split(String token) {
        String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        int sep = raw.indexOf(':');
        return new String[]{ raw.substring(0, sep), raw.substring(sep + 1) };
    }
}
