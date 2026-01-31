package com.redalert.backend.service;

import com.redalert.backend.domain.model.ClassAlertDto;

import java.time.LocalDateTime;

/**
 * Contract for AI-based email analysis (Strategy – GoF).
 * Implementations: Gemini, Ollama, etc.
 */
public interface AiAnalyzer {

    /**
     * Analyzes email content and extracts event/alert data.
     *
     * @param emailBody  Raw email body
     * @param receivedAt Reference for date resolution
     * @return Extracted alert data, or null if none
     */
    ClassAlertDto analyzeEmailContent(String emailBody, LocalDateTime receivedAt);
}
