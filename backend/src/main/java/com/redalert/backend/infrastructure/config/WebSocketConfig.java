package com.redalert.backend.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time notifications.
 * Uses STOMP protocol over WebSocket with SockJS fallback.
 * 
 * Configuration:
 * - Endpoint: /ws-Red Alert (with SockJS support)
 * - Message broker: /topic (for broadcasting)
 * - Application destination prefix: /app
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed-origins}")
    private String allowedOrigins;

    /**
     * Configures message broker for handling messages.
     * 
     * @param config MessageBrokerRegistry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for broadcasting to /topic
        config.enableSimpleBroker("/topic");

        // Set application destination prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers STOMP endpoints for WebSocket connections.
     * 
     * @param registry StompEndpointRegistry to configure
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-red-alert")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }
}
