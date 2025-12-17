package com.redalert.backend.domain.port;

import com.redalert.backend.domain.model.ClassAlertDto;

/**
 * Port for notification service.
 * Defines contract for sending alerts to frontend via WebSocket.
 */
public interface NotificationPort {

    /**
     * Sends alert notification to connected clients.
     * 
     * @param alert The alert to broadcast
     */
    void sendAlert(ClassAlertDto alert);
}
