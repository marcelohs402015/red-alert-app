package com.redalert.backend.service;

import com.redalert.backend.domain.model.Category;
import com.redalert.backend.infrastructure.persistence.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for monitoring categories (CRUD and active list).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        log.info("Creating new category: {}", category.getName());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existing = getCategoryById(id);
        if (!existing.getName().equals(updatedCategory.getName()) &&
                categoryRepository.existsByName(updatedCategory.getName())) {
            throw new IllegalArgumentException("Category with name '" + updatedCategory.getName() + "' already exists");
        }
        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());
        existing.setFromFilter(updatedCategory.getFromFilter());
        existing.setSubjectKeywords(updatedCategory.getSubjectKeywords());
        existing.setBodyKeywords(updatedCategory.getBodyKeywords());
        existing.setIsActive(updatedCategory.getIsActive());
        log.info("Updating category: {} with query: {}", existing.getName(), existing.buildGmailQuery());
        return categoryRepository.save(existing);
    }

    @Transactional
    public Category toggleCategory(Long id) {
        Category category = getCategoryById(id);
        category.setIsActive(!category.getIsActive());
        log.info("Toggling category '{}' to: {}", category.getName(), category.getIsActive());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        log.info("Deleting category: {}", category.getName());
        categoryRepository.delete(category);
    }
}
