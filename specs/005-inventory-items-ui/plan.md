# Implementation Plan: Inventory Items User Interface

**Branch**: `005-inventory-items-ui` | **Date**: 2026-08-20 | **Spec**: [spec.md](./spec.md)

**Input**: Frontend specification for inventory items management with full CRUD operations, status management, filtering, and pagination.

## Summary

Build a comprehensive Angular-based UI for managing inventory items. The feature enables warehouse managers to create, read, update, archive, restore, and delete inventory items. The UI integrates with the existing REST API (inventory-items-api.md contract) and enforces user data isolation. Key technical approach: modular component architecture with separate presentation, container, and service layers; reactive forms with client-side validation; paginated list view with status/category filtering; modal-based create/edit workflows.

## Technical Context

**Language/Version**: TypeScript 6.0.2 targeting Angular 22.1.0 / Node.js

**Primary Dependencies**: 
- Angular 22.1.0 (core framework)
- Angular Forms (reactive forms for validation)
- Angular Common (HTTP client for API calls)
- RxJS 7.8.0 (reactive programming)
- Angular Router (navigation between views)

**Storage**: PostgreSQL (backend; frontend uses in-memory state + HTTP caching)

**Testing**: Vitest 4.0.8 + jsdom for unit and component testing

**Target Platform**: Modern browsers (Chrome 120+, Firefox 115+, Safari 17+) on desktop and tablet (iPad-like)

**Project Type**: Web application (Single Page Application / SPA) with Angular frontend + existing Java/Spring Boot backend

**Performance Goals**: 
- Create item form submission: <60 seconds user task time
- List view load (20 items): <2 seconds with API response
- Edit save: <1 second from click to confirmation
- Archive/restore: <1 second response time
- Pagination and filter: <2 seconds per interaction

**Constraints**: 
- No external package integrations without justification (Constitution Principle I: Simplicity First)
- Must use Angular's built-in capabilities (HttpClient, forms, routing) rather than custom wrappers
- Client-side validation must mirror server-side validation rules for UX responsiveness
- Must NOT directly access backend repositories; all data flows through the REST API
- Mobile optimization deferred to future version

**Scale/Scope**: 
- Support up to 1000+ items per user in paginated view
- 20 items per page (matching backend API default)
- 6 user stories (P1: create, view, archive, list; P2: delete, reorganize)
- ~8-12 Angular components expected

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle I: Simplicity First ✓
- Feature uses only Angular's built-in capabilities (forms, HTTP, routing, services)
- No custom abstraction layers or framework wrappers introduced
- Reactive forms pattern follows Angular conventions
- Service layer provides straightforward data access without repository patterns

**Status**: ✅ PASS — Design remains simple, uses framework defaults

### Principle II: Technology Stack ✓
- Frontend: Angular (mandated, version 22.1.0 confirmed)
- Backend: Java/Spring Boot (existing, confirmed via API contract)
- Database: PostgreSQL (backend concern, confirmed)
- Database migrations: Flyway (backend concern, confirmed)
- Persistence: Spring Data (backend concern, confirmed)

**Status**: ✅ PASS — Stack alignment verified

### Principle III: Backend Layered Package Structure ✓
- **Not directly applicable to frontend feature**, but backend API follows this (verified in inventory-items-api.md)
- Frontend respects backend layer boundaries: does not bypass REST API
- No frontend replication of backend business logic

**Status**: ✅ PASS — Frontend respects backend boundaries

### Principle IV: Centralized Exception Processing ✓
- **Backend concern** (centralized @ControllerAdvice documented in API contract)
- Frontend implements centralized error handling in interceptor/service layer for API responses
- No scattered try/catch blocks in components

**Status**: ✅ PASS — Frontend will implement consistent error handling strategy

---

## Project Structure

### Documentation (this feature)

```text
specs/005-inventory-items-ui/
├── spec.md                    # Feature specification
├── plan.md                    # This file (implementation plan)
├── research.md                # Phase 0 output (design decisions, patterns)
├── data-model.md              # Phase 1 output (entity/DTO structures)
├── quickstart.md              # Phase 1 output (validation scenarios)
├── contracts/                 # Phase 1 output (API endpoint contracts)
│   └── inventory-items-api.md # Reference to backend contract
└── tasks.md                   # Phase 2 output (actionable task list)
```

### Source Code (repository)

#### Frontend Directory Structure

```text
frontend/
├── src/
│   ├── app/
│   │   ├── core/                           # Existing: Singleton services & HTTP
│   │   │   ├── http/
│   │   │   │   └── api.service.ts         # (existing) Base API client - extend for /v1/inventory-items
│   │   │   └── (auth, models, etc. - existing)
│   │   │
│   │   ├── auth/                           # Existing: Auth & interceptors
│   │   │   ├── interceptors/
│   │   │   │   └── auth.interceptor.ts    # (existing) Auth token injection
│   │   │   └── (services, guards, components - existing)
│   │   │
│   │   ├── categories/                     # Existing feature (reference pattern)
│   │   │   ├── models/
│   │   │   ├── components/
│   │   │   ├── pages/
│   │   │   └── services/
│   │   │
│   │   ├── features/                       # Existing: locations
│   │   │   └── locations/
│   │   │       ├── (models, components, pages, services)
│   │   │       └── locations.module.ts
│   │   │
│   │   ├── inventory-items/                # NEW FEATURE (follows categories pattern)
│   │   │   ├── models/
│   │   │   │   └── inventory-item.model.ts
│   │   │   │
│   │   │   ├── components/                 # Presentational (dumb) components
│   │   │   │   ├── item-list/
│   │   │   │   │   ├── item-list.component.ts
│   │   │   │   │   └── item-list.component.spec.ts
│   │   │   │   ├── item-form/
│   │   │   │   │   ├── item-form.component.ts
│   │   │   │   │   └── item-form.component.spec.ts
│   │   │   │   ├── item-detail/
│   │   │   │   │   ├── item-detail.component.ts
│   │   │   │   │   └── item-detail.component.spec.ts
│   │   │   │   └── filter-toolbar/
│   │   │   │       ├── filter-toolbar.component.ts
│   │   │   │       └── filter-toolbar.component.spec.ts
│   │   │   │
│   │   │   ├── pages/                     # Container/page components (smart)
│   │   │   │   ├── inventory-items-page/
│   │   │   │   │   ├── inventory-items-page.component.ts
│   │   │   │   │   └── inventory-items-page.component.spec.ts
│   │   │   │   └── item-detail-page/
│   │   │   │       ├── item-detail-page.component.ts
│   │   │   │       └── item-detail-page.component.spec.ts
│   │   │   │
│   │   │   ├── services/
│   │   │   │   ├── inventory-items.service.ts
│   │   │   │   └── inventory-items.service.spec.ts
│   │   │   │
│   │   │   └── inventory-items.module.ts  # Feature module (if not standalone)
│   │   │
│   │   ├── app.component.ts
│   │   ├── app.routes.ts
│   │   └── app.config.ts
│   │
│   ├── main.ts
│   ├── styles.css
│   └── index.html
│
├── package.json
├── angular.json
├── tsconfig.json
└── README.md
```

#### Backend Directory Structure (reference)

```text
backend/
├── src/main/java/com/inventory/
│   ├── api/
│   │   └── controller/
│   │       └── InventoryItemController.java
│   ├── model/
│   │   ├── InventoryItemResponseDTO.java
│   │   └── InventoryItemCreateRequestDTO.java
│   ├── entity/
│   │   └── InventoryItem.java
│   ├── service/
│   │   └── InventoryItemService.java
│   └── repository/
│       └── InventoryItemRepository.java
│
└── src/test/java/
    └── com/inventory/
        ├── integration/
        └── unit/
```

**Structure Decision**: 

Use **Option 2: Web Application** with separated frontend and backend. 

**Frontend Feature Structure** (aligns with existing patterns):
- Place `inventory-items/` at the top level of `/src/app/` (alongside `categories/`, `features/`, `auth/`)
- Follow the **categories pattern** used by existing categories feature:
  - Subdirectories: `models/`, `components/`, `pages/`, `services/`
  - **pages/** contains "container" or "smart" components (manage state, call APIs)
  - **components/** contains "presentational" or "dumb" components (receive @Input, emit @Output)
- **Reuse existing core services**:
  - `core/http/api.service.ts` — already provides base HTTP client; extend with `/v1/inventory-items` endpoints
  - `auth/interceptors/auth.interceptor.ts` — already handles auth token injection
- **No separate shared module** — existing approach: reuse components as needed, duplicate utility files if necessary (follows Constitution Principle I: Simplicity)

This structure aligns with existing frontend patterns (categories, locations) and the Constitution's simplicity principle by using framework conventions rather than creating new abstractions.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations detected. Constitution Check passed all gates ✅

---

## Phase 0: Research (Output: research.md)

No unresolved NEEDS CLARIFICATION markers in specification. All technical context is determined. Research phase focuses on documenting design decisions and best practices.

### Research Topics

1. **Angular reactive forms best practices** for inventory items CRUD
2. **Paginated list view patterns** in Angular with RxJS
3. **Modal dialog patterns** for create/edit workflows in Angular
4. **Client-side form validation** strategies (mirror backend rules)
5. **State management without external libraries** (using services + RxJS)
6. **Error handling strategies** for REST API responses
7. **Loading and empty states** UX patterns

### Expected Output Format

```markdown
# Research: Inventory Items UI Design Decisions

## Decision: Form Pattern (Create/Edit)
- **Chosen**: Reactive forms with separate service managing form state
- **Rationale**: Type-safe, testable, efficient change detection
- **Alternatives**: Template-driven forms (simpler but less scalable)

## Decision: State Management
- **Chosen**: Service-based state with RxJS Subjects/BehaviorSubjects
- **Rationale**: Aligns with Constitution Principle I (simplicity), uses Angular built-ins
- **Alternatives**: NgRx store (overkill for current scope), Akita (external dependency)

## Decision: List Pagination
- **Chosen**: HTTP pagination via query params, cached in service
- **Rationale**: Matches backend API design, reduces server calls
- **Alternatives**: Client-side pagination (requires full dataset in memory)

[... additional decisions ...]
```

---

## Phase 1: Design & Contracts

### Deliverable 1: Data Model (data-model.md)

**Purpose**: Document entity structures, DTOs, relationships, and validation rules used by the frontend.

**Expected Content**:
- Entity/DTO definitions from spec (InventoryItem, Category, Location)
- Frontend-specific form models (CreateItemFormModel, EditItemFormModel)
- API request/response shapes (from backend contract)
- Validation rule definitions
- State machine for item status (ACTIVE ↔ ARCHIVED)

**Key Entities for Frontend**:

```typescript
// InventoryItem (from backend API)
{
  id: number;
  name: string;
  description?: string;
  sku?: string;
  categoryId: number;
  categoryName: string;  // Backend provides readable name
  locationId: number;
  locationName: string;  // Backend provides readable name
  currentQuantity: number;
  unit: string;
  lowStockThreshold: number;
  status: 'ACTIVE' | 'ARCHIVED';
  createdDate: string;  // ISO-8601
  updatedDate: string;  // ISO-8601
}

// Create Form Model (Frontend)
{
  name: string;              // Required, non-empty, max 255
  description?: string;      // Optional, max 1000
  sku?: string;             // Optional, max 100, unique per user
  categoryId: number;        // Required, belongs to user
  locationId: number;        // Required, belongs to user
  unit: string;             // Required, max 50
  lowStockThreshold?: number; // Optional, >= 0, default 0
  initialQuantity?: number;  // Optional, >= 0, default 0
}

// Edit Form Model (Frontend - no currentQuantity)
{
  name?: string;
  description?: string;
  sku?: string;
  categoryId?: number;
  locationId?: number;
  unit?: string;
  lowStockThreshold?: number;
  // Note: currentQuantity is NOT editable
}

// List Filter State
{
  page: number;           // 0-indexed
  size: number;           // default 20
  status?: 'ACTIVE' | 'ARCHIVED' | null;  // null = all
  categoryId?: number | null;
}
```

### Deliverable 2: Contracts (contracts/)

**Purpose**: Document API endpoint contracts and communication patterns.

**Expected Content**:
- Reference to backend inventory-items-api.md
- Frontend request/response shapes
- Error response handling
- HTTP status code mappings

**Contract Reference File**: `contracts/inventory-items-api.md`

Example endpoints (from backend contract):
```
POST /api/v1/inventory-items                    # Create
GET /api/v1/inventory-items/{id}                # Get one
GET /api/v1/inventory-items                     # List with pagination/filtering
PATCH /api/v1/inventory-items/{id}              # Update
POST /api/v1/inventory-items/{id}/archive       # Archive
POST /api/v1/inventory-items/{id}/restore       # Restore
DELETE /api/v1/inventory-items/{id}             # Delete
```

### Deliverable 3: Quickstart Validation Guide (quickstart.md)

**Purpose**: Document runnable end-to-end validation scenarios that prove the feature works.

**Expected Content**:
- Setup prerequisites (backend running, authentication ready)
- Validation scenario 1: Create item with opening balance
- Validation scenario 2: Edit and view item details
- Validation scenario 3: Archive and restore item
- Validation scenario 4: List with pagination and filtering
- Validation scenario 5: Delete item with confirmation

**Example Scenario Format**:

```markdown
## Scenario 1: Create Inventory Item

**Prerequisites**: 
- Backend API running on http://localhost:8080
- User authenticated
- At least one category and location exist

**Steps**:
1. Navigate to /inventory/items
2. Click "Create New Item"
3. Fill form: name="Widget A", category="Supplies", location="Warehouse A", unit="pcs", initialQuantity=100
4. Click "Create"
5. Verify: Item appears in list with quantity 100, created date shows today

**Expected Outcome**: Item created, list refreshed, no validation errors
```

---

## API Integration Points

### HTTP Client Setup
- **Service**: `core/services/api.service.ts`
- **Responsibility**: Base HTTP client, error handling, request/response transformation
- **Interceptors**: Error handler, auth token injection (if needed)

### Service Layer
- **Service**: `features/inventory-items/services/inventory-items.service.ts`
- **Methods**: 
  - `createItem(data): Observable<InventoryItem>`
  - `getItem(id): Observable<InventoryItem>`
  - `listItems(filters): Observable<PagedResponse>`
  - `updateItem(id, data): Observable<InventoryItem>`
  - `archiveItem(id): Observable<InventoryItem>`
  - `restoreItem(id): Observable<InventoryItem>`
  - `deleteItem(id): Observable<void>`
  - `getCategories(): Observable<Category[]>`
  - `getLocations(): Observable<Location[]>`

### Component Communication Pattern
- **Container Components**: Subscribe to service observables, dispatch actions
- **Presentational Components**: Receive data via @Input, emit events via @Output
- **Forms**: Use Reactive Forms with FormBuilder, validate on blur/submit

---

## Testing Strategy

### Unit Tests
- **Service tests**: Mock HTTP client, test observable chains
- **Component tests**: Test form validation, input/output binding
- **Pipe tests**: Test date formatting pipe

### Integration Tests
- **API Integration**: Test end-to-end flows (create → list → edit → archive)
- **Form Validation**: Test client-side validation mirrors backend rules
- **Error Handling**: Test error messages display correctly

### Component Test Coverage
- `item-form`: Validation rules, form submission, error display
- `item-list`: Pagination, filtering, sorting, delete confirmation
- `item-detail`: Display all fields, archive/restore buttons, edit trigger
- `inventory-items-list` (container): API calls, state updates

---

## Implementation Phases

### Phase 1a: Core Setup (Foundation)
1. API service and HTTP client setup
2. Error handling interceptor
3. Shared components (spinner, error message, pagination)
4. Routing configuration

### Phase 1b: List View (MVP)
1. Container component for list
2. List presentation component
3. Filter toolbar component
4. Pagination component
5. Service state management for list

### Phase 1c: CRUD Operations
1. Item detail component (read)
2. Create/Edit form component
3. Modal/dialog setup
4. Form validation
5. Archive/Restore actions
6. Delete with confirmation

### Phase 1d: Polish & Testing
1. Component unit tests
2. Service integration tests
3. End-to-end validation scenarios
4. Error handling refinement
5. Loading state UX

---

## Next Steps

1. **Phase 0**: Execute research.md generation (document design decisions and patterns)
2. **Phase 1**: Generate data-model.md, contracts/, and quickstart.md
3. **Phase 2**: Execute `/speckit-tasks` to generate actionable task list
4. **Phase 3**: Begin implementation using tasks as guide

---

## Post-Phase 1 Re-check

After Phase 1 design completion, Constitution Check will be re-evaluated to confirm:
- All design decisions align with Principle I (simplicity)
- Technology stack remains unchanged (Angular, TypeScript, RxJS)
- Backend layered structure is respected
- Frontend error handling strategy is consistent

**Expected Result**: ✅ PASS (no violations anticipated)
