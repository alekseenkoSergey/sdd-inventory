# API Contract: Search and Filter Inventory Items

**Feature**: Search and Filters  
**Date**: 2026-08-20  
**Type**: REST API Endpoint

## Endpoint

```
GET /api/inventory-items
```

Retrieves a paginated list of inventory items with optional search and filter criteria applied.

---

## Query Parameters

### Search Parameter

| Name | Type | Required | Format | Description |
|------|------|----------|--------|-------------|
| `search` | String | No | Text (max 255 chars) | Partial text match against item name, description, or SKU (case-insensitive) |

**Example**: `?search=screw` matches items with "screw" in name, description, or SKU code

### Filter Parameters

| Name | Type | Required | Valid Values | Description |
|------|------|----------|--------------|-------------|
| `categoryId` | Long | No | Positive integer | Filter items by category ID. If not provided, items from all categories included |
| `locationId` | Long | No | Positive integer | Filter items by location ID. If not provided, items from all locations included |
| `status` | String | No | `ACTIVE`, `ARCHIVED`, `ALL` (default: `ALL`) | Filter items by lifecycle status |
| `stockState` | String | No | `OUT_OF_STOCK`, `LOW_STOCK`, `IN_STOCK`, `ALL` (default: `ALL`) | Filter items by stock level |

**Example**: `?categoryId=1&locationId=2&status=ACTIVE&stockState=LOW_STOCK`

### Pagination Parameters

| Name | Type | Required | Default | Constraints | Description |
|------|------|----------|---------|-------------|-------------|
| `page` | Integer | No | 0 | >= 0 | Zero-indexed page number |
| `size` | Integer | No | 20 | 1-1000 | Items per page |

**Example**: `?page=0&size=50` returns first 50 items

---

## Request Examples

### Example 1: Search Only

```
GET /api/inventory-items?search=LED%20bulb&page=0&size=20
```

Returns first 20 items matching "LED bulb" in name, description, or SKU.

### Example 2: Filters Only

```
GET /api/inventory-items?categoryId=3&status=ACTIVE&stockState=LOW_STOCK&page=0&size=20
```

Returns first 20 active items in category 3 with low stock (AND logic).

### Example 3: Combined Search and Filters

```
GET /api/inventory-items?search=electronics&categoryId=1&locationId=2&status=ACTIVE&stockState=IN_STOCK&page=0&size=20
```

Returns first 20 active items matching "electronics" in category 1, location 2, with in-stock status.

---

## Response

### Success Response (200 OK)

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "LED Light Bulb",
      "skuCode": "LED-001-60W",
      "description": "60W equivalent LED bulb, warm white",
      "categoryId": 1,
      "categoryName": "Lighting",
      "locationId": 5,
      "locationName": "Warehouse A - Shelf 3",
      "status": "ACTIVE",
      "quantity": 45,
      "isLowStock": false
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Fluorescent Ballast",
      "skuCode": "FLUO-BALLAST-40W",
      "description": "Electronic ballast for 40W fluorescent tubes",
      "categoryId": 1,
      "categoryName": "Lighting",
      "locationId": 6,
      "locationName": "Warehouse B - Storage",
      "status": "ACTIVE",
      "quantity": 3,
      "isLowStock": true
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": false,
  "hasPrevious": false
}
```

**Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `content` | Array | List of InventoryItemSearchResponseDTO objects |
| `totalElements` | Integer | Total number of items matching criteria (all pages) |
| `totalPages` | Integer | Total number of pages |
| `currentPage` | Integer | Current page number (zero-indexed) |
| `pageSize` | Integer | Number of items per page |
| `hasNext` | Boolean | Whether a next page exists |
| `hasPrevious` | Boolean | Whether a previous page exists |

**Item Response Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID/Long | Unique item identifier |
| `name` | String | Item name |
| `skuCode` | String | SKU/product code |
| `description` | String | Item description/notes |
| `categoryId` | Long | Category ID (for reference) |
| `categoryName` | String | Category display name (human-readable) |
| `locationId` | Long | Location ID (for reference) |
| `locationName` | String | Location display name (human-readable) |
| `status` | String | Item status: "ACTIVE" or "ARCHIVED" |
| `quantity` | Integer | Current stock quantity |
| `isLowStock` | Boolean | True if quantity <= low-stock threshold |

### Empty Result Response (200 OK)

When search/filters match no items:

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "currentPage": 0,
  "pageSize": 20,
  "hasNext": false,
  "hasPrevious": false
}
```

### Error Response (400 Bad Request)

Invalid filter parameters or search criteria:

```json
{
  "code": "INVALID_FILTER",
  "message": "Invalid stockState value. Must be: OUT_OF_STOCK, LOW_STOCK, IN_STOCK, or ALL",
  "details": {
    "invalidParameter": "stockState",
    "providedValue": "OVERSTOCKED",
    "validValues": ["OUT_OF_STOCK", "LOW_STOCK", "IN_STOCK", "ALL"]
  }
}
```

### Error Response (404 Not Found)

Referenced category or location does not exist:

```json
{
  "code": "CATEGORY_NOT_FOUND",
  "message": "Category with ID 999 does not exist",
  "details": {
    "categoryId": 999
  }
}
```

### Error Response (500 Internal Server Error)

Unexpected server error. See system logs.

---

## Performance Guarantees

- **Response time**: Search results return within 500ms for typical inventory (100-1000 items) on a standard application server
- **Max result set**: Page size capped at 1000 items to prevent abuse; pagination required for large result sets
- **Index coverage**: Searchable fields (name, description, SKU) and filter fields (category, location, status, quantity) are indexed for efficient querying

---

## Behavioral Specifications

### Search Behavior

- **Case-insensitive**: Search text "LED" matches "led", "Led", "LED"
- **Partial matching**: Search text "bul" matches "bulb", "bulk", "bulletin"
- **Multi-field match**: Single search matches across name, description, OR SKU code (OR logic within search)
- **Empty search**: Omit `search` parameter or pass empty string; treated as "no search filter"
- **Special characters**: Handled gracefully; no SQL injection risk (parameterized queries)

### Filter Behavior

- **AND logic**: All active filters combined with AND (item must match all)
- **Single-value filters**: Only one category, one location can be active at a time (though `ALL` can be specified for status/stockState)
- **Default values**: Omitted filter parameters default to include all values (equivalent to passing `ALL`)
- **NULL handling**: Items without a category or location (`NULL` values) are included when no category/location filter is applied. When a specific `categoryId` or `locationId` is provided, items without that assignment are excluded (natural SQL NULL filtering)
- **Invalid references**: If provided `categoryId` or `locationId` does not exist, return 404 with clear message
- **Invalid enum values**: If `status` or `stockState` contains invalid value, return 400 with valid options

### Pagination Behavior

- **Default page size**: 20 items
- **Zero-indexed pages**: `page=0` is first page
- **Out-of-range pages**: Request for `page=100` when only 5 pages exist returns empty results (200 OK) with `totalPages: 5`
- **Large page sizes**: `size` parameter clamped to 1000 maximum; requests for larger sizes truncated

### State Handling

- **Read-only**: Search and filter operations do not modify inventory state
- **Session state**: Filter and search state is session-scoped; expires on logout or page reload

### Stock State Classification

Stock state is calculated per-item using the item's own `lowStockThreshold` value:

- **OUT_OF_STOCK**: `quantity == 0`
- **LOW_STOCK**: `quantity > 0 AND quantity <= item.lowStockThreshold`
- **IN_STOCK**: `quantity > item.lowStockThreshold`

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `INVALID_FILTER` | 400 | Search/filter parameter validation failed (e.g., invalid enum value) |
| `CATEGORY_NOT_FOUND` | 404 | Referenced category ID does not exist |
| `LOCATION_NOT_FOUND` | 404 | Referenced location ID does not exist |
| `INVALID_PAGINATION` | 400 | Page or size parameters out of valid range |
| `INTERNAL_SERVER_ERROR` | 500 | Unexpected error; check application logs |

---

## Testing Scenarios

See `quickstart.md` for runnable validation test scenarios that exercise this contract.
