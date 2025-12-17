package com.redalert.backend.presentation.dto;

import com.redalert.backend.domain.model.EmailDto;

import java.time.LocalDateTime;

/**
 * Response DTO for email search results.
 */
public record EmailResponse(
        String id,
        String from,
        String subject,
        String snippet,
        LocalDateTime receivedAt,
        boolean isUnread) {
    /**
     * Converts domain EmailDto to response DTO.
     */
    public static EmailResponse fromDomain(EmailDto email) {
        return new EmailResponse(
                email.id(),
                email.from(),
                email.subject(),
                email.snippet(),
                email.receivedAt(),
                email.isUnread());
    }
}
