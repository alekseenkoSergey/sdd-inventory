# Tasks: Inventory Items User Interface

**Input**: Design documents from `/specs/005-inventory-items-ui/`

**Prerequisites**: plan.md (Angular 22.1.0, TypeScript 6.0.2, RxJS 7.8.0), spec.md (6 user stories: P1 core CRUD, P2 secondary operations), data-model.md, contracts/, research.md, quickstart.md

**Organization**: Tasks grouped by user story to enable independent implementation and testing of each story.

## Format: `- [ ] [ID] [P?] [Story] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story (US1, US2, US3, US4, US5, US6)
- **File paths**: Exact locations for Angular components, services, models

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic Angular structure

- [ ] T001 Create inventory-items feature directory structure at `frontend/src/app/inventory-items/`
- [ ] T002 Create models, components, pages, and services subdirectories
- [ ] T003 [P] Initialize Angular app configuration in `frontend/src/app/app.config.ts` (if needed)
- [ ] T004 [P] Setup routing configuration in `frontend/src/app/app.routes.ts` with inventory-items routes

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T005 [P] Create InventoryItem model interface in `frontend/src/app/inventory-items/models/inventory-item.model.ts`
- [ ] T006 [P] Create form DTOs (CreateItemFormModel, EditItemFormModel) in `frontend/src/app/inventory-items/models/inventory-item.model.ts`
- [ ] T007 Extend ApiService with `/v1/inventory-items` endpoint methods in `frontend/src/app/core/http/api.service.ts`
- [ ] T008 Create InventoryItemsService with RxJS state management in `frontend/src/app/inventory-items/services/inventory-items.service.ts`
- [ ] T009 Create PatchDTO (includes error handling) in service
- [ ] T010 [P] Create LoadingSpinner presentational component in `frontend/src/app/inventory-items/components/loading-spinner/` (if not reused from existing)
- [ ] T011 [P] Create ErrorMessage presentational component in `frontend/src/app/inventory-items/components/error-message/`
- [ ] T012 [P] Create Pagination component in `frontend/src/app/inventory-items/components/pagination/`
- [ ] T013 Create DateFormatter utility pipe in `frontend/src/app/inventory-items/components/` (or utilities/)
- [ ] T014 Setup HTTP error interceptor in `frontend/src/app/core/interceptors/` (extend existing auth interceptor if needed)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Create New Inventory Item (Priority: P1) 🎯 MVP

**Goal**: Enable users to create new inventory items with optional initial quantity

**Independent Test**: Create item with name="Widget A", category, location, unit="pcs", initialQuantity=100; verify item appears in list

### Implementation for User Story 1

- [ ] T015 [P] [US1] Create ItemFormComponent in `frontend/src/app/inventory-items/components/item-form/item-form.component.ts`
  - Reactive forms with form validation (required fields, max lengths, min values)
  - Support both create (with initialQuantity) and edit (without initialQuantity) modes
  - Display validation errors inline (name, quantity, threshold, SKU)
  - Disable currentQuantity field (read-only)
  - Emit save/cancel events

- [ ] T016 [P] [US1] Create ItemFormComponent template in `frontend/src/app/inventory-items/components/item-form/item-form.component.html`
  - Form fields: name (required), description, SKU, category (dropdown), location (dropdown), unit, lowStockThreshold, initialQuantity
  - Validation error messages below each field
  - Create and Cancel buttons
  - Loading state while submitting

- [ ] T017 [P] [US1] Create ItemFormComponent spec tests in `frontend/src/app/inventory-items/components/item-form/item-form.component.spec.ts`
  - Test form validation (required name, invalid quantities)
  - Test form submission emits save event
  - Test form cancel emits cancel event
  - Test read-only currentQuantity field

- [ ] T018 [P] [US1] Create ItemListComponent for displaying items in table in `frontend/src/app/inventory-items/components/item-list/item-list.component.ts`
  - Input: items array, pagination info, loading state
  - Output: page, edit, archive, delete, restore events
  - Display columns: name, SKU, category, location, quantity, unit, threshold, status, createdDate
  - Show visual distinction for archived items

- [ ] T019 [P] [US1] Create ItemListComponent template in `frontend/src/app/inventory-items/components/item-list/item-list.component.html`
  - Table with columns for all item fields
  - Action buttons: Edit, Archive/Restore, Delete
  - Status badge (ACTIVE green, ARCHIVED gray)
  - Format dates with DatePipe('medium')

- [ ] T020 [US1] Create InventoryItemsPageComponent (smart/container) in `frontend/src/app/inventory-items/pages/inventory-items-page/inventory-items-page.component.ts`
  - Subscribe to InventoryItemsService observables (items$, loading$, error$, filters$)
  - Implement form modal/page for create (emit from ItemFormComponent → call service.createItem)
  - Handle form submission → call API → refresh list
  - Dispatch list refresh on component init

- [ ] T021 [US1] Create InventoryItemsPageComponent template in `frontend/src/app/inventory-items/pages/inventory-items-page/inventory-items-page.component.html`
  - Display LoadingSpinner while loading
  - Display ErrorMessage if error with retry
  - "Create New Item" button
  - ItemListComponent (presentational)
  - Modal or page for item-form component

- [ ] T022 [US1] Implement service method `createItem()` in `frontend/src/app/inventory-items/services/inventory-items.service.ts`
  - Call POST /v1/inventory-items with CreateItemRequest
  - Handle response (InventoryItemResponseDTO)
  - Update items$ BehaviorSubject
  - Return Observable<InventoryItem>

- [ ] T023 [US1] Implement service method `listItems()` in `frontend/src/app/inventory-items/services/inventory-items.service.ts`
  - Call GET /v1/inventory-items with page, size, status, categoryId params
  - Update items$ and pagination state
  - Handle loading state

- [ ] T024 [US1] Create spec tests for InventoryItemsPageComponent in `frontend/src/app/inventory-items/pages/inventory-items-page/inventory-items-page.component.spec.ts`
  - Test list loads on init
  - Test create form opens/closes
  - Test form submission calls service.createItem()
  - Test item appears in list after create

- [ ] T025 [P] [US1] Create spec tests for InventoryItemsService in `frontend/src/app/inventory-items/services/inventory-items.service.spec.ts`
  - Test createItem() makes POST request
  - Test listItems() makes GET request with params
  - Test observables emit updated state

- [ ] T026 [US1] Integrate category and location dropdowns in ItemFormComponent
  - Call service.getCategories() and service.getLocations()
  - Populate dropdowns from observables
  - Display category/location names, not IDs

**Checkpoint**: User Story 1 complete - users can create items with initial quantity via form modal

---

## Phase 4: User Story 2 - View and Edit Item Details (Priority: P1)

**Goal**: Enable users to view complete item information and edit all fields except currentQuantity

**Independent Test**: Open item detail, edit name/description, save; verify changes persist without modifying quantity

### Implementation for User Story 2

- [ ] T027 [P] [US2] Create ItemDetailComponent in `frontend/src/app/inventory-items/components/item-detail/item-detail.component.ts`
  - Input: item (InventoryItem)
  - Output: edit, archive, delete events
  - Display all item fields (name, description, SKU, category, location, quantity, unit, threshold, status, createdDate, updatedDate)
  - Format dates human-readable

- [ ] T028 [P] [US2] Create ItemDetailComponent template in `frontend/src/app/inventory-items/components/item-detail/item-detail.component.html`
  - Display all fields as read-only text
  - Current quantity styled/highlighted if below threshold
  - Status badge (colored based on status)
  - Edit, Archive, Delete buttons

- [ ] T029 [US2] Create ItemDetailPageComponent (container) in `frontend/src/app/inventory-items/pages/item-detail-page/item-detail-page.component.ts`
  - Route param: item ID
  - Load item via service.getItem(id)
  - Show ItemDetailComponent + edit form
  - Handle edit → open form with pre-populated data

- [ ] T030 [US2] Implement service method `getItem(id)` in service
  - Call GET /v1/inventory-items/{id}
  - Return Observable<InventoryItem>

- [ ] T031 [US2] Implement service method `updateItem(id, data)` in service
  - Call PATCH /v1/inventory-items/{id} with EditItemRequest (no currentQuantity)
  - Update items$ state
  - Return Observable<InventoryItem>

- [ ] T032 [US2] Update ItemFormComponent to support edit mode
  - Accept item as @Input for pre-population
  - Disable currentQuantity field (not in edit form or read-only)
  - Emit different event type for edit vs create (or same event with mode flag)

- [ ] T033 [US2] Implement edit flow in ItemDetailPageComponent
  - Click Edit → show form with item data pre-populated
  - Form submit → call service.updateItem(id, formValue)
  - Success → close form, refresh item detail

- [ ] T034 [P] [US2] Create spec tests for ItemDetailComponent in `frontend/src/app/inventory-items/components/item-detail/item-detail.component.spec.ts`
  - Test all fields display correctly
  - Test read-only rendering
  - Test buttons emit events

- [ ] T035 [US2] Create spec tests for ItemDetailPageComponent in `frontend/src/app/inventory-items/pages/item-detail-page/item-detail-page.component.spec.ts`
  - Test item loads from service on init
  - Test edit form opens with pre-populated data
  - Test form submission calls service.updateItem()
  - Test currentQuantity field is not editable

- [ ] T036 [US2] Verify SKU uniqueness error handling
  - Service receives 400 SKU_DUPLICATE error from API
  - Display error message in form: "SKU already exists for this user"
  - User can retry with different SKU

**Checkpoint**: User Story 2 complete - users can view and edit items (except quantity)

---

## Phase 5: User Story 3 - Archive and Restore Items (Priority: P1)

**Goal**: Enable users to archive/restore items without permanent deletion

**Independent Test**: Archive item → disappears from active list, restore → reappears as ACTIVE

### Implementation for User Story 3

- [ ] T037 [US3] Implement service method `archiveItem(id)` in service
  - Call POST /v1/inventory-items/{id}/archive
  - Update items$ state (set status to ARCHIVED)
  - Return Observable<InventoryItem>

- [ ] T038 [US3] Implement service method `restoreItem(id)` in service
  - Call POST /v1/inventory-items/{id}/restore
  - Update items$ state (set status to ACTIVE)
  - Return Observable<InventoryItem>

- [ ] T039 [US3] Add Archive/Restore buttons to ItemDetailComponent
  - Button label changes based on status: "Archive" if ACTIVE, "Restore" if ARCHIVED
  - Button disabled while loading
  - Click → emit archive/restore event

- [ ] T040 [US3] Implement archive/restore in ItemDetailPageComponent
  - Click Archive → call service.archiveItem(id) → refresh detail
  - Click Restore → call service.restoreItem(id) → refresh detail

- [ ] T041 [US3] Add visual distinction for archived items in ItemListComponent
  - Strikethrough or gray text for archived items
  - "ARCHIVED" badge in status column (gray background)
  - Make it visually clear items are not active

- [ ] T042 [US3] Implement service method `setStatusFilter(status)` in service
  - Filter state: null (all), 'ACTIVE', 'ARCHIVED'
  - Call listItems() with new filter
  - Emit via filters$ observable

- [ ] T043 [US3] Add status filter dropdown to FilterToolbarComponent
  - Dropdown: "All", "Active", "Archived"
  - On change → call service.setStatusFilter()
  - Display current filter

- [ ] T044 [P] [US3] Create FilterToolbarComponent in `frontend/src/app/inventory-items/components/filter-toolbar/filter-toolbar.component.ts`
  - Inputs: current filters
  - Outputs: filterChanged event (or call service directly)
  - Dropdown: status (all/active/archived)
  - Dropdown: category (from service.categories$)
  - Button: Clear Filters

- [ ] T045 [US3] Verify idempotency
  - Test archive on already-archived item → no error, operation succeeds
  - Test restore on already-active item → no error, operation succeeds

- [ ] T046 [P] [US3] Create spec tests for archive/restore in `frontend/src/app/inventory-items/services/inventory-items.service.spec.ts`
  - Test archiveItem() makes POST request
  - Test restoreItem() makes POST request
  - Test items$ updates correctly

**Checkpoint**: User Story 3 complete - users can archive/restore items with visual distinction

---

## Phase 6: User Story 4 - View Items List with Filtering and Pagination (Priority: P1)

**Goal**: Enable users to browse items with pagination (20 per page), filter by status/category, clear filters

**Independent Test**: Create 25+ items, verify page 1 shows 20, click next → page 2 shows remainder; filter by category → shows subset

### Implementation for User Story 4

- [ ] T047 [US4] Implement pagination in InventoryItemsService
  - State: page, size (default 20)
  - Methods: setPage(page), getPage()
  - Update filters$ observable

- [ ] T048 [US4] Implement category filter in service
  - State: categoryId (null for all)
  - Method: setCategoryFilter(categoryId)
  - Call listItems() with updated params

- [ ] T049 [US4] Create PaginationComponent in `frontend/src/app/inventory-items/components/pagination/pagination.component.ts`
  - Inputs: currentPage, totalPages, pageSize
  - Outputs: pageChange event
  - Display: Previous, page numbers (or Page X of Y), Next buttons
  - Disable Previous on page 0, Next on last page

- [ ] T050 [US4] Create PaginationComponent template in `frontend/src/app/inventory-items/components/pagination/pagination.component.html`
  - Navigation controls
  - Current page display

- [ ] T051 [US4] Update ItemListComponent to use PaginationComponent
  - Display pagination below table
  - On page change → emit pageChange event → container calls service.setPage()

- [ ] T052 [US4] Update ItemListComponent to show category/location names (not IDs)
  - Service provides category/location lookup (from getCategories/getLocations)
  - Display readable names in list

- [ ] T053 [US4] Implement service method `setCategoryFilter(categoryId)` in service
  - Update filters$ state
  - Reset to page 0
  - Call listItems()

- [ ] T054 [US4] Implement service method `clearFilters()` in service
  - Reset all filters to defaults (page=0, status=null, categoryId=null)
  - Call listItems()

- [ ] T055 [US4] Add "Clear Filters" button to FilterToolbarComponent
  - Click → call service.clearFilters()
  - Disable if no active filters

- [ ] T056 [US4] Update InventoryItemsPageComponent to use FilterToolbarComponent
  - Display FilterToolbarComponent above ItemListComponent
  - Subscribe to filters$ to display current filter state

- [ ] T057 [P] [US4] Create spec tests for PaginationComponent in `frontend/src/app/inventory-items/components/pagination/pagination.component.spec.ts`
  - Test page navigation
  - Test button enable/disable logic
  - Test page change events

- [ ] T058 [P] [US4] Create spec tests for FilterToolbarComponent in `frontend/src/app/inventory-items/components/filter-toolbar/filter-toolbar.component.spec.ts`
  - Test filter dropdowns work
  - Test clear filters button
  - Test events emit correctly

- [ ] T059 [US4] Verify pagination params sent to API correctly
  - Test listItems() includes page, size in GET params
  - Test status filter sends status param
  - Test categoryId filter sends categoryId param

**Checkpoint**: User Story 4 complete - users can browse, filter, and paginate items

---

## Phase 7: User Story 5 - Delete Item Permanently (Priority: P2)

**Goal**: Enable users to permanently delete items with confirmation dialog

**Independent Test**: Delete item → confirmation dialog → confirm → item removed from list/not accessible

### Implementation for User Story 5

- [ ] T060 [P] [US5] Create ConfirmDialog component in `frontend/src/app/inventory-items/components/confirm-dialog/confirm-dialog.component.ts` (or reuse existing if available)
  - Input: title, message
  - Output: confirmed event
  - Display: confirm/cancel buttons

- [ ] T061 [US5] Implement service method `deleteItem(id)` in service
  - Call DELETE /v1/inventory-items/{id}
  - Remove from items$ state
  - Return Observable<void>

- [ ] T062 [US5] Add Delete button to ItemDetailComponent
  - Click → show confirmation dialog: "Are you sure you want to permanently delete this item?"
  - If confirmed → emit delete event

- [ ] T063 [US5] Implement delete in ItemDetailPageComponent
  - Receive delete event → show confirmation dialog
  - If confirmed → call service.deleteItem(id) → navigate back to list
  - Display error message if delete fails

- [ ] T064 [US5] Add Delete button to ItemListComponent (optional - action menu per item)
  - Or only in detail page

- [ ] T065 [P] [US5] Create spec tests for delete flow in `frontend/src/app/inventory-items/pages/item-detail-page/` tests
  - Test delete shows confirmation dialog
  - Test confirmation calls service.deleteItem()
  - Test item removed from state after delete

- [ ] T066 [US5] Verify item cannot be accessed after deletion
  - Try to navigate to deleted item → 404 error
  - Try to refresh list → deleted item not present

**Checkpoint**: User Story 5 complete - users can delete items with confirmation

---

## Phase 8: User Story 6 - Move Items Between Categories and Locations (Priority: P2)

**Goal**: Enable users to reassign items to different categories/locations via edit form

**Independent Test**: Edit item → change category → save → item appears in new category's filtered view, not old

### Implementation for User Story 6

- [ ] T067 [US6] Verify ItemFormComponent category dropdown works for edit
  - When editing item, category dropdown pre-populated with current value
  - Can select different category from dropdown
  - Save → service.updateItem() sends new categoryId

- [ ] T068 [US6] Verify ItemFormComponent location dropdown works for edit
  - When editing item, location dropdown pre-populated with current value
  - Can select different location from dropdown
  - Save → service.updateItem() sends new locationId

- [ ] T069 [US6] Test category change via edit flow
  - Edit item X (in Category A)
  - Change to Category B
  - Save
  - Refresh list with Category A filter → item X not shown
  - Refresh list with Category B filter → item X shown

- [ ] T070 [US6] Test location change via edit flow
  - Edit item X (in Location A)
  - Change to Location B
  - Save
  - Verify item detail shows Location B

- [ ] T071 [P] [US6] Create spec tests for category/location changes in `frontend/src/app/inventory-items/components/item-form/` tests
  - Test dropdown pre-population in edit mode
  - Test selection change
  - Test form submission with new values

**Checkpoint**: User Story 6 complete - users can reorganize items via category/location changes

---

## Phase 9: Integration & Polish

**Purpose**: Cross-feature integration, error handling refinement, validation

- [ ] T072 [P] Create inventory-items module file `frontend/src/app/inventory-items/inventory-items.module.ts` (if using modules instead of standalone)
  - Import all components, services
  - Configure routing

- [ ] T073 [P] Update app routing to include inventory-items routes
  - `/inventory/items` → InventoryItemsPageComponent
  - `/inventory/items/:id` → ItemDetailPageComponent

- [ ] T074 [P] Add navigation links to main app
  - Link to inventory items list in header/sidebar

- [ ] T075 Verify all error codes are handled
  - VALIDATION_ERROR → display field-level errors
  - SKU_DUPLICATE → show in form
  - CATEGORY_NOT_FOUND → show alert
  - LOCATION_NOT_FOUND → show alert
  - ITEM_NOT_FOUND → redirect to list
  - UNAUTHORIZED → redirect to login

- [ ] T076 [P] Test all form validation rules match backend
  - Name: required, max 255
  - Description: max 1000
  - SKU: max 100, unique per user
  - Unit: required, max 50
  - lowStockThreshold: >= 0
  - initialQuantity: >= 0

- [ ] T077 [P] Test date formatting pipe
  - Create date: "Aug 20, 2026, 2:30 PM"
  - Updated date: correct format

- [ ] T078 [P] Run 13 quickstart validation scenarios from `quickstart.md`
  - Scenario 1-13: all passing

- [ ] T079 [P] Performance testing
  - Create item: < 60 seconds
  - List load (20 items): < 2 seconds
  - Edit save: < 1 second
  - Archive/restore: < 1 second
  - Pagination: < 2 seconds

- [ ] T080 [P] Security verification
  - User data isolation: no cross-user access
  - Auth token sent with all requests
  - No sensitive data in local storage

- [ ] T081 Code cleanup and documentation
  - Add comments to complex logic
  - Update README with feature description
  - Document API integration points

- [ ] T082 [P] Final unit test run
  - All components with 80%+ coverage
  - All services with 90%+ coverage

- [ ] T083 Final integration test run
  - All 13 quickstart scenarios passing
  - No console errors/warnings
  - Performance metrics met

**Checkpoint**: Feature complete and ready for deployment

---

## Dependencies & Execution Order

### Phase Dependencies

1. **Setup (Phase 1)**: No dependencies - start immediately
2. **Foundational (Phase 2)**: Depends on Setup - **BLOCKS all user stories**
3. **User Stories (Phase 3-8)**:
   - All depend on Foundational (Phase 2) completion
   - **Can run in parallel** once Foundation is done:
     - US1 (P1) Create → can start after Foundational
     - US2 (P1) View/Edit → can start after Foundational (or after US1 for integrated testing)
     - US3 (P1) Archive → can start after Foundational (depends on US1 for testing)
     - US4 (P1) List/Filter → can start after Foundational
     - US5 (P2) Delete → can start after US1 (needs items to delete)
     - US6 (P2) Reorganize → can start after US1 (needs items to reorganize)
4. **Integration & Polish (Phase 9)**: Depends on all stories being complete

### User Story Dependencies

| Story | Depends On | Can Run Parallel With |
|-------|-----------|----------------------|
| US1 (Create) | Foundational | US2, US3, US4 |
| US2 (View/Edit) | Foundational + US1 data | US3, US4 |
| US3 (Archive) | Foundational + US1 data | US4 |
| US4 (List/Filter) | Foundational | US1, US2, US3 |
| US5 (Delete) | Foundational + US1 data | US6 (after US1) |
| US6 (Reorganize) | Foundational + US1 data + US2 | US5 (after US1) |

### Parallel Opportunities

**Phase 1 (Setup)**:
- T001-T004: All setup tasks can start immediately

**Phase 2 (Foundational)**:
- T005-T006: Model creation (parallel)
- T010-T013: Component creation (parallel)

**Phase 3-4 (US1 & US2)**:
- After Foundational: US1 and US2 can start in parallel
- Within US1: T015-T018 component creation (parallel)
- Within US2: T027-T028 component creation (parallel)

**Phase 5-6 (US3 & US4)**:
- After Foundational: US3 and US4 can start in parallel
- Within US3/US4: Component creation (parallel)

**Team Staffing Example** (3 developers):
1. **Sprint 1**: All developers complete Setup + Foundational (4-5 days)
2. **Sprint 2**: Developer A → US1, Developer B → US4, Developer C → US2 (parallel, 3-4 days)
3. **Sprint 3**: Developer A → US3, Developer B → US5, Developer C → US6 (parallel, 2-3 days)
4. **Sprint 4**: All developers → Integration & Polish (2-3 days)

---

## Implementation Strategy

### MVP First (User Story 1 Only)

Fastest path to demo/validation:

1. ✅ Complete Phase 1: Setup (3 tasks, 1 day)
2. ✅ Complete Phase 2: Foundational (10 tasks, 2-3 days)
3. ✅ Complete Phase 3: User Story 1 (12 tasks, 3-4 days)
4. 🎉 **STOP and VALIDATE**: Deploy, test Scenario 1-2 from quickstart.md
5. Users can create items → MVP delivered!

**Minimal Path**: T001-T004, T005-T014, T015-T026 (~60 tasks complete by Phase 3)

### Incremental Delivery Strategy

Add value with each completed user story:

1. **MVP**: US1 (Create) → Users can populate inventory
2. **v1.1**: US1 + US2 (Create + View/Edit) → Users can maintain data
3. **v1.2**: US1 + US2 + US3 (+ Archive) → Users can manage inactive items
4. **v1.3**: US1 + US2 + US3 + US4 (+ List/Filter) → Users can browse efficiently
5. **v1.4**: US1-4 + US5 (+ Delete) → Full CRUD available
6. **v1.5**: US1-5 + US6 (+ Reorganize) → Full feature complete

Each version is independently testable via quickstart scenarios.

### Parallel Team Strategy (3+ developers)

1. Developers A, B, C: Together complete Phases 1-2 (foundation)
2. Developer A continues → US1 (Create)
3. Developer B continues → US4 (List/Filter) - independent from US1
4. Developer C continues → US2 (View/Edit) - lightweight UI work
5. Once A finishes US1, can help B or C
6. Once C finishes, C starts US3 or US5
7. Stagger work so Phase 9 (Integration) starts with 1-2 developers

---

## Notes

- **[P] tasks**: Different files, no inter-task dependencies → can run in parallel
- **[Story] labels**: Map each task to a specific user story for traceability
- **File paths**: Exact locations enable LLM implementation without ambiguity
- **Each user story**: Independently completable and testable
- **Commit strategy**: Commit after each user story phase (at checkpoints)
- **Performance**: Target times specified in plan.md (60s create, 2s list, 1s edit, etc.)
- **Testing**: Use Vitest 4.0.8 + jsdom for unit tests, run 13 quickstart scenarios for e2e validation
- **Parallel opportunities**: When multiple tasks marked [P], they can be worked on simultaneously
- **MVP scope**: Complete Phase 1 + Phase 2 + Phase 3 (US1 only) for quickest demo

---

## Task Summary

| Phase | Tasks | Estimated Days (1 dev) | Purpose |
|-------|-------|------------------------|---------|
| **Setup** | T001-T004 (4 tasks) | 1 day | Initialize project structure |
| **Foundational** | T005-T014 (10 tasks) | 2-3 days | Core services, models, infrastructure |
| **US1 (Create)** | T015-T026 (12 tasks) | 3-4 days | **MVP: Create items** ← Deploy here |
| **US2 (View/Edit)** | T027-T036 (10 tasks) | 2-3 days | View and edit items |
| **US3 (Archive)** | T037-T046 (10 tasks) | 2-3 days | Archive/restore functionality |
| **US4 (List/Filter)** | T047-T059 (13 tasks) | 3-4 days | List, pagination, filtering |
| **US5 (Delete)** | T060-T066 (7 tasks) | 1-2 days | Delete with confirmation |
| **US6 (Reorganize)** | T067-T071 (5 tasks) | 1 day | Move between categories/locations |
| **Integration** | T072-T083 (12 tasks) | 2-3 days | Polish, testing, deployment |
| **TOTAL** | **83 tasks** | **~17-25 days (1 dev)** or **~5-7 days (3 devs parallel)** | Full feature complete |

---

## Ready to Implement

✅ All tasks specified with exact file paths  
✅ User story mapping clear (US1-US6 labels)  
✅ Dependencies identified (can run tasks in parallel within phases)  
✅ MVP scope defined (Phases 1-2 + Phase 3 US1 only)  
✅ Incremental delivery strategy ready  
✅ Performance targets specified  

**Next Step**: Begin Phase 1 Setup tasks
