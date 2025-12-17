package com.redalert.backend.presentation.controller;

import com.redalert.backend.application.usecase.AlertHistoryService;
import com.redalert.backend.presentation.dto.AlertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for alert history management with database persistence.
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Gerenciamento de histórico de alertas")
public class AlertController {

    private final AlertHistoryService alertHistoryService;

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
}
