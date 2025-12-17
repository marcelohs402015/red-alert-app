package com.redalert.backend.presentation.controller;

import com.redalert.backend.application.usecase.AlertHistoryService;
import com.redalert.backend.domain.model.ClassAlertDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for alert operations.
 * Provides endpoints to retrieve alert history.
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Gerenciamento de alertas e histórico")
public class AlertController {

    private final AlertHistoryService alertHistoryService;

    /**
     * Gets recent alerts from history.
     * 
     * @param limit Maximum number of alerts to return (default: 20)
     * @return List of recent alerts
     */
    @GetMapping("/history")
    @Operation(summary = "Obter histórico de alertas", description = "Retorna os alertas mais recentes")
    public ResponseEntity<AlertHistoryResponse> getAlertHistory(
            @Parameter(description = "Número máximo de alertas") @RequestParam(defaultValue = "20") int limit) {
        List<ClassAlertDto> alerts = alertHistoryService.getRecentAlerts(limit);
        int totalCount = alertHistoryService.getTotalCount();

        AlertHistoryResponse response = new AlertHistoryResponse(
                alerts,
                totalCount,
                alerts.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Clears alert history.
     */
    @DeleteMapping("/history")
    @Operation(summary = "Limpar histórico de alertas", description = "Remove todos os alertas do histórico")
    public ResponseEntity<Void> clearHistory() {
        alertHistoryService.clearHistory();
        return ResponseEntity.noContent().build();
    }

    /**
     * Response DTO for alert history.
     */
    public record AlertHistoryResponse(
            List<ClassAlertDto> alerts,
            int totalCount,
            int returnedCount) {
    }
}
