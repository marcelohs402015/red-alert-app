package com.redalert.backend.infrastructure.persistence;

import com.redalert.backend.domain.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data access for Alert entity (JPA).
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc();

    Page<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Alert> findByIsUrgentTrueOrderByCreatedAtDesc();

    List<Alert> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    List<Alert> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime after);

    long countByIsUrgentTrue();

    @Query("DELETE FROM Alert a WHERE a.createdAt < :before")
    void deleteOlderThan(LocalDateTime before);
}
