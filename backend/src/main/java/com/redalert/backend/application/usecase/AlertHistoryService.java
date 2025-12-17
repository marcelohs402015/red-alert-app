package com.redalert.backend.application.usecase;

import com.redalert.backend.domain.model.Alert;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.domain.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing alert history with database persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertHistoryService {

    private final AlertRepository alertRepository;

    /**
     * Adds a new alert to the database.
     */
    @Transactional
    public Alert addAlert(ClassAlertDto classAlert) {
        Alert alert = new Alert();
        alert.setTitle(classAlert.title());
        alert.setDescription(classAlert.description());
        alert.setAlertDate(classAlert.date());
        alert.setUrl(classAlert.url());
        alert.setIsUrgent(classAlert.isUrgent());

        Alert saved = alertRepository.save(alert);
        log.info("Alert saved to database: {} (ID: {})", saved.getTitle(), saved.getId());
        return saved;
    }

    /**
     * Adds alert with email metadata.
     */
    @Transactional
    public Alert addAlert(ClassAlertDto classAlert, String emailId, String emailFrom, String emailSubject) {
        Alert alert = new Alert();
        alert.setTitle(classAlert.title());
        alert.setDescription(classAlert.description());
        alert.setAlertDate(classAlert.date());
        alert.setUrl(classAlert.url());
        alert.setIsUrgent(classAlert.isUrgent());
        alert.setEmailId(emailId);
        alert.setEmailFrom(emailFrom);
        alert.setEmailSubject(emailSubject);

        Alert saved = alertRepository.save(alert);
        log.info("Alert saved with email metadata: {} (ID: {})", saved.getTitle(), saved.getId());
        return saved;
    }

    /**
     * Gets recent alerts with limit.
     */
    public List<Alert> getRecentAlerts(int limit) {
        return alertRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).getContent();
    }

    /**
     * Gets all alerts.
     */
    public List<Alert> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Gets only urgent alerts.
     */
    public List<Alert> getUrgentAlerts() {
        return alertRepository.findByIsUrgentTrueOrderByCreatedAtDesc();
    }

    /**
     * Gets alerts by category.
     */
    public List<Alert> getAlertsByCategory(Long categoryId) {
        return alertRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
    }

    /**
     * Counts total alerts.
     */
    public long countAlerts() {
        return alertRepository.count();
    }

    /**
     * Counts urgent alerts.
     */
    public long countUrgentAlerts() {
        return alertRepository.countByIsUrgentTrue();
    }

    /**
     * Clears all alerts from database.
     */
    @Transactional
    public void clearAllAlerts() {
        long count = alertRepository.count();
        alertRepository.deleteAll();
        log.info("Cleared {} alerts from database", count);
    }

    /**
     * Deletes alerts older than specified days.
     */
    @Transactional
    public void deleteOldAlerts(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        alertRepository.deleteOlderThan(cutoff);
        log.info("Deleted alerts older than {} days", daysOld);
    }
}
