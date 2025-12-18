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
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Adapter implementation for Ollama (Local LLM) AI analysis.
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class OllamaServiceAdapter implements AiAnalysisPort {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ollama.api.url:http://localhost:11434/api/chat}")
    private String apiUrl;

    @Value("${ollama.model:llama3}")
    private String modelName;

    @Override
    @CircuitBreaker(name = "ollamaService", fallbackMethod = "fallbackAnalysis")
    public ClassAlertDto analyzeEmailContent(String emailBody, LocalDateTime receivedAt) {
        try {
            log.info("Sending email content to Ollama ({}) for analysis.", modelName);

            String prompt = buildPrompt(emailBody, receivedAt);
            String requestBody = buildRequestBody(prompt);

            // Call Ollama API
            String jsonResponse = webClientBuilder.build()
                    .post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseOllamaResponse(jsonResponse);

        } catch (Exception e) {
            log.error("Error analyzing email with Ollama", e);
            throw new AiAnalysisException("Failed to analyze email content with Ollama", e);
        }
    }

    private String buildPrompt(String emailBody, LocalDateTime receivedAt) {
        return """
                You are a smart assistant. Analyze the email and find if there is a class, meeting, or live event.
                Today's date: %s
                Extract: title, date (ISO YYYY-MM-DDTHH:mm:ss), url, and a rich description.
                Return ONLY a JSON object. If nothing is found, return null.

                Format:
                {
                    "title": "Title",
                    "date": "2025-12-18T19:00:00",
                    "url": "http://...",
                    "description": "Short summary",
                    "isUrgent": true
                }

                EMAIL CONTENT:
                %s
                """.formatted(receivedAt.toString(), emailBody);
    }

    private String buildRequestBody(String prompt) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "model", modelName,
                    "stream", false,
                    "messages", new Object[] {
                            Map.of("role", "user", "content", prompt)
                    }));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to build request body", e);
        }
    }

    private ClassAlertDto parseOllamaResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String content = root.path("message").path("content").asText();

            // Minimal cleaning
            content = content.replace("```json", "").replace("```", "").trim();

            if (content.isEmpty() || "null".equalsIgnoreCase(content)) {
                return null;
            }

            return objectMapper.readValue(content, ClassAlertDto.class);
        } catch (Exception e) {
            log.warn("Failed to parse Ollama response: {}. Error: {}", jsonResponse, e.getMessage());
            return null;
        }
    }

    private ClassAlertDto fallbackAnalysis(String emailBody, LocalDateTime receivedAt, Throwable throwable) {
        log.warn("Ollama service failed, using fallback. Error: {}", throwable.getMessage());
        return null;
    }
}
