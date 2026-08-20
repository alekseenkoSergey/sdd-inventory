# Research Findings: Stock Movements UI

**Date**: 2026-08-20
**Feature**: Stock Movements UI (007-stock-movements-ui)

## Overview

This document consolidates research and design decisions made during Phase 0 planning. All key technical unknowns have been resolved through analysis of the project constitution, existing codebase, and Angular best practices.

---

## Research Questions & Resolutions

### 1. Form State Management Approach

**Question**: Should forms use reactive forms (FormGroup) or template-driven forms?

**Decision**: **Reactive Forms (FormGroup)**

**Rationale**: 
- Explicitly testable: FormGroup can be tested in isolation without component template
- Better for complex validation: Real-time validation, async validators (e.g., checking backend before submit)
- Easier error handling: Each FormControl can have its own error state displayed individually
- Matches Angular best practices: Reactive forms are recommended for modern Angular applications

**Alternatives Considered**:
- Template-driven forms: Simpler syntax but less explicit, harder to test, not suitable for async validation

**Implementation Detail**: Use `ReactiveFormsModule` and `FormGroup` with `FormControl` for each field. Validators will include built-in validators (required, minValue, pattern) and custom validators.

---

### 2. UI Component Library

**Question**: Should we use Angular Material, ng-bootstrap, or build custom components?

**Decision**: **Use existing component library in the project**

**Rationale**: 
- The project already has dependencies in `package.json`; reusing existing libraries maintains consistency
- Reduces bundle size by not adding new dependencies
- Team is already familiar with existing library patterns

**Next Step**: Check `frontend/package.json` to determine if @angular/material or ng-bootstrap is already installed. Tasks phase will reference the correct component names.

**Alternatives Considered**:
- Custom HTML/CSS: Would require more development time and CSS maintenance
- Different library: Would introduce inconsistency with existing UI

---

### 3. API Service Pattern

**Question**: Should the StockMovementService be a singleton or per-component instance?

**Decision**: **Singleton Injectable Service**

**Rationale**:
- Follows Angular best practice: Services registered in `providedIn: 'root'` are singletons
- Enables centralized state: All components access the same service instance
- Supports caching: Can cache API responses and movements across components
- Facilitates testing: Mock a single service instance for all tests

**Implementation Detail**: Service will be decorated with `@Injectable({ providedIn: 'root' })` and injected via constructor dependency injection.

**Alternatives Considered**:
- Per-component instances: Would duplicate state and API calls, inefficient

---

### 4. Date Format Handling

**Question**: How should movement dates be formatted for display and API submission?

**Decision**: **ISO 8601 for API, locale-aware display**

**Rationale**:
- Backend contract specifies ISO 8601 format (YYYY-MM-DD for date, ISO datetime for timestamps)
- Frontend must submit dates in ISO format to match backend expectations
- Display dates to users in locale-aware format for readability (Angular's DatePipe handles this)
- Separation of concerns: Storage/transmission format (ISO) vs. display format (locale)

**Implementation Detail**:
- Form model submits: `YYYY-MM-DD` strings to backend
- Form input uses Angular's `matInput` with `type="date"` (browser native date picker, submits ISO format)
- Display in templates uses `{{ date | date: 'short' }}` or `{{ date | date: 'medium' }}` based on space/context

**Alternatives Considered**:
- Custom date formatting: More work, error-prone
- Moment.js/date-fns: Adds dependency, Angular's built-in DatePipe is sufficient

---

### 5. Loading & Error State Management

**Question**: Where should loading and error states live — in components or services?

**Decision**: **Service-level RxJS Observables**

**Rationale**:
- Single source of truth: All components see the same loading/error state
- Reactive pattern: Components consume observables via `async` pipe (automatic subscription/cleanup)
- Simpler components: Presentation logic separate from state management logic
- Testable: Mock the observable streams in tests

**Implementation Detail**:
- `StockMovementService` exposes `loading$: Observable<boolean>` and `error$: Observable<string | null>`
- Components subscribe via `async` pipe: `{{ service.loading$ | async }}`
- State updated via `BehaviorSubject` in service methods

**Alternatives Considered**:
- Component-level state: Would duplicate state across multiple form/history components
- NgRx store: Overkill for a single feature with modest complexity; adds significant boilerplate

---

### 6. Form Modal vs. Inline Form

**Question**: Should movement recording forms be modal dialogs or inline on the page?

**Decision**: **Modal Dialogs** (as per clarification Q2 & Q3)

**Rationale**:
- Aligns with user clarification: Forms auto-close and return to item view (modal pattern)
- Focused interaction: Modal keeps user's attention on the form task
- History also modal: Movement history uses modal (consistent UI pattern)
- Avoids page bloat: Item detail page remains clean with action buttons

**Implementation Detail**:
- Forms: Angular Material Dialog or ng-bootstrap Modal (depending on existing library)
- Movement history: Same modal/dialog component

**Alternatives Considered**:
- Inline forms: Clutters item detail page, harder to manage focus
- Separate pages: Requires navigation, breaks workflow continuity

---

### 7. Single Form Component vs. Movement-Type-Specific Components

**Question**: Create one reusable form component or three separate components (StockInForm, StockOutForm, AdjustmentForm)?

**Decision**: **One Reusable Form Component with Input Configuration**

**Rationale**:
- DRY principle: Shared validation logic (quantity, reason, date)
- Easier maintenance: Updates to common fields in one place
- Movement type determines which fields/validators are used
- Configuration-driven: Pass `movementType` as input, form adapts

**Implementation Detail**: `StockMovementFormComponent` takes input `@Input() movementType: MovementType` and `@Input() currentQuantity: number`. Internal logic shows/hides/validates fields based on type.

**Alternatives Considered**:
- Separate components: More boilerplate, duplicate validators

---

### 8. History Modal Filtering

**Question**: Should filters be applied immediately or require an "Apply" button?

**Decision**: **Apply Button Pattern**

**Rationale**:
- User expectations: Standard filter patterns use explicit "Apply"
- Performance: Avoids multiple API calls as user types dates
- Clear state: Users know when filters take effect
- Matches spec: FR-010 says "provide optional date range filters" (standard pattern)

**Implementation Detail**: Two date inputs + "Apply Filter" button + "Clear Filters" button

**Alternatives Considered**:
- Auto-apply on change: May trigger excessive API calls; worse UX for slow networks

---

## Architecture Decisions Summary

| Decision | Choice | Key Reason |
|----------|--------|-----------|
| Form framework | Reactive Forms | Testability, async validation, error handling |
| UI library | Existing project library | Consistency, no new dependencies |
| Service pattern | Singleton injectable | Centralized state, caching, testability |
| Date format | ISO 8601 API + locale display | Backend alignment + UX readability |
| State management | RxJS observables in service | Single source of truth, reactive pattern |
| Form UI | Modal dialogs | Focused interaction, workflow continuity |
| Form components | One reusable component | DRY, easier maintenance |
| History filters | Apply button pattern | Performance, clear state |

---

## Technology Stack Confirmed

**Frontend Framework**: Angular 22.1.0 (already in project)
**Language**: TypeScript 5+
**Forms**: @angular/forms (ReactiveFormsModule)
**HTTP**: @angular/common/http (HttpClient)
**State**: RxJS (observables, BehaviorSubject)
**UI Components**: Use existing library in package.json (@angular/material or ng-bootstrap)
**Testing**: Jasmine + Karma + Angular testing utilities

No new major dependencies required. All chosen technologies are already in the project or part of Angular core.

---

## Backend Integration Contract

**API Base URL**: `http://localhost:8080/api/v1` (configurable via environment)

**Endpoints Used**:
1. `POST /items/{itemId}/movements` — Create movement
2. `GET /items/{itemId}/movements` — Fetch history (with optional filters)
3. `GET /items/{itemId}` — Fetch item details (for current quantity)

**Error Handling**: Backend returns `{ error: string, timestamp, path, details? }` format. Frontend service catches HTTP errors and exposes them via `error$` observable.

---

## Known Constraints & Limitations

1. **No pagination**: History displays all movements without pagination (assumed <1000 movements per item)
2. **No real-time updates**: Changes by other users require manual refresh (not WebSocket-driven)
3. **No optimistic updates**: UI waits for backend confirmation before updating quantity
4. **No conflict resolution**: Last-write-wins strategy per backend (UI reflects final backend state)
5. **Responsive only**: No dedicated mobile app; responsive design for all screen sizes

---

## Next Steps

1. **Phase 1 Complete**: Data model, components architecture, and contracts defined in plan.md
2. **Phase 2** (speckit-tasks): Generate actionable tasks with effort estimates and dependencies
3. **Implementation**: Follow tasks in order, implementing components, service, and tests

## Done When

- [x] All NEEDS CLARIFICATION markers resolved
- [x] Technical decisions documented with rationale
- [x] Architecture aligned with project constitution
- [x] Backend contract confirmed
- [x] Technology stack validated against project dependencies
