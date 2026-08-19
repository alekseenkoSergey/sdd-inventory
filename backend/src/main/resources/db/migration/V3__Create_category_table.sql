-- Flyway versioned migration (V3)
-- Creates the Category table with constraints and indexes
-- Depends on: V001__initial_schema, V2__create_users_table

CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,

    -- Foreign key to User table
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES "users"(id) ON DELETE CASCADE
);

-- Composite unique index: case-insensitive name per user
CREATE UNIQUE INDEX uk_category_user_name ON category(user_id, LOWER(TRIM(name)));

-- Index for common queries
CREATE INDEX idx_category_user_id ON category(user_id);

-- Index to support FK lookups efficiently
CREATE INDEX idx_category_id_user_id ON category(id, user_id);

-- Audit trigger: automatically update updated_at on modification
CREATE OR REPLACE FUNCTION update_category_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_category_update
BEFORE UPDATE ON category
FOR EACH ROW
EXECUTE FUNCTION update_category_updated_at();
