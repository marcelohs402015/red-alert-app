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
 * Unread count response.
 */
export interface UnreadCountResponse {
    count: number;
    from: string;
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
     * Searches for Full Cycle emails.
     */
    async searchFullCycleEmails(maxResults: number = 10): Promise<EmailSearchResponse> {
        const response = await fetch(
            `${API_BASE_URL}/emails/fctech?maxResults=${maxResults}`
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
        unreadOnly?: boolean;
        maxResults?: number;
    }): Promise<EmailSearchResponse> {
        const queryParams = new URLSearchParams();
        if (params.from) queryParams.append('from', params.from);
        if (params.subject) queryParams.append('subject', params.subject);
        if (params.unreadOnly !== undefined) queryParams.append('unreadOnly', String(params.unreadOnly));
        if (params.maxResults) queryParams.append('maxResults', String(params.maxResults));

        const response = await fetch(
            `${API_BASE_URL}/emails/search?${queryParams.toString()}`
        );
        if (!response.ok) throw new Error('Failed to search emails');
        return response.json();
    },

    /**
     * Gets unread email count from Full Cycle.
     */
    async getFullCycleUnreadCount(): Promise<UnreadCountResponse> {
        const response = await fetch(`${API_BASE_URL}/emails/fctech/count`);
        if (!response.ok) throw new Error('Failed to get unread count');
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
     * Clears alert history.
     */
    async clearAlertHistory(): Promise<void> {
        const response = await fetch(`${API_BASE_URL}/alerts/history`, {
            method: 'DELETE',
        });
        if (!response.ok) throw new Error('Failed to clear alert history');
    },
};
