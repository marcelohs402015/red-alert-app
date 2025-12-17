package com.redalert.backend.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient used in HTTP integrations.
 * Provides a pre-configured WebClient.Builder bean for dependency injection.
 */
@Configuration
public class WebClientConfig {

    /**
     * Provides a WebClient.Builder bean for HTTP calls.
     * Used primarily for Gemini AI API integration.
     * 
     * @return Configured WebClient.Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .defaultHeader("Content-Type", "application/json");
    }
}
