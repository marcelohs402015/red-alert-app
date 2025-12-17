package com.redalert.backend.domain.repository;

import com.redalert.backend.domain.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Alert entity.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Finds all alerts ordered by creation date (newest first).
     */
    List<Alert> findAllByOrderByCreatedAtDesc();

    /**
     * Finds recent alerts with pagination.
     */
    Page<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Finds only urgent alerts.
     */
    List<Alert> findByIsUrgentTrueOrderByCreatedAtDesc();

    /**
     * Finds alerts by category.
     */
    List<Alert> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    /**
     * Finds alerts created after a specific date.
     */
    List<Alert> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);

    /**
     * Counts urgent alerts.
     */
    long countByIsUrgentTrue();

    /**
     * Deletes old alerts (cleanup).
     */
    @Query("DELETE FROM Alert a WHERE a.createdAt < :before")
    void deleteOlderThan(LocalDateTime before);
}
