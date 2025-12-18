/**
 * Alert data structure received from backend via WebSocket.
 */
export interface ClassAlert {
    title: string;
    date: string;
    url: string | null;
    description: string;
    isUrgent: boolean;
    calendarLink?: string | null;
}

/**
 * WebSocket connection status.
 */
export type ConnectionStatus = 'connected' | 'disconnected' | 'connecting' | 'error';
