package com.redalert.backend.infrastructure.ai.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redalert.backend.application.exception.AiAnalysisException;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.domain.port.AiAnalysisPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Adapter implementation for Gemini AI analysis.
 * Implements AiAnalysisPort using Google's Gemini API.
 * 
 * This adapter uses Gemini Pro to analyze email text and extract structured
 * event data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiServiceAdapter implements AiAnalysisPort {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    /**
     * Analyzes email content using Gemini AI.
     */
    @Override
    @CircuitBreaker(name = "geminiService", fallbackMethod = "fallbackAnalysis")
    public ClassAlertDto analyzeEmailContent(String emailBody, LocalDateTime receivedAt) {
        try {
            log.info("Sending email content to Gemini for analysis. Reference date: {}", receivedAt);

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

            return parseGeminiResponse(jsonResponse);

        } catch (Exception e) {
            log.error("Error analyzing email with Gemini", e);
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

        return """
                You are a smart assistant for a student. Your job is to analyze notification emails from school/courses and identify if there is a scheduled class, meeting, or live event.

                Current Context:
                - Today's Date (Email Received): %s
                - Timezone: America/Sao_Paulo (UTC-3)

                Instructions:
                1. Analyze the email text below.
                2. If it mentions a class, meeting, live session, or webinar (e.g., "AULA AO VIVO", "MENTORIA", "REUNIÃO"), extract the details.
                3. Resolve relative dates (e.g., "tomorrow", "amanhã", "next monday") based on TODAY'S DATE.
                4. Extract the LINK/URL if available.
                5. Create a RICH DESCRIPTION that summarizes the email content, key topics, and instructions. This description will be used as the calendar event body.
                6. Return ONLY a valid JSON object. Do not include markdown formatting like ```json.

                JSON Structure:
                {
                    "title": "Short title of the event",
                    "date": "ISO 8601 format (YYYY-MM-DDTHH:mm:ss)",
                    "url": "https://...",
                    "description": "Detailed summary of agenda/topics/instructions from email body",
                    "isUrgent": true
                }

                If NO relevant event is found, return null.

                EMAIL CONTENT:
                %s
                """
                .formatted(receivedAt.toString(), cleanBody);
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
                log.warn("Gemini returned no candidates");
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
            log.warn("Failed to parse Gemini response: {}. Error: {}", jsonResponse, e.getMessage());
            return null;
        }
    }

    private ClassAlertDto fallbackAnalysis(String emailBody, LocalDateTime receivedAt, Throwable throwable) {
        log.warn("Gemini service unavailable, using fallback. Error: {}", throwable.getMessage());
        return null;
    }
}
