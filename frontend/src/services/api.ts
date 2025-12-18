import type { ClassAlert } from '../types/alert';

/**
 * API Service for backend communication.
 */
const API_BASE_URL = 'http://localhost:8086/api/v1';

/**
 * Email data from backend.
 */
export interface Email {
    id: string;
    from: string;
    subject: string;
    snippet: string;
    receivedAt: string;
    isUnread: boolean;
}

/**
 * Email search response from backend.
 */
export interface EmailSearchResponse {
    emails: Email[];
    totalCount: number;
    query: string;
    searchTimeMs: number;
}

/**
 * Alert history response.
 */
export interface AlertHistoryResponse {
    alerts: ClassAlert[];
    totalCount: number;
    returnedCount: number;
}

/**
 * API service for Red Alert backend using native fetch.
 */
export const api = {
    /**
     * Searches for Full Cycle emails (emails from fullcycle.com.br).
     */
    async searchFullCycleEmails(maxResults: number = 10): Promise<EmailSearchResponse> {
        const response = await fetch(
            `${API_BASE_URL}/emails/search?from=fullcycle.com.br&unreadOnly=true&maxResults=${maxResults}`
        );
        if (!response.ok) throw new Error('Failed to fetch emails');
        return response.json();
    },

    /**
     * Searches emails with custom filters.
     */
    async searchEmails(params: {
        from?: string;
        subject?: string;
        body?: string;
        unreadOnly?: boolean;
        maxResults?: number;
    }): Promise<EmailSearchResponse> {
        const queryParams = new URLSearchParams();
        if (params.from) queryParams.append('from', params.from);
        if (params.subject) queryParams.append('subject', params.subject);
        if (params.body) queryParams.append('body', params.body);
        if (params.unreadOnly !== undefined) queryParams.append('unreadOnly', String(params.unreadOnly));
        if (params.maxResults) queryParams.append('maxResults', String(params.maxResults));

        const response = await fetch(
            `${API_BASE_URL}/emails/search?${queryParams.toString()}`
        );
        if (!response.ok) throw new Error('Failed to search emails');
        return response.json();
    },

    /**
     * Triggers email polling manually (same as scheduler).
     */
    async triggerPolling(): Promise<{ success: boolean; message: string }> {
        const response = await fetch(`${API_BASE_URL}/emails/poll`, {
            method: 'POST',
        });
        if (!response.ok) throw new Error('Failed to trigger polling');
        return response.json();
    },

    /**
     * Gets alert history.
     */
    async getAlertHistory(limit: number = 20): Promise<AlertHistoryResponse> {
        const response = await fetch(
            `${API_BASE_URL}/alerts/history?limit=${limit}`
        );
        if (!response.ok) throw new Error('Failed to get alert history');
        return response.json();
    },

    /**
     * Gets all processed emails from the database.
     */
    async clearAlertHistory(): Promise<void> {
        const response = await fetch(`${API_BASE_URL}/alerts/history`, {
            method: 'DELETE',
        });
        if (!response.ok) throw new Error('Failed to clear alert history');
    },

    async getProcessedEmails(): Promise<Email[]> {
        const response = await fetch(`${API_BASE_URL}/processed-emails`);
        if (!response.ok) throw new Error('Failed to fetch processed emails');
        return response.json();
    },
};
