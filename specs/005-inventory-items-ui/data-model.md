# Data Model: Inventory Items UI

**Date**: 2026-08-20  
**Feature**: Inventory Items User Interface  
**Purpose**: Document entity structures, DTOs, relationships, and validation rules for the frontend

---

## Overview

The frontend data model consists of:
1. **Domain Models** (backend entities): InventoryItem, Category, Location
2. **Frontend DTOs** (API request/response shapes): Aligned with backend API contract
3. **Form Models** (TypeScript interfaces for reactive forms)
4. **State Models** (service state management)

All models align with the backend API contract (`inventory-items-api.md`).

---

## Domain Models

### InventoryItem

Represents a single inventory item. This is the primary entity managed by the feature.

```typescript
interface InventoryItem {
  id: number;                           // Auto-generated, IDENTITY
  name: string;                         // Non-empty, max 255 chars
  description?: string;                 // Optional, max 1000 chars
  sku?: string;                         // Optional, max 100 chars, unique per user
  categoryId: number;                   // Foreign key to Category, belongs to user
  locationId: number;                   // Foreign key to Location, belongs to user
  currentQuantity: number;              // Decimal >= 0, calculated from stock movements
  unit: string;                         // Required, max 50 chars (e.g., "pcs", "kg", "liters")
  lowStockThreshold: number;            // Decimal >= 0, default 0
  status: 'ACTIVE' | 'ARCHIVED';       // Status: active or archived
  createdDate: string;                  // ISO-8601 UTC timestamp (e.g., "2026-08-20T14:30:00Z")
  updatedDate: string;                  // ISO-8601 UTC timestamp
}

// Additional fields for frontend display (populated from category/location lookups)
interface InventoryItemWithNames extends InventoryItem {
  categoryName: string;                 // Human-readable category name
  locationName: string;                 // Human-readable location name
}
```

**Lifecycle**:
- Created: `status = ACTIVE`
- Can transition: `ACTIVE → ARCHIVED` (archive)
- Can transition: `ARCHIVED → ACTIVE` (restore)
- Can be deleted: Permanently removed

**Validation Rules**:
- `name`: Required, non-empty, max 255 characters
- `currentQuantity`: Never negative (enforced server-side after stock movements)
- `lowStockThreshold`: >= 0 (validated on create/edit)
- `sku`: Optional, max 100 characters, unique per (user_id, sku)
- `categoryId`: Must exist and belong to authenticated user
- `locationId`: Must exist and belong to authenticated user
- `unit`: Required, max 50 characters
- `status`: Read-only; cannot be changed via edit (only via archive/restore)

---

### Category

Represents an inventory category. Populated from the backend API.

```typescript
interface Category {
  id: number;
  name: string;
  // ... other fields from backend (not all used in this feature)
}
```

**Usage**: Dropdown/select in create/edit forms; filter in list view

---

### Location

Represents a storage location. Populated from the backend API.

```typescript
interface Location {
  id: number;
  name: string;
  // ... other fields from backend (not all used in this feature)
}
```

**Usage**: Dropdown/select in create/edit forms; display in list view

---

## Frontend Form Models

### CreateItemFormModel

Model for create form (mapped from form.value before submission).

```typescript
interface CreateItemFormModel {
  name: string;                         // Required
  description?: string;                 // Optional
  sku?: string;                         // Optional
  categoryId: number;                   // Required
  locationId: number;                   // Required
  unit: string;                         // Required
  lowStockThreshold?: number;           // Optional, default 0
  initialQuantity?: number;             // Optional, default 0 (creates stock movement if > 0)
}
```

**Validation Rules** (Reactive Forms):
- `name`: required, maxLength(255)
- `description`: maxLength(1000)
- `sku`: maxLength(100)
- `categoryId`: required
- `locationId`: required
- `unit`: required, maxLength(50)
- `lowStockThreshold`: min(0)
- `initialQuantity`: min(0)

**Server Validation** (backend validates):
- SKU uniqueness per user
- Category belongs to user
- Location belongs to user
- All field constraints

---

### EditItemFormModel

Model for edit form (mapped from form.value before submission).

```typescript
interface EditItemFormModel {
  name?: string;                        // Optional (PATCH allows partial updates)
  description?: string;                 // Optional
  sku?: string;                         // Optional
  categoryId?: number;                  // Optional
  locationId?: number;                  // Optional
  unit?: string;                        // Optional
  lowStockThreshold?: number;           // Optional
  // Note: currentQuantity is NOT included; it's read-only
}
```

**Validation Rules** (Reactive Forms):
- `name`: if provided, required and maxLength(255)
- `description`: maxLength(1000)
- `sku`: maxLength(100)
- `categoryId`: if provided, required
- `locationId`: if provided, required
- `unit`: if provided, required and maxLength(50)
- `lowStockThreshold`: if provided, min(0)

**Key Difference from Create**:
- `currentQuantity` is NEVER editable (read-only field disabled in form)
- `initialQuantity` field not present (only for create)
- All fields optional (PATCH semantics)

---

## API Request/Response Shapes

### Create Request (POST /api/v1/inventory-items)

```typescript
interface InventoryItemCreateRequestDTO {
  name: string;
  description?: string;
  sku?: string;
  categoryId: number;
  locationId: number;
  unit: string;
  lowStockThreshold?: number;
  initialQuantity?: number;
}
```

**Response**: `InventoryItemResponseDTO` (201 Created)

---

### Update Request (PATCH /api/v1/inventory-items/{id})

```typescript
interface InventoryItemPatchDTO {
  name?: string;
  description?: string;
  sku?: string;
  categoryId?: number;
  locationId?: number;
  unit?: string;
  lowStockThreshold?: number;
  // Note: currentQuantity intentionally NOT included
}
```

**Response**: `InventoryItemResponseDTO` (200 OK)

---

### Response (GET, POST, PATCH endpoints)

```typescript
interface InventoryItemResponseDTO {
  id: number;
  name: string;
  description?: string;
  sku?: string;
  categoryId: number;
  locationId: number;
  currentQuantity: number;
  unit: string;
  lowStockThreshold: number;
  status: 'ACTIVE' | 'ARCHIVED';
  createdDate: string;                  // ISO-8601
  updatedDate: string;                  // ISO-8601
}
```

---

### List Response (GET /api/v1/inventory-items)

```typescript
interface PagedListResponse {
  content: InventoryItemResponseDTO[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}
```

---

### Archive Request (POST /api/v1/inventory-items/{id}/archive)

```
Empty body
```

**Response**: `InventoryItemResponseDTO` with `status: "ARCHIVED"` (200 OK)

---

### Restore Request (POST /api/v1/inventory-items/{id}/restore)

```
Empty body
```

**Response**: `InventoryItemResponseDTO` with `status: "ACTIVE"` (200 OK)

---

### Delete Request (DELETE /api/v1/inventory-items/{id})

```
Empty body
```

**Response**: Empty (204 No Content)

---

## Service State Models

### InventoryItemsService State

```typescript
// Service manages these observable streams:
interface InventoryItemsServiceState {
  items$: Observable<InventoryItem[]>;           // Current page items
  loading$: Observable<boolean>;                 // Is loading?
  error$: Observable<ApiError | null>;           // Current error (if any)
  filters$: Observable<ItemFilters>;             // Current filters
  totalPages$: Observable<number>;               // Total pages for pagination
  categories$: Observable<Category[]>;           // Available categories
  locations$: Observable<Location[]>;            // Available locations
}

interface ItemFilters {
  page: number;                                  // 0-indexed page number
  size: number;                                  // Items per page (default 20)
  status?: 'ACTIVE' | 'ARCHIVED' | null;        // null = all statuses
  categoryId?: number | null;                    // null = all categories
}

interface ApiError {
  status: number;                                // HTTP status (400, 404, 500, etc.)
  message: string;                               // User-friendly error message
  code?: string;                                 // Backend error code (VALIDATION_ERROR, etc.)
}
```

---

## Form Validation Rules

All validation rules are enforced both client-side (Angular Validators) and server-side (backend).

### Create Form Validation

| Field | Client Validators | Server Validation | Error Message |
|-------|-------------------|-------------------|---------------|
| name | required, maxLength(255) | required, maxLength(255) | "Name is required" / "Name must not exceed 255 characters" |
| description | maxLength(1000) | maxLength(1000) | "Description must not exceed 1000 characters" |
| sku | maxLength(100) | maxLength(100), unique per user | "SKU must not exceed 100 characters" / "SKU already exists" |
| categoryId | required | required, belongs to user | "Category is required" / "Category not found" |
| locationId | required | required, belongs to user | "Location is required" / "Location not found" |
| unit | required, maxLength(50) | required, maxLength(50) | "Unit is required" / "Unit must not exceed 50 characters" |
| lowStockThreshold | min(0) | min(0) | "Low-stock threshold must not be negative" |
| initialQuantity | min(0) | min(0) | "Initial quantity must not be negative" |

### Edit Form Validation

Same rules as create, but all fields optional (PATCH semantics).

| Field | Client Validators | Server Validation |
|-------|-------------------|-------------------|
| name | if provided: required, maxLength(255) | if provided: required, maxLength(255) |
| description | maxLength(1000) | maxLength(1000) |
| sku | maxLength(100) | maxLength(100), unique per user |
| categoryId | if provided: required | if provided: required, belongs to user |
| locationId | if provided: required | if provided: required, belongs to user |
| unit | if provided: required, maxLength(50) | if provided: required, maxLength(50) |
| lowStockThreshold | if provided: min(0) | if provided: min(0) |

---

## Status State Machine

```
┌─────────┐
│ CREATED │ (Initial state on create)
└────┬────┘
     │ (set status = ACTIVE)
     ▼
┌─────────────┐
│   ACTIVE    │ (Can archive)
│  (default)  │
└────┬────────┘
     │ (archive)
     ▼
┌──────────────┐
│  ARCHIVED    │ (Can restore)
│ (not active) │
└────┬─────────┘
     │ (restore)
     ▼
     ├──→ ACTIVE

Permanent deletion:
  ACTIVE │ ARCHIVED → [DELETED] (removed from system)
```

---

## API Error Response Format

All error responses (4xx, 5xx) follow this format (from backend contract):

```typescript
interface ApiErrorResponse {
  timestamp: string;                    // ISO-8601
  status: number;                       // HTTP status code
  error: string;                        // Error code (e.g., "VALIDATION_ERROR")
  message: string;                      // User-friendly message
  path: string;                         // Request path
}
```

**Common Error Codes**:
- `VALIDATION_ERROR` (400): Invalid input (empty name, negative quantity, etc.)
- `SKU_DUPLICATE` (400): SKU already exists for this user
- `CATEGORY_NOT_FOUND` (404): Category doesn't exist or belongs to different user
- `LOCATION_NOT_FOUND` (404): Location doesn't exist or belongs to different user
- `ITEM_NOT_FOUND` (404): Item doesn't exist or belongs to different user
- `UNAUTHORIZED` (401): Missing/invalid authentication token

---

## List View Display

### Table Columns

The list view displays items in a table with these columns:

| Column | Source Field | Format | Sortable |
|--------|--------------|--------|----------|
| Name | item.name | Plain text | Optional |
| SKU | item.sku | Plain text (or "-" if empty) | Optional |
| Category | item.categoryName | Plain text | Optional |
| Location | item.locationName | Plain text | Optional |
| Quantity | item.currentQuantity | Numeric, decimal if applicable | Optional |
| Unit | item.unit | Plain text | No |
| Low-Stock Threshold | item.lowStockThreshold | Numeric | No |
| Status | item.status | Badge: ACTIVE (green), ARCHIVED (gray) | No |
| Created | item.createdDate | DatePipe('medium') | Optional |
| Actions | — | Edit, Archive/Restore, Delete buttons | N/A |

---

## Detail View Display

The detail view displays all item fields:

| Field | Display Format |
|-------|----------------|
| ID | Plain text (read-only) |
| Name | Plain text |
| Description | Plain text (or empty if not provided) |
| SKU | Plain text (or empty if not provided) |
| Category | Category name (clickable to filter?) |
| Location | Location name |
| Current Quantity | Numeric (read-only, styled if below threshold) |
| Unit | Plain text |
| Low-Stock Threshold | Numeric (highlight if current < threshold) |
| Status | Badge with current status |
| Created Date | DatePipe('medium') |
| Updated Date | DatePipe('medium') |

---

## Relationships

```
┌──────────────────┐
│ InventoryItem    │
│────────────────  │
│ id (PK)          │
│ name             │
│ categoryId (FK)  │──→ (many-to-one) Category
│ locationId (FK)  │──→ (many-to-one) Location
│ status           │
│ currentQuantity  │──→ (calculated from StockMovement)
└──────────────────┘

┌──────────────┐
│ Category     │
│──────────────│
│ id (PK)      │
│ name         │
│ userId (FK)  │
└──────────────┘

┌──────────────┐
│ Location     │
│──────────────│
│ id (PK)      │
│ name         │
│ userId (FK)  │
└──────────────┘
```

**User Data Isolation**:
- All entities (InventoryItem, Category, Location) belong to exactly one user
- Enforced via userId foreign keys (backend)
- Frontend assumes API returns only user-owned items

---

## Summary

- **Primary Entity**: InventoryItem (8+ fields + read-only fields)
- **State**: ACTIVE ↔ ARCHIVED (archive/restore)
- **Relationships**: Item → Category (many-to-one), Item → Location (many-to-one)
- **Validation**: 8 fields with client+server validation
- **API Contract**: Aligned with backend inventory-items-api.md
- **Frontend Storage**: Service-based state with RxJS observables
- **Pagination**: Server-side (20 items per page)
- **Filters**: Status (all/active/archived) + Category

All models are TypeScript interfaces; no GraphQL/OpenAPI code generation needed.
