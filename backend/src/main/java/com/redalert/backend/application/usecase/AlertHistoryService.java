package com.redalert.backend.application.usecase;

import com.redalert.backend.domain.model.ClassAlertDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for managing alert history.
 * Stores alerts in memory for quick retrieval.
 */
@Service
@Slf4j
public class AlertHistoryService {

    @Getter
    private final List<ClassAlertDto> alertHistory = Collections.synchronizedList(new ArrayList<>());

    private static final int MAX_HISTORY_SIZE = 100;

    /**
     * Adds an alert to history.
     * 
     * @param alert The alert to add
     */
    public void addAlert(ClassAlertDto alert) {
        synchronized (alertHistory) {
            alertHistory.add(0, alert); // Add to beginning

            // Keep only last MAX_HISTORY_SIZE alerts
            if (alertHistory.size() > MAX_HISTORY_SIZE) {
                alertHistory.remove(alertHistory.size() - 1);
            }
        }

        log.debug("Alert added to history. Total alerts: {}", alertHistory.size());
    }

    /**
     * Gets recent alerts.
     * 
     * @param limit Maximum number of alerts to return
     * @return List of recent alerts
     */
    public List<ClassAlertDto> getRecentAlerts(int limit) {
        synchronized (alertHistory) {
            int size = Math.min(limit, alertHistory.size());
            return new ArrayList<>(alertHistory.subList(0, size));
        }
    }

    /**
     * Clears all alert history.
     */
    public void clearHistory() {
        synchronized (alertHistory) {
            alertHistory.clear();
        }
        log.info("Alert history cleared");
    }

    /**
     * Gets total alert count.
     */
    public int getTotalCount() {
        return alertHistory.size();
    }
}
