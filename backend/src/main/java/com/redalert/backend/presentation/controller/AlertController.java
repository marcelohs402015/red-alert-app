package com.redalert.backend.presentation.controller;

import com.redalert.backend.application.usecase.AlertHistoryService;
import com.redalert.backend.application.usecase.ProcessedEmailService;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.domain.model.ProcessedEmail;
import com.redalert.backend.domain.port.NotificationPort;
import com.redalert.backend.domain.repository.ProcessedEmailRepository;
import com.redalert.backend.presentation.dto.AlertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for alert history management with database persistence.
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alerts", description = "Gerenciamento de histórico de alertas")
public class AlertController {

        private final AlertHistoryService alertHistoryService;
        private final ProcessedEmailRepository processedEmailRepository;
        private final NotificationPort notificationPort;

        /**
         * Gets alert history from database.
         */
        @GetMapping("/history")
        @Operation(summary = "Obter histórico de alertas")
        public ResponseEntity<Map<String, Object>> getAlertHistory(
                        @RequestParam(defaultValue = "20") int limit) {
                List<AlertResponse> alerts = alertHistoryService.getRecentAlerts(limit)
                                .stream()
                                .map(AlertResponse::fromEntity)
                                .toList();

                return ResponseEntity.ok(Map.of(
                                "alerts", alerts,
                                "totalCount", alertHistoryService.countAlerts(),
                                "returnedCount", alerts.size()));
        }

        /**
         * Gets only urgent alerts.
         */
        @GetMapping("/urgent")
        @Operation(summary = "Obter apenas alertas urgentes")
        public ResponseEntity<List<AlertResponse>> getUrgentAlerts() {
                List<AlertResponse> alerts = alertHistoryService.getUrgentAlerts()
                                .stream()
                                .map(AlertResponse::fromEntity)
                                .toList();
                return ResponseEntity.ok(alerts);
        }

        /**
         * Gets alert statistics.
         */
        @GetMapping("/stats")
        @Operation(summary = "Obter estatísticas de alertas")
        public ResponseEntity<Map<String, Object>> getStats() {
                return ResponseEntity.ok(Map.of(
                                "total", alertHistoryService.countAlerts(),
                                "urgent", alertHistoryService.countUrgentAlerts()));
        }

        /**
         * Clears all alert history from database.
         */
        @DeleteMapping("/history")
        @Operation(summary = "Limpar histórico de alertas")
        public ResponseEntity<Void> clearHistory() {
                alertHistoryService.clearAllAlerts();
                return ResponseEntity.noContent().build();
        }

        /**
         * Deletes old alerts.
         */
        @DeleteMapping("/cleanup")
        @Operation(summary = "Limpar alertas antigos")
        public ResponseEntity<Void> cleanupOldAlerts(
                        @RequestParam(defaultValue = "30") int daysOld) {
                alertHistoryService.deleteOldAlerts(daysOld);
                return ResponseEntity.noContent().build();
        }

        /**
         * Simulates an alert from a processed email.
         * Used for testing the overlay functionality.
         *
         * @param processedEmailId ID of the processed email to simulate alert from
         * @return Success response with alert details
         */
        @PostMapping("/simulate/{processedEmailId}")
        @Operation(summary = "Simular alerta a partir de um email processado")
        public ResponseEntity<Map<String, Object>> simulateAlertFromEmail(
                        @PathVariable Long processedEmailId) {

                ProcessedEmail email = processedEmailRepository.findById(processedEmailId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Processed email not found with id: " + processedEmailId));

                ClassAlertDto alert = new ClassAlertDto(
                                "🚨 " + email.getSubject(),
                                LocalDateTime.now(),
                                null, // No URL for simulation
                                "Alerta simulado a partir do email de: " + email.getFromAddress(),
                                true, // Always urgent for simulation
                                null // no calendar link
                );

                // Save to history
                alertHistoryService.addAlert(alert);

                // Send via WebSocket
                notificationPort.sendAlert(alert);

                log.info("🧪 Simulated alert sent for email: {}", email.getSubject());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Alert simulated and sent successfully",
                                "alert", Map.of(
                                                "title", alert.title(),
                                                "date", alert.date().toString(),
                                                "description", alert.description(),
                                                "isUrgent", alert.isUrgent())));
        }

        /**
         * Simulates a custom test alert.
         * Used for testing the overlay with custom data.
         */
        @PostMapping("/simulate/test")
        @Operation(summary = "Simular alerta de teste customizado")
        public ResponseEntity<Map<String, Object>> simulateTestAlert(
                        @RequestParam(defaultValue = "🔴 AO VIVO AGORA: Aula de Teste") String title,
                        @RequestParam(defaultValue = "Esta é uma simulação de alerta para testar o overlay.") String description,
                        @RequestParam(required = false) String url) {

                ClassAlertDto alert = new ClassAlertDto(
                                title,
                                LocalDateTime.now(),
                                url,
                                description,
                                true, // Always urgent for test
                                null // no calendar link
                );

                // Save to history
                alertHistoryService.addAlert(alert);

                // Send via WebSocket
                notificationPort.sendAlert(alert);

                log.info("🧪 Test alert sent: {}", title);

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Test alert sent successfully",
                                "alert", Map.of(
                                                "title", alert.title(),
                                                "date", alert.date().toString(),
                                                "description", alert.description(),
                                                "isUrgent", alert.isUrgent(),
                                                "url", url != null ? url : "")));
        }

        private final com.redalert.backend.application.usecase.EmailPollingService emailPollingService;

        @DeleteMapping("/calendar")
        @Operation(summary = "Limpar eventos do calendário", description = "Deleta TODOS os eventos do calendário principal para a data especificada.")
        public ResponseEntity<String> clearCalendarEvents(
                        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
                int count = emailPollingService.clearCalendarEvents(date);
                return ResponseEntity.ok("Deleted " + count + " events from calendar on " + date);
        }
}
