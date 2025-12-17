package com.redalert.backend.presentation.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for processed emails.
 */
public record ProcessedEmailResponse(
        Long id,
        String emailId,
        String fromAddress,
        String subject,
        String snippet,
        LocalDateTime receivedAt,
        String categoryName,
        Long categoryId,
        LocalDateTime processedAt) {
}
