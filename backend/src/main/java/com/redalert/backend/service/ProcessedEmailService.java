package com.redalert.backend.service;

import com.redalert.backend.domain.model.Category;
import com.redalert.backend.domain.model.ProcessedEmail;
import com.redalert.backend.infrastructure.persistence.ProcessedEmailRepository;
import com.redalert.backend.presentation.dto.ProcessedEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service for processed emails (save, list, delete).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessedEmailService {

    private final ProcessedEmailRepository processedEmailRepository;

    @Transactional
    public ProcessedEmail saveIfNotExists(
            String emailId,
            String fromAddress,
            String subject,
            String snippet,
            LocalDateTime receivedAt,
            Category category) {
        return processedEmailRepository.findByEmailId(emailId)
                .orElseGet(() -> {
                    ProcessedEmail email = ProcessedEmail.builder()
                            .emailId(emailId)
                            .fromAddress(fromAddress)
                            .subject(subject)
                            .snippet(snippet)
                            .receivedAt(receivedAt)
                            .category(category)
                            .build();
                    log.info("Saving processed email: {} - {}", emailId, subject);
                    return processedEmailRepository.save(email);
                });
    }

    public boolean isAlreadyProcessed(String emailId) {
        return processedEmailRepository.existsByEmailId(emailId);
    }

    /**
     * Returns a processed email by ID for use in simulate/alert flows.
     */
    public ProcessedEmail getProcessedEmailById(Long id) {
        return processedEmailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processed email not found with id: " + id));
    }

    public List<ProcessedEmailResponse> getAllProcessedEmails() {
        return processedEmailRepository.findAllByOrderByProcessedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProcessedEmailResponse> getProcessedEmailsByCategory(Long categoryId) {
        return processedEmailRepository.findByCategoryIdOrderByProcessedAtDesc(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteProcessedEmail(Long id) {
        ProcessedEmail email = processedEmailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processed email not found with id: " + id));
        log.info("Deleting processed email: {} - {}", email.getEmailId(), email.getSubject());
        processedEmailRepository.delete(email);
    }

    @Transactional
    public void deleteAllProcessedEmails() {
        log.info("Deleting all processed emails");
        processedEmailRepository.deleteAll();
    }

    public long getProcessedEmailCount() {
        return processedEmailRepository.count();
    }

    private ProcessedEmailResponse toResponse(ProcessedEmail email) {
        return new ProcessedEmailResponse(
                email.getId(),
                email.getEmailId(),
                email.getFromAddress(),
                email.getSubject(),
                email.getSnippet(),
                email.getReceivedAt(),
                email.getCategory() != null ? email.getCategory().getName() : null,
                email.getCategory() != null ? email.getCategory().getId() : null,
                email.getProcessedAt());
    }
}
