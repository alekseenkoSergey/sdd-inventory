CREATE TABLE stock_movement (
  id BIGSERIAL PRIMARY KEY,
  item_id BIGINT NOT NULL,
  movement_type VARCHAR(20) NOT NULL,
  quantity BIGINT NOT NULL CHECK (quantity > 0),
  adjustment_direction VARCHAR(20),
  reason TEXT,
  movement_date DATE NOT NULL,
  created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (item_id) REFERENCES inventory_item(id) ON DELETE CASCADE,
  CONSTRAINT adjustment_direction_required_for_adjustment
    CHECK ((movement_type != 'ADJUSTMENT' OR adjustment_direction IS NOT NULL))
);

CREATE INDEX idx_stock_movement_item_date ON stock_movement(item_id, movement_date);
CREATE INDEX idx_stock_movement_created ON stock_movement(created_date);
