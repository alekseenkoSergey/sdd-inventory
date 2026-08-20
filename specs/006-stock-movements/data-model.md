# Data Model: Stock Movements

**Date**: 2026-08-20  
**Feature**: Stock Movements Backend  
**Status**: Final

## Database Schema

### Table: stock_movement

```sql
CREATE TABLE stock_movement (
  id BIGSERIAL PRIMARY KEY,
  item_id BIGINT NOT NULL,
  movement_type VARCHAR(20) NOT NULL,
  quantity BIGINT NOT NULL CHECK (quantity > 0),
  adjustment_direction VARCHAR(20),
  reason TEXT,
  movement_date DATE NOT NULL,
  created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
  CONSTRAINT adjustment_direction_required_for_adjustment 
    CHECK ((movement_type != 'ADJUSTMENT' OR adjustment_direction IS NOT NULL))
);

CREATE INDEX idx_stock_movement_item_date ON stock_movement(item_id, movement_date);
CREATE INDEX idx_stock_movement_created ON stock_movement(created_date);
```

### Table: item (Modified)

Existing `item` table gains stock movement integration:

```sql
-- Ensure current_quantity has NOT NULL constraint and appropriate default
ALTER TABLE item
  MODIFY COLUMN current_quantity BIGINT NOT NULL DEFAULT 0;

-- Add check constraint: current_quantity >= 0
ALTER TABLE item
  ADD CONSTRAINT check_current_quantity_non_negative CHECK (current_quantity >= 0);
```

## Entity Models

### StockMovement Entity

**JPA Entity** for persistence and business logic

```java
package org.example.sddinventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movement", indexes = {
    @Index(name = "idx_stock_movement_item_date", columnList = "item_id, movement_date"),
    @Index(name = "idx_stock_movement_created", columnList = "created_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @NotNull
    @Positive(message = "Quantity must be greater than 0")
    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_direction", nullable = true)
    private AdjustmentDirection adjustmentDirection;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @NotNull
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // Validation: adjustmentDirection required for ADJUSTMENT type
    @PrePersist
    private void validateAdjustmentDirection() {
        if (this.movementType == MovementType.ADJUSTMENT && this.adjustmentDirection == null) {
            throw new IllegalArgumentException(
                "adjustmentDirection is required for ADJUSTMENT movements"
            );
        }
    }
}
```

### Item Entity (Enhanced)

Existing `Item` entity gains stock movement awareness:

```java
package org.example.sddinventory.entity;

// ... existing imports ...

@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @Column(name = "current_quantity", nullable = false)
    private Long currentQuantity = 0L;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // ... other existing fields ...

    /**
     * Updates current quantity based on a stock movement.
     * Called after a StockMovement is persisted.
     * Enforces last-write-wins concurrency: unconditional update.
     *
     * @param movement the stock movement that triggered the update
     * @throws IllegalArgumentException if movement type is unsupported
     */
    public void updateCurrentQuantityFromMovement(StockMovement movement) {
        switch (movement.getMovementType()) {
            case OPENING_BALANCE:
                // Opening balance sets current quantity (typically only on item creation)
                this.currentQuantity = movement.getQuantity();
                break;
            case STOCK_IN:
                // Stock in increases quantity
                this.currentQuantity += movement.getQuantity();
                break;
            case STOCK_OUT:
                // Stock out decreases quantity
                this.currentQuantity -= movement.getQuantity();
                break;
            case ADJUSTMENT:
                // Adjustment direction determines increase or decrease
                if (movement.getAdjustmentDirection() == AdjustmentDirection.INCREASE) {
                    this.currentQuantity += movement.getQuantity();
                } else {
                    this.currentQuantity -= movement.getQuantity();
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported movement type: " + movement.getMovementType());
        }
    }

    /**
     * Validates that a proposed movement would not violate business rules.
     * Returns the quantity that would result after the movement.
     *
     * @param movement the proposed movement
     * @return the new current quantity after the movement
     * @throws IllegalArgumentException if the movement is invalid
     */
    public Long validateMovement(StockMovement movement) {
        long newQuantity = this.currentQuantity;

        switch (movement.getMovementType()) {
            case OPENING_BALANCE:
                // Opening balance: no validation (typically on item creation only)
                newQuantity = movement.getQuantity();
                break;
            case STOCK_IN:
                newQuantity += movement.getQuantity();
                break;
            case STOCK_OUT:
                newQuantity -= movement.getQuantity();
                if (newQuantity < 0) {
                    throw new IllegalArgumentException(
                        String.format("Stock out of %d units would make quantity negative (current: %d)",
                            movement.getQuantity(), this.currentQuantity)
                    );
                }
                break;
            case ADJUSTMENT:
                if (movement.getAdjustmentDirection() == AdjustmentDirection.INCREASE) {
                    newQuantity += movement.getQuantity();
                } else {
                    newQuantity -= movement.getQuantity();
                }
                if (newQuantity < 0) {
                    throw new IllegalArgumentException(
                        String.format("Adjustment of %d would make quantity negative (current: %d)",
                            movement.getQuantity(), this.currentQuantity)
                    );
                }
                break;
        }

        return newQuantity;
    }
}
```

## Value Objects (Enums)

### MovementType Enum

```java
package org.example.sddinventory.entity;

public enum MovementType {
    OPENING_BALANCE("Opening Balance"),
    STOCK_IN("Stock In"),
    STOCK_OUT("Stock Out"),
    ADJUSTMENT("Adjustment");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
```

### AdjustmentDirection Enum

```java
package org.example.sddinventory.entity;

public enum AdjustmentDirection {
    INCREASE("Increase"),
    DECREASE("Decrease");

    private final String label;

    AdjustmentDirection(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
```

## Data Transfer Objects (DTOs)

### StockMovementRequestDTO

```java
package org.example.sddinventory.model;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.sddinventory.entity.AdjustmentDirection;
import org.example.sddinventory.entity.MovementType;
import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementRequestDTO {
    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Long quantity;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate movementDate;  // Defaults to today if not provided

    private AdjustmentDirection adjustmentDirection;  // Required for ADJUSTMENT type; validated in service

    // Custom validation method (for complex rules)
    public void validateForMovementType() {
        if (this.movementType == MovementType.ADJUSTMENT && this.adjustmentDirection == null) {
            throw new IllegalArgumentException(
                "adjustmentDirection is required for ADJUSTMENT movements"
            );
        }
    }
}
```

### StockMovementResponseDTO

```java
package org.example.sddinventory.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.example.sddinventory.entity.AdjustmentDirection;
import org.example.sddinventory.entity.MovementType;
import org.example.sddinventory.entity.StockMovement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementResponseDTO {
    private Long id;
    private Long itemId;
    private MovementType movementType;
    private Long quantity;
    private AdjustmentDirection adjustmentDirection;
    private String reason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate movementDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdDate;

    private Long itemCurrentQuantity;  // Updated quantity after movement

    /**
     * Convert entity to response DTO
     */
    public static StockMovementResponseDTO fromEntity(StockMovement movement, Long currentQuantity) {
        return StockMovementResponseDTO.builder()
            .id(movement.getId())
            .itemId(movement.getItemId())
            .movementType(movement.getMovementType())
            .quantity(movement.getQuantity())
            .adjustmentDirection(movement.getAdjustmentDirection())
            .reason(movement.getReason())
            .movementDate(movement.getMovementDate())
            .createdDate(movement.getCreatedDate())
            .itemCurrentQuantity(currentQuantity)
            .build();
    }
}
```

## Relationships & Constraints

### Entity Relationships

- **StockMovement → Item**: Many-to-one (multiple movements per item)
  - Foreign Key: `stock_movement.item_id → item.id`
  - Cascade: ON DELETE CASCADE (if item deleted, movements deleted too)
  - Lazy loading not recommended; eager fetch needed for quantity updates

### Business Rule Constraints

| Rule | Implementation | Database Check |
|------|----------------|-----------------|
| Quantity > 0 | Jakarta Validation + Application Check | CHECK (quantity > 0) |
| No negative current quantity | Service validation before persist | CHECK (current_quantity >= 0) |
| adjustmentDirection required for ADJUSTMENT | Entity @PrePersist validator | CHECK (movement_type != 'ADJUSTMENT' OR adjustment_direction IS NOT NULL) |
| Movement date can be any date | No constraint; application-set | None (LocalDate accepts all dates) |
| Reason optional | Nullable column | NULL allowed |

## Indexes

```sql
-- Item history lookup (most common query)
CREATE INDEX idx_stock_movement_item_date ON stock_movement(item_id, movement_date);

-- Audit trail queries
CREATE INDEX idx_stock_movement_created ON stock_movement(created_date);

-- Consider future indexes if needed:
-- CREATE INDEX idx_stock_movement_type ON stock_movement(movement_type);
-- CREATE INDEX idx_stock_movement_adjustment_dir ON stock_movement(adjustment_direction) WHERE movement_type = 'ADJUSTMENT';
```

## Data Consistency Model

**Consistency Strategy**: Last-write-wins (per clarification Q1)

- When two concurrent movements are recorded for the same item, both are persisted
- The most recently recorded movement (by `createdDate`) determines the final `current_quantity`
- No pessimistic locking or optimistic versioning; unconditional save()
- Audit trail remains complete; sequence of movements preserved

**Eventual Consistency**: Not applicable; all updates are immediately visible via query

**Concurrency Handling**: Database handles FK integrity; application enforces business logic via service layer

