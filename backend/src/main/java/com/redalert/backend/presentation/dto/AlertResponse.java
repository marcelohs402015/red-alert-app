package com.redalert.backend.presentation.dto;

import com.redalert.backend.domain.model.Alert;

import java.time.LocalDateTime;

/**
 * DTO for alert responses with database fields.
 */
public record AlertResponse(
        Long id,
        String title,
        String description,
        LocalDateTime alertDate,
        String url,
        Boolean isUrgent,
        String emailId,
        String emailFrom,
        String emailSubject,
        Long categoryId,
        LocalDateTime createdAt) {
    /**
     * Converts from domain entity.
     */
    public static AlertResponse fromEntity(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getAlertDate(),
                alert.getUrl(),
                alert.getIsUrgent(),
                alert.getEmailId(),
                alert.getEmailFrom(),
                alert.getEmailSubject(),
                alert.getCategory() != null ? alert.getCategory().getId() : null,
                alert.getCreatedAt());
    }
}
