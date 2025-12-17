package com.redalert.backend.domain.repository;

import com.redalert.backend.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds all active categories.
     */
    List<Category> findByIsActiveTrue();

    /**
     * Finds category by name.
     */
    Optional<Category> findByName(String name);

    /**
     * Checks if category with name exists.
     */
    boolean existsByName(String name);
}
