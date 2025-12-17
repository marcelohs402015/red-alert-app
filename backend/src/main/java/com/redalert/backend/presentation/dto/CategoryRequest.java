package com.redalert.backend.presentation.dto;

import com.redalert.backend.domain.model.Category;

/**
 * DTO for category requests.
 */
public record CategoryRequest(
        String name,
        String description,
        String emailQuery,
        Boolean isActive) {
    /**
     * Converts to domain entity.
     */
    public Category toEntity() {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setEmailQuery(emailQuery);
        category.setIsActive(isActive != null ? isActive : true);
        return category;
    }
}
