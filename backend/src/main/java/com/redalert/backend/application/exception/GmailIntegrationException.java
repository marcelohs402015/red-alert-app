package com.redalert.backend.application.exception;

/**
 * Custom exception for Gmail integration failures.
 */
public class GmailIntegrationException extends RuntimeException {

    public GmailIntegrationException(String message) {
        super(message);
    }

    public GmailIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
