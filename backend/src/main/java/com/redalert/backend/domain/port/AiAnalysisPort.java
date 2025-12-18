package com.redalert.backend.domain.port;

import com.redalert.backend.domain.model.ClassAlertDto;

/**
 * Port (interface) for AI analysis service.
 * This defines the contract that infrastructure adapters must implement.
 * Following Hexagonal Architecture, the domain defines what it needs,
 * not how it's implemented.
 */
public interface AiAnalysisPort {

    /**
     * Analyzes email content using AI to extract class alert information.
     * 
     * @param emailBody  The raw email body content to analyze
     * @param receivedAt The timestamp when the email was received (reference for
     *                   date resolution)
     * @return ClassAlertDto containing extracted information, or null if no alert
     *         found
     * @throws AiAnalysisException if AI service fails
     */
    ClassAlertDto analyzeEmailContent(String emailBody, java.time.LocalDateTime receivedAt);
}
