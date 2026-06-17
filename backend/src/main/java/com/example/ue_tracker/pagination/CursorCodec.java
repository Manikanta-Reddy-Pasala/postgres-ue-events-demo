package com.example.ue_tracker.pagination;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/**
 * Encodes/decodes opaque keyset cursors as base64 of {@code "<epochMicros>:<tie>"}.
 * One format for every table; the caller decides what {@code tie} means.
 */
@Component
public class CursorCodec {

    public String encode(Instant updatedAt, String tie) {
        long micros = updatedAt.getEpochSecond() * 1_000_000L + updatedAt.getNano() / 1_000L;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((micros + ":" + tie).getBytes(StandardCharsets.UTF_8));
    }

    /** Returns null for null/blank tokens. */
    public PageCursor decode(String token) {
        if (token == null || token.isBlank()) return null;
        String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        int sep = raw.indexOf(':');
        long micros = Long.parseLong(raw.substring(0, sep));
        Instant ts = Instant.ofEpochSecond(micros / 1_000_000L, (micros % 1_000_000L) * 1_000L);
        return new PageCursor(ts, raw.substring(sep + 1));
    }
}
