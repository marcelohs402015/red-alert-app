package com.redalert.backend.application.exception;

/**
 * Custom exception for AI analysis failures.
 * Follows Clean Code principle of meaningful exception names.
 */
public class AiAnalysisException extends RuntimeException {

    public AiAnalysisException(String message) {
        super(message);
    }

    public AiAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
