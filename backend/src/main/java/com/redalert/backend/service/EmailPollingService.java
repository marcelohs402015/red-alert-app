package com.redalert.backend.service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.redalert.backend.domain.model.Category;
import com.redalert.backend.domain.model.ClassAlertDto;
import com.redalert.backend.service.exception.CalendarIntegrationException;
import com.redalert.backend.service.exception.GmailIntegrationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Core use case: poll Gmail, analyze with AI, create calendar events, notify via WebSocket.
 * Single responsibility: orchestrate email → alert → calendar flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPollingService {

    private final Gmail gmail;
    private final Calendar calendar;
    private final AiAnalyzer aiAnalyzer;
    private final NotificationSender notificationSender;
    private final AlertHistoryService alertHistoryService;
    private final CategoryService categoryService;
    private final ProcessedEmailService processedEmailService;

    private static final String USER_ID = "me";
    private static final String LABEL_UNREAD = "UNREAD";

    @Scheduled(fixedDelayString = "${email.polling.fixed-delay}")
    @CircuitBreaker(name = "gmailService", fallbackMethod = "fallbackPolling")
    public void pollEmails() {
        try {
            log.info("Starting email polling cycle");
            List<Category> activeCategories = categoryService.getActiveCategories();
            if (activeCategories.isEmpty()) {
                log.info("No active categories configured. Skipping polling.");
                return;
            }
            log.info("Polling {} active categories", activeCategories.size());
            for (Category category : activeCategories) {
                log.info("[POLL] Categoria ativa: '{}' | fromFilter='{}' | subjectKeywords='{}' | bodyKeywords='{}' | query gerada: {}",
                        category.getName(), category.getFromFilter(), category.getSubjectKeywords(), category.getBodyKeywords(), category.buildGmailQuery());
            }
            int totalProcessed = 0;
            for (Category category : activeCategories) {
                try {
                    totalProcessed += pollCategory(category);
                } catch (Exception e) {
                    log.error("Error polling category '{}': {}", category.getName(), e.getMessage());
                }
            }
            log.info("Email polling completed. Processed {} messages.", totalProcessed);
        } catch (Exception e) {
            log.error("Error during email polling", e);
            throw new GmailIntegrationException("Failed to poll emails", e);
        }
    }

    private int pollCategory(Category category) throws IOException {
        String query = category.buildGmailQuery();
        log.info("Polling category '{}' with query: {}", category.getName(), query);
        List<Message> messages = fetchMessages(query);
        if (messages == null || messages.isEmpty()) {
            log.warn("[GMAIL] Nenhum email encontrado para categoria '{}'. Query usada: [{}].", category.getName(), query);
            return 0;
        }
        log.info("[GMAIL] Encontrados {} email(s) para categoria '{}' (query: {})", messages.size(), category.getName(), query);
        int processed = 0;
        int limitPerCycle = 3;
        for (Message message : messages) {
            if (processed >= limitPerCycle) {
                log.info("Reached processing limit ({} messages) for this cycle.", limitPerCycle);
                break;
            }
            log.info("[POLL] Processando mensagem: id={}", message.getId());
            processMessage(message, category);
            processed++;
        }
        return processed;
    }

    private List<Message> fetchMessages(String query) throws IOException {
        log.info("[GMAIL] Executando busca: query=[{}]", query);
        ListMessagesResponse response = gmail.users()
                .messages()
                .list(USER_ID)
                .setQ(query)
                .setMaxResults(10L)
                .execute();
        List<Message> messages = response.getMessages();
        log.info("[GMAIL] Resposta da API: {} mensagem(ns) retornada(s)", messages == null ? 0 : messages.size());
        return messages;
    }

    private void processMessage(Message message, Category category) {
        try {
            String messageId = message.getId();
            Message fullMessage = gmail.users()
                    .messages()
                    .get(USER_ID, messageId)
                    .setFormat("full")
                    .execute();

            String from = extractHeader(fullMessage, "From");
            String subject = extractSubject(fullMessage);
            String date = extractHeader(fullMessage, "Date");
            String emailBody = extractEmailBody(fullMessage);

            log.info("---------- JSON EMAIL INICIO ----------");
            log.info("messageId={}, category={}, from={}, subject={}, bodyLength={}",
                    messageId, category.getName(), from, subject, emailBody != null ? emailBody.length() : 0);
            log.info("---------- JSON EMAIL FIM ----------");

            if (emailBody == null || emailBody.isBlank()) {
                log.warn("Empty email body for message ID: {}", messageId);
                markAsRead(messageId);
                return;
            }
            log.info("📧 Processing email: '{}' from '{}'", subject, from);

            String snippet = emailBody.length() > 200
                    ? emailBody.substring(0, 200) + "..."
                    : emailBody;
            snippet = snippet.replace("\n", " ").replace("\r", " ");
            LocalDateTime receivedAt = parseEmailDate(date);

            processedEmailService.saveIfNotExists(
                    messageId, from, subject, snippet, receivedAt, category);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            ClassAlertDto alert = aiAnalyzer.analyzeEmailContent(emailBody, receivedAt);
            log.info("=== ALERT PROCESSING START ===");
            log.info("AI Analysis result: {}", alert != null ? "Found" : "NULL");

            if (alert == null) {
                log.info("AI analysis returned null, creating basic alert from email data.");
                alert = new ClassAlertDto(
                        subject,
                        receivedAt,
                        null,
                        String.format("Email from: %s\n\n%s", from, snippet),
                        true,
                        null);
            }

            log.info("Processing calendar event for: '{}'", alert.title());
            String calendarLink = createCalendarEvent(alert);
            alert = new ClassAlertDto(
                    alert.title(),
                    alert.date(),
                    alert.url(),
                    alert.description(),
                    true,
                    calendarLink);
            log.info("🗓️ Calendar Event Status: {}", calendarLink != null ? "Created/Linked" : "Failed");

            alertHistoryService.addAlert(alert);
            log.info(">>> Sending notification for alert: title='{}'", alert.title());
            notificationSender.sendAlert(alert);
            log.info("=== ALERT PROCESSING END ===");
            markAsRead(messageId);

        } catch (Exception e) {
            log.error("Error processing message: {}", message.getId(), e);
        }
    }

    private String extractSubject(Message message) {
        return extractHeader(message, "Subject");
    }

    private String extractHeader(Message message, String headerName) {
        if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
            return message.getPayload().getHeaders().stream()
                    .filter(h -> headerName.equalsIgnoreCase(h.getName()))
                    .findFirst()
                    .map(h -> h.getValue())
                    .orElse("(Not Found)");
        }
        return "(Not Found)";
    }

    private String extractEmailBody(Message message) {
        try {
            if (message.getPayload() == null) return null;
            String body = findBodyPart(message.getPayload(), "text/plain");
            if (body == null) {
                body = findBodyPart(message.getPayload(), "text/html");
                if (body != null) body = stripHtml(body);
            }
            return body;
        } catch (Exception e) {
            log.error("Error extracting email body", e);
            return null;
        }
    }

    private String findBodyPart(com.google.api.services.gmail.model.MessagePart part, String mimeType) {
        if (mimeType.equalsIgnoreCase(part.getMimeType()) && part.getBody() != null && part.getBody().getData() != null) {
            return decodeBase64(part.getBody().getData());
        }
        if (part.getParts() != null) {
            for (var subPart : part.getParts()) {
                String result = findBodyPart(subPart, mimeType);
                if (result != null) return result;
            }
        }
        return null;
    }

    private String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<style([\\s\\S]*?)</style>", "")
                .replaceAll("<script([\\s\\S]*?)</script>", "")
                .replaceAll("<[^>]*>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String decodeBase64(String encodedData) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedData);
        return new String(decodedBytes);
    }

    private String createCalendarEvent(ClassAlertDto alert) {
        try {
            log.info("📅 Checking for existing calendar events with title: '{}'", alert.title());
            ZonedDateTime startOfDay = alert.date().toLocalDate().atStartOfDay(ZoneId.systemDefault());
            ZonedDateTime endOfDay = alert.date().toLocalDate().atTime(23, 59, 59).atZone(ZoneId.systemDefault());
            com.google.api.client.util.DateTime timeMin = new com.google.api.client.util.DateTime(
                    Date.from(startOfDay.toInstant()));
            com.google.api.client.util.DateTime timeMax = new com.google.api.client.util.DateTime(
                    Date.from(endOfDay.toInstant()));

            var events = calendar.events().list("primary")
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setQ(alert.title())
                    .execute();

            if (events.getItems() != null && !events.getItems().isEmpty()) {
                String existingLink = events.getItems().get(0).getHtmlLink();
                log.info("⚠️ DUPLICATE DETECTED: Event '{}' already exists. Using link: {}", alert.title(), existingLink);
                return existingLink;
            }
            log.info("🚀 CREATING NEW CALENDAR EVENT: '{}'", alert.title());

            Event event = new Event()
                    .setSummary(alert.title())
                    .setDescription(alert.description())
                    .setLocation(alert.url() != null ? alert.url() : "Online");
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
            Event createdEvent = calendar.events().insert("primary", event).execute();
            log.info("✅ SUCCESS: Calendar event created! Link: {}", createdEvent.getHtmlLink());
            return createdEvent.getHtmlLink();
        } catch (Exception e) {
            log.error("❌ FAILURE: Could not create/check calendar event for '{}'. Error: {}", alert.title(), e.getMessage());
            return null;
        }
    }

    private void markAsRead(String messageId) {
        try {
            ModifyMessageRequest modifyRequest = new ModifyMessageRequest()
                    .setRemoveLabelIds(Collections.singletonList(LABEL_UNREAD));
            gmail.users().messages().modify(USER_ID, messageId, modifyRequest).execute();
            log.debug("Marked message as read: {}", messageId);
        } catch (IOException e) {
            log.error("Failed to mark message as read: {}", messageId, e);
        }
    }

    private void fallbackPolling(Throwable throwable) {
        log.error("Gmail service unavailable, skipping polling cycle. Error: {}", throwable.getMessage());
    }

    public int clearCalendarEvents(java.time.LocalDate date) {
        try {
            log.info("Starting calendar cleanup for date: {}", date);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            com.google.api.client.util.DateTime timeMin = new com.google.api.client.util.DateTime(
                    java.util.Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()));
            com.google.api.client.util.DateTime timeMax = new com.google.api.client.util.DateTime(
                    java.util.Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
            var events = calendar.events().list("primary")
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();
            if (items == null || items.isEmpty()) {
                log.info("No events found to delete for date {}", date);
                return 0;
            }
            log.info("Found {} events to delete on {}", items.size(), date);
            int deletedCount = 0;
            for (Event event : items) {
                try {
                    calendar.events().delete("primary", event.getId()).execute();
                    log.info("Deleted event: {} (ID: {})", event.getSummary(), event.getId());
                    deletedCount++;
                } catch (IOException e) {
                    log.error("Failed to delete event ID: {}", event.getId(), e);
                }
            }
            log.info("Calendar cleanup completed. Deleted {} events.", deletedCount);
            return deletedCount;
        } catch (IOException e) {
            log.error("Failed to list calendar events for cleanup", e);
            throw new CalendarIntegrationException("Failed to clean calendar", e);
        }
    }

    private LocalDateTime parseEmailDate(String dateString) {
        if (dateString == null || dateString.isBlank()) return LocalDateTime.now();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH);
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString.trim(), formatter);
            return zonedDateTime.toLocalDateTime();
        } catch (Exception e) {
            log.warn("Failed to parse email date '{}', using current time", dateString);
            return LocalDateTime.now();
        }
    }
}
