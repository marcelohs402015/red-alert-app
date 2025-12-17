package com.redalert.backend.presentation.controller;

import com.redalert.backend.application.usecase.EmailPollingService;
import com.redalert.backend.domain.model.EmailDto;
import com.redalert.backend.domain.port.GmailPort;
import com.redalert.backend.presentation.dto.EmailResponse;
import com.redalert.backend.presentation.dto.EmailSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for email operations.
 */
@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Emails", description = "Operações de busca e consulta de emails")
public class EmailController {

    private final GmailPort gmailPort;
    private final EmailPollingService emailPollingService;

    /**
     * Manually triggers email polling for all active categories.
     * This is the SAME process that runs on the scheduler every minute.
     */
    @PostMapping("/poll")
    @Operation(summary = "Disparar busca de emails (mesmo processo do scheduler)")
    public ResponseEntity<Map<String, Object>> triggerPolling() {
        log.info("🚀 Manual email polling triggered via API");

        try {
            emailPollingService.pollEmails();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Polling executado! Verifique os logs do console para detalhes.",
                    "timestamp", LocalDateTime.now().toString()));
        } catch (Exception e) {
            log.error("Error during manual polling", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now().toString()));
        }
    }

    /**
     * Searches for emails with custom query.
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar emails com filtros customizados")
    public ResponseEntity<EmailSearchResponse> searchEmails(
            @Parameter(description = "Remetente (email ou domínio)") @RequestParam(required = false) String from,
            @Parameter(description = "Palavras-chave no assunto") @RequestParam(required = false) String subject,
            @Parameter(description = "Palavras-chave no corpo") @RequestParam(required = false) String body,
            @Parameter(description = "Apenas não lidos") @RequestParam(defaultValue = "true") boolean unreadOnly,
            @Parameter(description = "Número máximo de resultados") @RequestParam(defaultValue = "10") int maxResults) {

        long startTime = System.currentTimeMillis();

        // Build Gmail query
        StringBuilder queryBuilder = new StringBuilder();

        if (from != null && !from.isBlank()) {
            queryBuilder.append("from:").append(from).append(" ");
        }

        if (subject != null && !subject.isBlank()) {
            queryBuilder.append("subject:").append(subject).append(" ");
        }

        if (body != null && !body.isBlank()) {
            queryBuilder.append(body).append(" ");
        }

        if (unreadOnly) {
            queryBuilder.append("is:unread");
        }

        String query = queryBuilder.toString().trim();
        if (query.isEmpty()) {
            query = "is:unread";
        }

        log.info("Searching emails with query: {}", query);

        List<EmailDto> emails = gmailPort.searchEmails(query, maxResults);
        List<EmailResponse> emailResponses = emails.stream()
                .map(EmailResponse::fromDomain)
                .toList();

        long searchTime = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(new EmailSearchResponse(
                emailResponses,
                emailResponses.size(),
                query,
                searchTime));
    }
}
