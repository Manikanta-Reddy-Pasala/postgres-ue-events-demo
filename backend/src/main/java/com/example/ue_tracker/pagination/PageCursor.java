package com.example.ue_tracker.pagination;

import java.time.Instant;

/**
 * Decoded keyset cursor. {@code tie} is the seek tiebreaker as text:
 * the row {@code id} for history tables, the {@code imsi_or_supi} for latest tables.
 */
public record PageCursor(Instant updatedAt, String tie) {}
