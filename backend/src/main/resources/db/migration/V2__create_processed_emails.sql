-- Red Alert Database Schema
-- Version: V2
-- Description: Table for storing processed emails

-- =====================================================
-- PROCESSED_EMAILS TABLE
-- Stores emails that were fetched and processed by the system
-- =====================================================
CREATE TABLE IF NOT EXISTS processed_emails (
    id BIGSERIAL PRIMARY KEY,
    email_id VARCHAR(255) NOT NULL UNIQUE,
    from_address VARCHAR(500) NOT NULL,
    subject VARCHAR(500),
    snippet TEXT,
    received_at TIMESTAMP,
    category_id BIGINT,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key to categories (optional)
    CONSTRAINT fk_processed_email_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) ON DELETE SET NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_processed_emails_email_id ON processed_emails(email_id);
CREATE INDEX idx_processed_emails_received ON processed_emails(received_at DESC);
CREATE INDEX idx_processed_emails_category ON processed_emails(category_id);
CREATE INDEX idx_processed_emails_processed ON processed_emails(processed_at DESC);

-- Comments for documentation
COMMENT ON TABLE processed_emails IS 'Stores emails that were fetched and processed by the system';
COMMENT ON COLUMN processed_emails.email_id IS 'Gmail message ID (unique)';
COMMENT ON COLUMN processed_emails.from_address IS 'Email sender (From header)';
COMMENT ON COLUMN processed_emails.subject IS 'Email subject';
COMMENT ON COLUMN processed_emails.snippet IS 'Email preview snippet';
COMMENT ON COLUMN processed_emails.received_at IS 'When the email was received';
COMMENT ON COLUMN processed_emails.category_id IS 'Category that matched this email';
COMMENT ON COLUMN processed_emails.processed_at IS 'When the email was processed by the system';
