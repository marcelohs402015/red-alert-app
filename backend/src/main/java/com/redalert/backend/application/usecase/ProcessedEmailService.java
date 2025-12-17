package com.redalert.backend.application.usecase;

import com.redalert.backend.domain.model.Category;
import com.redalert.backend.domain.model.ProcessedEmail;
import com.redalert.backend.domain.repository.ProcessedEmailRepository;
import com.redalert.backend.presentation.dto.ProcessedEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing processed emails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessedEmailService {

    private final ProcessedEmailRepository processedEmailRepository;

    /**
     * Saves a processed email if it doesn't already exist.
     *
     * @param emailId     Gmail message ID
     * @param fromAddress Email sender
     * @param subject     Email subject
     * @param snippet     Email snippet
     * @param receivedAt  When email was received
     * @param category    Category that matched (optional)
     * @return The saved or existing ProcessedEmail
     */
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

    /**
     * Checks if an email has already been processed.
     */
    public boolean isAlreadyProcessed(String emailId) {
        return processedEmailRepository.existsByEmailId(emailId);
    }

    /**
     * Gets all processed emails.
     */
    public List<ProcessedEmailResponse> getAllProcessedEmails() {
        return processedEmailRepository.findAllByOrderByProcessedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Gets processed emails by category.
     */
    public List<ProcessedEmailResponse> getProcessedEmailsByCategory(Long categoryId) {
        return processedEmailRepository.findByCategoryIdOrderByProcessedAtDesc(categoryId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Deletes a processed email by ID.
     */
    @Transactional
    public void deleteProcessedEmail(Long id) {
        ProcessedEmail email = processedEmailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Processed email not found with id: " + id));

        log.info("Deleting processed email: {} - {}", email.getEmailId(), email.getSubject());
        processedEmailRepository.delete(email);
    }

    /**
     * Deletes all processed emails.
     */
    @Transactional
    public void deleteAllProcessedEmails() {
        log.info("Deleting all processed emails");
        processedEmailRepository.deleteAll();
    }

    /**
     * Gets the count of processed emails.
     */
    public long getProcessedEmailCount() {
        return processedEmailRepository.count();
    }

    /**
     * Converts entity to response DTO.
     */
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
