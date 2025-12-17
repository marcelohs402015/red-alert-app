package com.redalert.backend.domain.repository;

import com.redalert.backend.domain.model.ProcessedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProcessedEmail entity.
 */
@Repository
public interface ProcessedEmailRepository extends JpaRepository<ProcessedEmail, Long> {

    /**
     * Finds a processed email by its Gmail message ID.
     */
    Optional<ProcessedEmail> findByEmailId(String emailId);

    /**
     * Checks if an email has already been processed.
     */
    boolean existsByEmailId(String emailId);

    /**
     * Finds all processed emails ordered by processed date (most recent first).
     */
    List<ProcessedEmail> findAllByOrderByProcessedAtDesc();

    /**
     * Finds processed emails by category ID.
     */
    List<ProcessedEmail> findByCategoryIdOrderByProcessedAtDesc(Long categoryId);

    /**
     * Deletes a processed email by its Gmail message ID.
     */
    void deleteByEmailId(String emailId);
}
