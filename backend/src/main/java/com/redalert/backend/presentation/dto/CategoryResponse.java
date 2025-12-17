package com.redalert.backend.presentation.dto;

import com.redalert.backend.domain.model.Category;

import java.time.LocalDateTime;

/**
 * DTO for category responses with specific filter fields.
 */
public record CategoryResponse(
        Long id,
        String name,
        String description,
        String fromFilter,
        String subjectKeywords,
        String bodyKeywords,
        Boolean isActive,
        String generatedQuery,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /**
     * Creates response from entity.
     */
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getFromFilter(),
                category.getSubjectKeywords(),
                category.getBodyKeywords(),
                category.getIsActive(),
                category.buildGmailQuery(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
