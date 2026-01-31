package com.redalert.backend.service.exception;

/**
 * Thrown when Gmail integration fails.
 */
public class GmailIntegrationException extends RuntimeException {

    public GmailIntegrationException(String message) {
        super(message);
    }

    public GmailIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
