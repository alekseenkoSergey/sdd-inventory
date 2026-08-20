# Implementation Plan: Stock Movements UI

**Branch**: `007-stock-movements-ui` | **Date**: 2026-08-20 | **Spec**: [spec.md](./spec.md)

**Input**: Frontend specification for recording and viewing stock movements via Angular UI consuming backend Stock Movements API

## Summary

The Stock Movements UI feature provides a set of Angular components and services to allow authenticated users to:
1. Record stock movements (stock in, stock out, adjustment) for inventory items
2. View movement history with date-range filtering
3. Display current stock quantity reflecting all movements

The frontend integrates with the existing backend Stock Movements API (implemented in feature 006-stock-movements) and follows the project constitution's Angular + Spring Boot architecture. Forms auto-close after successful submission with toast notifications, and movement history is accessed via modal dialogs from the item detail page.

## Technical Context

**Language/Version**: TypeScript 5+ / Angular 22.1.0

**Primary Dependencies**: Angular core, Angular Forms (reactive forms), Angular Material or Bootstrap (for UI components), RxJS for state management

**Storage**: N/A — frontend only; state managed in memory and persisted via backend API calls

**Testing**: Jasmine/Karma for unit tests, Angular testing utilities

**Target Platform**: Web browser (desktop, tablet, mobile responsive)

**Project Type**: Single-page application (SPA) / web-service UI

**Performance Goals**: Form submission completes and UI updates within 2 seconds (SC-001); typical workflow completes in under 1 minute (SC-006)

**Constraints**: Mobile-responsive design (SC-007); graceful API error handling (FR-013)

**Scale/Scope**: Modest — 4 primary UI components (item detail view, 3 movement forms, history modal); integrated into existing item management pages

## Constitution Check

*GATE: Must pass before Phase 1 design. Re-check after Phase 1 design.*

**Principle II - Technology Stack**: ✅ PASS
- Using Angular (specified in constitution) for frontend
- No new frameworks or infrastructure being introduced
- Conforming to existing project's SPA patterns

**Principle I - Simplicity First**: ✅ PASS
- Implementing only specified requirements: movement forms, history view, current quantity display
- Reusing existing Angular framework capabilities
- No custom state management framework; using Angular forms and services

**Constitution applicability**: Constitution focuses on backend architecture (Java, Spring Boot, PostgreSQL). Frontend UI is outside the backend layered-package-structure scope, but principles of simplicity apply. No violations.

**Re-evaluation required**: After Phase 1 (design), confirm no backend changes needed.

## Project Structure

### Documentation (this feature)

```text
specs/007-stock-movements-ui/
├── spec.md              # Feature specification (COMPLETE)
├── plan.md              # This file (IN PROGRESS)
├── research.md          # Phase 0 output (TO GENERATE)
├── data-model.md        # Phase 1 output (TO GENERATE)
├── quickstart.md        # Phase 1 output (TO GENERATE)
├── contracts/           # Phase 1 output (TO GENERATE)
└── checklists/
    └── requirements.md  # Quality checklist
```

### Source Code (repository structure)

```text
frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── item-detail/              # EXISTING - to be extended
│   │   │   ├── stock-movements/          # NEW - movement-related components
│   │   │   │   ├── movement-form/        # Shared or movement-type-specific forms
│   │   │   │   ├── movement-history-modal/  # History modal component
│   │   │   │   └── shared/               # Shared utilities for forms
│   │   │   └── ...
│   │   ├── services/
│   │   │   ├── stock-movement.service.ts # NEW - API client for movements
│   │   │   └── ...
│   │   └── models/
│   │       ├── stock-movement.model.ts   # NEW - TypeScript interfaces
│   │       └── ...
│   └── assets/
└── tests/
    ├── unit/
    │   └── stock-movements/              # NEW - unit tests for components/services
    └── e2e/                              # NEW - e2e tests for workflows
```

**Structure Decision**: Web application (SPA) structure — Angular frontend + Spring Boot backend (separation already in place). Stock Movements UI is a frontend-only feature with corresponding backend API already implemented. All new code goes under `frontend/src/app/` in a new `stock-movements/` module directory for organization.

## Complexity Tracking

No violations detected. Stock Movements UI follows the existing Angular + Spring Boot architecture prescribed by the constitution. The feature is a straightforward integration of existing technologies without introducing new frameworks or patterns.

---

## Phase 0: Research & Unknowns Resolution

### Key Questions to Resolve

1. **Form state management**: Should forms use reactive forms (FormGroup) or template-driven forms?
   - **Resolution**: Reactive forms (more explicit, better for complex validation and async operations)
   - **Rationale**: Needed for real-time validation, async validation from backend, and easier testing

2. **Error handling & toast notifications**: What Angular Material or Bootstrap component library should be used?
   - **Resolution**: Use the existing component library already in the project (check package.json for @angular/material or ng-bootstrap)
   - **Rationale**: Maintain consistency with existing UI patterns

3. **API service pattern**: Should the stock movement service be a singleton injectable or instantiated per component?
   - **Resolution**: Singleton injectable service (Angular best practice)
   - **Rationale**: Allows centralized API management, caching, and logging

4. **Date handling**: How should movement dates be formatted for display and submission?
   - **Resolution**: Use ISO 8601 format for API (backend contract), display in locale-aware format via Angular's date pipe
   - **Rationale**: Matches backend contract, separates display from storage format

5. **Loading & error states**: Should loading and error states be managed at component or service level?
   - **Resolution**: Service level using RxJS observables (loading$, error$ streams)
   - **Rationale**: Components consume observables via async pipe; reactive and clean separation of concerns

### Generated Research Artifacts

**Decision Log** (document as part of this plan):
- Form implementation: Reactive forms with FormGroup, FormControl, and custom validators
- UI components: Use existing Material/Bootstrap in project; defer specific component choice to tasks phase
- API service: Singleton StockMovementService with dependency injection
- Date format: ISO 8601 for API, locale-aware display pipe
- State management: RxJS observables in services; async pipe in templates

---

## Phase 1: Design & Contracts

### Data Model

**Frontend Models** (`frontend/src/app/models/stock-movement.model.ts`)

```typescript
// API Request/Response types matching backend contract

interface StockMovement {
  id: number;
  itemId: number;
  movementType: MovementType;
  quantity: number;
  adjustmentDirection?: AdjustmentDirection;
  reason?: string;
  movementDate: string; // ISO 8601 YYYY-MM-DD
  createdDate: string;  // ISO 8601 datetime
  itemCurrentQuantity: number;
}

enum MovementType {
  OPENING_BALANCE = 'OPENING_BALANCE',
  STOCK_IN = 'STOCK_IN',
  STOCK_OUT = 'STOCK_OUT',
  ADJUSTMENT = 'ADJUSTMENT'
}

enum AdjustmentDirection {
  INCREASE = 'INCREASE',
  DECREASE = 'DECREASE'
}

interface CreateStockMovementRequest {
  movementType: MovementType;
  quantity: number;
  reason?: string;
  movementDate?: string; // optional; defaults to today on backend
  adjustmentDirection?: AdjustmentDirection; // required if ADJUSTMENT
}

interface MovementHistoryQuery {
  itemId: number;
  startDate?: string; // ISO 8601
  endDate?: string;   // ISO 8601
}

interface ApiError {
  error: string;
  timestamp: string;
  path: string;
  details?: string;
}
```

**Component State Models**

```typescript
interface FormState {
  isLoading: boolean;
  error?: string;
  success?: string;
  formData?: Partial<CreateStockMovementRequest>;
}

interface HistoryState {
  movements: StockMovement[];
  isLoading: boolean;
  error?: string;
  filters: {
    startDate?: string;
    endDate?: string;
  };
}
```

### Component Architecture

**1. Item Detail View Enhancement**
- Display current quantity prominently (from Item entity)
- Add three action buttons: "Record Stock In", "Record Stock Out", "Record Adjustment"
- Add "View Movement History" button
- Trigger form modals or show movement history modal on button clicks

**2. Movement Form Component** (reusable or specialized)
- Reactive form with fields: quantity, reason, movement date
- Stock In: only above fields
- Stock Out: only above fields (quantity field has max validation based on current quantity)
- Adjustment: above fields + adjustment direction (radio/dropdown)
- Validation:
  - Quantity: required, must be positive integer > 0
  - Reason: optional, max 500 characters
  - Movement date: optional, any date (past/future allowed)
  - Adjustment direction: required if movement type is ADJUSTMENT
- On submit:
  - Disable submit button, show loading indicator
  - Call backend API
  - On success: close form (emit close event), show toast notification
  - On error: display error message in form, re-enable submit button

**3. Movement History Modal Component**
- Opens when user clicks "View History" button
- Displays list of all movements in chronological order (oldest first)
- Each movement shows: type, quantity, direction (if applicable), reason, movement date, created date
- Include date range filters (start date, end date) with apply/reset buttons
- Show "No movements recorded" if list is empty or after filtering returns no results
- Close button and clicking outside modal closes it

### Contracts (API Integration)

**Backend API Contract Reference**: `/specs/006-stock-movements/contracts/stock-movement-api.md`

**Frontend Integration Points**:

```
POST /api/v1/items/{itemId}/movements
  Request body: CreateStockMovementRequest
  Response: StockMovement (201 Created)
  Errors: 400 Bad Request (validation), 404 Not Found, 500 Server Error

GET /api/v1/items/{itemId}/movements?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
  Response: StockMovement[] (200 OK)
  Errors: 404 Not Found, 500 Server Error

GET /api/v1/items/{itemId}
  Response: Item with currentQuantity field (200 OK)
  Errors: 404 Not Found, 500 Server Error
```

**Contract Validation**:
- All request dates must be ISO 8601 format
- Quantity must be positive integer > 0
- Movement type must be one of: OPENING_BALANCE, STOCK_IN, STOCK_OUT, ADJUSTMENT
- Adjustment direction must be provided if movement type is ADJUSTMENT

### Service Architecture

**StockMovementService** (`frontend/src/app/services/stock-movement.service.ts`)

```typescript
@Injectable({ providedIn: 'root' })
export class StockMovementService {
  private apiUrl = '/api/v1';

  // Observable streams for state
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  public error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Create stock movement
  createMovement(itemId: number, request: CreateStockMovementRequest): Observable<StockMovement> {
    this.loadingSubject.next(true);
    return this.http.post<StockMovement>(`${this.apiUrl}/items/${itemId}/movements`, request)
      .pipe(
        finalize(() => this.loadingSubject.next(false)),
        tap(() => this.errorSubject.next(null)),
        catchError(error => this.handleError(error))
      );
  }

  // Get movement history
  getMovementHistory(itemId: number, filters?: MovementHistoryQuery): Observable<StockMovement[]> {
    this.loadingSubject.next(true);
    let params = new HttpParams();
    if (filters?.startDate) params = params.set('startDate', filters.startDate);
    if (filters?.endDate) params = params.set('endDate', filters.endDate);

    return this.http.get<StockMovement[]>(`${this.apiUrl}/items/${itemId}/movements`, { params })
      .pipe(
        finalize(() => this.loadingSubject.next(false)),
        catchError(error => this.handleError(error))
      );
  }

  private handleError(error: any): Observable<never> {
    const errorMessage = error?.error?.error || 'An error occurred';
    this.errorSubject.next(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
```

### Quickstart Validation Guide

**Goal**: Demonstrate that the Stock Movements UI feature works end-to-end.

**Prerequisites**:
- Backend Stock Movements API running at `http://localhost:8080` (feature 006-stock-movements)
- Frontend Angular dev server running at `http://localhost:4200`
- Browser with developer tools or test harness

**Setup**:
1. Ensure backend is running: `cd backend && mvn spring-boot:run`
2. Ensure frontend is running: `cd frontend && npm start`
3. Navigate to an item detail page in the UI

**Test Scenario 1: Record Stock In**
1. On item detail page, click "Record Stock In" button
2. Form modal appears with fields: Quantity, Reason, Movement Date
3. Enter quantity: 50, reason: "Test delivery", movement date: today
4. Click "Submit"
5. Form closes, success toast appears (e.g., "Stock movement recorded successfully")
6. Current quantity on item detail page increases by 50
7. Click "View Movement History" button
8. Modal opens and shows new movement with type "STOCK_IN", quantity 50, reason "Test delivery"

**Test Scenario 2: Record Stock Out (invalid quantity)**
1. On item detail page, click "Record Stock Out" button
2. Form modal appears
3. Enter quantity: 9999 (exceeds current stock)
4. Click "Submit"
5. Error message appears in form: "Stock out of 9999 units would make quantity negative (current: X)"
6. Form remains open, user can correct quantity or close

**Test Scenario 3: Record Adjustment with Direction**
1. On item detail page, click "Record Adjustment" button
2. Form modal appears with additional "Direction" field (radio buttons: Increase/Decrease)
3. Select "Increase", enter quantity: 10
4. Click "Submit"
5. Form closes, success notification appears
6. Current quantity increases by 10
7. In history modal, verify adjustment shows direction "INCREASE"

**Test Scenario 4: Filter Movement History by Date**
1. Click "View Movement History" button
2. Enter start date: 2026-08-01, end date: 2026-08-15
3. Click "Apply Filter"
4. Only movements within date range are displayed
5. Clear filters and verify all movements return

**Expected Outcomes**:
- All movements recorded via UI are persisted in backend
- Current quantity reflects cumulative movements
- Movement history displays all movements in order (oldest first)
- Date filters work correctly
- Error messages from backend are displayed to user
- Forms auto-close on success and display notifications

---

## Next Phase

Phase 2 (not executed by `/speckit-plan`) will run `/speckit-tasks` to generate actionable development tasks based on this plan. Output will be `tasks.md` with dependencies, effort estimates, and implementation order.
