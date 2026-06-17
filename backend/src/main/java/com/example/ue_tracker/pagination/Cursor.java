package com.example.ue_tracker.pagination;

import java.time.Instant;

public record Cursor(Instant updatedAt, long id) {}
