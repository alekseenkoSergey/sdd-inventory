# Research & Design Decisions: Location Management

**Date**: 2026-08-19  
**Feature**: Location Management  
**Phase**: 0 (Research) - consolidated findings from clarification workflow

## Decision: Block-on-Delete Strategy

**Decision**: Implement block-on-delete: prevent deletion of locations containing items; users must reassign or delete items first.

**Rationale**:
- Simpler data model: no need for default "Unassigned" location
- Matches user intent in clarification (Option A selected)
- Aligns with Simplicity First principle (constitution)
- Prevents accidental data loss; users explicitly control item lifecycle
- Consistent with existing Category feature pattern in codebase

**Alternatives Considered**:
- Move-on-delete (move items to default location): adds complexity around default location lifecycle; requires handling orphaned items; less transparent to users
- Both strategies (configurable): unnecessary complexity for v1

## Decision: Centralized Exception Handling

**Decision**: All location operation failures handled via existing `GlobalExceptionHandler` `@ControllerAdvice` class.

**Rationale**:
- Project already has established pattern (see GlobalExceptionHandler for Category exceptions)
- Centralizes error response formatting and HTTP status mapping
- Eliminates duplicate try/catch logic in controllers/services
- Consistent error contract across all features

**Alternatives Considered**:
- Custom error DTOs per feature: violates Simplicity First; breaks consistency
- Direct try/catch in controller: duplicates logic; inconsistent responses

## Decision: Comprehensive Logging

**Decision**: Log all location operations (create, rename, delete, successes and failures) at appropriate levels (INFO/WARN/ERROR).

**Rationale**:
- Enables debugging and audit trails for user support
- Aligns with operational best practices for API services
- Clarification requirement (Q3); required for production readiness
- Uses SLF4J/Logback (already configured in Spring Boot)

**Alternatives Considered**:
- No logging: impossible to debug user-reported issues; no audit trail
- Log only failures: misses context for investigation; incomplete audit history

## Decision: Database Uniqueness Constraint

**Decision**: Enforce location name uniqueness at both application and database level.

**Rationale**:
- Application-level validation in service: user feedback before DB round-trip
- Database-level unique constraint: prevents race conditions; data integrity guarantee
- Pattern already used in Category entity (unique constraint on user_id + name)
- Aligns with spec FR-003: "unique per user"

**Implementation**: Composite unique constraint `(user_id, name)` in location table.

## Decision: Pessimistic Locking for Concurrent Updates

**Decision**: Use `@Version` column for optimistic locking (Spring Data's default pattern).

**Rationale**:
- Matches existing Category entity implementation
- Lightweight: single version column, no read locks
- Sufficient for inventory management scale and expected contention
- Spring Data handles version checking transparently

**Alternatives Considered**:
- Pessimistic locking: overkill; unnecessary blocking for low-contention scenario
- No locking: data corruption risk; violates integrity constraints

## Decision: API Response Format

**Decision**: Use standardized location response format following existing project pattern:
- Success: JSON object with location fields (id, name, userId, createdAt, updatedAt)
- Errors: Standardized error response via GlobalExceptionHandler (timestamp, status, error code, message, path)

**Rationale**:
- Consistency with Category API responses
- Centralized error handling ensures uniform error format
- No custom error DTOs needed; use existing pattern

## Decision: Authentication & Authorization

**Decision**: All location operations require authenticated user; users can only manage their own locations.

**Rationale**:
- Existing OAuth2/Spring Security infrastructure
- Spec FR-009: user data isolation requirement
- Clarification Q1 (Option A): all authenticated users can manage own locations
- Controller should extract userId from authenticated principal

**Implementation**: Use `@PreAuthorize` or manual principal checks in service to enforce user isolation.

## Decision: API Endpoints Structure

**Decision**: RESTful endpoints following Spring Boot conventions:
- POST /locations — create
- GET /locations — list user's locations
- GET /locations/{id} — retrieve by id
- PUT /locations/{id} — rename
- DELETE /locations/{id} — delete

**Rationale**:
- Standard REST pattern; aligns with project conventions
- Maps 1:1 to functional requirements (FR-001 through FR-007)
- Easy to test and document

**Alternatives Considered**:
- RPC-style (updateLocationName, deleteLocation): less RESTful; inconsistent with project

## Decision: Frontend State Management

**Decision**: Component-level state with shared services; no Redux/NgRx.

**Rationale**:
- Simplicity First principle: Location feature doesn't require global state management
- Data scope: Each user manages only their own locations; natural component isolation
- RxJS Observable patterns already used in Angular components throughout project
- LocationService handles API communication; components manage UI state (loading, error, form)

**Alternatives Considered**:
- Redux/NgRx: Overkill for this feature; adds complexity without benefit
- Akita: Still too much scaffolding for simple location management

## Decision: Form Validation Strategy

**Decision**: Reactive Forms with client-side validation + server-side validation.

**Rationale**:
- Reactive Forms align with project pattern
- Client-side validation: Immediate user feedback, no round-trip delay
- Server-side validation: Enforce business rules (duplicate name), prevent race conditions
- FormBuilder provides clean, type-safe form definition

**Alternatives Considered**:
- Template-driven forms: Less type-safe; harder to test
- No client validation: Poor UX; requires server round-trip for every keystroke

## Decision: Modal/Dialog for Forms

**Decision**: Use Angular Material Dialog or native HTML dialog element.

**Rationale**:
- Keep location list visible while editing
- Isolate form interaction (prevent accidental navigation)
- Standard UX pattern users expect
- Clear separation between list view and edit view

**Alternatives Considered**:
- Inline editing in the list: Cluttered; harder to manage complex error states
- Separate page for edit: Unnecessary navigation; breaks context

## Decision: Error Display Strategy

**Decision**: Show errors inline in forms + toast/alert above list.

**Rationale**:
- Form errors (validation): Display below/next to field for immediate feedback
- API errors during list operations: Show dismissible alert at top
- User can see what failed and why without navigating

**Alternatives Considered**:
- Silent errors: Poor UX; user doesn't know why operation failed
- Single global error modal: Interrupts workflow; requires explicit dismiss

## Decision: List Refresh Behavior

**Decision**: Automatic refresh after successful CRUD; manual refresh button available.

**Rationale**:
- Automatic: User sees immediate feedback that operation succeeded
- Manual: User can refresh if they suspect stale data or after network issue
- Observable pattern with RxJS: Clean, reactive approach

**Alternatives Considered**:
- Always manual refresh: Users won't use it; stale data accumulates
- No refresh button: Users can't recover from suspected stale state
