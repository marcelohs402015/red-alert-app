-- Red Alert Database Schema
-- Version: V1
-- Description: Initial schema with categories table for dynamic email monitoring

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    email_query VARCHAR(500) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on active categories for faster queries
CREATE INDEX idx_categories_active ON categories(is_active) WHERE is_active = true;

-- Insert default categories
INSERT INTO categories (name, description, email_query, is_active) VALUES
('Full Cycle', 'Emails da Full Cycle', 'from:fullcycle.com.br is:unread', true),
('FCTECH', 'Emails relacionados a FCTECH', 'FCTECH is:unread', true);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE categories IS 'Stores email monitoring categories with their Gmail search queries';
COMMENT ON COLUMN categories.name IS 'Unique name for the category';
COMMENT ON COLUMN categories.description IS 'Human-readable description of what this category monitors';
COMMENT ON COLUMN categories.email_query IS 'Gmail search query (e.g., "from:example.com is:unread")';
COMMENT ON COLUMN categories.is_active IS 'Whether this category is currently being monitored';
COMMENT ON COLUMN categories.created_at IS 'Timestamp when category was created';
COMMENT ON COLUMN categories.updated_at IS 'Timestamp when category was last updated';
