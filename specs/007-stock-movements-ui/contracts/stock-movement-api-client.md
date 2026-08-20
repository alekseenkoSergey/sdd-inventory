# Stock Movement API Client Contract

**Frontend API Client**: Angular HttpClient wrapper for Stock Movements API

**Backend API**: See `/specs/006-stock-movements/contracts/stock-movement-api.md`

---

## Service Interface: StockMovementService

Located at: `frontend/src/app/services/stock-movement.service.ts`

### Observable Streams

```typescript
// Loading state
public loading$: Observable<boolean>

// Error messages (or null if no error)
public error$: Observable<string | null>
```

### Methods

#### createMovement(itemId: number, request: CreateStockMovementRequest): Observable<StockMovement>

Creates a new stock movement for an item.

**Parameters**:
- `itemId`: Numeric ID of the item
- `request`: Movement details matching backend contract

**Returns**: Observable of `StockMovement` (the created movement with server-assigned ID)

**Side effects**:
- Sets `loading$` to `true` during request
- Clears `error$` on success
- Sets `error$` to error message on failure
- Sets `loading$` to `false` when complete

**Example**:
```typescript
this.stockMovementService.createMovement(456, {
  movementType: MovementType.STOCK_IN,
  quantity: 50,
  reason: 'Delivery from supplier',
  movementDate: '2026-08-20'
}).subscribe({
  next: (movement) => {
    console.log('Movement recorded:', movement);
    // Update UI, close form, show toast
  },
  error: (err) => {
    console.error('Failed to record movement:', err);
    // Error already in error$ stream
  }
});
```

#### getMovementHistory(itemId: number, filters?: MovementHistoryQuery): Observable<StockMovement[]>

Retrieves all movements for an item, optionally filtered by date range.

**Parameters**:
- `itemId`: Numeric ID of the item
- `filters` (optional): Date filters
  - `startDate`: ISO 8601 YYYY-MM-DD (movements on or after)
  - `endDate`: ISO 8601 YYYY-MM-DD (movements on or before)

**Returns**: Observable of `StockMovement[]` (array of movements in chronological order)

**Side effects**:
- Sets `loading$` to `true` during request
- Sets `loading$` to `false` when complete
- Propagates errors via `error$`

**Example**:
```typescript
this.stockMovementService.getMovementHistory(456, {
  startDate: '2026-08-01',
  endDate: '2026-08-31'
}).subscribe({
  next: (movements) => {
    console.log('Movements:', movements);
    // Update history list
  },
  error: (err) => {
    console.error('Failed to load history:', err);
  }
});
```

---

## Request Contracts

### POST /api/v1/items/{itemId}/movements

**Frontend Request Builder**:
```typescript
const request: CreateStockMovementRequest = {
  movementType: MovementType.STOCK_IN,
  quantity: 50,
  reason: 'Optional notes',
  movementDate: '2026-08-20', // ISO 8601 YYYY-MM-DD, optional
  adjustmentDirection: undefined // Only for ADJUSTMENT type
};
```

**HTTP Details**:
- Method: POST
- URL: `/api/v1/items/{itemId}/movements`
- Headers: `Content-Type: application/json`, `Authorization: Bearer [token]`
- Body: JSON-serialized request

### GET /api/v1/items/{itemId}/movements

**Frontend Request Builder**:
```typescript
// No date filters
const url = '/api/v1/items/456/movements';

// With date filters
const url = '/api/v1/items/456/movements?startDate=2026-08-01&endDate=2026-08-31';
```

**HTTP Details**:
- Method: GET
- URL: `/api/v1/items/{itemId}/movements[?startDate=...&endDate=...]`
- Headers: `Authorization: Bearer [token]`

---

## Response Contracts

### Success Response (201 Created)

```typescript
interface StockMovement {
  id: number;                                 // Assigned by backend
  itemId: number;
  movementType: MovementType;                 // Echoed from request
  quantity: number;                           // Echoed from request
  adjustmentDirection?: AdjustmentDirection;  // Echoed from request or null
  reason?: string;                            // Echoed from request or null
  movementDate: string;                       // ISO 8601 YYYY-MM-DD
  createdDate: string;                        // ISO 8601 datetime (server-set)
  itemCurrentQuantity: number;                // Updated quantity after movement
}
```

### Success Response (200 OK) — History List

```typescript
StockMovement[]  // Array of movements, ordered oldest first
```

### Error Response (400 Bad Request)

```typescript
{
  error: "Quantity must be greater than 0",
  timestamp: "2026-08-20T14:30:15Z",
  path: "/api/v1/items/456/movements"
}
```

**Common error messages** (frontend should handle these gracefully):
- `"Quantity must be greater than 0"`
- `"Stock out of X units would make quantity negative (current: Y)"`
- `"Adjustment of X would make quantity negative (current: Y)"`
- `"adjustmentDirection is required for ADJUSTMENT movements"`
- `"Item with id NNN not found"`

### Error Response (404 Not Found)

```typescript
{
  error: "Item with id 999 not found",
  timestamp: "2026-08-20T14:30:15Z",
  path: "/api/v1/items/999/movements"
}
```

### Error Response (500 Server Error)

```typescript
{
  error: "Internal server error",
  timestamp: "2026-08-20T14:30:15Z",
  path: "/api/v1/items/456/movements"
}
```

---

## Error Handling Strategy

### Component-Level Pattern

```typescript
export class StockInFormComponent {
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  constructor(private svc: StockMovementService) {
    this.loading$ = this.svc.loading$;
    this.error$ = this.svc.error$;
  }

  onSubmit(form: FormGroup) {
    this.svc.createMovement(this.itemId, form.value).subscribe({
      next: (movement) => {
        // Success: close form, show toast, update quantity
        this.formSubmitted.emit();
      },
      error: (err) => {
        // Error already exposed via error$ stream; template displays it
        // No additional action needed
      }
    });
  }
}
```

### Template Pattern

```html
<!-- Display error from service -->
<div *ngIf="error$ | async as error" class="alert alert-error">
  {{ error }}
</div>

<!-- Show loading state -->
<button [disabled]="loading$ | async">
  {{ (loading$ | async) ? 'Saving...' : 'Submit' }}
</button>
```

---

## Timeout & Retry Strategy

**Default Behavior** (no automatic retry):
- Form submission waits for backend response indefinitely
- UI disables button and shows loading indicator
- On timeout (browser default ~30 seconds), error is shown to user
- User can click Submit again to retry

**Optional Enhancement** (deferred to tasks phase):
- Implement exponential backoff retry for network errors (not validation errors)
- Show "Retrying..." message after delay
- Max 3 retries before failing

---

## Validation Layers

1. **Frontend Form Validation** (before API call):
   - Quantity: required, positive integer > 0
   - Reason: max 500 characters
   - Movement Date: valid ISO format if provided
   - Direction: required if type is ADJUSTMENT

2. **Backend Validation** (API response):
   - Same validations as frontend
   - Additional: quantity bounds (stock out ≤ current, adjustment ≤ current)
   - Authorization: user must be authenticated

3. **Frontend Error Display**:
   - Backend validation errors shown in form as inline alerts
   - Network/timeout errors shown as toasts
   - User can correct and resubmit

