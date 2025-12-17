package com.redalert.backend.application.usecase;

import com.redalert.backend.domain.model.Category;
import com.redalert.backend.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing email monitoring categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Gets all categories.
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Gets only active categories.
     */
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    /**
     * Gets category by ID.
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    /**
     * Creates a new category.
     */
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }

        log.info("Creating new category: {}", category.getName());
        return categoryRepository.save(category);
    }

    /**
     * Updates an existing category.
     */
    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existing = getCategoryById(id);

        // Check if name is being changed and if new name already exists
        if (!existing.getName().equals(updatedCategory.getName()) &&
                categoryRepository.existsByName(updatedCategory.getName())) {
            throw new IllegalArgumentException("Category with name '" + updatedCategory.getName() + "' already exists");
        }

        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());
        existing.setEmailQuery(updatedCategory.getEmailQuery());
        existing.setIsActive(updatedCategory.getIsActive());

        log.info("Updating category: {}", existing.getName());
        return categoryRepository.save(existing);
    }

    /**
     * Toggles category active status.
     */
    @Transactional
    public Category toggleCategory(Long id) {
        Category category = getCategoryById(id);
        category.setIsActive(!category.getIsActive());

        log.info("Toggling category '{}' to: {}", category.getName(), category.getIsActive());
        return categoryRepository.save(category);
    }

    /**
     * Deletes a category.
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        log.info("Deleting category: {}", category.getName());
        categoryRepository.delete(category);
    }
}
