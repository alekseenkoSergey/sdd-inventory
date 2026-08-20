# Phase 1 Design: Data Model and Schema

**Date**: 2026-08-20

## InventoryItem Entity

### Overview

Represents an inventory item belonging to a user. The core domain entity for this feature.

### Fields

| Field | Type | Constraints | Notes |
|-------|------|-----------|-------|
| `id` | UUID / Long | Primary Key, auto-generated | Unique identifier |
| `userId` | UUID / Long | Foreign Key (User), NOT NULL, indexed | User who owns this item. Enforces data isolation |
| `name` | String | NOT NULL, max 255 chars | Item name. May be duplicated (not unique) |
| `description` | String | Nullable, max 1000 chars | Optional item description or notes |
| `sku` | String | Nullable, max 100 chars, unique per (userId, sku) | Optional item code. Unique within user's inventory when provided |
| `categoryId` | UUID / Long | Foreign Key (Category), NOT NULL, indexed | Category this item belongs to. Must belong to same user |
| `locationId` | UUID / Long | Foreign Key (Location), NOT NULL, indexed | Storage location. Must belong to same user |
| `currentQuantity` | Decimal / Long | NOT NULL, default 0, >= 0 | Current stock quantity. Never negative. Only changed via StockMovement |
| `unit` | String | NOT NULL, max 50 chars | Unit of measure (e.g., "pcs", "boxes", "kg", "liters") |
| `lowStockThreshold` | Decimal / Long | NOT NULL, default 0, >= 0 | Threshold for low-stock alerts. Never negative |
| `status` | Enum: {ACTIVE, ARCHIVED} | NOT NULL, default ACTIVE | Current item status |
| `createdDate` | Timestamp (UTC) | NOT NULL, auto-set | Creation timestamp (server time) |
| `updatedDate` | Timestamp (UTC) | NOT NULL, auto-set on update | Last modification timestamp (server time) |

### Validation Rules

- `name`: Must not be empty/null (FR-012)
- `currentQuantity`: Must never be negative (FR-013)
- `lowStockThreshold`: Must never be negative (FR-014)
- `categoryId`: Must belong to same user as item (FR-016)
- `locationId`: Must belong to same user as item (FR-016)
- `sku`: When provided, must be unique within (userId, sku) pair (FR-017)
- `unit`: Must be explicitly specified (FR-001)

### Relationships

- **User** (many-to-one): Each item belongs to exactly one user
- **Category** (many-to-one): References a category owned by the same user
- **Location** (many-to-one): References a location owned by the same user
- **StockMovement** (one-to-many): Each item may have multiple stock movements (implemented in separate feature)

### State Transitions

```
ACTIVE ←→ ARCHIVED

- ACTIVE → ARCHIVED: Archive operation (FR-006)
- ARCHIVED → ACTIVE: Restore operation (FR-007)
- ACTIVE: Can receive new stock movements
- ARCHIVED: Cannot receive new stock movements (FR-008)
```

### Database Table Schema

```sql
CREATE TABLE inventory_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    sku VARCHAR(100),
    category_id UUID NOT NULL REFERENCES category(id),
    location_id UUID NOT NULL REFERENCES location(id),
    current_quantity DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (current_quantity >= 0),
    unit VARCHAR(50) NOT NULL,
    low_stock_threshold DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (low_stock_threshold >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user CHECK (
        -- Validated at application level: category must belong to same user
    ),
    CONSTRAINT fk_location_user CHECK (
        -- Validated at application level: location must belong to same user
    )
);

-- Indexes for query performance
CREATE INDEX idx_inventory_item_user_id ON inventory_item(user_id);
CREATE INDEX idx_inventory_item_category_id ON inventory_item(category_id);
CREATE INDEX idx_inventory_item_location_id ON inventory_item(location_id);

-- Partial unique index for optional SKU uniqueness
CREATE UNIQUE INDEX idx_inventory_item_user_sku 
    ON inventory_item(user_id, sku) 
    WHERE sku IS NOT NULL;
```

### Java Entity Class Structure

```java
@Entity
@Table(name = "inventory_item")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 100)
    private String sku;
    
    @Column(nullable = false)
    private UUID categoryId;
    
    @Column(nullable = false)
    private UUID locationId;
    
    @Column(nullable = false)
    @Digits(integer = 13, fraction = 2)
    private BigDecimal currentQuantity;
    
    @Column(nullable = false, length = 50)
    private String unit;
    
    @Column(nullable = false)
    @Digits(integer = 13, fraction = 2)
    private BigDecimal lowStockThreshold;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;
    
    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdDate;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedDate;
    
    // Constructors, getters, setters omitted for brevity
}

public enum ItemStatus {
    ACTIVE,
    ARCHIVED
}
```

---

## Related Entities (Reference)

These entities are defined in separate features but referenced here for context:

### Category (Defined in feature `002-inventory-categories`)

- `id`: Primary key
- `userId`: Foreign key to User
- `name`: Category name
- Other fields as per that feature's spec

### Location (Defined in feature `003-location-management`)

- `id`: Primary key
- `userId`: Foreign key to User
- `name`: Location name
- Other fields as per that feature's spec

### User (Pre-existing)

- `id`: Primary key
- Other authentication/profile fields

### StockMovement (Defined in separate feature, referenced here)

- `id`: Primary key
- `itemId`: Foreign key to InventoryItem
- `movementType`: Enum (e.g., OPENING_BALANCE, RECEIPT, SALE, ADJUSTMENT)
- `quantity`: Change in quantity
- Other fields for audit trail

---

## Data Constraints & Rules

### User Data Isolation (FR-015, SC-005)

- Every query includes `user_id` filter
- Service layer enforces: current user ID must match item's user_id
- Category and location must be owned by same user (application-level validation)
- Database FK constraints cannot fully enforce multi-user scoping; application layer is authoritative

### Quantity Immutability (FR-004, FR-005)

- `currentQuantity` is read-only after creation
- Only StockMovement operations can change quantity
- Update DTOs do not include currentQuantity field
- Service rejects any attempt to modify currentQuantity directly

### Archival Constraint (FR-008)

- Archived items cannot receive stock movements
- StockMovement service checks item.status before creating movement
- Prevents business rule violation at service layer

### SKU Uniqueness (FR-017)

- Database partial unique index enforces: `(user_id, sku)` is unique where `sku IS NOT NULL`
- Allows multiple items with `sku = NULL`
- Eliminates need for application-level uniqueness check

---

## Assumptions Reflected

- **PostgreSQL as storage**: Uses PostgreSQL-specific features (partial indexes, UUID type, CHECK constraints)
- **Flyway migrations**: Schema is deployed via versioned migration (not in this doc; see migration task)
- **Server-side timestamps**: CreatedDate and UpdatedDate are set by server
- **Hard deletes**: No soft-delete flag; DELETE operation removes row entirely
- **Pre-existing categories and locations**: FKs reference these entities; no creation logic in this feature

---

## Migration Path (Flyway)

This schema will be deployed as a Flyway migration (e.g., `V5__Create_inventory_items_table.sql`) in the task phase.
