package com.redalert.backend.domain.model;

import java.time.LocalDateTime;

/**
 * Domain DTO representing a class alert extracted from email.
 * This is a Rich Domain Model that encapsulates alert data.
 * 
 * @param title       Alert title/subject
 * @param date        Date and time of the event
 * @param url         URL related to the event (if any)
 * @param description Detailed description of the alert
 * @param isUrgent    Flag indicating if this is an urgent alert requiring
 *                    immediate action
 */
public record ClassAlertDto(
        String title,
        LocalDateTime date,
        String url,
        String description,
        boolean isUrgent,
        String calendarLink) {
    /**
     * Validates that required fields are not null or empty.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public ClassAlertDto {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
    }
}
