package com.redalert.backend.presentation.dto;

import java.util.List;

/**
 * Response DTO for email search with metadata.
 */
public record EmailSearchResponse(
        List<EmailResponse> emails,
        int totalCount,
        String query,
        long searchTimeMs) {
}
