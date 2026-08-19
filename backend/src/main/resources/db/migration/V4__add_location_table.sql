-- Create location table for storing user-defined storage locations
CREATE TABLE location (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_location_user FOREIGN KEY (user_id) REFERENCES "users"(id) ON DELETE CASCADE,
    CONSTRAINT uk_location_user_name UNIQUE (user_id, name)
);

-- Index for common queries filtering by user
CREATE INDEX idx_location_user_id ON location(user_id);
