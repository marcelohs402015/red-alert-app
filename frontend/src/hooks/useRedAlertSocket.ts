import { useEffect, useState, useCallback } from 'react';
import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { ClassAlert, ConnectionStatus } from '../types/alert';

/**
 * Custom hook for Red Alert WebSocket connection.
 * 
 * Manages WebSocket connection to backend and listens for class alerts.
 * Automatically reconnects on disconnection.
 * 
 * @returns Object containing connection status and latest alert
 */
const useRedAlertSocket = () => {
    const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>('disconnected');
    const [latestAlert, setLatestAlert] = useState<ClassAlert | null>(null);
    const [client, setClient] = useState<Client | null>(null);

    const WEBSOCKET_URL = 'http://localhost:8081/ws-red-alert';
    const TOPIC = '/topic/alerts';

    /**
     * Connects to WebSocket server.
     */
    const connect = useCallback(() => {
        setConnectionStatus('connecting');

        const stompClient = new Client({
            webSocketFactory: () => new SockJS(WEBSOCKET_URL) as any,

            onConnect: () => {
                console.log('✅ Connected to Red Alert WebSocket');
                setConnectionStatus('connected');

                // Subscribe to alerts topic
                stompClient.subscribe(TOPIC, (message: IMessage) => {
                    try {
                        const alert: ClassAlert = JSON.parse(message.body);
                        console.log('🚨 Alert received:', alert);
                        setLatestAlert(alert);

                        // Play alert sound
                        playAlertSound();
                    } catch (error) {
                        console.error('Error parsing alert message:', error);
                    }
                });
            },

            onDisconnect: () => {
                console.log('❌ Disconnected from Red Alert WebSocket');
                setConnectionStatus('disconnected');
            },

            onStompError: (frame) => {
                console.error('❌ STOMP error:', frame);
                setConnectionStatus('error');
            },

            // Reconnect settings
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        stompClient.activate();
        setClient(stompClient);
    }, []);

    /**
     * Disconnects from WebSocket server.
     */
    const disconnect = useCallback(() => {
        if (client) {
            client.deactivate();
            setClient(null);
            setConnectionStatus('disconnected');
        }
    }, [client]);

    /**
     * Plays alert sound when new alert arrives.
     */
    const playAlertSound = () => {
        try {
            // Create oscillator for beep sound
            const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);

            oscillator.frequency.value = 800; // Frequency in Hz
            oscillator.type = 'sine';

            gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);

            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.5);
        } catch (error) {
            console.warn('Could not play alert sound:', error);
        }
    };

    /**
     * Clears the current alert.
     */
    const clearAlert = useCallback(() => {
        setLatestAlert(null);
    }, []);

    // Auto-connect on mount
    useEffect(() => {
        connect();

        return () => {
            disconnect();
        };
    }, [connect, disconnect]);

    return {
        connectionStatus,
        latestAlert,
        clearAlert,
        reconnect: connect,
    };
};

export default useRedAlertSocket;
