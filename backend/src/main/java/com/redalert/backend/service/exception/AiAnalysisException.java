package com.redalert.backend.service.exception;

/**
 * Thrown when AI analysis fails (e.g. Gemini/Ollama unavailable).
 */
public class AiAnalysisException extends RuntimeException {

    public AiAnalysisException(String message) {
        super(message);
    }

    public AiAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
