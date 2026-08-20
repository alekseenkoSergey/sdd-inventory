# Inventory Items REST API Contracts

**Date**: 2026-08-20

## Overview

REST API endpoints for inventory item management. All endpoints enforce user data isolation by using the authenticated user's ID from Spring Security context.

**Base URL**: `/api/v1/inventory-items`

**Authentication**: Bearer token (Spring Security) required for all endpoints

**Content-Type**: `application/json`

---

## Endpoint: Create Inventory Item

**Method**: `POST /api/v1/inventory-items`

**Authentication**: Required

**Description**: Create a new inventory item. If initialQuantity > 0, automatically creates an opening balance stock movement.

### Request

```json
{
  "name": "Widget A",
  "description": "Premium widget variant",
  "sku": "SKU-001",
  "categoryId": 1,
  "locationId": 2,
  "unit": "pcs",
  "lowStockThreshold": 10,
  "initialQuantity": 100
}
```

**Request DTO**: `InventoryItemCreateRequestDTO`

| Field | Type | Required | Constraints | Notes |
|-------|------|----------|-----------|-------|
| `name` | String | Yes | Non-empty, max 255 | Item name |
| `description` | String | No | Max 1000 chars | Optional notes |
| `sku` | String | No | Max 100, unique per user | Optional item code |
| `categoryId` | Long | Yes | Must exist in category table; must belong to requesting user | Category reference (validated on backend) |
| `locationId` | Long | Yes | Must exist in location table; must belong to requesting user | Location reference (validated on backend) |
| `unit` | String | Yes | Max 50 chars | Unit of measure |
| `lowStockThreshold` | Decimal | No | >= 0, default 0 | Low-stock alert threshold |
| `initialQuantity` | Decimal | No | >= 0, default 0 | Opening quantity (creates stock movement if > 0) |

### Response (201 Created)

```json
{
  "id": 123,
  "name": "Widget A",
  "description": "Premium widget variant",
  "sku": "SKU-001",
  "categoryId": 1,
  "locationId": 2,
  "currentQuantity": 100,
  "unit": "pcs",
  "lowStockThreshold": 10,
  "status": "ACTIVE",
  "createdDate": "2026-08-20T14:30:00Z",
  "updatedDate": "2026-08-20T14:30:00Z"
}
```

**Response DTO**: `InventoryItemResponseDTO`

| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | Unique identifier (auto-generated, IDENTITY) |
| `name` | String | Item name |
| `description` | String | Optional description |
| `sku` | String | Optional SKU code |
| `categoryId` | Long | Category reference (Long ID matching Category entity) |
| `locationId` | Long | Location reference (Long ID matching Location entity) |
| `currentQuantity` | Decimal | Current stock (auto-calculated) |
| `unit` | String | Unit of measure |
| `lowStockThreshold` | Decimal | Low-stock threshold |
| `status` | String | "ACTIVE" or "ARCHIVED" |
| `createdDate` | ISO-8601 Timestamp | UTC server time |
| `updatedDate` | ISO-8601 Timestamp | UTC server time |

### Error Responses

| Status | Code | Reason |
|--------|------|--------|
| 400 | VALIDATION_ERROR | Invalid input (empty name, negative quantity, etc.) |
| 400 | SKU_DUPLICATE | SKU already exists for this user |
| 404 | CATEGORY_NOT_FOUND | CategoryId doesn't exist or belongs to different user |
| 404 | LOCATION_NOT_FOUND | LocationId doesn't exist or belongs to different user |
| 401 | UNAUTHORIZED | Missing/invalid authentication token |

---

## Endpoint: Get Inventory Item

**Method**: `GET /api/v1/inventory-items/{id}`

**Authentication**: Required

**Description**: Retrieve a single inventory item by ID. Enforces user ownership.

### Response (200 OK)

Same as Create response (see InventoryItemResponseDTO above).

### Error Responses

| Status | Code | Reason |
|--------|------|--------|
| 404 | ITEM_NOT_FOUND | Item doesn't exist or belongs to different user |
| 401 | UNAUTHORIZED | Missing/invalid authentication token |

---

## Endpoint: List Inventory Items

**Method**: `GET /api/v1/inventory-items`

**Authentication**: Required

**Description**: List all inventory items for the authenticated user. Supports pagination and filtering.

### Query Parameters

| Parameter | Type | Default | Notes |
|-----------|------|---------|-------|
| `page` | Integer | 0 | Page number (0-indexed) |
| `size` | Integer | 20 | Items per page |
| `status` | String | (none) | Filter by status: ACTIVE, ARCHIVED, or omit for all |
| `categoryId` | UUID | (none) | Filter by category |

### Response (200 OK)

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "name": "Widget A",
      "description": "Premium widget variant",
      "sku": "SKU-001",
      "categoryId": "550e8400-e29b-41d4-a716-446655440000",
      "locationId": "550e8400-e29b-41d4-a716-446655440001",
      "currentQuantity": 100,
      "unit": "pcs",
      "lowStockThreshold": 10,
      "status": "ACTIVE",
      "createdDate": "2026-08-20T14:30:00Z",
      "updatedDate": "2026-08-20T14:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

### Error Responses

| Status | Code | Reason |
|--------|------|--------|
| 401 | UNAUTHORIZED | Missing/invalid authentication token |

---

## Endpoint: Update Inventory Item

**Method**: `PATCH /api/v1/inventory-items/{id}`

**Authentication**: Required

**Description**: Update item fields. Does NOT accept currentQuantity (read-only). Enforces user ownership and category/location same-user check.

### Request

```json
{
  "name": "Widget A Updated",
  "description": "Updated description",
  "sku": "SKU-001-NEW",
  "categoryId": "550e8400-e29b-41d4-a716-446655440003",
  "locationId": "550e8400-e29b-41d4-a716-446655440004",
  "unit": "boxes",
  "lowStockThreshold": 20
}
```

**Request DTO**: `InventoryItemPatchDTO`

| Field | Type | Required | Constraints | Notes |
|-------|------|----------|-----------|-------|
| `name` | String | No | Non-empty if provided, max 255 | Item name |
| `description` | String | No | Max 1000 chars | Optional notes |
| `sku` | String | No | Max 100, unique per user | Optional item code |
| `categoryId` | Long | No | Must exist in category table; must belong to requesting user | Category reference (validated on backend) |
| `locationId` | Long | No | Must exist in location table; must belong to requesting user | Location reference (validated on backend) |
| `unit` | String | No | Max 50 chars | Unit of measure |
| `lowStockThreshold` | Decimal | No | >= 0 | Low-stock threshold |
| **NOT INCLUDED** | | | | `currentQuantity` is read-only; use stock movements API |

### Response (200 OK)

Same as Get response (updated InventoryItemResponseDTO).

### Error Responses

| Status | Code | Reason |
|--------|------|--------|
| 400 | VALIDATION_ERROR | Invalid input (empty name if provided, negative threshold, etc.) |
| 400 | SKU_DUPLICATE | New SKU already exists for this user |
| 404 | ITEM_NOT_FOUND | Item doesn't exist or belongs to different user |
| 404 | CATEGORY_NOT_FOUND | CategoryId doesn't exist or belongs to different user |
| 404 | LOCATION_NOT_FOUND | LocationId doesn't exist or belongs to different user |
| 401 | UNAUTHORIZED | Missing/invalid authentication token |

---

## Endpoint: Archive Inventory Item

**Method**: `POST /api/v1/inventory-items/{id}/archive`

**Authentication**: Required

**Description**: Archive an inventory item (change status to ARCHIVED). Idempotent—succeeds even if already archived.

### Request

Empty body.

### Response (200 OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440002",
  "name": "Widget A",
  "description": "Premium widget variant",
  "sku": "SKU-001",
  "categoryId": "550e8400-e29b-41d4-a716-446655440000",
  "locationId": "550e8400-e29b-41d4-a716-446655440001",
  "currentQuantity": 100,
  "unit": "pcs",
  "lowStockThreshold": 10,
  "status": "ARCHIVED",
  "createdDate": "2026-08-20T14:30:00Z",
  "updatedDate": "2026-08-20T14:35:00Z"
}
```

### Error Responses

| Status | Code | Reason |
|--------|------|--------|
| 404 | ITEM_NOT_FOUND | Item doesn't exist or belongs to different user |
| 401 | UNAUTHORIZED | Missing/invalid authentication token |

---

## Endpoint: Restore Inventory Item

**Method**: `POST /api/v1/inventory-items/{id}/restore`

**Authentication**: Required

**Description**: Restore an archived inventory item (change status to ACTIVE). Idempotent—succeeds even if already active.

### Request

Empty body.

### Response (200 OK)

Same format as Archive response, but with `"status": "ACTIVE"`.

### Error Responses

| Status | Code | Reason |
|--------|------|--------|
| 404 | ITEM_NOT_FOUND | Item doesn't exist or belongs to different user |
| 401 | UNAUTHORIZED | Missing/invalid authentication token |

---

## Endpoint: Delete Inventory Item

**Method**: `DELETE /api/v1/inventory-items/{id}`

**Authentication**: Required

**Description**: Permanently delete an inventory item. Hard delete (no soft delete). Cannot be undone.

### Request

Empty body.

### Response (204 No Content)

No response body.

### Error Responses

| Status | Code | Reason |
|--------|------|--------|
| 404 | ITEM_NOT_FOUND | Item doesn't exist or belongs to different user |
| 401 | UNAUTHORIZED | Missing/invalid authentication token |

---

## Global Error Response Format

All error responses (4xx, 5xx) use this format:

```json
{
  "timestamp": "2026-08-20T14:30:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Item name must not be empty",
  "path": "/api/v1/inventory-items"
}
```

**Handled by**: Centralized `@ControllerAdvice` exception handler (per constitutional principle IV).

---

## Validation Rules Applied at API Level

The following validations are enforced before processing:

- Name: Non-empty (required), max 255 characters
- Description: Max 1000 characters (optional)
- SKU: Max 100 characters, unique per (user_id, sku) when provided (optional)
- Unit: Required, max 50 characters
- LowStockThreshold: >= 0
- InitialQuantity (create only): >= 0
- CategoryId and LocationId: Must belong to authenticated user
- Status: Read-only (cannot be changed via update; use archive/restore endpoints)

---

## User Data Isolation Enforcement

Every endpoint:
1. Extracts authenticated user ID from Spring Security context
2. Filters queries by user_id
3. Validates category/location ownership before operations
4. Returns 404 (not 403) for items belonging to other users (treats them as not found)

---

## API Client Example (Pseudocode)

```
Create item:
  POST /api/v1/inventory-items
  Authorization: Bearer {token}
  {name: "Widget A", categoryId: ..., locationId: ..., unit: "pcs", initialQuantity: 100}

Update item:
  PATCH /api/v1/inventory-items/{id}
  Authorization: Bearer {token}
  {name: "Widget A Updated", lowStockThreshold: 20}

Archive item:
  POST /api/v1/inventory-items/{id}/archive
  Authorization: Bearer {token}

List items:
  GET /api/v1/inventory-items?status=ACTIVE&categoryId={categoryId}
  Authorization: Bearer {token}

Delete item:
  DELETE /api/v1/inventory-items/{id}
  Authorization: Bearer {token}
```
