package com.redalert.backend.service;

import com.redalert.backend.domain.model.ClassAlertDto;

/**
 * Contract for sending alert notifications (Observer/Adapter – GoF).
 * Implementation: WebSocket.
 */
public interface NotificationSender {

    /**
     * Sends alert to connected clients.
     */
    void sendAlert(ClassAlertDto alert);
}
