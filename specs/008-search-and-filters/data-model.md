# Data Model: Search and Filters

**Feature**: Search and Filters  
**Date**: 2026-08-20

## Entity Model

### InventoryItem (Existing, Enhanced for Search)

The primary searchable entity. No new fields required; search/filter operate on existing fields.

**Searchable Fields**:
- `name` (String): Item name — indexed for search performance
- `description` (String): Item notes/description — indexed for search performance  
- `skuCode` (String): Internal product code — indexed for search performance

**Filterable Fields**:
- `category` (Category FK): References category entity — indexed for filter performance
- `location` (Location FK): References location entity — indexed for filter performance
- `status` (Enum: ACTIVE, ARCHIVED): Item lifecycle state — indexed for filter performance
- `quantity` (Integer): Stock level — used for stock state calculation
- `lowStockThreshold` (Integer): Per-item threshold for "low stock" classification (already exists in table)

**Existing Fields Used**:
- `id` (UUID/Long): Primary key
- `createdAt` (Timestamp): Audit
- `updatedAt` (Timestamp): Audit

**No Changes to Entity Structure Required**: All necessary fields already exist.

---

### Category (Existing Reference)

Represents item categories. Used as filter dimension.

**Fields**:
- `id`: Primary key
- `name`: Category name (e.g., "Electronics", "Tools")
- `description`: Optional category description

**Index Strategy**: Index on `id` for FK lookups; `name` indexed if category search is added later.

---

### Location (Existing Reference)

Represents storage locations. Used as filter dimension.

**Fields**:
- `id`: Primary key
- `name`: Location name (e.g., "Warehouse A", "Shelf B3")
- `description`: Optional location description

**Index Strategy**: Index on `id` for FK lookups; `name` indexed if location search is added later.

---

### Search/Filter Query Parameters (API Contract)

Not a database entity, but defines the queryable dimensions passed to the repository:

```
SearchFilterRequest:
  - search (String, optional): Text query for name/description/SKU partial match
  - categoryId (Long, optional): Filter to single category
  - locationId (Long, optional): Filter to single location
  - status (Enum: ACTIVE, ARCHIVED, ALL; default: ALL): Lifecycle filter
  - stockState (Enum: OUT_OF_STOCK, LOW_STOCK, IN_STOCK, ALL; default: ALL): Inventory level filter
  - page (Integer, default: 0): Pagination page number
  - size (Integer, default: 20): Items per page
```

---

## Database Schema Changes

### New Indexes (via Flyway Migration)

Create indexes to support search and filter performance:

```sql
-- Search indexes (case-insensitive, supports ILIKE)
CREATE INDEX idx_inventory_item_name_lower ON inventory_item (LOWER(name));
CREATE INDEX idx_inventory_item_description_lower ON inventory_item (LOWER(description));
CREATE INDEX idx_inventory_item_sku_code_lower ON inventory_item (LOWER(sku_code));

-- Filter indexes (foreign keys + status + quantity for stock state)
CREATE INDEX idx_inventory_item_category_id ON inventory_item (category_id);
CREATE INDEX idx_inventory_item_location_id ON inventory_item (location_id);
CREATE INDEX idx_inventory_item_status ON inventory_item (status);
CREATE INDEX idx_inventory_item_quantity ON inventory_item (quantity);

-- Composite indexes for common filter combinations
CREATE INDEX idx_inventory_item_category_location ON inventory_item (category_id, location_id);
CREATE INDEX idx_inventory_item_status_quantity ON inventory_item (status, quantity);
```

**No schema structure changes**: Only indexes added; existing table structure remains unchanged.

---

## API Data Transfer Objects (DTOs)

### InventoryItemSearchResponseDTO

Returned by search/filter API. May include minimal fields for list view:

```java
class InventoryItemSearchResponseDTO {
  Long id;
  String name;
  String skuCode;
  String description;
  String categoryName;        // Denormalized for UI convenience
  String locationName;        // Denormalized for UI convenience
  String status;              // ACTIVE, ARCHIVED
  Integer quantity;
  Boolean isLowStock;         // Computed based on low-stock threshold
}
```

**Note**: If full InventoryItemResponseDTO already exists from previous features, reuse it instead of creating a new DTO. This DTO shape is advisory; follow project DTO naming conventions from constitution Section III.

### SearchFilterRequest (Query Parameters)

Encapsulates filter parameters passed to backend:

```java
class SearchFilterRequest {
  @RequestParam(required = false) String search;
  @RequestParam(required = false) Long categoryId;
  @RequestParam(required = false) Long locationId;
  @RequestParam(required = false) String status;      // "ACTIVE", "ARCHIVED", "ALL"
  @RequestParam(required = false) String stockState;  // "OUT_OF_STOCK", "LOW_STOCK", "IN_STOCK", "ALL"
  @RequestParam(defaultValue = "0") Integer page;
  @RequestParam(defaultValue = "20") Integer size;
}
```

---

## Validation Rules

### Search Parameter Validation

- **search field**: Must be <= 255 characters; special characters allowed (sanitized at repository layer)
- **Empty search**: Treated as no search filter (equivalent to omitting parameter)

### Filter Parameter Validation

- **categoryId/locationId**: Must be valid FK references if provided; controller validates existence
- **status**: Must be valid enum (ACTIVE, ARCHIVED, ALL); defaults to ALL if not recognized
- **stockState**: Must be valid enum (OUT_OF_STOCK, LOW_STOCK, IN_STOCK, ALL); defaults to ALL if not recognized
- **page/size**: page >= 0, size > 0 && size <= 1000 (reasonable limit to prevent abuse)

**Validation location**: Controller layer validates all parameters before passing to service.

---

## State Transitions

No new state transitions. Existing InventoryItem state machine unchanged:
- ACTIVE ↔ ARCHIVED (existing lifecycle)

Search and filters are read-only operations; they do not transition entity state.

---

## Query Logic

### Search Query (Backend)

Partial match across multiple fields using ILIKE (case-insensitive):

```
WHERE LOWER(name) LIKE LOWER(CONCAT('%', search, '%'))
   OR LOWER(description) LIKE LOWER(CONCAT('%', search, '%'))
   OR LOWER(sku_code) LIKE LOWER(CONCAT('%', search, '%'))
```

**Index coverage**: LOWER(name), LOWER(description), LOWER(sku_code) indexes support this query.

### Stock State Classification

Computed at query time based on quantity and per-item low-stock threshold:

```
OUT_OF_STOCK  := quantity = 0
LOW_STOCK     := quantity > 0 AND quantity <= inventory_item.low_stock_threshold
IN_STOCK      := quantity > inventory_item.low_stock_threshold
```

**Low-stock threshold source**: Each item has its own `low_stock_threshold` attribute (already present in inventory_item table). Query accesses this directly without requiring configuration lookups.

### Filter Combinations (AND Logic)

All filters combined with AND:

```
WHERE (search condition if provided)
  AND (category = categoryId if provided)
  AND (location = locationId if provided)
  AND (status = status if provided AND status != 'ALL')
  AND (stock_state condition if provided AND stockState != 'ALL')
```

---

## Performance Considerations

### Query Optimization

- **Index coverage**: Indexes on search and filter columns support predicate evaluation without full table scans
- **Limit and offset**: Pagination via `LIMIT size OFFSET (page * size)` prevents large result sets
- **Index statistics**: PostgreSQL query planner uses indexes effectively for < 10k item target

### Anticipated Query Plans

For typical query with search + 2 filters on 1000 items:
- Sequential scan with index scan on searchable columns
- Nested loop join on category/location FKs
- Performance target: 500ms achieved with proper indexes and pagination

---

## Clarifications Resolved

1. ✅ **Low-stock threshold**: Per-item attribute (already exists in inventory_item table) — Query accesses `inventory_item.low_stock_threshold` directly

2. ✅ **Empty category/location handling**: Items without category/location included in unfiltered results; excluded from filtered results (NULL values naturally filtered out by WHERE clause)

3. ✅ **Search submission mode**: Submit-based (user presses Enter or clicks Search button) — Real-time keystroke updates not required
