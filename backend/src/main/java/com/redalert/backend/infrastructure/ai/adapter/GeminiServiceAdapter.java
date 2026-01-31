package com.redalert.backend.infrastructure.ai.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.service.AiAnalyzer;
import com.redalert.backend.service.exception.AiAnalysisException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gemini AI analysis implementation (Strategy/Adapter – GoF).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiServiceAdapter implements AiAnalyzer {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    /**
     * Analyzes email content using Gemini AI.
     */
    @Override
    @CircuitBreaker(name = "geminiService", fallbackMethod = "fallbackAnalysis")
    public ClassAlertDto analyzeEmailContent(String emailBody, LocalDateTime receivedAt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("GEMINI_API_KEY not configured. Gemini AI analysis disabled.");
            return null;
        }
        try {
            log.info("[LLM] Enviando para Gemini: bodyLength={}, receivedAt={}, bodyPreview={}", emailBody.length(), receivedAt,
                    emailBody.length() > 200 ? emailBody.substring(0, 200).replace("\n", " ") + "..." : emailBody.replace("\n", " "));

            String prompt = buildPrompt(emailBody, receivedAt);
            String requestBody = buildRequestBody(prompt);

            // Call Gemini API
            String jsonResponse = webClientBuilder.build()
                    .post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Blocking is acceptable here as we are in a synchronous polling loop

            ClassAlertDto result = parseGeminiResponse(jsonResponse);
            if (result == null) {
                log.warn("[LLM] Gemini retornou null (sem evento extraído ou resposta inválida). Resposta bruta (trecho): {}", jsonResponse != null && jsonResponse.length() > 300 ? jsonResponse.substring(0, 300) + "..." : jsonResponse);
            } else {
                log.info("[LLM] Gemini extraiu evento: title='{}', date='{}', isUrgent={}", result.title(), result.date(), result.isUrgent());
            }
            return result;

        } catch (Exception e) {
            log.error("[LLM] Erro ao analisar email com Gemini", e);
            throw new AiAnalysisException("Failed to analyze email content", e);
        }
    }

    private String buildPrompt(String emailBody, LocalDateTime receivedAt) {
        // Truncate email body if too long to save tokens/costs
        String cleanBody = emailBody.length() > 5000
                ? emailBody.substring(0, 5000)
                : emailBody;

        // Escape special chars
        cleanBody = cleanBody.replace("\"", "'").replace("\n", " ");

        // Format receivedAt in a more readable way for the LLM
        String todayDate = receivedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String todayTime = receivedAt.format(DateTimeFormatter.ofPattern("HH:mm"));
        String todayISO = receivedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        return """
                You are a smart assistant specialized in analyzing Portuguese emails for scheduled classes, mentorias, and meetings.

                CRITICAL CONTEXT:
                - TODAY's Date (when email was received): %s at %s
                - TODAY's Date in ISO format: %s
                - Timezone: America/Sao_Paulo (UTC-3)

                CRITICAL EXTRACTION INSTRUCTIONS:

                1. EVENT DATE AND TIME:
                   - If email says "Hoje" or "hoje" (Today) → use TODAY's date (%s)
                   - If says "Amanhã" or "amanhã" (Tomorrow) → add 1 day to TODAY
                   - If says "29/01" or "29/01/2026" → convert to ISO: 2026-01-29
                   - If says "às 19h00" or "19:00" → use that time
                   - Required ISO format: YYYY-MM-DDTHH:mm:ss (example: 2026-01-29T19:00:00)
                   - If no explicit date found, use TODAY's date
                   - If no time found, default to 19:00

                2. ACCESS LINK (HIGHEST PRIORITY):
                   - Look for "Link de acesso", "link", "acesse", "https://", "http://"
                   - Extract the full link (e.g., https://fcycle.co/m29-01)
                   - Put in "url" field. The link is MORE IMPORTANT than any email text.

                3. TITLE:
                   - Short, clear event title (e.g., "Mentoria ao vivo - Full Cycle")

                4. DESCRIPTION (FOCUS ON LINK):
                   - If there is an access link: description MUST be basically the link. Example: "Link de acesso: https://fcycle.co/m29-01"
                   - Do NOT fill with long email context. The class link is what matters.
                   - If no link, then use a very short summary (1 line)

                5. URGENCY:
                   - Always true for live classes, mentorias, and scheduled meetings

                RESPONSE FORMAT (RETURN ONLY JSON, NO markdown):
                {
                    "title": "Event title",
                    "date": "2026-01-29T19:00:00",
                    "url": "https://class-link.com or null",
                    "description": "Link de acesso: https://... (if link exists; otherwise 1-line summary)",
                    "isUrgent": true
                }

                DATE CONVERSION EXAMPLES:
                - "Hoje, 29/01, às 19h00" → date: "2026-01-29T19:00:00" (if today is 29/01/2026)
                - "Amanhã às 20:00" → date: "2026-01-30T20:00:00" (if today is 29/01/2026)
                - "30/01/2026 às 15:30" → date: "2026-01-30T15:30:00"

                If NO scheduled event is found, return null.

                EMAIL CONTENT:
                %s
                """
                .formatted(todayDate, todayTime, todayISO, todayISO, cleanBody);
    }

    private String buildRequestBody(String prompt) {
        // Simple JSON construction to avoid object mapping specific requests
        // Gemini API expects: { "contents": [{ "parts": [{ "text": "..." }] }] }
        try {
            var part = objectMapper.createObjectNode().put("text", prompt);
            var parts = objectMapper.createArrayNode().add(part);
            var content = objectMapper.createObjectNode().set("parts", parts);
            var contents = objectMapper.createArrayNode().add(content);
            var root = objectMapper.createObjectNode().set("contents", contents);

            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    private ClassAlertDto parseGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // Navigate to: candidates[0].content.parts[0].text
            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || candidates.isEmpty()) {
                log.warn("[LLM] Gemini retornou sem 'candidates' (resposta pode ser erro ou bloqueio). Resposta (início): {}", jsonResponse != null && jsonResponse.length() > 500 ? jsonResponse.substring(0, 500) : jsonResponse);
                return null;
            }

            String responseText = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            // Clean up response text (sometimes AI adds markdown blocks)
            responseText = responseText.replace("```json", "").replace("```", "").trim();

            if ("null".equalsIgnoreCase(responseText)) {
                return null;
            }

            // Parse result into DTO
            // We use a temporary class or direct mapping if DTO matches perfectly
            // Since DTO is a record, Jackson supports it well
            return objectMapper.readValue(responseText, ClassAlertDto.class);

        } catch (Exception e) {
            log.warn("[LLM] Falha ao fazer parse da resposta Gemini. Erro: {} | Resposta (início): {}", e.getMessage(), jsonResponse != null && jsonResponse.length() > 400 ? jsonResponse.substring(0, 400) : jsonResponse);
            return null;
        }
    }

    private ClassAlertDto fallbackAnalysis(String emailBody, LocalDateTime receivedAt, Throwable throwable) {
        log.warn("Gemini service unavailable, using fallback. Error: {}", throwable.getMessage());
        return null;
    }
}
