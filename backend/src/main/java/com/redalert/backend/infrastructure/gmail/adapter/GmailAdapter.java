package com.redalert.backend.infrastructure.gmail.adapter;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.redalert.backend.application.exception.GmailIntegrationException;
import com.redalert.backend.domain.model.EmailDto;
import com.redalert.backend.domain.port.GmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter implementation for Gmail operations.
 * Implements GmailPort using Google Gmail API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GmailAdapter implements GmailPort {

    private final Gmail gmail;
    private static final String USER_ID = "me";

    @Override
    public List<EmailDto> searchEmails(String query, int maxResults) {
        try {
            log.info("Searching emails with query: {}", query);

            ListMessagesResponse response = gmail.users()
                    .messages()
                    .list(USER_ID)
                    .setQ(query)
                    .setMaxResults((long) maxResults)
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages == null || messages.isEmpty()) {
                log.debug("No emails found for query: {}", query);
                return List.of();
            }

            List<EmailDto> emails = new ArrayList<>();
            for (Message message : messages) {
                try {
                    EmailDto email = fetchEmailDetails(message.getId());
                    emails.add(email);
                } catch (Exception e) {
                    log.error("Error fetching email details for ID: {}", message.getId(), e);
                }
            }

            log.info("Found {} emails", emails.size());
            return emails;

        } catch (IOException e) {
            log.error("Error searching emails", e);
            throw new GmailIntegrationException("Failed to search emails", e);
        }
    }

    @Override
    public int getUnreadCount(String from) {
        try {
            String query = String.format("from:%s is:unread", from);

            ListMessagesResponse response = gmail.users()
                    .messages()
                    .list(USER_ID)
                    .setQ(query)
                    .execute();

            List<Message> messages = response.getMessages();
            int count = messages != null ? messages.size() : 0;

            log.debug("Unread count for {}: {}", from, count);
            return count;

        } catch (IOException e) {
            log.error("Error getting unread count", e);
            return 0;
        }
    }

    /**
     * Fetches detailed information about an email.
     */
    private EmailDto fetchEmailDetails(String messageId) throws IOException {
        Message message = gmail.users()
                .messages()
                .get(USER_ID, messageId)
                .setFormat("metadata")
                .setMetadataHeaders(List.of("From", "Subject", "Date"))
                .execute();

        String from = getHeader(message, "From");
        String subject = getHeader(message, "Subject");
        String snippet = message.getSnippet();

        // Convert internal date to LocalDateTime
        Long internalDate = message.getInternalDate();
        LocalDateTime receivedAt = internalDate != null
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(internalDate), ZoneId.systemDefault())
                : LocalDateTime.now();

        // Check if unread
        boolean isUnread = message.getLabelIds() != null &&
                message.getLabelIds().contains("UNREAD");

        return new EmailDto(
                messageId,
                from != null ? from : "Unknown",
                subject != null ? subject : "No Subject",
                snippet != null ? snippet : "",
                receivedAt,
                isUnread);
    }

    /**
     * Extracts header value from email message.
     */
    private String getHeader(Message message, String headerName) {
        if (message.getPayload() == null || message.getPayload().getHeaders() == null) {
            return null;
        }

        return message.getPayload().getHeaders().stream()
                .filter(header -> headerName.equalsIgnoreCase(header.getName()))
                .map(MessagePartHeader::getValue)
                .findFirst()
                .orElse(null);
    }
}
