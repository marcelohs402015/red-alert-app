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
import com.redalert.backend.domain.model.Category;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.domain.port.AiAnalysisPort;
import com.redalert.backend.domain.port.NotificationPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Core use case for email polling and alert processing.
 * 
 * This service:
 * 1. Polls Gmail using ACTIVE categories from database
 * 2. Analyzes each email using AI
 * 3. Creates calendar events for urgent alerts
 * 4. Sends notifications via WebSocket
 * 5. Marks processed emails as read
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
    private final CategoryService categoryService;

    private static final String USER_ID = "me";
    private static final String LABEL_UNREAD = "UNREAD";

    // Track processed message IDs to avoid duplicates
    private final Set<String> processedMessageIds = new HashSet<>();

    /**
     * Scheduled task that polls Gmail for unread emails.
     * Uses ACTIVE categories from database to build queries.
     */
    @Scheduled(fixedDelayString = "${email.polling.fixed-delay}")
    @CircuitBreaker(name = "gmailService", fallbackMethod = "fallbackPolling")
    public void pollEmails() {
        try {
            log.info("Starting email polling cycle");

            // Get all active categories from database
            List<Category> activeCategories = categoryService.getActiveCategories();

            if (activeCategories.isEmpty()) {
                log.info("No active categories configured. Skipping polling.");
                return;
            }

            log.info("Polling {} active categories", activeCategories.size());

            int totalProcessed = 0;

            // Process each category
            for (Category category : activeCategories) {
                try {
                    int processed = pollCategory(category);
                    totalProcessed += processed;
                } catch (Exception e) {
                    log.error("Error polling category '{}': {}", category.getName(), e.getMessage());
                    // Continue with other categories
                }
            }

            log.info("Email polling completed. Processed {} messages.", totalProcessed);

        } catch (Exception e) {
            log.error("Error during email polling", e);
            throw new GmailIntegrationException("Failed to poll emails", e);
        }
    }

    /**
     * Polls Gmail for a specific category.
     * 
     * @param category The category to poll
     * @return Number of messages processed
     */
    private int pollCategory(Category category) throws IOException {
        String query = category.getEmailQuery();

        // Ensure query includes is:unread
        if (!query.toLowerCase().contains("is:unread")) {
            query = query + " is:unread";
        }

        log.debug("Polling category '{}' with query: {}", category.getName(), query);

        List<Message> messages = fetchMessages(query);

        if (messages == null || messages.isEmpty()) {
            log.debug("No messages found for category '{}'", category.getName());
            return 0;
        }

        log.info("Found {} message(s) for category '{}'", messages.size(), category.getName());

        int processed = 0;
        for (Message message : messages) {
            // Skip already processed messages
            if (processedMessageIds.contains(message.getId())) {
                continue;
            }

            processMessage(message, category);
            processedMessageIds.add(message.getId());
            processed++;

            // Keep processed IDs cache small
            if (processedMessageIds.size() > 1000) {
                processedMessageIds.clear();
            }
        }

        return processed;
    }

    /**
     * Fetches messages from Gmail using the given query.
     */
    private List<Message> fetchMessages(String query) throws IOException {
        ListMessagesResponse response = gmail.users()
                .messages()
                .list(USER_ID)
                .setQ(query)
                .setMaxResults(10L) // Limit to prevent overload
                .execute();

        return response.getMessages();
    }

    /**
     * Processes a single email message.
     */
    private void processMessage(Message message, Category category) {
        try {
            String messageId = message.getId();
            log.debug("Processing message ID: {} for category: {}", messageId, category.getName());

            // Fetch full message content
            Message fullMessage = gmail.users()
                    .messages()
                    .get(USER_ID, messageId)
                    .setFormat("full")
                    .execute();

            // Extract subject
            String subject = extractSubject(fullMessage);

            // Extract email body
            String emailBody = extractEmailBody(fullMessage);

            if (emailBody == null || emailBody.isBlank()) {
                log.warn("Empty email body for message ID: {}", messageId);
                markAsRead(messageId);
                return;
            }

            log.info("Processing email: '{}' from category '{}'", subject, category.getName());

            // Analyze with AI
            ClassAlertDto alert = aiAnalysisPort.analyzeEmailContent(emailBody);

            if (alert != null && alert.isUrgent()) {
                log.info("🚨 Urgent alert detected: {}", alert.title());

                // Save to history
                alertHistoryService.addAlert(alert);

                // Create calendar event
                createCalendarEvent(alert);

                // Send WebSocket notification
                notificationPort.sendAlert(alert);
            } else {
                log.debug("No urgent alert found in email '{}'", subject);
            }

            // Mark as read to avoid reprocessing
            markAsRead(messageId);

        } catch (Exception e) {
            log.error("Error processing message: {}", message.getId(), e);
        }
    }

    /**
     * Extracts subject from Gmail message.
     */
    private String extractSubject(Message message) {
        if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
            return message.getPayload().getHeaders().stream()
                    .filter(h -> "Subject".equalsIgnoreCase(h.getName()))
                    .findFirst()
                    .map(h -> h.getValue())
                    .orElse("(No Subject)");
        }
        return "(No Subject)";
    }

    /**
     * Extracts email body from Gmail message.
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
     */
    private String decodeBase64(String encodedData) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedData);
        return new String(decodedBytes);
    }

    /**
     * Creates a Google Calendar event for the alert.
     */
    private void createCalendarEvent(ClassAlertDto alert) {
        try {
            Event event = new Event()
                    .setSummary(alert.title())
                    .setDescription(alert.description())
                    .setLocation(alert.url());

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
     * Marks an email message as read.
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
        }
    }

    /**
     * Fallback method when Gmail service is unavailable.
     */
    private void fallbackPolling(Throwable throwable) {
        log.error("Gmail service unavailable, skipping polling cycle. Error: {}", throwable.getMessage());
    }
}
