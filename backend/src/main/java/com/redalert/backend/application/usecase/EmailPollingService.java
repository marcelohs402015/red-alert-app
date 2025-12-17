package com.redalert.backend.application.usecase;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.redalert.backend.application.exception.CalendarIntegrationException;
import com.redalert.backend.application.exception.GmailIntegrationException;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.domain.port.AiAnalysisPort;
import com.redalert.backend.domain.port.NotificationPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Core use case for email polling and alert processing.
 * 
 * This service:
 * 1. Polls Gmail for unread emails every minute
 * 2. Analyzes each email using AI
 * 3. Creates calendar events for urgent alerts
 * 4. Sends notifications via WebSocket
 * 5. Marks processed emails as read
 * 
 * Follows Clean Architecture principles:
 * - Use case orchestrates domain logic
 * - Depends on ports (interfaces), not implementations
 * - Infrastructure details are injected via DI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPollingService {

    private final Gmail gmail;
    private final Calendar calendar;
    private final AiAnalysisPort aiAnalysisPort;
    private final NotificationPort notificationPort;
    private final AlertHistoryService alertHistoryService;

    @Value("${email.polling.query}")
    private String emailQuery;

    private static final String USER_ID = "me";
    private static final String LABEL_UNREAD = "UNREAD";

    /**
     * Scheduled task that polls Gmail for unread emails.
     * Runs every minute (configurable via application.yml).
     * 
     * Process flow:
     * 1. Fetch unread emails from Gmail
     * 2. For each email:
     * a. Extract email body
     * b. Analyze with AI
     * c. If urgent alert found:
     * - Create Google Calendar event
     * - Send WebSocket notification
     * d. Mark email as read
     */
    @Scheduled(fixedDelayString = "${email.polling.fixed-delay}")
    @CircuitBreaker(name = "gmailService", fallbackMethod = "fallbackPolling")
    public void pollEmails() {
        try {
            log.info("Starting email polling cycle");

            List<Message> messages = fetchUnreadMessages();

            if (messages == null || messages.isEmpty()) {
                log.debug("No unread messages found");
                return;
            }

            log.info("Found {} unread message(s)", messages.size());

            for (Message message : messages) {
                processMessage(message);
            }

            log.info("Email polling cycle completed");

        } catch (IOException e) {
            log.error("Error during email polling", e);
            throw new GmailIntegrationException("Failed to poll emails", e);
        }
    }

    /**
     * Fetches unread messages from Gmail.
     * 
     * @return List of unread messages
     * @throws IOException if Gmail API call fails
     */
    private List<Message> fetchUnreadMessages() throws IOException {
        ListMessagesResponse response = gmail.users()
                .messages()
                .list(USER_ID)
                .setQ(emailQuery)
                .execute();

        return response.getMessages();
    }

    /**
     * Processes a single email message.
     * 
     * @param message The message to process
     */
    private void processMessage(Message message) {
        try {
            String messageId = message.getId();
            log.debug("Processing message ID: {}", messageId);

            // Fetch full message content
            Message fullMessage = gmail.users()
                    .messages()
                    .get(USER_ID, messageId)
                    .setFormat("full")
                    .execute();

            // Extract email body
            String emailBody = extractEmailBody(fullMessage);

            if (emailBody == null || emailBody.isBlank()) {
                log.warn("Empty email body for message ID: {}", messageId);
                markAsRead(messageId);
                return;
            }

            // Analyze with AI
            ClassAlertDto alert = aiAnalysisPort.analyzeEmailContent(emailBody);

            if (alert != null && alert.isUrgent()) {
                log.info("Urgent alert detected: {}", alert.title());

                // Save to history
                alertHistoryService.addAlert(alert);

                // Create calendar event
                createCalendarEvent(alert);

                // Send WebSocket notification
                notificationPort.sendAlert(alert);
            }

            // Mark as read to avoid reprocessing
            markAsRead(messageId);

        } catch (Exception e) {
            log.error("Error processing message: {}", message.getId(), e);
            // Continue processing other messages
        }
    }

    /**
     * Extracts email body from Gmail message.
     * Handles both plain text and HTML content.
     * 
     * @param message The Gmail message
     * @return Decoded email body
     */
    private String extractEmailBody(Message message) {
        try {
            if (message.getPayload() == null) {
                return null;
            }

            // Try to get body from payload
            if (message.getPayload().getBody() != null &&
                    message.getPayload().getBody().getData() != null) {
                return decodeBase64(message.getPayload().getBody().getData());
            }

            // Try to get from parts (multipart messages)
            if (message.getPayload().getParts() != null &&
                    !message.getPayload().getParts().isEmpty()) {

                for (var part : message.getPayload().getParts()) {
                    if (part.getBody() != null && part.getBody().getData() != null) {
                        return decodeBase64(part.getBody().getData());
                    }
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Error extracting email body", e);
            return null;
        }
    }

    /**
     * Decodes Base64 encoded email content.
     * 
     * @param encodedData Base64 encoded string
     * @return Decoded string
     */
    private String decodeBase64(String encodedData) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedData);
        return new String(decodedBytes);
    }

    /**
     * Creates a Google Calendar event for the alert.
     * 
     * @param alert The alert to create event for
     */
    private void createCalendarEvent(ClassAlertDto alert) {
        try {
            Event event = new Event()
                    .setSummary(alert.title())
                    .setDescription(alert.description())
                    .setLocation(alert.url());

            // Convert LocalDateTime to Google Calendar DateTime
            Date startDate = Date.from(alert.date().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(alert.date().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(startDate))
                    .setTimeZone(ZoneId.systemDefault().getId());

            EventDateTime end = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(endDate))
                    .setTimeZone(ZoneId.systemDefault().getId());

            event.setStart(start);
            event.setEnd(end);

            // Insert event into primary calendar
            Event createdEvent = calendar.events()
                    .insert("primary", event)
                    .execute();

            log.info("Calendar event created: {} (ID: {})", alert.title(), createdEvent.getId());

        } catch (IOException e) {
            log.error("Failed to create calendar event", e);
            throw new CalendarIntegrationException("Failed to create calendar event", e);
        }
    }

    /**
     * Marks an email message as read by removing UNREAD label.
     * 
     * @param messageId The message ID to mark as read
     */
    private void markAsRead(String messageId) {
        try {
            ModifyMessageRequest modifyRequest = new ModifyMessageRequest()
                    .setRemoveLabelIds(Collections.singletonList(LABEL_UNREAD));

            gmail.users()
                    .messages()
                    .modify(USER_ID, messageId, modifyRequest)
                    .execute();

            log.debug("Marked message as read: {}", messageId);

        } catch (IOException e) {
            log.error("Failed to mark message as read: {}", messageId, e);
            // Don't throw - this is not critical
        }
    }

    /**
     * Fallback method when Gmail service is unavailable.
     * 
     * @param throwable The exception that triggered fallback
     */
    private void fallbackPolling(Throwable throwable) {
        log.error("Gmail service unavailable, skipping polling cycle. Error: {}", throwable.getMessage());
    }
}
