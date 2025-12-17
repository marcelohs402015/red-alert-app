package com.redalert.backend.presentation.dto;

import com.redalert.backend.domain.model.Category;

import java.time.LocalDateTime;

/**
 * DTO for category responses.
 */
public record CategoryResponse(
        Long id,
        String name,
        String description,
        String emailQuery,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    /**
     * Converts from domain entity.
     */
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getEmailQuery(),
                category.getIsActive(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
