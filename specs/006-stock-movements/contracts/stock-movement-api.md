# Stock Movement API Contracts

**Version**: 1.0  
**Date**: 2026-08-20  
**Base URL**: `http://localhost:8080/api/v1`

## Overview

The Stock Movement API provides endpoints for recording inventory stock movements (opens balance, stock in, stock out, adjustments) and querying movement history. All operations enforce business rules to prevent negative inventory and ensure audit trail completeness.

## Data Types

### Enums

**MovementType**
```
OPENING_BALANCE  - Initial quantity when item is created
STOCK_IN         - Inbound stock (e.g., supplier delivery)
STOCK_OUT        - Outbound stock (e.g., sales, disposal)
ADJUSTMENT       - Inventory discrepancy correction
```

**AdjustmentDirection**
```
INCREASE  - Adjustment increases inventory
DECREASE  - Adjustment decreases inventory
```

### Timestamp Formats

- **Date fields** (movementDate): ISO 8601 format `YYYY-MM-DD`
- **DateTime fields** (createdDate): ISO 8601 format `YYYY-MM-DDTHH:mm:ssZ`

## Endpoints

### 1. Create Stock Movement

**Endpoint**: `POST /items/{itemId}/movements`

**Authentication**: Required (Bearer token or session)

**Request Body**

```json
{
  "movementType": "STOCK_IN",
  "quantity": 50,
  "reason": "Supplier delivery order #12345",
  "movementDate": "2026-08-20",
  "adjustmentDirection": null
}
```

**Request Fields**

| Field | Type | Required | Constraints | Notes |
|-------|------|----------|-------------|-------|
| movementType | MovementType | Yes | One of: OPENING_BALANCE, STOCK_IN, STOCK_OUT, ADJUSTMENT | Determines how quantity affects current inventory |
| quantity | Integer | Yes | Must be > 0 | Cannot be zero or negative |
| reason | String | No | Max 500 characters | Optional context for the movement; helps with auditing |
| movementDate | Date | No | Any past or future date; ISO 8601 format | Defaults to today if omitted; represents when business event occurred |
| adjustmentDirection | AdjustmentDirection | Conditional | Required if movementType = "ADJUSTMENT" | Omit or null for non-adjustment types |

**Response (201 Created)**

```json
{
  "id": 123,
  "itemId": 456,
  "movementType": "STOCK_IN",
  "quantity": 50,
  "adjustmentDirection": null,
  "reason": "Supplier delivery order #12345",
  "movementDate": "2026-08-20",
  "createdDate": "2026-08-20T14:30:15Z",
  "itemCurrentQuantity": 150
}
```

**Response Fields**

| Field | Type | Notes |
|-------|------|-------|
| id | Long | Unique identifier for this movement |
| itemId | Long | Reference to the item |
| movementType | MovementType | Echoes request; normalized to uppercase |
| quantity | Long | Echoes request |
| adjustmentDirection | AdjustmentDirection | Echoes request (may be null for non-ADJUSTMENT types) |
| reason | String | Echoes request (null if not provided) |
| movementDate | Date | Date when business event occurred |
| createdDate | DateTime | Server-set timestamp when movement was persisted |
| itemCurrentQuantity | Long | **New** current quantity after this movement applied |

**Error Responses**

**400 Bad Request** - Validation error

```json
{
  "error": "Quantity must be greater than 0",
  "timestamp": "2026-08-20T14:30:15Z",
  "path": "/api/v1/items/456/movements"
}
```

Common validation errors:
- `"Quantity must be greater than 0"` — quantity <= 0
- `"Quantity is required"` — quantity field missing
- `"Movement type is required"` — movementType field missing
- `"adjustmentDirection is required for ADJUSTMENT movements"` — movementType is ADJUSTMENT but adjustmentDirection is null
- `"Reason must not exceed 500 characters"` — reason too long

**400 Bad Request** - Business rule violation

```json
{
  "error": "Stock out of 60 units would make quantity negative (current: 50)",
  "timestamp": "2026-08-20T14:30:15Z",
  "path": "/api/v1/items/456/movements"
}
```

Common business rule violations:
- `"Stock out of X units would make quantity negative (current: Y)"` — stock out would result in negative quantity
- `"Adjustment of X would make quantity negative (current: Y)"` — adjustment with direction DECREASE would result in negative quantity

**404 Not Found** - Item not found

```json
{
  "error": "Item with id 999 not found",
  "timestamp": "2026-08-20T14:30:15Z",
  "path": "/api/v1/items/999/movements"
}
```

**Example Request/Response Sequence**

```bash
# Create stock in movement
curl -X POST http://localhost:8080/api/v1/items/456/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "STOCK_IN",
    "quantity": 50,
    "reason": "Supplier delivery",
    "movementDate": "2026-08-20"
  }'

# Response (201):
{
  "id": 123,
  "itemId": 456,
  "movementType": "STOCK_IN",
  "quantity": 50,
  "adjustmentDirection": null,
  "reason": "Supplier delivery",
  "movementDate": "2026-08-20",
  "createdDate": "2026-08-20T14:30:15Z",
  "itemCurrentQuantity": 150
}
```

---

### 2. Get Movement History

**Endpoint**: `GET /items/{itemId}/movements`

**Authentication**: Required (Bearer token or session)

**Query Parameters**

| Parameter | Type | Required | Constraints | Notes |
|-----------|------|----------|-------------|-------|
| startDate | Date | No | ISO 8601 format YYYY-MM-DD | Filters to movements on or after this date |
| endDate | Date | No | ISO 8601 format YYYY-MM-DD | Filters to movements on or before this date |

**Response (200 OK)**

```json
[
  {
    "id": 1,
    "itemId": 456,
    "movementType": "OPENING_BALANCE",
    "quantity": 100,
    "adjustmentDirection": null,
    "reason": null,
    "movementDate": "2026-08-01",
    "createdDate": "2026-08-01T09:00:00Z",
    "itemCurrentQuantity": 100
  },
  {
    "id": 2,
    "itemId": 456,
    "movementType": "STOCK_IN",
    "quantity": 50,
    "adjustmentDirection": null,
    "reason": "Supplier delivery",
    "movementDate": "2026-08-15",
    "createdDate": "2026-08-15T14:30:15Z",
    "itemCurrentQuantity": 150
  },
  {
    "id": 3,
    "itemId": 456,
    "movementType": "ADJUSTMENT",
    "quantity": 5,
    "adjustmentDirection": "DECREASE",
    "reason": "Inventory shrinkage",
    "movementDate": "2026-08-20",
    "createdDate": "2026-08-20T10:15:00Z",
    "itemCurrentQuantity": 145
  }
]
```

**Response Fields**: Same as create response (per movement in array)

**Ordering**: Movements returned in order by `createdDate` (ascending; oldest first)

**Filtering**: When startDate and/or endDate provided, only movements with `movementDate` within range are included

**Pagination**: None — all matching movements returned in single response

**Error Responses**

**404 Not Found** - Item not found

```json
{
  "error": "Item with id 999 not found",
  "timestamp": "2026-08-20T14:30:15Z",
  "path": "/api/v1/items/999/movements"
}
```

**Example Request/Response**

```bash
# Get all movements for item 456
curl -X GET http://localhost:8080/api/v1/items/456/movements \
  -H "Accept: application/json"

# Get movements between two dates
curl -X GET "http://localhost:8080/api/v1/items/456/movements?startDate=2026-08-15&endDate=2026-08-20" \
  -H "Accept: application/json"
```

---

### 3. Get Item with Current Quantity

**Endpoint**: `GET /items/{itemId}` (Existing endpoint, enhanced)

**Authentication**: Required (Bearer token or session)

**Response (200 OK)**

```json
{
  "id": 456,
  "name": "Widget",
  "currentQuantity": 145,
  "category": {
    "id": 10,
    "name": "Hardware"
  },
  "createdDate": "2026-07-01T10:00:00Z",
  "updatedDate": "2026-08-20T10:15:00Z"
}
```

**Changed Fields**

| Field | Type | Change | Notes |
|-------|------|--------|-------|
| currentQuantity | Long | Updated by stock movements | Now reflects accumulated movements instead of direct edits |

**No Direct Edit**: The `currentQuantity` field in item create/update requests is ignored or rejected (API behavior TBD based on existing implementation). Stock quantity is changed exclusively via stock movement creation.

---

## Error Handling

All endpoints return error responses in consistent format:

```json
{
  "error": "Human-readable error message",
  "timestamp": "ISO 8601 datetime",
  "path": "Request path that caused error",
  "details": "Optional field for complex errors"
}
```

**HTTP Status Codes**

| Code | Meaning | Example Scenario |
|------|---------|------------------|
| 201 | Created | Stock movement successfully recorded |
| 200 | OK | Movement history retrieved; item retrieved |
| 400 | Bad Request | Validation error; business rule violation |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | User lacks permission to access item |
| 404 | Not Found | Item not found; resource doesn't exist |
| 409 | Conflict | Concurrent update conflict (if optimistic locking added later) |
| 500 | Server Error | Unexpected server error |

---

## Concurrency & Consistency

**Concurrency Model**: Last-write-wins

When two movements are recorded simultaneously for the same item:
1. Both movements are persisted
2. Both are assigned unique IDs and `createdDate` timestamps
3. The movement with the later `createdDate` determines the final `currentQuantity`
4. Audit trail contains both movements in timestamp order

**Example**:
- Item current quantity: 100
- Movement A: +50 (created 14:30:00) → quantity would be 150
- Movement B: -30 (created 14:30:01) → quantity would be 70
- Final current quantity: 70 (Movement B is more recent)

---

## Rate Limiting & Throttling

Not specified. Assume standard Spring Boot rate limiting if needed (to be implemented if required).

---

## Versioning

**API Version**: v1 (in URL path `/api/v1/`)

**Contract Stability**: Breaking changes require new version (v2, v3, etc.). Non-breaking additions (new optional fields) can be added to v1.

---

## Example Workflows

### Workflow 1: Record Stock In and Query History

```bash
# 1. Create stock in movement
POST /api/v1/items/456/movements
{
  "movementType": "STOCK_IN",
  "quantity": 100,
  "reason": "Purchase order #5678"
}
→ 201 Created, id=1001, itemCurrentQuantity=200

# 2. Query movement history
GET /api/v1/items/456/movements
→ 200 OK
[
  { id: 1000, movementType: "OPENING_BALANCE", quantity: 100, ... },
  { id: 1001, movementType: "STOCK_IN", quantity: 100, itemCurrentQuantity: 200, ... }
]
```

### Workflow 2: Attempt Invalid Stock Out

```bash
# Current quantity: 100
# Attempt to sell 150 units

POST /api/v1/items/456/movements
{
  "movementType": "STOCK_OUT",
  "quantity": 150
}
→ 400 Bad Request
{
  "error": "Stock out of 150 units would make quantity negative (current: 100)"
}

# Quantity remains 100 (unchanged)
```

### Workflow 3: Record Adjustment

```bash
POST /api/v1/items/456/movements
{
  "movementType": "ADJUSTMENT",
  "quantity": 5,
  "adjustmentDirection": "DECREASE",
  "reason": "Physical count discrepancy"
}
→ 201 Created, itemCurrentQuantity=95
```

