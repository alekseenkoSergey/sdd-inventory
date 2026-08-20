CREATE TABLE inventory_item (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    sku VARCHAR(100),
    category_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    current_quantity DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (current_quantity >= 0),
    unit VARCHAR(50) NOT NULL,
    low_stock_threshold DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (low_stock_threshold >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_item_user_id ON inventory_item(user_id);
CREATE INDEX idx_inventory_item_category_id ON inventory_item(category_id);
CREATE INDEX idx_inventory_item_location_id ON inventory_item(location_id);

CREATE UNIQUE INDEX idx_inventory_item_user_sku
    ON inventory_item(user_id, sku)
    WHERE sku IS NOT NULL;
