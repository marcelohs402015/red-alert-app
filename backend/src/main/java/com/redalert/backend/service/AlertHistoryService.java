package com.redalert.backend.service;

import com.redalert.backend.domain.model.Alert;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.infrastructure.persistence.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application service for alert history (persistence and queries).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertHistoryService {

    private final AlertRepository alertRepository;

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

    public List<Alert> getRecentAlerts(int limit) {
        return alertRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).getContent();
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Alert> getUrgentAlerts() {
        return alertRepository.findByIsUrgentTrueOrderByCreatedAtDesc();
    }

    public List<Alert> getAlertsByCategory(Long categoryId) {
        return alertRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
    }

    public long countAlerts() {
        return alertRepository.count();
    }

    public long countUrgentAlerts() {
        return alertRepository.countByIsUrgentTrue();
    }

    @Transactional
    public void clearAllAlerts() {
        long count = alertRepository.count();
        alertRepository.deleteAll();
        log.info("Cleared {} alerts from database", count);
    }

    @Transactional
    public void deleteOldAlerts(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        alertRepository.deleteOlderThan(cutoff);
        log.info("Deleted alerts older than {} days", daysOld);
    }
}
