# Data Model: Location Management

**Date**: 2026-08-19  
**Feature**: Location Management

## Entity: Location

### Database Table

```sql
CREATE TABLE location (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_location_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT uk_location_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_location_user_id ON location(user_id);
```

### Entity Class

```java
@Entity
@Table(name = "location", uniqueConstraints = {
    @UniqueConstraint(name = "uk_location_user_name", columnNames = {"user_id", "name"})
})
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors, getters, setters...
}
```

### Field Specifications

| Field | Type | Constraints | Description |
|-------|------|-----------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| user_id | BIGINT | NOT NULL, FK → user.id | Owner of the location |
| name | VARCHAR(255) | NOT NULL, UNIQUE (user_id, name) | Location name; unique per user; max 255 chars |
| created_at | TIMESTAMP | NOT NULL, IMMUTABLE | Timestamp of creation |
| updated_at | TIMESTAMP | NOT NULL | Timestamp of last update |
| version | BIGINT | NOT NULL, DEFAULT 0 | Optimistic locking version |

### Validation Rules

- **name**: Non-empty, non-whitespace-only (validated in service before persist)
- **user_id**: Must correspond to authenticated user (enforced in controller/service)
- **Uniqueness**: Composite unique constraint (user_id, name) prevents duplicates per user
- **Cascade Delete**: If user is deleted, all their locations are cascade-deleted

### Relationships

- **Location → User**: Many-to-one (implicit via user_id FK)
- **Location ← Item**: One-to-many (Item table has location_id FK; not modified by this feature)

### State Lifecycle

1. **CREATED**: Location created; name assigned; stored in DB
2. **RENAMED**: Name updated; version incremented; updated_at refreshed
3. **DELETED**: Location removed from DB; cascade rules apply to items (blocked if non-empty)

No explicit status field needed; all states implicit in DB presence/absence.

## DTOs

### CreateLocationRequestDTO

```java
@Data
public class CreateLocationRequestDTO {
    @NotBlank(message = "Location name cannot be empty or whitespace-only")
    @Size(min = 1, max = 255, message = "Location name must be between 1 and 255 characters")
    private String name;
}
```

### RenameLocationRequestDTO

```java
@Data
public class RenameLocationRequestDTO {
    @NotBlank(message = "Location name cannot be empty or whitespace-only")
    @Size(min = 1, max = 255, message = "Location name must be between 1 and 255 characters")
    private String name;
}
```

### LocationResponseDTO

```java
@Data
public class LocationResponseDTO {
    private Long id;
    private Long userId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## Database Migration

### Flyway Migration File

**File**: `V{sequence}__add_location_table.sql`

```sql
-- Create location table for storing user-defined storage locations
CREATE TABLE location (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_location_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT uk_location_user_name UNIQUE (user_id, name)
);

-- Index for common queries filtering by user
CREATE INDEX idx_location_user_id ON location(user_id);
```

**Sequence Number**: To be determined based on latest migration version in project.

## Validations & Constraints

| Constraint | Level | Implementation | Spec Reference |
|-----------|-------|-----------------|-----------------|
| Name not empty/whitespace | Application | DTO @NotBlank validation | FR-002 |
| Name not empty/whitespace | Database | CHECK constraint possible but not required | FR-002 |
| Unique per user | Application | Service layer check before create/rename | FR-003, FR-005 |
| Unique per user | Database | UNIQUE(user_id, name) constraint | FR-003, FR-005 |
| User ownership | Application | userId extracted from authenticated principal | FR-009 |
| User ownership | Database | FK relationship + access control in service | FR-009 |
| Cannot delete non-empty | Application | Service queries item count before delete; throws exception | FR-007 |
| Cannot delete non-empty | Database | No constraint (enforced at app level per block strategy) | FR-007 |

## Query Patterns

### Find All Locations for User

```java
List<Location> findByUserId(Long userId);
```

### Find Location by ID and User (authorization check)

```java
Optional<Location> findByIdAndUserId(Long id, Long userId);
```

### Check Name Uniqueness for User

```java
boolean existsByUserIdAndName(Long userId, String name);
```

### Count Items in Location (cross-entity check)

```java
// In ItemRepository:
Integer countByLocationId(Long locationId);
```

## Frontend Data Binding

### TypeScript Interfaces (Frontend)

The frontend consumes the LocationResponseDTO as:

```typescript
interface Location {
  id: number;
  userId: number;
  name: string;
  createdAt: string;  // ISO 8601; formatted for display via Angular date pipe
  updatedAt: string;  // ISO 8601; used for optimistic locking verification
}
```

**Frontend Rendering**:
- `name`: Display in list table, form input
- `createdAt`: Format using `| date: 'short'` or 'medium' in template
- `updatedAt`: Track for refresh logic; not typically displayed to user
- `id`: Use as trackBy key in *ngFor for performance

### Frontend API Calls

**Request DTOs** (sent to backend):
```typescript
// Create
POST /locations
{ "name": "Home Office" }

// Rename
PUT /locations/1
{ "name": "Home Studio" }

// List
GET /locations

// Get single
GET /locations/1

// Delete
DELETE /locations/1
```

**Response DTOs** (returned from backend):
```typescript
// 201 Created (POST)
{
  "id": 1,
  "userId": 42,
  "name": "Home Office",
  "createdAt": "2026-08-19T10:30:00",
  "updatedAt": "2026-08-19T10:30:00"
}

// 200 OK (GET, PUT)
[Same as 201]

// 204 No Content (DELETE)
[Empty body]

// Error (4xx, 5xx)
{
  "timestamp": "2026-08-19T10:35:00Z",
  "status": 409,
  "error": "LOCATION_NAME_NOT_UNIQUE",
  "message": "A location with this name already exists in your account",
  "path": "/locations"
}
```

## Notes

- Location follows same pattern as existing Category entity
- No soft-delete: hard delete only (block if items exist)
- Optimistic locking via @Version handles concurrent updates
- Cascade delete on user removes all locations (expected behavior for user cleanup)
- Frontend consumes API as pure REST; no special ORM or serialization concerns
- ISO 8601 timestamps: Angular date pipe handles localization automatically
