package com.redalert.backend.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Category entity for email monitoring configuration.
 * Each category defines specific filters for Gmail search.
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    /**
     * Email sender filter (e.g., fullcycle.com.br)
     * Will be used in: from:fullcycle.com.br
     */
    @Column(name = "from_filter", length = 255)
    private String fromFilter;

    /**
     * Keywords to search in subject (comma separated)
     * e.g., "AO VIVO, MBA, AGORA"
     * Will be used in: subject:(AO VIVO OR MBA OR AGORA)
     */
    @Column(name = "subject_keywords", length = 500)
    private String subjectKeywords;

    /**
     * Keywords to search in email body (comma separated)
     * e.g., "link de acesso, aula"
     * Will be used in: (link de acesso OR aula)
     */
    @Column(name = "body_keywords", length = 500)
    private String bodyKeywords;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Builds the Gmail API query string from the filter fields.
     * 
     * @return Complete query string for Gmail API
     */
    public String buildGmailQuery() {
        StringBuilder query = new StringBuilder();

        // Add from filter
        if (fromFilter != null && !fromFilter.isBlank()) {
            query.append("from:").append(fromFilter.trim()).append(" ");
        }

        // Add subject keywords with OR operator
        if (subjectKeywords != null && !subjectKeywords.isBlank()) {
            List<String> keywords = parseKeywords(subjectKeywords);
            if (!keywords.isEmpty()) {
                query.append("subject:(");
                query.append(keywords.stream()
                        .map(k -> k.contains(" ") ? "\"" + k + "\"" : k)
                        .collect(Collectors.joining(" OR ")));
                query.append(") ");
            }
        }

        // Add body keywords (no operator, just search terms)
        if (bodyKeywords != null && !bodyKeywords.isBlank()) {
            List<String> keywords = parseKeywords(bodyKeywords);
            if (!keywords.isEmpty()) {
                query.append("(");
                query.append(keywords.stream()
                        .map(k -> k.contains(" ") ? "\"" + k + "\"" : k)
                        .collect(Collectors.joining(" OR ")));
                query.append(") ");
            }
        }

        // Always add is:unread
        query.append("is:unread");

        return query.toString().trim();
    }

    /**
     * Parses comma-separated keywords into a list.
     */
    private List<String> parseKeywords(String keywords) {
        return Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
