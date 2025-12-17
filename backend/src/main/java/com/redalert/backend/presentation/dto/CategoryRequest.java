package com.redalert.backend.presentation.dto;

import com.redalert.backend.domain.model.Category;

/**
 * DTO for category requests with specific filter fields.
 */
public record CategoryRequest(
        String name,
        String description,
        String fromFilter,
        String subjectKeywords,
        String bodyKeywords,
        Boolean isActive) {

    /**
     * Converts to domain entity.
     */
    public Category toEntity() {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setFromFilter(fromFilter);
        category.setSubjectKeywords(subjectKeywords);
        category.setBodyKeywords(bodyKeywords);
        category.setIsActive(isActive != null ? isActive : true);
        return category;
    }
}
