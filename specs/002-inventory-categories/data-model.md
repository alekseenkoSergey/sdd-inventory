# Data Model: Inventory Categories

**Phase 1 Output** | **Date**: 2026-08-19

## Domain Entities

### Category

**Purpose**: Represents a named group of inventory items.

**JPA Entity**: `com.example.inventory.entity.Category`

**Attributes**:

| Attribute | Type | Constraints | Notes |
|-----------|------|-------------|-------|
| `id` | UUID (PK) | NOT NULL, UNIQUE | Generated on creation; immutable |
| `userId` | UUID (FK) | NOT NULL, Foreign Key to User | Links category to owning user; ensures data isolation |
| `name` | VARCHAR(255) | NOT NULL, UNIQUE per userId | Case-insensitive uniqueness; trimmed of leading/trailing whitespace before storage |
| `createdAt` | TIMESTAMP | NOT NULL, DEFAULT = NOW() | Immutable audit timestamp |
| `updatedAt` | TIMESTAMP | NOT NULL, DEFAULT = NOW(), AUTO_UPDATE | Updated on every modification |
| `version` | BIGINT | NOT NULL, DEFAULT = 0 | JPA @Version for optimistic locking; incremented on each update |

**Relationships**:

- **One-to-Many with Item**: One category can have many inventory items (via Item.categoryId)
  - On category deletion: FK constraint prevents deletion if items exist (database enforces the blocking rule)
  - Cascade behavior: No cascade on delete (items remain with null categoryId if category deleted without proper reassignment checks)

**Validation Rules**:

| Rule | Source | Enforced At |
|------|--------|-------------|
| Name cannot be null | FR-001 | Application + Database NOT NULL |
| Name must be non-empty after trimming | FR-001, Clarification Q2 | Application (trim, then check length > 0) + Database CHECK constraint |
| Name must be unique per user (case-insensitive) | FR-002, Clarification Q2 | Application (case-insensitive lookup) + Database unique index on (userId, LOWER(name)) |
| User isolation: category belongs to authenticated user | FR-008 | Application (service layer filters by userId) |
| Cannot delete if items reference category | FR-005 | Application (count check) + Database FK constraint |

**State Transitions**:

```
[Created]
   ↓
[Active] ← → [Renamed (same state, different name)]
   ↓
[Deleted] (only if no items assigned)
```

**Example Entity (pseudocode)**:

```java
@Entity
@Table(name = "category", uniqueConstraints = {
    @UniqueConstraint(name = "uk_category_user_name", 
                      columnNames = {"user_id", "name"})
})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Getters, setters, constructor omitted for brevity
}
```

## Related Entities (Referenced, Not Managed by This Feature)

### Item

**Purpose**: Inventory item that may belong to a category.

**Relationship to Category**: Many-to-One (optional)

**Relevant Attributes**:

| Attribute | Type | Constraint |
|-----------|------|-----------|
| `id` | UUID (PK) | NOT NULL |
| `userId` | UUID (FK) | NOT NULL |
| `categoryId` | UUID (FK) | NULLABLE, Foreign Key to Category |
| `name` | VARCHAR(255) | NOT NULL |
| `quantity` | INTEGER | NOT NULL, DEFAULT = 0 |

**Note**: Item-Category relationship is optional and managed separately. This feature focus is on category operations only. Item reassignment during category deletion is handled by requiring users to manually reassign items (blocking deletion if items exist).

## Database Schema

### Migration: V3__Create_category_table.sql

```sql
-- Flyway versioned migration (V3)
-- Creates the Category table with constraints and indexes
-- Depends on: V001__initial_schema, V2__create_users_table

CREATE TABLE category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Composite unique index: case-insensitive name per user
    CONSTRAINT uk_category_user_name UNIQUE (user_id, LOWER(TRIM(name))),
    
    -- Name cannot be empty after trimming
    CONSTRAINT ck_category_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    
    -- Foreign key to User table (assuming user table exists)
    -- Uncomment and adjust table/column names as needed:
    -- CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- Index for common queries
CREATE INDEX idx_category_user_id ON category(user_id);

-- Index to support FK lookups efficiently
CREATE INDEX idx_category_id_user_id ON category(id, user_id);

-- Audit trigger (optional): automatically update updated_at on modification
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
```

### Migration: V4__Add_category_fk_to_item.sql (Future)

```sql
-- Flyway versioned migration (V4)
-- Add optional foreign key from Item to Category (after Item table is created)
-- Assumes Item table exists and categoryId column is added separately

ALTER TABLE item
ADD CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES category(id);

-- Ensure category deletion is blocked if items reference it
-- (PostgreSQL enforces this automatically due to FK constraint; no CASCADE)
```

## DTOs (Request/Response Models)

### CreateCategoryRequestDTO

**Purpose**: Request DTO for creating a new category.

**Fields**:

| Field | Type | Validation | Example |
|-------|------|-----------|---------|
| `name` | String | @NotBlank, @Size(min=1, max=255) | "Electronics" |

**Usage**: POST /api/categories with JSON body

**Example**:
```json
{
  "name": "Office Supplies"
}
```

### RenameCategoryRequestDTO

**Purpose**: Request DTO for renaming an existing category.

**Fields**:

| Field | Type | Validation | Example |
|-------|------|-----------|---------|
| `name` | String | @NotBlank, @Size(min=1, max=255) | "Tools" |

**Usage**: PATCH /api/categories/{categoryId} with JSON body

**Example**:
```json
{
  "name": "Power Tools"
}
```

### CategoryResponseDTO

**Purpose**: Response DTO for category data (returned by all endpoints).

**Fields**:

| Field | Type | Nullable | Example |
|-------|------|----------|---------|
| `id` | UUID | NO | "550e8400-e29b-41d4-a716-446655440000" |
| `name` | String | NO | "Electronics" |
| `itemCount` | Integer | NO | 5 |
| `createdAt` | ISO8601 DateTime | NO | "2026-08-19T10:30:00Z" |
| `updatedAt` | ISO8601 DateTime | NO | "2026-08-19T14:15:30Z" |

**Usage**: Returned from GET, POST (create), PATCH (rename)

**Example**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Electronics",
  "itemCount": 3,
  "createdAt": "2026-08-19T10:30:00Z",
  "updatedAt": "2026-08-19T14:15:30Z"
}
```

### ErrorResponseDTO

**Purpose**: Standardized error response.

**Fields**:

| Field | Type | Example |
|-------|------|---------|
| `status` | Integer | 409 |
| `error` | String | "CATEGORY_HAS_ITEMS" |
| `message` | String | "Cannot delete: 5 items assigned. Please reassign items first." |
| `timestamp` | ISO8601 DateTime | "2026-08-19T15:00:00Z" |

**Usage**: Returned when errors occur (HTTP 400, 403, 404, 409, 500)

**Example** (409 Conflict):
```json
{
  "status": 409,
  "error": "CATEGORY_HAS_ITEMS",
  "message": "Cannot delete: 5 items assigned. Please reassign items to another category first.",
  "timestamp": "2026-08-19T15:00:00Z"
}
```

## Key Design Decisions

### 1. UUID Primary Keys

**Rationale**: 
- Prevents sequential ID exposure (security best practice)
- Enables distributed generation without coordination
- PostgreSQL's `gen_random_uuid()` is cryptographically secure

### 2. Composite Unique Index: (user_id, LOWER(name))

**Rationale**:
- Enforces case-insensitive uniqueness at database layer (data integrity guarantee)
- Allows different users to have categories with the same name
- Consistent with user expectations (most systems treat names case-insensitively)

### 3. Optimistic Locking via @Version

**Rationale**:
- Detects concurrent modifications without row-level locks
- Allows high concurrency for read-heavy workloads
- Necessary for concurrent edit detection (required by feature spec clarification Q3)

### 4. Nullable categoryId on Item (Future)

**Rationale**:
- Items can exist without categories (uncategorized inventory)
- Deletion blocking prevents orphaned items from category deletion
- Decouples item creation from category availability

## Constraints & Assumptions

- **User table exists** and has `id` UUID column
- **Item table exists** and will have optional `categoryId` FK (managed by separate feature/migration)
- **Authentication framework** provides authenticated user ID to service layer
- **Flyway** migrations are versioned and applied automatically on application startup
- **PostgreSQL** supports UUID type, `LOWER()` function, and composite unique constraints

## Migration Strategy

1. **V3**: Create category table with all constraints, indexes, triggers
2. **V4** (future): Add FK from Item to Category once Item feature is ready
3. **Rollback**: Reverse migrations managed by Flyway (flyway_schema_history table tracks applied migrations)

**Migration Version Sequence in Project**:
- V001: Initial schema
- V2: Create users table
- V3: Create category table (this feature)
- V4: Add category FK to items table (this feature, post-Item creation)
