package com.redalert.backend.presentation.exception;

import com.redalert.backend.application.exception.AiAnalysisException;
import com.redalert.backend.application.exception.CalendarIntegrationException;
import com.redalert.backend.application.exception.GmailIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for REST API.
 * Provides consistent error responses across the application.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles Gmail integration exceptions.
     */
    @ExceptionHandler(GmailIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleGmailIntegrationException(GmailIntegrationException ex) {
        log.error("Gmail integration error", ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Gmail service unavailable",
                ex.getMessage(),
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handles Calendar integration exceptions.
     */
    @ExceptionHandler(CalendarIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleCalendarIntegrationException(CalendarIntegrationException ex) {
        log.error("Calendar integration error", ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Calendar service unavailable",
                ex.getMessage(),
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handles AI analysis exceptions.
     */
    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAiAnalysisException(AiAnalysisException ex) {
        log.error("AI analysis error", ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "AI service unavailable",
                ex.getMessage(),
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handles generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                ex.getMessage(),
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error response DTO.
     */
    public record ErrorResponse(
            int status,
            String error,
            String message,
            LocalDateTime timestamp) {
    }
}
