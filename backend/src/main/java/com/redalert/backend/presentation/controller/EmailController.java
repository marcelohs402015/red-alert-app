package com.redalert.backend.presentation.controller;

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

import java.util.List;

/**
 * REST Controller for email operations.
 * Provides endpoints to search and retrieve emails from Gmail.
 */
@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Emails", description = "Operações de busca e consulta de emails")
public class EmailController {

    private final GmailPort gmailPort;

    /**
     * Searches for emails from FCTECH.
     * 
     * @param maxResults Maximum number of results (default: 10)
     * @return List of emails found
     */
    @GetMapping("/fctech")
    @Operation(summary = "Buscar emails da FCTECH", description = "Retorna emails não lidos da FCTECH")
    public ResponseEntity<EmailSearchResponse> searchFctechEmails(
            @Parameter(description = "Número máximo de resultados") @RequestParam(defaultValue = "10") int maxResults) {
        long startTime = System.currentTimeMillis();

        String query = "from:fctech.com.br is:unread";
        log.info("Searching FCTECH emails with query: {}", query);

        List<EmailDto> emails = gmailPort.searchEmails(query, maxResults);
        List<EmailResponse> emailResponses = emails.stream()
                .map(EmailResponse::fromDomain)
                .toList();

        long searchTime = System.currentTimeMillis() - startTime;

        EmailSearchResponse response = new EmailSearchResponse(
                emailResponses,
                emailResponses.size(),
                query,
                searchTime);

        return ResponseEntity.ok(response);
    }

    /**
     * Searches for emails with custom query.
     * 
     * @param from       Sender email or domain (optional)
     * @param subject    Subject keywords (optional)
     * @param unreadOnly Only unread emails (default: true)
     * @param maxResults Maximum number of results (default: 10)
     * @return List of emails found
     */
    @GetMapping("/search")
    @Operation(summary = "Buscar emails com filtros customizados", description = "Busca emails com filtros personalizados")
    public ResponseEntity<EmailSearchResponse> searchEmails(
            @Parameter(description = "Remetente (email ou domínio)") @RequestParam(required = false) String from,

            @Parameter(description = "Palavras-chave no assunto") @RequestParam(required = false) String subject,

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

        if (unreadOnly) {
            queryBuilder.append("is:unread ");
        }

        String query = queryBuilder.toString().trim();
        if (query.isEmpty()) {
            query = "is:unread"; // Default query
        }

        log.info("Searching emails with query: {}", query);

        List<EmailDto> emails = gmailPort.searchEmails(query, maxResults);
        List<EmailResponse> emailResponses = emails.stream()
                .map(EmailResponse::fromDomain)
                .toList();

        long searchTime = System.currentTimeMillis() - startTime;

        EmailSearchResponse response = new EmailSearchResponse(
                emailResponses,
                emailResponses.size(),
                query,
                searchTime);

        return ResponseEntity.ok(response);
    }

    /**
     * Gets unread email count from FCTECH.
     * 
     * @return Unread count
     */
    @GetMapping("/fctech/count")
    @Operation(summary = "Contar emails não lidos da FCTECH", description = "Retorna quantidade de emails não lidos da FCTECH")
    public ResponseEntity<UnreadCountResponse> getFctechUnreadCount() {
        int count = gmailPort.getUnreadCount("fctech.com.br");
        return ResponseEntity.ok(new UnreadCountResponse(count, "fctech.com.br"));
    }

    /**
     * Response DTO for unread count.
     */
    public record UnreadCountResponse(int count, String from) {
    }
}
