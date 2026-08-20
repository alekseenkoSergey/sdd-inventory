# Data Model: Stock Movements UI

**Date**: 2026-08-20
**Feature**: Stock Movements UI (007-stock-movements-ui)

## Overview

This document defines the frontend data model (TypeScript types and interfaces) used by the Stock Movements UI feature. The model aligns with the backend API contract defined in `/specs/006-stock-movements/contracts/stock-movement-api.md`.

---

## Core Domain Models

### Enums

#### MovementType
Defines the types of stock movements that can be recorded.

```typescript
export enum MovementType {
  OPENING_BALANCE = 'OPENING_BALANCE',
  STOCK_IN = 'STOCK_IN',
  STOCK_OUT = 'STOCK_OUT',
  ADJUSTMENT = 'ADJUSTMENT'
}
```

**Values**:
- `OPENING_BALANCE`: Initial quantity when item is created
- `STOCK_IN`: Inbound stock (e.g., supplier delivery)
- `STOCK_OUT`: Outbound stock (e.g., sales, disposal)
- `ADJUSTMENT`: Inventory discrepancy correction

#### AdjustmentDirection
Defines the direction of adjustment movements (increase or decrease).

```typescript
export enum AdjustmentDirection {
  INCREASE = 'INCREASE',
  DECREASE = 'DECREASE'
}
```

**Values**:
- `INCREASE`: Adjustment increases inventory
- `DECREASE`: Adjustment decreases inventory
- **Required only when**: `movementType === MovementType.ADJUSTMENT`

---

## API Models

### StockMovement (API Response)
Represents a single recorded stock movement retrieved from the backend.

```typescript
export interface StockMovement {
  id: number;                                 // Unique identifier assigned by backend
  itemId: number;                             // Reference to Item
  movementType: MovementType;                 // Type of movement
  quantity: number;                           // Number of units (always > 0)
  adjustmentDirection?: AdjustmentDirection;  // Required if movementType === 'ADJUSTMENT'
  reason?: string;                            // Optional notes (max 500 chars)
  movementDate: string;                       // ISO 8601 date YYYY-MM-DD (when business event occurred)
  createdDate: string;                        // ISO 8601 datetime (when record created in system)
  itemCurrentQuantity: number;                // Current quantity after this movement applied
}
```

**Field Constraints**:
- `id`: Unique, assigned by backend, read-only
- `quantity`: Positive integer > 0
- `reason`: Optional, max 500 characters
- `movementDate`: Any date (past/future allowed); defaults to today if omitted on request
- `createdDate`: Server-set timestamp, read-only
- `itemCurrentQuantity`: Reflects the state after this movement

### CreateStockMovementRequest (API Request)
Request body sent to backend when creating a new stock movement.

```typescript
export interface CreateStockMovementRequest {
  movementType: MovementType;                 // Required: type of movement
  quantity: number;                           // Required: units to move (> 0)
  reason?: string;                            // Optional: context for the movement
  movementDate?: string;                      // Optional: ISO 8601 YYYY-MM-DD; defaults to today
  adjustmentDirection?: AdjustmentDirection;  // Required only if movementType === 'ADJUSTMENT'
}
```

**Validation Rules** (enforced on frontend before submission):
- `movementType`: Must be a valid enum value
- `quantity`: Required, must be positive integer > 0
- `reason`: Optional, if provided must be ≤ 500 characters
- `movementDate`: Optional, if provided must be ISO 8601 format
- `adjustmentDirection`: Required if `movementType` is `ADJUSTMENT`; else should be omitted

### MovementHistoryQuery (API Query Parameters)
Parameters for filtering movement history from backend.

```typescript
export interface MovementHistoryQuery {
  itemId: number;          // Item ID to fetch history for
  startDate?: string;      // Optional: ISO 8601 YYYY-MM-DD; filter to movements on or after this date
  endDate?: string;        // Optional: ISO 8601 YYYY-MM-DD; filter to movements on or before this date
}
```

### ApiError (Error Response)
Standard error response format from backend.

```typescript
export interface ApiError {
  error: string;           // Human-readable error message
  timestamp: string;       // ISO 8601 datetime of error
  path: string;            // Request path that caused error
  details?: string;        // Optional details for complex errors
}
```

**Common Error Messages**:
- `"Quantity must be greater than 0"` — validation error
- `"Stock out of X units would make quantity negative (current: Y)"` — business rule violation
- `"Item with id NNN not found"` — resource not found
- `"adjustmentDirection is required for ADJUSTMENT movements"` — validation error

---

## Component State Models

### FormState
Manages form submission state and feedback.

```typescript
export interface FormState {
  isLoading: boolean;                         // True while submitting to backend
  error?: string;                             // Error message from backend or form validation
  success?: string;                           // Success message (optional)
  formData?: Partial<CreateStockMovementRequest>;  // Last submitted form data (for recovery)
}
```

**Usage**: Each movement form component manages its own FormState to track submission progress and errors.

### HistoryState
Manages movement history list and filtering state.

```typescript
export interface HistoryState {
  movements: StockMovement[];                 // List of movements for item
  isLoading: boolean;                         // True while fetching from backend
  error?: string;                             // Error message from backend
  filters: {
    startDate?: string;                       // ISO 8601 YYYY-MM-DD or null
    endDate?: string;                         // ISO 8601 YYYY-MM-DD or null
  };
  totalCount?: number;                        // Optional: total movements (before filtering)
}
```

**Usage**: Movement history modal component manages HistoryState to display, filter, and handle errors.

---

## Form Models (TypeScript FormGroup)

### StockInFormModel
Reactive form model for recording stock in movements.

```typescript
export interface StockInFormModel {
  quantity: number;                           // Required: positive integer
  reason?: string;                            // Optional: max 500 chars
  movementDate?: string;                      // Optional: ISO 8601 YYYY-MM-DD
}
```

### StockOutFormModel
Reactive form model for recording stock out movements.

```typescript
export interface StockOutFormModel {
  quantity: number;                           // Required: positive integer, ≤ currentQuantity
  reason?: string;                            // Optional: max 500 chars
  movementDate?: string;                      // Optional: ISO 8601 YYYY-MM-DD
}
```

### AdjustmentFormModel
Reactive form model for recording adjustment movements.

```typescript
export interface AdjustmentFormModel {
  quantity: number;                           // Required: positive integer
  adjustmentDirection: AdjustmentDirection;   // Required: 'INCREASE' or 'DECREASE'
  reason?: string;                            // Optional: max 500 chars
  movementDate?: string;                      // Optional: ISO 8601 YYYY-MM-DD
}
```

---

## Item Model (Existing, Enhanced)

The existing Item entity is enhanced to expose `currentQuantity` field that reflects all stock movements.

```typescript
export interface Item {
  id: number;                                 // Unique identifier
  name: string;                               // Item name
  currentQuantity: number;                    // Updated by stock movements (read-only in UI)
  category?: {
    id: number;
    name: string;
  };
  createdDate?: string;                       // ISO 8601 datetime
  updatedDate?: string;                       // ISO 8601 datetime (updated by movements)
}
```

**Note**: `currentQuantity` MUST NOT be edited directly by UI; it is updated exclusively through stock movement creation.

---

## Display Models (For UI Rendering)

### MovementDisplayItem
Formatted movement for display in history list (includes human-readable labels).

```typescript
export interface MovementDisplayItem {
  movement: StockMovement;                    // Raw backend movement
  typeLabel: string;                          // 'Opening Balance' | 'Stock In' | 'Stock Out' | 'Adjustment'
  directionLabel?: string;                    // 'Increase' | 'Decrease' (if applicable)
  movementDateFormatted: string;              // Localized date string
  createdDateFormatted: string;               // Localized datetime string
}
```

**Purpose**: Separates data model from presentation logic; UI templates reference these formatted strings instead of parsing enums.

---

## Validation Rules

### Quantity Validation
- **Constraint**: Must be positive integer > 0
- **Error**: "Quantity must be greater than 0"
- **Applied on**: Client-side (FormControl validator) + backend

### Reason Validation
- **Constraint**: Optional, max 500 characters
- **Error**: "Reason must not exceed 500 characters"
- **Applied on**: Client-side (FormControl validator) + backend

### Reason Validation
- **Constraint**: Optional, max 500 characters if provided
- **Error**: "Reason must not exceed 500 characters"
- **Applied on**: Client-side (FormControl validator) + backend

### Adjustment Direction Validation
- **Constraint**: Required if `movementType === 'ADJUSTMENT'`; must be 'INCREASE' or 'DECREASE'
- **Error**: "adjustmentDirection is required for ADJUSTMENT movements"
- **Applied on**: Client-side (FormControl validator) + backend

### Stock Out Quantity Validation
- **Constraint**: Must be ≤ current item quantity
- **Error**: "Stock out of X units would make quantity negative (current: Y)"
- **Applied on**: Backend (frontend should warn but backend is authoritative)

### Adjustment Decrease Validation
- **Constraint**: If `adjustmentDirection === 'DECREASE'`, result must not be negative
- **Error**: "Adjustment of X would make quantity negative (current: Y)"
- **Applied on**: Backend (frontend should warn but backend is authoritative)

### Movement Date Validation
- **Constraint**: Optional, if provided must be ISO 8601 format YYYY-MM-DD
- **Error**: "Invalid date format"
- **Applied on**: Client-side (date input type); backend validates format

---

## State Transitions

### Form Submission Flow

```
IDLE
  ↓ [User submits form]
LOADING
  ↓ [Backend responds]
SUCCESS (then auto-close, show notification)
  OR
ERROR (display message, remain open for correction)
  ↓ [User clicks submit again]
LOADING (retry)
```

### History Modal Flow

```
CLOSED
  ↓ [User clicks "View History"]
LOADING (fetch movements)
  ↓ [Movements loaded]
DISPLAYING
  ↓ [User applies date filter]
LOADING (fetch filtered movements)
  ↓ [Movements loaded]
DISPLAYING (filtered)
  ↓ [User closes modal]
CLOSED
```

---

## Integration Points with Backend

### POST /api/v1/items/{itemId}/movements

**Request**: `CreateStockMovementRequest`
```json
{
  "movementType": "STOCK_IN",
  "quantity": 50,
  "reason": "Supplier delivery",
  "movementDate": "2026-08-20"
}
```

**Response (201)**: `StockMovement`
```json
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

### GET /api/v1/items/{itemId}/movements?startDate=...&endDate=...

**Response (200)**: `StockMovement[]`
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
  }
]
```

### GET /api/v1/items/{itemId}

**Response (200)**: `Item` with enhanced currentQuantity
```json
{
  "id": 456,
  "name": "Widget",
  "currentQuantity": 150,
  "category": {
    "id": 10,
    "name": "Hardware"
  },
  "createdDate": "2026-07-01T10:00:00Z",
  "updatedDate": "2026-08-20T10:15:00Z"
}
```

---

## Design Notes

1. **Immutability**: StockMovement instances from backend are read-only; no UI edits or deletion
2. **Time zones**: All times are ISO 8601 UTC; browser locale used only for display formatting
3. **Null handling**: Optional fields (reason, adjustmentDirection) are null or omitted if not applicable
4. **Quantity always positive**: Backend enforces > 0; frontend prevents ≤ 0 at form level
5. **Last-write-wins**: No optimistic locking; if concurrent movements occur, backend determines final currentQuantity
