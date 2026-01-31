package com.redalert.backend.infrastructure.messaging.adapter;

import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket notification implementation (Adapter – GoF).
 * Broadcasts alerts to /topic/alerts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationAdapter implements NotificationSender {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC_ALERTS = "/topic/alerts";

    /**
     * Sends alert to all connected WebSocket clients.
     * 
     * @param alert The alert to broadcast
     */
    @Override
    public void sendAlert(ClassAlertDto alert) {
        log.info("========================================");
        log.info("WebSocketNotificationAdapter.sendAlert() CALLED");
        log.info("Alert Title: {}", alert.title());
        log.info("Alert isUrgent: {}", alert.isUrgent());
        log.info("Alert Date: {}", alert.date());
        log.info("Topic: {}", TOPIC_ALERTS);
        log.info("SimpMessagingTemplate: {}", messagingTemplate != null ? "INITIALIZED" : "NULL!!!");

        try {
            log.info(">>> Calling messagingTemplate.convertAndSend()...");
            messagingTemplate.convertAndSend(TOPIC_ALERTS, alert);
            log.info(">>> SUCCESS! Alert sent to topic: {}", TOPIC_ALERTS);
        } catch (Exception e) {
            log.error("!!! FAILED to send alert via WebSocket !!!", e);
            log.error("Exception class: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            // Don't throw exception - notification failure shouldn't break the flow
        }

        log.info("WebSocketNotificationAdapter.sendAlert() FINISHED");
        log.info("========================================");
    }
}
