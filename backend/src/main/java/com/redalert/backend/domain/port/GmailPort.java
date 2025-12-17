package com.redalert.backend.domain.port;

import com.redalert.backend.domain.model.EmailDto;

import java.util.List;

/**
 * Port for Gmail operations.
 * Defines contract for email search and retrieval.
 */
public interface GmailPort {

    /**
     * Searches for emails matching the given query.
     * 
     * @param query      Gmail search query (e.g., "from:fctech.com.br is:unread")
     * @param maxResults Maximum number of results to return
     * @return List of emails matching the query
     */
    List<EmailDto> searchEmails(String query, int maxResults);

    /**
     * Gets unread email count for a specific sender.
     * 
     * @param from Sender email address or domain
     * @return Number of unread emails
     */
    int getUnreadCount(String from);
}
