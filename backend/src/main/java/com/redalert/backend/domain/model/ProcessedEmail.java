package com.redalert.backend.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a processed email.
 * Stores emails that were fetched and processed by the monitoring system.
 */
@Entity
@Table(name = "processed_emails")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Gmail message ID (unique identifier).
     */
    @Column(name = "email_id", nullable = false, unique = true, length = 255)
    private String emailId;

    /**
     * Email sender (From header).
     */
    @Column(name = "from_address", nullable = false, length = 500)
    private String fromAddress;

    /**
     * Email subject.
     */
    @Column(length = 500)
    private String subject;

    /**
     * Email preview snippet.
     */
    @Column(columnDefinition = "TEXT")
    private String snippet;

    /**
     * When the email was received.
     */
    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    /**
     * Category that matched this email.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * When the email was processed by the system.
     */
    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }
}
