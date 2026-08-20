# API Reference: Inventory Items Endpoints

**Source**: Backend specification at `/specs/004-inventory-items/contracts/inventory-items-api.md`

This document serves as the frontend reference for the inventory items REST API. The authoritative backend contract is in the backend specification.

---

## Base URL

```
/api/v1/inventory-items
```

## Authentication

All endpoints require bearer token authentication via Spring Security.

**Header**: `Authorization: Bearer {token}`

---

## Endpoints

### POST /api/v1/inventory-items

**Create a new inventory item**

**Request**:
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

**Response (201 Created)**:
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

**Error Responses**:
- `400 VALIDATION_ERROR`: Invalid input (empty name, negative quantity, etc.)
- `400 SKU_DUPLICATE`: SKU already exists for this user
- `404 CATEGORY_NOT_FOUND`: Category doesn't exist or belongs to different user
- `404 LOCATION_NOT_FOUND`: Location doesn't exist or belongs to different user
- `401 UNAUTHORIZED`: Missing/invalid authentication token

---

### GET /api/v1/inventory-items

**List inventory items with pagination and filtering**

**Query Parameters**:
- `page` (int, default 0): Page number (0-indexed)
- `size` (int, default 20): Items per page
- `status` (string): Filter by status: `ACTIVE`, `ARCHIVED`, or omit for all
- `categoryId` (number): Filter by category ID

**Example**: `GET /api/v1/inventory-items?page=0&size=20&status=ACTIVE&categoryId=1`

**Response (200 OK)**:
```json
{
  "content": [
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
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0,
  "pageSize": 20
}
```

**Error Responses**:
- `401 UNAUTHORIZED`: Missing/invalid authentication token

---

### GET /api/v1/inventory-items/{id}

**Retrieve a single inventory item by ID**

**Response (200 OK)**:
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

**Error Responses**:
- `404 ITEM_NOT_FOUND`: Item doesn't exist or belongs to different user
- `401 UNAUTHORIZED`: Missing/invalid authentication token

---

### PATCH /api/v1/inventory-items/{id}

**Update inventory item fields (partial update)**

**Request** (all fields optional):
```json
{
  "name": "Widget A Updated",
  "description": "Updated description",
  "sku": "SKU-001-NEW",
  "categoryId": 2,
  "locationId": 3,
  "unit": "boxes",
  "lowStockThreshold": 20
}
```

**Response (200 OK)**:
```json
{
  "id": 123,
  "name": "Widget A Updated",
  "description": "Updated description",
  "sku": "SKU-001-NEW",
  "categoryId": 2,
  "locationId": 3,
  "currentQuantity": 100,
  "unit": "boxes",
  "lowStockThreshold": 20,
  "status": "ACTIVE",
  "createdDate": "2026-08-20T14:30:00Z",
  "updatedDate": "2026-08-20T14:40:00Z"
}
```

**Important**: 
- `currentQuantity` is NOT included in request (read-only)
- Stock quantity changes only via stock movements API

**Error Responses**:
- `400 VALIDATION_ERROR`: Invalid input
- `400 SKU_DUPLICATE`: New SKU already exists for this user
- `404 ITEM_NOT_FOUND`: Item doesn't exist or belongs to different user
- `404 CATEGORY_NOT_FOUND`: Category doesn't exist or belongs to different user
- `404 LOCATION_NOT_FOUND`: Location doesn't exist or belongs to different user
- `401 UNAUTHORIZED`: Missing/invalid authentication token

---

### POST /api/v1/inventory-items/{id}/archive

**Archive an inventory item (change status to ARCHIVED)**

**Request**: Empty body

**Response (200 OK)**:
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
  "status": "ARCHIVED",
  "createdDate": "2026-08-20T14:30:00Z",
  "updatedDate": "2026-08-20T14:35:00Z"
}
```

**Note**: This operation is idempotent. Calling it on an already-archived item succeeds without error.

**Error Responses**:
- `404 ITEM_NOT_FOUND`: Item doesn't exist or belongs to different user
- `401 UNAUTHORIZED`: Missing/invalid authentication token

---

### POST /api/v1/inventory-items/{id}/restore

**Restore an archived inventory item (change status to ACTIVE)**

**Request**: Empty body

**Response (200 OK)**:
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
  "updatedDate": "2026-08-20T14:35:00Z"
}
```

**Note**: This operation is idempotent. Calling it on an already-active item succeeds without error.

**Error Responses**:
- `404 ITEM_NOT_FOUND`: Item doesn't exist or belongs to different user
- `401 UNAUTHORIZED`: Missing/invalid authentication token

---

### DELETE /api/v1/inventory-items/{id}

**Permanently delete an inventory item**

**Request**: Empty body

**Response (204 No Content)**: No response body

**Error Responses**:
- `404 ITEM_NOT_FOUND`: Item doesn't exist or belongs to different user
- `401 UNAUTHORIZED`: Missing/invalid authentication token

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

**Common Error Codes**:
- `VALIDATION_ERROR` (400): Invalid input (validation constraint violated)
- `SKU_DUPLICATE` (400): SKU already exists for this user
- `CATEGORY_NOT_FOUND` (404): Category doesn't exist or belongs to different user
- `LOCATION_NOT_FOUND` (404): Location doesn't exist or belongs to different user
- `ITEM_NOT_FOUND` (404): Item doesn't exist or belongs to different user
- `UNAUTHORIZED` (401): Missing/invalid authentication token

---

## Frontend Implementation Notes

### Handling Pagination

```typescript
// Query params for filtering/pagination
const params = new HttpParams()
  .set('page', '0')
  .set('size', '20')
  .set('status', 'ACTIVE')  // optional
  .set('categoryId', '1');  // optional

this.http.get('/api/v1/inventory-items', { params });
```

### Handling Errors

```typescript
// Error interceptor transforms backend error response
const errorResponse = error.error;
const userMessage = errorResponse.message || 'An unexpected error occurred';
// Display to user in UI
```

### User Data Isolation

- All endpoints automatically filter by authenticated user (Spring Security)
- If user tries to access item belonging to another user: 404 response (treated as not found)
- Frontend should NOT have UI to access other users' data
- Backend enforces the boundary; UI is secondary defense

---

## Related Documents

- **Backend API Contract**: `/specs/004-inventory-items/contracts/inventory-items-api.md` (authoritative source)
- **Data Model**: [../data-model.md](../data-model.md)
- **Quickstart Guide**: [../quickstart.md](../quickstart.md)
