package com.redalert.backend.infrastructure.messaging.adapter;

import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.domain.port.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Adapter implementation for WebSocket notifications.
 * Implements NotificationPort using Spring's SimpMessagingTemplate.
 * 
 * This adapter broadcasts alerts to all connected WebSocket clients
 * subscribed to /topic/alerts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationAdapter implements NotificationPort {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC_ALERTS = "/topic/alerts";

    /**
     * Sends alert to all connected WebSocket clients.
     * 
     * @param alert The alert to broadcast
     */
    @Override
    public void sendAlert(ClassAlertDto alert) {
        try {
            log.info("Broadcasting alert via WebSocket: {}", alert.title());
            messagingTemplate.convertAndSend(TOPIC_ALERTS, alert);
            log.debug("Alert sent successfully to topic: {}", TOPIC_ALERTS);
        } catch (Exception e) {
            log.error("Failed to send alert via WebSocket", e);
            // Don't throw exception - notification failure shouldn't break the flow
        }
    }
}
