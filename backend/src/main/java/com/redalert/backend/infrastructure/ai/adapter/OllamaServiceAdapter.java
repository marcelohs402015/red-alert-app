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
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Ollama (local LLM) AI analysis implementation (Strategy/Adapter – GoF).
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class OllamaServiceAdapter implements AiAnalyzer {

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
            log.info("[LLM] Enviando para Ollama ({}): bodyLength={}, receivedAt={}, bodyPreview={}", 
                    modelName, emailBody.length(), receivedAt,
                    emailBody.length() > 200 ? emailBody.substring(0, 200).replace("\n", " ") + "..." : emailBody.replace("\n", " "));

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

            ClassAlertDto result = parseOllamaResponse(jsonResponse);
            if (result == null) {
                log.warn("[LLM] Ollama retornou null (sem evento extraído ou resposta inválida). Resposta bruta (trecho): {}", 
                        jsonResponse != null && jsonResponse.length() > 300 ? jsonResponse.substring(0, 300) + "..." : jsonResponse);
            } else {
                log.info("[LLM] Ollama extraiu evento: title='{}', date='{}', url='{}', isUrgent={}", 
                        result.title(), result.date(), result.url(), result.isUrgent());
            }
            return result;

        } catch (Exception e) {
            log.error("[LLM] Erro ao analisar email com Ollama", e);
            throw new AiAnalysisException("Failed to analyze email content with Ollama", e);
        }
    }

    private String buildPrompt(String emailBody, LocalDateTime receivedAt) {
        // Format receivedAt in a more readable way for the LLM
        String todayDate = receivedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String todayTime = receivedAt.format(DateTimeFormatter.ofPattern("HH:mm"));
        String todayISO = receivedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        return """
                Você é um assistente especializado em extrair informações de e-mails de agendamento de aulas, mentorias e reuniões em Português.

                CONTEXTO IMPORTANTE:
                - Data de HOJE (quando o e-mail foi recebido): %s às %s
                - Data de HOJE no formato ISO: %s
                - Timezone: America/Sao_Paulo (UTC-3)

                INSTRUÇÕES CRÍTICAS PARA EXTRAÇÃO:

                1. DATA E HORA DO EVENTO:
                   - Se o e-mail diz "Hoje" ou "hoje" → use a data de HOJE (%s)
                   - Se diz "Amanhã" ou "amanhã" → adicione 1 dia à data de HOJE
                   - Se diz "29/01" ou "29/01/2026" → converta para formato ISO: 2026-01-29
                   - Se diz "às 19h00" ou "19:00" → use essa hora
                   - Formato final ISO obrigatório: YYYY-MM-DDTHH:mm:ss (exemplo: 2026-01-29T19:00:00)
                   - Se não encontrar data explícita, use a data de HOJE
                   - Se não encontrar hora, use 19:00 como padrão

                2. LINK DE ACESSO (PRIORIDADE MÁXIMA):
                   - Procure por "Link de acesso", "link", "acesse", "https://", "http://"
                   - Extraia o link completo (ex: https://fcycle.co/m29-01)
                   - Coloque no campo "url"
                   - O link é MAIS IMPORTANTE que qualquer texto do e-mail

                3. TÍTULO:
                   - Título curto e claro do evento (ex: "Mentoria ao vivo - Full Cycle")

                4. DESCRIÇÃO (FOCO NO LINK):
                   - Se houver link de acesso: a descrição DEVE ser basicamente o link. Exemplo: "Link de acesso: https://fcycle.co/m29-01"
                   - NÃO preencha com texto longo do contexto do e-mail. O link da aula é o essencial.
                   - Se não houver link, aí sim use um resumo bem curto (1 linha)

                5. URGÊNCIA:
                   - Sempre true para aulas ao vivo, mentorias e reuniões agendadas

                FORMATO DE RESPOSTA (RETORNE APENAS JSON, SEM markdown):
                {
                    "title": "Título do evento",
                    "date": "2026-01-29T19:00:00",
                    "url": "https://link-da-aula.com ou null",
                    "description": "Link de acesso: https://... (se tiver link; senão 1 linha de resumo)",
                    "isUrgent": true
                }

                EXEMPLOS DE CONVERSÃO DE DATA:
                - "Hoje, 29/01, às 19h00" → date: "2026-01-29T19:00:00" (se hoje é 29/01/2026)
                - "Amanhã às 20:00" → date: "2026-01-30T20:00:00" (se hoje é 29/01/2026)
                - "30/01/2026 às 15:30" → date: "2026-01-30T15:30:00"

                Se NÃO encontrar nenhum evento agendado, retorne null.

                CONTEÚDO DO E-MAIL:
                %s
                """
                .formatted(todayDate, todayTime, todayISO, todayISO, emailBody);
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

            ClassAlertDto result = objectMapper.readValue(content, ClassAlertDto.class);
            log.info("[LLM] Parse bem-sucedido: title='{}', date='{}'", result.title(), result.date());
            return result;
        } catch (Exception e) {
            log.warn("[LLM] Falha ao fazer parse da resposta Ollama. Erro: {} | Resposta (início): {}", 
                    e.getMessage(), jsonResponse != null && jsonResponse.length() > 400 ? jsonResponse.substring(0, 400) : jsonResponse);
            return null;
        }
    }

    private ClassAlertDto fallbackAnalysis(String emailBody, LocalDateTime receivedAt, Throwable throwable) {
        log.warn("Ollama service failed, using fallback. Error: {}", throwable.getMessage());
        return null;
    }
}
