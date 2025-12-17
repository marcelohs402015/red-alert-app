package com.redalert.backend.application.exception;

/**
 * Custom exception for Google Calendar integration failures.
 */
public class CalendarIntegrationException extends RuntimeException {

    public CalendarIntegrationException(String message) {
        super(message);
    }

    public CalendarIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
