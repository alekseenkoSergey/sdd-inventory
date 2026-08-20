# Implementation Plan: Search and Filters

**Branch**: `008-search-and-filters` | **Date**: 2026-08-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/008-search-and-filters/spec.md`

## Summary

The search and filters feature enables users to efficiently query inventory by name, description, SKU code, and organize results by category, location, status (Active/Archived), and stock state. The implementation adds a REST API endpoint supporting parameterized search/filter queries, database indexes for performance, and Angular UI components in the main inventory view. Search uses case-insensitive partial matching; filters combine using AND logic. Target response time is 500ms for typical inventories (100-1000 items).

## Technical Context

**Language/Version**: Java 17+ (backend), TypeScript/Angular (frontend)

**Primary Dependencies**: 
- Backend: Spring Boot 3.x, Spring Data JPA, Flyway (migrations)
- Frontend: Angular 16+, Bootstrap/Material (UI components)

**Storage**: PostgreSQL (existing)

**Testing**: JUnit 5 (backend), Jasmine/Karma or Jest (frontend)

**Target Platform**: Web application (browser)

**Project Type**: Web service (backend API) + SPA frontend

**Performance Goals**: 
- Search results display within 500ms for typical inventory (100-1000 items)
- Support 10,000+ items in inventory with acceptable performance
- Pagination limit: 1000 items per page

**Constraints**: 
- Must maintain backward compatibility with existing inventory API
- No breaking changes to InventoryItem entity structure
- Session-scoped filter state (no database persistence of filter preferences)

**Scale/Scope**: 
- Small-to-medium inventory (up to 10,000 items)
- Single-user web application per constitution
- Typical queries: 100-1000 result items

## Constitution Check

**GATE ASSESSMENT** ✅ PASS

| Principle | Assessment | Status |
|-----------|-----------|--------|
| **I. Simplicity First** | Design uses straightforward repository methods and template-driven forms. No over-abstraction or speculative patterns. | ✅ PASS |
| **II. Technology Stack** | Uses Spring Boot (backend), PostgreSQL (storage), Angular (frontend) per required stack. No new major frameworks introduced. | ✅ PASS |
| **III. Backend Layered Package Structure** | API follows controller → service → repository pattern. DTOs named `InventoryItemSearchResponseDTO` per naming rules. No business logic in DTOs. | ✅ PASS |
| **IV. Centralized Exception Handling** | Validation and error handling delegated to centralized @ControllerAdvice. No local try/catch in controllers. | ✅ PASS |

**Gate Status**: ✅ **PASS** — No constitutional violations. Design complies with all four core principles.

## Project Structure

### Documentation (this feature)

```text
specs/008-search-and-filters/
├── plan.md              # This file (/speckit-plan output)
├── spec.md              # Feature specification
├── research.md          # Phase 0: Research findings
├── data-model.md        # Phase 1: Data model & schema
├── quickstart.md        # Phase 1: Validation scenarios
├── contracts/
│   └── search-filter-api.md    # Phase 1: API contract
└── tasks.md             # Phase 2: Task list (/speckit-tasks output)
```

### Source Code Structure

Backend (Spring Boot):

```text
backend/src/main/java/com/inventory/
├── controller/
│   └── InventoryItemController.java       # Add search/filter parameters to existing GET endpoint
├── model/
│   └── InventoryItemSearchResponseDTO.java # Response DTO (new)
├── service/
│   └── InventoryItemService.java          # Add search/filter logic (enhance existing)
├── repository/
│   └── InventoryItemRepository.java       # Add search/filter query methods (enhance existing)
└── exception/
    └── ExceptionAdvice.java               # Centralized error handling (existing)

backend/src/main/resources/db/migration/
└── V*__add_search_filter_indexes.sql      # Flyway migration for indexes (new)
```

Frontend (Angular):

```text
frontend/src/app/
├── inventory/
│   ├── inventory-list.component.ts        # Main inventory view (enhance existing)
│   ├── inventory-list.component.html      # Template (enhance existing)
│   ├── search-filter.component.ts         # Search/filter component (new)
│   ├── search-filter.component.html       # Search/filter template (new)
│   ├── inventory.service.ts               # API service (enhance existing)
│   └── models/
│       └── inventory-item.model.ts        # DTO models (enhance existing)
```

**Structure Decision**: Web application with backend API and frontend SPA. Backend enhances existing InventoryItemController and InventoryItemService; frontend adds new SearchFilterComponent integrated into existing inventory-list view. No new major modules created; follows existing project structure.

---

## Implementation Approach

### Backend Changes

1. **Repository**: Add methods to InventoryItemRepository for search/filter queries
2. **Service**: Add search/filter logic to InventoryItemService; orchestrate repository calls
3. **Controller**: Enhance GET `/api/inventory-items` endpoint to accept search/filter parameters
4. **DTO**: Create InventoryItemSearchResponseDTO for response serialization
5. **Database**: Flyway migration to create indexes on searchable/filterable columns

### Frontend Changes

1. **Service**: Enhance InventoryService to construct API calls with search/filter parameters
2. **Component**: Create SearchFilterComponent with:
   - Search box accepting text input; submit on Enter key or Search button click
   - Filter dropdowns for category, location, status, stock state
   - Clear individual filters or reset all filters
   - Emits search/filter parameters to parent on submission
3. **Template**: Add SearchFilterComponent to inventory-list view; bind results to table
4. **Models**: Enhance existing InventoryItem model if needed for response fields (e.g., isLowStock computed flag)

### No Breaking Changes

- Existing GET `/api/inventory-items` endpoint remains functional
- New parameters are optional; backward compatible with calls without filters
- InventoryItem entity structure unchanged; no new fields required

---

## Complexity Tracking

No constitutional violations or architectural exceptions required.

| Item | Status | Justification |
|------|--------|---------------|
| All design within existing layers | ✅ OK | Uses existing repository/service/controller pattern |
| No new frameworks | ✅ OK | Uses existing Spring Boot, Angular, PostgreSQL |
| No breaking API changes | ✅ OK | Backward compatible enhancement |
| Database only adds indexes | ✅ OK | No structural changes to existing tables |
