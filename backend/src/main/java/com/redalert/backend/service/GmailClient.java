package com.redalert.backend.service;

import com.redalert.backend.domain.model.EmailDto;

import java.util.List;

/**
 * Contract for Gmail operations (Adapter – GoF).
 * Implementation: Gmail API.
 */
public interface GmailClient {

    /**
     * Searches emails with the given query.
     */
    List<EmailDto> searchEmails(String query, int maxResults);

    /**
     * Returns unread count for a sender/domain.
     */
    int getUnreadCount(String from);
}
