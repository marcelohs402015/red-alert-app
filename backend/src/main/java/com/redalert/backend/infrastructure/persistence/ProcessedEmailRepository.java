package com.redalert.backend.infrastructure.persistence;

import com.redalert.backend.domain.model.ProcessedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for ProcessedEmail entity (JPA).
 */
@Repository
public interface ProcessedEmailRepository extends JpaRepository<ProcessedEmail, Long> {

    Optional<ProcessedEmail> findByEmailId(String emailId);

    boolean existsByEmailId(String emailId);

    List<ProcessedEmail> findAllByOrderByProcessedAtDesc();

    List<ProcessedEmail> findByCategoryIdOrderByProcessedAtDesc(Long categoryId);

    void deleteByEmailId(String emailId);
}
