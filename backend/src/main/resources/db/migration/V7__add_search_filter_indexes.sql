-- Search indexes (case-insensitive, supports ILIKE)
CREATE INDEX IF NOT EXISTS idx_inventory_item_name_lower ON inventory_item (LOWER(name));
CREATE INDEX IF NOT EXISTS idx_inventory_item_description_lower ON inventory_item (LOWER(description));
CREATE INDEX IF NOT EXISTS idx_inventory_item_sku_lower ON inventory_item (LOWER(sku));

-- Filter indexes (foreign keys + status + quantity for stock state)
CREATE INDEX IF NOT EXISTS idx_inventory_item_status ON inventory_item (status);
CREATE INDEX IF NOT EXISTS idx_inventory_item_current_quantity ON inventory_item (current_quantity);

-- Composite indexes for common filter combinations
CREATE INDEX IF NOT EXISTS idx_inventory_item_category_location ON inventory_item (category_id, location_id);
CREATE INDEX IF NOT EXISTS idx_inventory_item_status_quantity ON inventory_item (status, current_quantity);
