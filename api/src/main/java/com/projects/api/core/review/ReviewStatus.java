package com.projects.api.core.review;

import java.util.Locale;

public enum ReviewStatus {
    PENDING_MODERATION,
    APPROVED,
    REJECTED,
    FLAGGED,
    ARCHIVED;

    public static ReviewStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Review status is required");
        }
        return ReviewStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
