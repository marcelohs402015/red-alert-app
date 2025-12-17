package com.redalert.backend.infrastructure.ai.adapter;

import com.redalert.backend.application.exception.AiAnalysisException;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.domain.port.AiAnalysisPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Adapter implementation for Gemini AI analysis.
 * Implements AiAnalysisPort using Google's Gemini API.
 * 
 * This adapter:
 * - Sends email content to Gemini for analysis
 * - Parses JSON response into ClassAlertDto
 * - Implements Circuit Breaker pattern for resilience
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiServiceAdapter implements AiAnalysisPort {

    private final WebClient.Builder webClientBuilder;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    /**
     * Analyzes email content using Gemini AI.
     * 
     * @param emailBody The email content to analyze
     * @return ClassAlertDto if alert found, null otherwise
     * @throws AiAnalysisException if AI service fails
     */
    @Override
    @CircuitBreaker(name = "geminiService", fallbackMethod = "fallbackAnalysis")
    public ClassAlertDto analyzeEmailContent(String emailBody) {
        try {
            log.info("Sending email content to Gemini for analysis");

            // TODO: Replace with actual Gemini API integration
            // For now, using a stub implementation

            String prompt = buildPrompt(emailBody);

            // Stub: Simulate AI response
            // In production, this would call Gemini API
            ClassAlertDto result = simulateGeminiResponse(emailBody);

            if (result != null) {
                log.info("Gemini identified alert: {}", result.title());
            } else {
                log.debug("No alert identified in email");
            }

            return result;

        } catch (Exception e) {
            log.error("Error analyzing email with Gemini", e);
            throw new AiAnalysisException("Failed to analyze email content", e);
        }
    }

    /**
     * Builds the prompt for Gemini API.
     * 
     * @param emailBody The email content
     * @return Formatted prompt
     */
    private String buildPrompt(String emailBody) {
        return """
                Analyze the following email and extract class/meeting information.
                Return a JSON object with the following structure:
                {
                    "title": "Event title",
                    "date": "ISO 8601 datetime",
                    "url": "Meeting URL if present",
                    "description": "Event description",
                    "isUrgent": true/false
                }

                If no class/meeting information is found, return null.

                Email content:
                %s
                """.formatted(emailBody);
    }

    /**
     * Simulates Gemini API response.
     * TODO: Replace with actual API call using WebClient
     * 
     * @param emailBody Email content
     * @return Simulated ClassAlertDto or null
     */
    private ClassAlertDto simulateGeminiResponse(String emailBody) {
        // Stub logic: Check if email contains keywords
        String lowerBody = emailBody.toLowerCase();

        if (lowerBody.contains("aula") ||
                lowerBody.contains("reunião") ||
                lowerBody.contains("meeting") ||
                lowerBody.contains("class")) {

            return new ClassAlertDto(
                    "Aula Detectada - " + extractSubject(emailBody),
                    LocalDateTime.now().plusHours(2), // Simulate 2 hours from now
                    extractUrl(emailBody),
                    "Evento detectado automaticamente pelo Red Alert",
                    true);
        }

        return null;
    }

    /**
     * Extracts subject from email body (stub implementation).
     */
    private String extractSubject(String emailBody) {
        // Simple extraction - in production, this would be more sophisticated
        String[] lines = emailBody.split("\n");
        return lines.length > 0 ? lines[0].substring(0, Math.min(50, lines[0].length())) : "Sem título";
    }

    /**
     * Extracts URL from email body (stub implementation).
     */
    private String extractUrl(String emailBody) {
        // Simple URL extraction
        if (emailBody.contains("http://") || emailBody.contains("https://")) {
            int start = emailBody.indexOf("http");
            int end = emailBody.indexOf(" ", start);
            if (end == -1)
                end = emailBody.length();
            return emailBody.substring(start, Math.min(end, start + 200));
        }
        return null;
    }

    /**
     * Fallback method when Gemini service is unavailable.
     * 
     * @param emailBody Email content
     * @param throwable The exception that triggered fallback
     * @return null (no analysis available)
     */
    private ClassAlertDto fallbackAnalysis(String emailBody, Throwable throwable) {
        log.warn("Gemini service unavailable, using fallback. Error: {}", throwable.getMessage());
        return null;
    }
}
