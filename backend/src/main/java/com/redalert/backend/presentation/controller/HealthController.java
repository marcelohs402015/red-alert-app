package com.redalert.backend.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health check controller for monitoring application status.
 * Provides basic health endpoint for load balancers and monitoring tools.
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    /**
     * Basic health check endpoint.
     * 
     * @return Health status response
     */
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        HealthResponse response = new HealthResponse(
                "UP",
                "Red Alert Backend is running",
                LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Health response DTO.
     */
    public record HealthResponse(
            String status,
            String message,
            LocalDateTime timestamp) {
    }
}
