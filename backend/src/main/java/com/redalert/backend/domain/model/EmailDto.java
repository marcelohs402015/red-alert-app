package com.redalert.backend.domain.model;

import java.time.LocalDateTime;

/**
 * Domain DTO representing an email message.
 * 
 * @param id         Email message ID from Gmail
 * @param from       Sender email address
 * @param subject    Email subject
 * @param snippet    Email preview/snippet
 * @param receivedAt Date and time when email was received
 * @param isUnread   Whether the email is unread
 */
public record EmailDto(
        String id,
        String from,
        String subject,
        String snippet,
        LocalDateTime receivedAt,
        boolean isUnread) {
    /**
     * Validates that required fields are not null or empty.
     */
    public EmailDto {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Email ID cannot be null or empty");
        }
    }
}
