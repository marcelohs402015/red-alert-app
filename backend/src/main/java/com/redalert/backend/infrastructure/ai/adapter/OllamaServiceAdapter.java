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
                Você é um assistente inteligente especializado em analisar e-mails de agendamento de aulas e reuniões.
                Sua tarefa é extrair informações cruciais de um e-mail em Português.
                Data de hoje: %s

                Analise o conteúdo do e-mail e extraia os seguintes campos no formato JSON:
                - title: Um título curto e claro (ex: "Aula de Inglês - Lesson 7").
                - date: A data e hora exata do evento no formato ISO (YYYY-MM-DDTHH:mm:ss). Se encontrar "06/01/2026 às 08:00", converta para "2026-01-06T08:00:00".
                - url: O link da reunião (ex: Microsoft Teams, Google Meet, Zoom). Se não houver, deixe null.
                - description: Um resumo amigável em Português incluindo detalhes como nome do Professor e o que será estudado.
                - isUrgent: Sempre true para este tipo de e-mail.

                FORMATO DE RESPOSTA (RETORNE APENAS O JSON):
                {
                    "title": "...",
                    "date": "...",
                    "url": "...",
                    "description": "...",
                    "isUrgent": true
                }

                Se não encontrar nenhum evento ou data, retorne null.

                CONTEÚDO DO E-MAIL:
                %s
                """
                .formatted(receivedAt.toString(), emailBody);
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
