package com.redalert.backend.service.exception;

/**
 * Thrown when Google Calendar integration fails.
 */
public class CalendarIntegrationException extends RuntimeException {

    public CalendarIntegrationException(String message) {
        super(message);
    }

    public CalendarIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
