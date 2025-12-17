package com.redalert.backend.presentation.controller;

import com.redalert.backend.application.usecase.ProcessedEmailService;
import com.redalert.backend.presentation.dto.ProcessedEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing processed emails.
 * Provides endpoints to list and delete emails that were fetched by the system.
 */
@RestController
@RequestMapping("/api/v1/processed-emails")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000" })
public class ProcessedEmailController {

    private final ProcessedEmailService processedEmailService;

    /**
     * Gets all processed emails.
     *
     * @return List of all processed emails ordered by most recent first
     */
    @GetMapping
    public ResponseEntity<List<ProcessedEmailResponse>> getAllProcessedEmails() {
        log.info("Fetching all processed emails");
        List<ProcessedEmailResponse> emails = processedEmailService.getAllProcessedEmails();
        return ResponseEntity.ok(emails);
    }

    /**
     * Gets processed emails by category.
     *
     * @param categoryId Category ID to filter by
     * @return List of processed emails for the category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProcessedEmailResponse>> getProcessedEmailsByCategory(
            @PathVariable Long categoryId) {
        log.info("Fetching processed emails for category: {}", categoryId);
        List<ProcessedEmailResponse> emails = processedEmailService.getProcessedEmailsByCategory(categoryId);
        return ResponseEntity.ok(emails);
    }

    /**
     * Gets the count of processed emails.
     *
     * @return Count of processed emails
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getProcessedEmailCount() {
        long count = processedEmailService.getProcessedEmailCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Deletes a processed email by ID.
     *
     * @param id Processed email ID to delete
     * @return Success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProcessedEmail(@PathVariable Long id) {
        log.info("Deleting processed email with id: {}", id);
        processedEmailService.deleteProcessedEmail(id);
        return ResponseEntity.ok(Map.of(
                "message", "Processed email deleted successfully",
                "id", id.toString()));
    }

    /**
     * Deletes all processed emails.
     *
     * @return Success response
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteAllProcessedEmails() {
        log.info("Deleting all processed emails");
        processedEmailService.deleteAllProcessedEmails();
        return ResponseEntity.ok(Map.of("message", "All processed emails deleted successfully"));
    }
}
