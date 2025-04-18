-- Migration script to update profile_images table

-- First, create a temporary table with the new structure
CREATE TABLE profile_images_new (
                                    id BIGSERIAL PRIMARY KEY,
                                    customer_id VARCHAR(255) NOT NULL,
                                    file_path VARCHAR(255) NOT NULL,
                                    content_type VARCHAR(255) NOT NULL,
                                    last_updated TIMESTAMP NOT NULL,
                                    file_size BIGINT
);

-- Create an index on customer_id for better query performance
CREATE INDEX idx_profile_images_customer_id ON profile_images_new (customer_id);

-- Copy data (this will be empty for new deployments)
-- For existing installations, we could export images to disk here, but
-- this would be complex to do in a migration script.
-- We are assuming a fresh installation or backup/restore process will handle this

-- Drop the old table and rename the new one
ALTER TABLE IF EXISTS profile_images RENAME TO profile_images_old;
ALTER TABLE profile_images_new RENAME TO profile_images;

-- Clean up (optional - might want to keep old table for a while)
-- DROP TABLE profile_images_old;

-- Add configuration property for file storage
CREATE TABLE IF NOT EXISTS app_configuration (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Add file storage configuration
INSERT INTO app_configuration (config_key, config_value, created_at, updated_at)
VALUES
    ('file.upload.dir', './uploads/images', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('file.base.url', 'http://localhost:8080/api/files', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('profile.image.default.url', 'http://localhost:8080/api/static/default-profile.png', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT (config_key) DO NOTHING;