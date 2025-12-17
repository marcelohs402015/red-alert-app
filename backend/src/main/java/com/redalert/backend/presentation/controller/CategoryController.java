package com.redalert.backend.presentation.controller;

import com.redalert.backend.application.usecase.CategoryService;
import com.redalert.backend.domain.model.Category;
import com.redalert.backend.presentation.dto.CategoryRequest;
import com.redalert.backend.presentation.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for category management.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Gerenciamento de categorias de monitoramento")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Gets all categories.
     */
    @GetMapping
    @Operation(summary = "Listar todas as categorias")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(categories);
    }

    /**
     * Gets only active categories.
     */
    @GetMapping("/active")
    @Operation(summary = "Listar categorias ativas")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        List<CategoryResponse> categories = categoryService.getActiveCategories()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(categories);
    }

    /**
     * Gets category by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }

    /**
     * Creates a new category.
     */
    @PostMapping
    @Operation(summary = "Criar nova categoria")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CategoryResponse.fromEntity(category));
    }

    /**
     * Updates an existing category.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        Category category = categoryService.updateCategory(id, request.toEntity());
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }

    /**
     * Toggles category active status.
     */
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Ativar/Desativar categoria")
    public ResponseEntity<CategoryResponse> toggleCategory(@PathVariable Long id) {
        Category category = categoryService.toggleCategory(id);
        return ResponseEntity.ok(CategoryResponse.fromEntity(category));
    }

    /**
     * Deletes a category.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar categoria")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
