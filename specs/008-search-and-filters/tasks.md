# Tasks: Search and Filters

**Input**: Design documents from `/specs/008-search-and-filters/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/search-filter-api.md, quickstart.md

**Organization**: Tasks are grouped by user story (P1, P1, P2, P2, P3) to enable independent implementation and testing of each story. Each user story is independently testable and deliverable as an MVP increment.

**Format**: `[ID] [P?] [Story?] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4, US5)

## Path Conventions

- **Backend**: `backend/src/main/java/com/inventory/`, `backend/src/main/resources/db/migration/`
- **Frontend**: `frontend/src/app/inventory/`
- **Tests**: `backend/src/test/java/com/inventory/`, `frontend/src/app/inventory/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and database schema setup

- [x] T001 Create Flyway migration V*__add_search_filter_indexes.sql for search/filter indexes in backend/src/main/resources/db/migration/
- [x] T002 Create InventoryItemSearchResponseDTO in backend/src/main/java/com/inventory/model/InventoryItemSearchResponseDTO.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core backend infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T003 Enhance InventoryItemRepository with searchByMultipleFields() method in backend/src/main/java/com/inventory/repository/InventoryItemRepository.java
- [x] T004 Enhance InventoryItemService with search/filter orchestration logic in backend/src/main/java/com/inventory/service/InventoryItemService.java
- [x] T005 Enhance InventoryItemController GET /api/inventory-items endpoint to accept search/filter parameters in backend/src/main/java/com/inventory/controller/InventoryItemController.java
- [x] T006 [P] Create SearchFilterComponent in frontend/src/app/inventory/search-filter/search-filter.component.ts
- [x] T007 [P] Create search-filter.component.html template in frontend/src/app/inventory/search-filter/search-filter.component.html

**Checkpoint**: Backend API and frontend shell ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Search items by multiple fields (Priority: P1) 🎯 MVP

**Goal**: Users can search items by name, description, or SKU code with case-insensitive partial matching

**Independent Test**: Search box accepts text; returns matching items by name, description, OR SKU; empty state displays for no matches

### Implementation for User Story 1

- [x] T008 [US1] Implement searchByMultipleFields query method in InventoryItemRepository using ILIKE for case-insensitive matching in backend/src/main/java/com/inventory/repository/InventoryItemRepository.java
- [x] T009 [US1] Add searchInventoryItems method to InventoryItemService in backend/src/main/java/com/inventory/service/InventoryItemService.java
- [x] T010 [US1] Wire search parameter binding in InventoryItemController GET endpoint in backend/src/main/java/com/inventory/controller/InventoryItemController.java
- [x] T011 [P] [US1] Update search-filter.component.ts to handle search box input and Enter key submission in frontend/src/app/inventory/search-filter/search-filter.component.ts
- [x] T012 [P] [US1] Create search input field in search-filter.component.html with Enter key binding and Search button in frontend/src/app/inventory/search-filter/search-filter.component.html
- [x] T013 [US1] Enhance inventory.service.ts to construct API calls with search parameter in frontend/src/app/inventory/inventory.service.ts
- [x] T014 [US1] Integrate SearchFilterComponent into inventory-list.component.html and bind search results to table in frontend/src/app/inventory/inventory-list/inventory-list.component.html
- [x] T015 [US1] Update inventory-list.component.ts to handle search results and empty state in frontend/src/app/inventory/inventory-list/inventory-list.component.ts
- [x] T016 [US1] Create empty state component/template showing message when search returns no results in frontend/src/app/inventory/empty-state/empty-state.component.ts

**Checkpoint**: User Story 1 complete and independently testable. Users can search items by name/description/SKU.

---

## Phase 4: User Story 2 - Filter items by category and location (Priority: P1)

**Goal**: Users can filter items by category and location independently or in combination using AND logic

**Independent Test**: Category and location dropdown filters work; combined filters use AND logic; results accurate; filters can be cleared

### Implementation for User Story 2

- [ ] T017 [P] [US2] Add filterByCategoryAndLocation query method to InventoryItemRepository in backend/src/main/java/com/inventory/repository/InventoryItemRepository.java
- [ ] T018 [P] [US2] Add filterInventoryItems method to InventoryItemService in backend/src/main/java/com/inventory/service/InventoryItemService.java
- [ ] T019 [US2] Wire categoryId and locationId parameter binding in InventoryItemController in backend/src/main/java/com/inventory/controller/InventoryItemController.java
- [ ] T020 [P] [US2] Add category filter dropdown to search-filter.component.html fetching categories from API in frontend/src/app/inventory/search-filter/search-filter.component.html
- [ ] T021 [P] [US2] Add location filter dropdown to search-filter.component.html fetching locations from API in frontend/src/app/inventory/search-filter/search-filter.component.html
- [ ] T022 [US2] Update search-filter.component.ts to handle category/location filter changes and construct filter parameters in frontend/src/app/inventory/search-filter/search-filter.component.ts
- [ ] T023 [US2] Enhance inventory.service.ts to include categoryId and locationId parameters in API calls in frontend/src/app/inventory/inventory.service.ts
- [ ] T024 [US2] Update inventory-list.component.ts to display category and location names in table (already from previous feature) in frontend/src/app/inventory/inventory-list/inventory-list.component.ts
- [ ] T025 [US2] Add clear filter buttons to search-filter.component for individual and reset-all functionality in frontend/src/app/inventory/search-filter/search-filter.component.html

**Checkpoint**: User Story 2 complete. Users can filter by category and location with AND logic.

---

## Phase 5: User Story 3 - Filter items by status (Active/Archived) (Priority: P2)

**Goal**: Users can filter items by status (Active/Archived/All) to distinguish between current and archived inventory

**Independent Test**: Status filter dropdown works; ACTIVE/ARCHIVED/ALL options work correctly; can combine with other filters

### Implementation for User Story 3

- [ ] T026 [P] [US3] Add filterByStatus query method to InventoryItemRepository in backend/src/main/java/com/inventory/repository/InventoryItemRepository.java
- [ ] T027 [P] [US3] Update InventoryItemService to handle status filtering in backend/src/main/java/com/inventory/service/InventoryItemService.java
- [ ] T028 [US3] Wire status parameter binding in InventoryItemController in backend/src/main/java/com/inventory/controller/InventoryItemController.java
- [ ] T029 [P] [US3] Add status filter dropdown to search-filter.component.html with ACTIVE/ARCHIVED/ALL options in frontend/src/app/inventory/search-filter/search-filter.component.html
- [ ] T030 [US3] Update search-filter.component.ts to handle status filter changes and construct status parameter in frontend/src/app/inventory/search-filter/search-filter.component.ts
- [ ] T031 [US3] Enhance inventory.service.ts to include status parameter in API calls in frontend/src/app/inventory/inventory.service.ts
- [ ] T032 [US3] Verify status filter combines correctly with search and other filters in frontend/src/app/inventory/inventory-list/inventory-list.component.ts

**Checkpoint**: User Story 3 complete. Users can filter by status (Active/Archived).

---

## Phase 6: User Story 4 - Filter items by stock state (Priority: P2)

**Goal**: Users can filter items by stock state (Out of Stock, Low Stock, In Stock, All) based on per-item lowStockThreshold

**Independent Test**: Stock state filter works for all four options; items classified correctly; can combine with other filters

### Implementation for User Story 4

- [ ] T033 [P] [US4] Add filterByStockState query method to InventoryItemRepository using per-item lowStockThreshold in backend/src/main/java/com/inventory/repository/InventoryItemRepository.java
- [ ] T034 [P] [US4] Update InventoryItemService to compute stock state classification in backend/src/main/java/com/inventory/service/InventoryItemService.java
- [ ] T035 [US4] Wire stockState parameter binding in InventoryItemController in backend/src/main/java/com/inventory/controller/InventoryItemController.java
- [ ] T036 [P] [US4] Add stock state filter dropdown to search-filter.component.html with OUT_OF_STOCK/LOW_STOCK/IN_STOCK/ALL options in frontend/src/app/inventory/search-filter/search-filter.component.html
- [ ] T037 [US4] Update search-filter.component.ts to handle stock state filter changes and construct stockState parameter in frontend/src/app/inventory/search-filter/search-filter.component.ts
- [ ] T038 [US4] Enhance inventory.service.ts to include stockState parameter in API calls in frontend/src/app/inventory/inventory.service.ts
- [ ] T039 [US4] Add isLowStock computed field to InventoryItem model in frontend/src/app/inventory/models/inventory-item.model.ts
- [ ] T040 [US4] Update table to display stock state indicator/badge in inventory-list.component.html in frontend/src/app/inventory/inventory-list/inventory-list.component.html

**Checkpoint**: User Story 4 complete. Users can filter by stock state (Out of Stock, Low Stock, In Stock).

---

## Phase 7: User Story 5 - Combine multiple search and filter criteria (Priority: P3)

**Goal**: Users can apply search AND all filters together for complex inventory queries (e.g., "low-stock electronics in warehouse")

**Independent Test**: All five dimensions (search + 4 filters) work together with AND logic; results reflect all criteria; can modify individual filters dynamically

### Implementation for User Story 5

- [ ] T041 [US5] Update InventoryItemRepository query methods to combine all search/filter parameters with AND logic in backend/src/main/java/com/inventory/repository/InventoryItemRepository.java
- [ ] T042 [US5] Update InventoryItemService to coordinate all filter dimensions in backend/src/main/java/com/inventory/service/InventoryItemService.java
- [ ] T043 [US5] Verify InventoryItemController passes all parameters correctly in backend/src/main/java/com/inventory/controller/InventoryItemController.java
- [ ] T044 [US5] Update search-filter.component.ts to manage all five filter dimensions and emit combined parameters in frontend/src/app/inventory/search-filter/search-filter.component.ts
- [ ] T045 [US5] Enhance inventory.service.ts to construct API calls with all parameters (search, categoryId, locationId, status, stockState) in frontend/src/app/inventory/inventory.service.ts
- [ ] T046 [US5] Add visual indicator showing active filters in search-filter.component.html in frontend/src/app/inventory/search-filter/search-filter.component.html
- [ ] T047 [US5] Verify pagination works correctly with all combined filters in inventory-list.component.ts in frontend/src/app/inventory/inventory-list/inventory-list.component.ts

**Checkpoint**: User Story 5 complete. All five user stories fully integrated and functional.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements, documentation, and validation

- [ ] T048 [P] Add unit tests for InventoryItemRepository search/filter methods in backend/src/test/java/com/inventory/repository/InventoryItemRepositoryTest.java
- [ ] T049 [P] Add unit tests for InventoryItemService search/filter logic in backend/src/test/java/com/inventory/service/InventoryItemServiceTest.java
- [ ] T050 [P] Add integration tests for search/filter API endpoint in backend/src/test/java/com/inventory/controller/InventoryItemControllerTest.java
- [ ] T051 [P] Add component unit tests for SearchFilterComponent in frontend/src/app/inventory/search-filter/search-filter.component.spec.ts
- [ ] T052 [P] Add integration tests for inventory-list with search/filter in frontend/src/app/inventory/inventory-list/inventory-list.component.spec.ts
- [ ] T053 [P] Add e2e tests for search and filter workflows in frontend/e2e/inventory-search-filter.e2e.ts
- [ ] T054 Code cleanup: Remove temporary debugging code; format all modified files per project standards
- [ ] T055 Performance validation: Run API queries on 10k item inventory to verify 500ms response target in contracts/search-filter-api.md
- [ ] T056 Run quickstart.md validation scenarios (15 test scenarios) to verify end-to-end functionality in specs/008-search-and-filters/quickstart.md
- [ ] T057 Update documentation with search/filter feature overview in docs/FEATURES.md
- [ ] T058 [P] API documentation: Add search/filter examples to API docs with request/response samples
- [ ] T059 Test accessibility: Verify search/filter dropdowns are keyboard navigable and screen-reader friendly in frontend/

**Checkpoint**: All tasks complete; feature ready for production

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup)
    ↓
Phase 2 (Foundational) ← BLOCKING ALL USER STORIES
    ↓
Phases 3-7 (User Stories 1-5) ← CAN RUN IN PARALLEL after Phase 2
    ↓
Phase 8 (Polish) ← FINAL POLISH
```

### User Story Dependencies

- **User Story 1 (P1)**: No inter-story dependencies. Can start after Phase 2.
- **User Story 2 (P1)**: No dependencies on US1. Can start after Phase 2. Independently testable.
- **User Story 3 (P2)**: No dependencies on US1/US2. Can start after Phase 2. Independently testable.
- **User Story 4 (P2)**: No dependencies on US1/US2/US3. Can start after Phase 2. Independently testable.
- **User Story 5 (P3)**: Integrates US1-US4 but should be independently testable. Can start after Phase 2.

### Within Each User Story

- Backend repository method → Backend service method → Backend controller wiring (sequential in backend)
- Frontend component template → Frontend component logic → Frontend service → Frontend view integration (sequential in frontend)
- Backend and frontend can proceed in parallel once Phase 2 complete

### Parallel Opportunities

**Phase 1**:
- T001, T002 can run in parallel (different files)

**Phase 2**:
- T003, T004, T005 must be sequential (T003 → T004 → T005)
- T006, T007 can run in parallel (different frontend files)

**Phase 3 (US1)**:
- T008, T009, T010 sequential in backend (T008 → T009 → T010)
- T011, T012 can run in parallel (different frontend files)
- Backend (T008-T010) and Frontend (T011-T015) can run in parallel once Phase 2 done

**Phases 4-7 (US2-US5)**:
- All user story phases can run IN PARALLEL if team capacity allows:
  - Developer 1: US1 + US2
  - Developer 2: US3 + US4
  - Developer 3: US5
- Within each story, parallelizable tasks marked [P] can run concurrently

**Phase 8**:
- All test tasks marked [P] can run in parallel
- Test → Cleanup → Performance → Documentation → Validation (final sequence)

---

## Parallel Example: Full Team

```bash
# After Phase 2 complete, launch all user stories in parallel:

Developer Team A:
  - Complete User Story 1 (Phase 3, T008-T016)
  - Complete User Story 2 (Phase 4, T017-T025)

Developer Team B:
  - Complete User Story 3 (Phase 5, T026-T032)
  - Complete User Story 4 (Phase 6, T033-T040)

Developer Team C:
  - Complete User Story 5 (Phase 7, T041-T047)

# Once all stories complete, Teams converge on Phase 8 (Polish)
```

---

## Implementation Strategy

### MVP First (Recommended)

1. **Phase 1**: Setup database indexes and DTOs (1-2 hours)
2. **Phase 2**: Backend API foundation + frontend shell (2-3 hours)
3. **Phase 3**: User Story 1 - Search only (2-3 hours)
4. **STOP AND VALIDATE**: Test US1 independently, deploy MVP
5. Phase 4: Add Category/Location filters
6. Phase 5: Add Status filter
7. Phase 6: Add Stock State filter
8. Phase 7: Combine all filters
9. Phase 8: Polish & tests

### Incremental Delivery

Each user story is independently deployable:
- **After Phase 3**: Users can search items (search-only MVP)
- **After Phase 4**: Users can also filter by category/location
- **After Phase 5**: Users can also filter by status
- **After Phase 6**: Users can also filter by stock state
- **After Phase 7**: Users have full search + filter power
- **After Phase 8**: Production-ready with tests and docs

### Single Developer

1. Complete Phase 1-2 (foundational work)
2. Complete Phase 3 (User Story 1 - MVP)
3. Complete Phase 4-7 sequentially (User Stories 2-5)
4. Complete Phase 8 (Polish & tests)

---

## Task Estimates & Sizing

| Phase | Tasks | Est. Time | Notes |
|-------|-------|-----------|-------|
| Phase 1 | T001-T002 | 1-2 hrs | Setup indexes + DTO |
| Phase 2 | T003-T007 | 3-4 hrs | Critical backend foundation + frontend shell |
| Phase 3 (US1) | T008-T016 | 2-3 hrs | Search implementation (MVP) |
| Phase 4 (US2) | T017-T025 | 2-3 hrs | Category/Location filters |
| Phase 5 (US3) | T026-T032 | 1-2 hrs | Status filter (simpler) |
| Phase 6 (US4) | T033-T040 | 2-3 hrs | Stock state filter (per-item threshold) |
| Phase 7 (US5) | T041-T047 | 1-2 hrs | Combined filtering integration |
| Phase 8 | T048-T059 | 3-4 hrs | Tests, docs, validation |
| **Total** | **59 tasks** | **15-23 hrs** | 1-2 person-days |

---

## Quality Checkpoints

**After Phase 2**: Foundation complete - no UI visible yet, backend API ready
- ✅ Database indexes created
- ✅ DTO created
- ✅ Repository methods callable
- ✅ Service layer orchestrating
- ✅ Controller accepting parameters
- ✅ Frontend component shell exists

**After Phase 3** (US1 - MVP): Users can search
- ✅ Search box visible and functional
- ✅ Search results returned for name/description/SKU
- ✅ Empty state displays for no results
- ✅ Independently testable

**After Phase 4** (US2): Users can filter by location
- ✅ Category filter dropdown visible
- ✅ Location filter dropdown visible
- ✅ Category + Location filters use AND logic
- ✅ Can combine with search

**After Phase 5** (US3): Users can filter by status
- ✅ Status filter (ACTIVE/ARCHIVED/ALL) working
- ✅ Can combine with search + category + location

**After Phase 6** (US4): Users can filter by stock state
- ✅ Stock state filter (OUT/LOW/IN/ALL) working
- ✅ Uses per-item lowStockThreshold correctly
- ✅ All five dimensions working together

**After Phase 7** (US5): Full feature integration
- ✅ All five user stories complete and integrated
- ✅ Search + 4 filter dimensions working together

**After Phase 8** (Polish): Production ready
- ✅ All unit/integration/e2e tests passing
- ✅ 500ms response time target met on 10k items
- ✅ All 15 quickstart scenarios passing
- ✅ Documentation complete
- ✅ Accessibility verified
- ✅ Ready for production deployment

---

## Notes

- Each [P] task can run in parallel with others marked [P] in same scope
- Each [Story] task maps to a specific user story for traceability
- Tests are included in Phase 8 - can be moved earlier if TDD preferred
- Remove completed checkboxes are tracked for progress visibility
- Commit after each user story completion (end of Phase 3, 4, 5, 6, 7)
- Each user story should pass its independent test criteria before moving to next story
- Stop at any checkpoint to validate and deploy
- No user story blocks another - all can be implemented in parallel after Phase 2
