package com.projects.api.core.notification;

import java.util.Locale;

public enum NotificationStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    BOUNCED,
    READ;

    public static NotificationStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Notification status is required");
        }
        return NotificationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
