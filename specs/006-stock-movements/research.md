# Research Phase: Stock Movements

**Date**: 2026-08-20  
**Feature**: Stock Movements Backend API  
**Status**: Complete

## Research Questions Resolved

### 1. Spring Data JPA Enum Mapping

**Question**: How should MovementType and AdjustmentDirection enums be stored in PostgreSQL?

**Decision**: Use `@Enumerated(EnumType.STRING)` on JPA entity fields

**Rationale**:
- String representation improves database readability; DBA can query movements directly without enum knowledge
- Easier to audit and debug (SQL: `WHERE movement_type = 'STOCK_IN'` vs ordinal integer)
- Future-proof: If enum members are reordered, existing data remains valid
- Spring Data handles serialization/deserialization automatically

**Alternatives Considered**:
- `EnumType.ORDINAL`: Smaller storage footprint (1 byte vs 10+ bytes), but enum reordering breaks existing data
- Custom type mapper: Overkill for simple enum; adds unnecessary complexity

**Implementation**:
```java
@Enumerated(EnumType.STRING)
@Column(name = "movement_type", nullable = false)
private MovementType movementType;

@Enumerated(EnumType.STRING)
@Column(name = "adjustment_direction", nullable = true)
private AdjustmentDirection adjustmentDirection;
```

### 2. Concurrency Strategy: Last-Write-Wins

**Question**: How should concurrent stock movements for the same item be handled?

**Decision**: Implement last-write-wins via unconditional `save()` without optimistic locking

**Rationale**:
- Per clarification Q1, concurrent movements for same item are rare in single-location inventory scenario
- Spring Data's default behavior (`entityManager.persist()`) is sufficient
- Simpler implementation; no version field or conflict exceptions needed
- Both movements are persisted; the most recent one determines final quantity
- Last-write timestamp preserved in `createdDate` field for audit trail

**Alternatives Considered**:
- Optimistic locking with `@Version`: Adds complexity (version column, ConflictException) for edge case
- Pessimistic database locks: Unnecessary overhead; serializes all concurrent requests
- Event sourcing: Over-engineered for current scope

**Implementation**:
```java
// In StockMovementService.recordMovement()
stockMovement = StockMovement.builder()
  .itemId(itemId)
  .movementType(movementType)
  .quantity(quantity)
  .reason(reason)
  .movementDate(movementDate != null ? movementDate : LocalDate.now())
  .createdDate(LocalDateTime.now())
  .adjustmentDirection(adjustmentDirection)
  .build();

StockMovement saved = repository.save(stockMovement);  // No optimistic locking
item.updateCurrentQuantity(saved);
itemRepository.save(item);  // Unconditional save; last-write wins
```

**Note for Future**: If concurrent stock updates become problematic post-launch, optimistic locking can be retrofitted by adding `@Version Long version` without changing API contracts.

### 3. Flyway Database Migration Strategy

**Question**: How should database schema versions be managed for stock movements table?

**Decision**: Use sequential versioning (V001_, V002_, etc.); follow existing project pattern

**Rationale**:
- Existing Inventory Tracker migrations use sequential versioning
- Avoids version conflicts in multi-developer environments
- Easier to review migration history and understand dependency order
- Flyway validates version uniqueness and order

**Location**: `backend/src/main/resources/db/migration/`

**Implementation**:
```sql
-- V{next_version}__Create_stock_movement_table.sql
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

### 4. REST API Design: No Pagination

**Question**: Should movement history queries support pagination or return all movements?

**Decision**: Return all movements in a single query; no pagination limit (per Q5 clarification)

**Rationale**:
- Single-location inventory scale: Items rarely exceed 1000 movements
- Simpler API contract (no limit/offset/cursor parameters)
- Clients can filter in-memory for UI display if needed
- Aligns with goal of "optimize for correctness and simplicity"

**Alternatives Considered**:
- Offset/limit pagination: Standard REST; necessary for large-scale systems; adds API complexity
- Cursor-based pagination: Better for large result sets; unnecessary overhead here

**Implementation**:
```java
// StockMovementController
@GetMapping("/items/{itemId}/movements")
public ResponseEntity<List<StockMovementResponseDTO>> getMovementHistory(
    @PathVariable Long itemId,
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate
) {
    // Query all movements; optional date range filters
    List<StockMovement> movements = service.getMovementHistory(itemId, startDate, endDate);
    return ResponseEntity.ok(movements.stream().map(StockMovementResponseDTO::fromEntity).toList());
}
```

**Future**: If pagination becomes necessary post-launch (e.g., items with 10k+ movements), optional query parameters can be added without breaking existing clients that don't use them.

### 5. Validation Framework

**Question**: How should input validation be performed for stock movements?

**Decision**: Use Jakarta Validation (javax.validation) annotations in DTOs and entities; custom validators for business rules

**Rationale**:
- Spring Boot 3.x native support via spring-boot-starter-validation
- Declarative annotations reduce boilerplate; framework handles serialization errors
- Testable: Validation rules are self-documenting
- Centralized error messages

**Implementation Examples**:
```java
// DTO validation
public class StockMovementRequestDTO {
    @NotNull(message = "Movement type is required")
    private MovementType movementType;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Long quantity;

    @Enumerated(EnumType.STRING)
    private AdjustmentDirection adjustmentDirection;

    @PastOrPresent(message = "Movement date cannot be in the future")
    private LocalDate movementDate;
}

// Entity-level validation
@Entity
@Table(name = "stock_movement")
public class StockMovement {
    @NotNull
    @Positive
    private Long quantity;

    // Custom constraint: adjustment_direction required for ADJUSTMENT type
    @ValidAdjustmentDirection
    private AdjustmentDirection adjustmentDirection;
}

// Custom validator
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdjustmentDirectionValidator.class)
public @interface ValidAdjustmentDirection {
    String message() default "Adjustment direction required for ADJUSTMENT type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class AdjustmentDirectionValidator implements ConstraintValidator<ValidAdjustmentDirection, StockMovement> {
    @Override
    public boolean isValid(StockMovement movement, ConstraintValidatorContext context) {
        if (movement.getMovementType() == MovementType.ADJUSTMENT) {
            return movement.getAdjustmentDirection() != null;
        }
        return true;
    }
}
```

## Summary of Key Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Enum Storage | STRING | Readable, auditable, future-proof |
| Concurrency | Last-write-wins | Rare in single-location; simple |
| Migrations | Sequential | Matches project pattern |
| Pagination | None | Single-location scale; simple |
| Validation | Jakarta + Custom | Spring-native, declarative |

All decisions align with Inventory Tracker Constitution Principle I (Simplicity First) and Principle II (Technology Stack).

