# Tasks: Stock Movements UI

**Input**: Design documents from `/specs/007-stock-movements-ui/`

**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**Status**: All design phases complete; ready for implementation

**Organization**: Tasks organized by user story (6 stories total, all P1 priority) to enable independent implementation and testing.

---

## Format: `- [ ] [ID] [P?] [Story] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: US1-US6 (maps to user stories from spec.md)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and Angular component structure

- [ ] T001 Create stock movements component directory structure: `frontend/src/app/stock-movements/` with subdirectories `movement-form/`, `movement-history-modal/`, `shared/`
- [ ] T002 Create models directory and file: `frontend/src/app/models/stock-movement.model.ts` with all enums and interfaces from data-model.md
- [ ] T003 Create services directory and file: `frontend/src/app/services/stock-movement.service.ts` (service structure only, no implementation yet)
- [ ] T004 [P] Create unit test directory structure: `frontend/tests/unit/stock-movements/` with subdirectories for each component
- [ ] T005 [P] Create e2e test directory structure: `frontend/tests/e2e/` for end-to-end tests

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Implement StockMovementService API methods: `createMovement()` and `getMovementHistory()` in `frontend/src/app/services/stock-movement.service.ts`
- [ ] T007 Implement loading$ and error$ observable streams in `frontend/src/app/services/stock-movement.service.ts`
- [ ] T008 Add HTTP error handling and error message extraction in `frontend/src/app/services/stock-movement.service.ts`
- [ ] T009 Create shared form validators for quantity, reason, and date fields in `frontend/src/app/stock-movements/shared/validators.ts`
- [ ] T010 [P] Create display model service for formatting movements (enums → labels) in `frontend/src/app/stock-movements/shared/display-model.service.ts`
- [ ] T011 Setup RxJS operators and observables patterns in stock-movement.service.ts (finalize, tap, catchError)
- [ ] T012 Configure API base URL and endpoints (check environment configuration) in `frontend/src/environments/`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - View Item Details with Current Stock (Priority: P1) 🎯

**Goal**: Display the current stock quantity on the item detail page, reflecting all applied stock movements.

**Independent Test**: Navigate to item detail page and verify current quantity is displayed and matches backend state after refreshing.

### Implementation for User Story 1

- [ ] T013 [P] [US1] Enhance existing item-detail component to display current quantity: `frontend/src/app/components/item-detail/item-detail.component.ts` (update template to show quantity)
- [ ] T014 [P] [US1] Update item-detail component template to show current quantity prominently: `frontend/src/app/components/item-detail/item-detail.component.html`
- [ ] T015 [US1] Modify ItemService to fetch currentQuantity from item API response and expose via observable stream: `frontend/src/app/services/item.service.ts`
- [ ] T016 [US1] Handle zero or missing movements gracefully: update item-detail template to display "0" or "No movements recorded" appropriately
- [ ] T017 [US1] Create unit tests for current quantity display in `frontend/tests/unit/stock-movements/current-quantity.spec.ts`
- [ ] T018 [US1] Verify responsive design for current quantity display (mobile, tablet, desktop) in item-detail template

**Checkpoint**: User Story 1 complete and independently testable. Current quantity displayed correctly on item detail page.

---

## Phase 4: User Story 2 - Record Stock In Movement (Priority: P1)

**Goal**: Provide a form for users to record stock in movements (inbound stock), with validation and success feedback.

**Independent Test**: Submit a valid stock in form; verify form closes, success notification appears, current quantity increases, and backend API called correctly.

### Implementation for User Story 2

- [ ] T019 [P] [US2] Create StockInFormComponent: `frontend/src/app/stock-movements/movement-form/stock-in-form.component.ts` with reactive form (quantity, reason, date fields)
- [ ] T020 [P] [US2] Create StockInFormComponent template: `frontend/src/app/stock-movements/movement-form/stock-in-form.component.html` with form fields, validation messages, loading state
- [ ] T021 [US2] Implement form submission logic in StockInFormComponent: call service.createMovement(), handle success (close modal, show toast), handle error (display in form)
- [ ] T022 [US2] Implement form validation in StockInFormComponent: quantity required and > 0, reason max 500 chars, date optional
- [ ] T023 Research and determine notification service strategy: Check if frontend already has @angular/material snackbar or ng-bootstrap toast component library in package.json; decide: use existing Material snackbar, use ng-bootstrap toast, or create custom NotificationService. Document choice in comments and update T024 accordingly. File: `frontend/package.json` and `frontend/src/app/services/`
- [ ] T024 [US2] Implement notification/toast display (based on T023 decision): if Material use MatSnackBar, if ng-bootstrap use ToastrService, or implement custom NotificationService at `frontend/src/app/services/notification.service.ts`
- [ ] T025 [US2] Add "Record Stock In" button to item-detail component and implement modal open logic: `frontend/src/app/components/item-detail/item-detail.component.ts`
- [ ] T026 [US2] Implement modal dialog for stock in form (use Material Dialog or ng-bootstrap Modal): update item-detail to open modal and pass itemId
- [ ] T027 [US2] Handle form close event (user cancels) and refresh item quantity: ensure modal closes and cleanup
- [ ] T028 [US2] Create unit tests for StockInFormComponent in `frontend/tests/unit/stock-movements/stock-in-form.spec.ts`
- [ ] T029 [P] [US2] Create e2e test for stock in workflow in `frontend/tests/e2e/stock-in.spec.ts` (from form open → submit → close → quantity updated)

**Checkpoint**: User Story 2 complete. Stock in form works independently; current quantity updates after successful submission.

---

## Phase 5: User Story 3 - Record Stock Out Movement (Priority: P1)

**Goal**: Provide a form for users to record stock out movements (outbound stock), with validation to prevent negative quantities and error feedback.

**Independent Test**: Submit a stock out form with both valid and invalid quantities; verify validation works, error shown, and valid submission succeeds.

### Implementation for User Story 3

- [ ] T030 [P] [US3] Create StockOutFormComponent: `frontend/src/app/stock-movements/movement-form/stock-out-form.component.ts` with reactive form (quantity, reason, date fields)
- [ ] T031 [P] [US3] Create StockOutFormComponent template: `frontend/src/app/stock-movements/movement-form/stock-out-form.component.html` with form fields and validation messages
- [ ] T032 [US3] Implement form submission logic in StockOutFormComponent: call service.createMovement() with movementType STOCK_OUT
- [ ] T033 [US3] Add quantity max validation (≤ current quantity) in StockOutFormComponent: accept currentQuantity as input and validate
- [ ] T034 [US3] Handle backend validation errors (e.g., "Stock out would make quantity negative") and display in form
- [ ] T035 [US3] Add "Record Stock Out" button to item-detail component and implement modal open logic: `frontend/src/app/components/item-detail/item-detail.component.ts`
- [ ] T036 [US3] Implement modal dialog for stock out form: update item-detail to open modal and pass itemId and currentQuantity
- [ ] T037 [US3] Create unit tests for StockOutFormComponent in `frontend/tests/unit/stock-movements/stock-out-form.spec.ts`
- [ ] T038 [P] [US3] Create e2e test for stock out workflow (valid and invalid quantities) in `frontend/tests/e2e/stock-out.spec.ts`

**Checkpoint**: User Story 3 complete. Stock out form validates correctly; prevents invalid submissions; current quantity decreases after valid submission.

---

## Phase 6: User Story 4 - Record Adjustment Movement (Priority: P1)

**Goal**: Provide a form for users to record adjustment movements with explicit direction (increase/decrease) selection.

**Independent Test**: Submit adjustment forms with both increase and decrease directions; verify direction recorded correctly and quantity updated appropriately.

### Implementation for User Story 4

- [ ] T039 [P] [US4] Create AdjustmentFormComponent: `frontend/src/app/stock-movements/movement-form/adjustment-form.component.ts` with reactive form (quantity, direction, reason, date fields)
- [ ] T040 [P] [US4] Create AdjustmentFormComponent template: `frontend/src/app/stock-movements/movement-form/adjustment-form.component.html` with direction radio buttons or dropdown, validation messages
- [ ] T041 [US4] Implement form submission logic in AdjustmentFormComponent: call service.createMovement() with movementType ADJUSTMENT and adjustmentDirection
- [ ] T042 [US4] Implement adjustment direction validation: direction is required for adjustment type
- [ ] T043 [US4] Add dynamic quantity max validation based on direction: if DECREASE, max ≤ current quantity
- [ ] T044 [US4] Handle backend validation errors for decreases that would make quantity negative
- [ ] T045 [US4] Add "Record Adjustment" button to item-detail component and implement modal open logic: `frontend/src/app/components/item-detail/item-detail.component.ts`
- [ ] T046 [US4] Implement modal dialog for adjustment form: update item-detail to open modal and pass itemId and currentQuantity
- [ ] T047 [US4] Create unit tests for AdjustmentFormComponent in `frontend/tests/unit/stock-movements/adjustment-form.spec.ts`
- [ ] T048 [P] [US4] Create e2e test for adjustment workflow (increase and decrease) in `frontend/tests/e2e/adjustment.spec.ts`

**Checkpoint**: User Story 4 complete. Adjustment form with direction clearly displayed and validated. Quantity updated correctly for both increase and decrease.

---

## Phase 7: User Story 5 - View Movement History (Priority: P1)

**Goal**: Provide a modal dialog where users can view complete movement history for an item with all details.

**Independent Test**: Click "View History" button; verify modal opens, all movements displayed with correct details, in chronological order (oldest first).

### Implementation for User Story 5

- [ ] T049 [P] [US5] Create MovementHistoryModalComponent: `frontend/src/app/stock-movements/movement-history-modal/movement-history-modal.component.ts` with history list display
- [ ] T050 [P] [US5] Create MovementHistoryModalComponent template: `frontend/src/app/stock-movements/movement-history-modal/movement-history-modal.component.html` with movement table/list and modal controls
- [ ] T051 [US5] Implement data loading in MovementHistoryModalComponent: fetch movements via service.getMovementHistory(), display loading state
- [ ] T052 [US5] Format movements for display: convert enums to labels, format dates via Angular DatePipe, handle null fields (adjustment direction for non-adjustments)
- [ ] T053 [US5] Implement movements list rendering: show type, quantity, direction (if applicable), reason, movementDate, createdDate
- [ ] T054 [US5] Ensure movements displayed in chronological order (oldest first): verify sort order from backend or implement client-side sort if needed
- [ ] T055 [US5] Handle empty history: display message "No movements recorded" when list is empty
- [ ] T056 [US5] Add "View Movement History" button to item-detail component: `frontend/src/app/components/item-detail/item-detail.component.ts`
- [ ] T057 [US5] Implement modal dialog for history: open MovementHistoryModalComponent in Material Dialog or ng-bootstrap Modal, pass itemId
- [ ] T058 [US5] Implement modal close functionality: close button and click-outside-to-close
- [ ] T059 [US5] Create unit tests for MovementHistoryModalComponent in `frontend/tests/unit/stock-movements/movement-history-modal.spec.ts`
- [ ] T060 [P] [US5] Create e2e test for movement history workflow in `frontend/tests/e2e/movement-history.spec.ts`

**Checkpoint**: User Story 5 complete. Movement history modal displays all movements correctly with proper formatting and order.

---

## Phase 8: User Story 6 - Filter Movement History by Date Range (Priority: P1)

**Goal**: Provide date filters in the history modal to allow users to focus on specific time periods.

**Independent Test**: Apply date filters to history; verify only movements within range displayed; clear filters; verify all movements return.

### Implementation for User Story 6

- [ ] T061 [US6] Add date filter fields to MovementHistoryModalComponent template: start date and end date inputs, "Apply Filter" and "Clear Filters" buttons in `frontend/src/app/stock-movements/movement-history-modal/movement-history-modal.component.html`
- [ ] T062 [US6] Implement date filter logic in MovementHistoryModalComponent: create form group for filters, handle apply/clear button clicks
- [ ] T063 [US6] Implement filter submission: call service.getMovementHistory() with startDate and endDate parameters when Apply clicked; show loading spinner during fetch
- [ ] T064 [US6] Update movements list when filters applied: show filtered results, maintain loading state during fetch, disable Apply button while loading
- [ ] T065 [US6] Implement clear filters: reset form, fetch all movements (no date filter parameters)
- [ ] T066 [US6] Handle empty filtered results: display message "No movements in this date range" when filter returns nothing
- [ ] T067 [US6] Format dates correctly for API submission: ensure ISO 8601 YYYY-MM-DD format sent to backend
- [ ] T068 [US6] Create unit tests for filter logic in `frontend/tests/unit/stock-movements/movement-history-filters.spec.ts`
- [ ] T069 [P] [US6] Create e2e test for date filtering in `frontend/tests/e2e/movement-history-filtering.spec.ts`

**Checkpoint**: User Story 6 complete. Date filters work correctly; movements filtered and displayed as expected.

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements affecting multiple user stories and overall quality

- [ ] T070 [P] Implement comprehensive error handling for all API calls: ensure all backend errors gracefully displayed to user with friendly messages
- [ ] T071 [P] Add responsive design CSS/styling for all components: ensure mobile, tablet, desktop views work correctly (part of SC-007)
- [ ] T072 [P] Add accessibility attributes: aria labels, tab order, keyboard navigation for forms and modals
- [ ] T073 [P] Add loading indicators for form submissions and history fetches: disable buttons, show spinners
- [ ] T074 Implement performance optimization: RxJS unsubscribe patterns, memory leak prevention in components (ngOnDestroy)
- [ ] T075 Add browser console logging for debugging (development mode): log API calls, state changes
- [ ] T076 [P] Additional unit tests for edge cases: empty quantities, max reason length, past/future dates
- [ ] T077 Refactor shared form validators: ensure consistent validation across all movement forms
- [ ] T078 [P] Integration tests: test multiple user stories together (record movement → check history → filter)
- [ ] T079 Run quickstart.md validation scenarios: verify all 11 test scenarios pass end-to-end
- [ ] T080 [P] Code cleanup and refactoring: extract common patterns, remove duplication
- [ ] T081 Verify responsive design on actual mobile devices (if available): phone, tablet tests
- [ ] T082 Document API error codes and how UI handles each: reference contracts/stock-movement-api-client.md

**Checkpoint**: Feature complete with polish and testing coverage.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - **BLOCKS all user stories**
- **User Stories 1-6 (Phases 3-8)**: All depend on Foundational phase completion
  - User stories can proceed in parallel (if staffed)
  - Or sequentially in priority order (US1 → US2 → ... → US6)
- **Polish (Phase 9)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1** (View Current Stock): Can start after Foundational
  - Independent - no dependencies on other stories
  - Unblocks: US2, US3, US4, US5, US6 (all need US1 context)

- **User Story 2** (Record Stock In): Can start after Foundational + US1
  - Independent implementation - can run in parallel with US3, US4
  - Depends on: US1 (conceptually - item detail context)

- **User Story 3** (Record Stock Out): Can start after Foundational + US1
  - Independent implementation - can run in parallel with US2, US4
  - Depends on: US1 (conceptually)

- **User Story 4** (Record Adjustment): Can start after Foundational + US1
  - Independent implementation - can run in parallel with US2, US3
  - Depends on: US1 (conceptually)

- **User Story 5** (View History): Can start after Foundational + US1
  - Independent implementation - can run in parallel with US2-US4
  - Depends on: US1 (item detail context)

- **User Story 6** (Filter History): Can start after Foundational + US5
  - Depends on: US5 (history modal)

### Parallel Opportunities

**Parallel Phase 1** (Setup):
- T001, T002, T003 can run sequentially (dependencies on each other)
- T004, T005 can run in parallel (independent directories)

**Parallel Phase 2** (Foundational):
- T006, T007, T008, T009 can run sequentially (service implementation)
- T010, T011, T012 can run in parallel (independent utilities)

**Parallel after Foundational** (User Stories):

Once Foundational complete, with sufficient team capacity:

```
Developer A: User Story 1 (US1 tasks T013-T018)
              → Complete US1 first (foundation for others)

Developer B: User Story 2 (US2 tasks T019-T028)
              → Can start once US1 complete
              → Forms/services independent

Developer C: User Story 3 (US3 tasks T029-T037)
              → Can start once US1 complete
              → Parallel with Developer B

Developer D: User Story 4 (US4 tasks T038-T046)
              → Can start once US1 complete
              → Parallel with Developers B & C

Developer E: User Story 5 (US5 tasks T047-T058)
              → Can start once US1 complete
              → Parallel with other story developers

Developer F: User Story 6 (US6 tasks T059-T067)
              → Must wait for US5 complete
              → Then can implement filters in parallel
```

### Within Each User Story

- Test tasks marked [P] can run in parallel (independent)
- Model/service tasks run first, then component implementation
- Component template updates follow TypeScript implementation
- Unit tests can run after component code

---

## Implementation Strategy

### MVP First (User Story 1 Only)

Minimum viable product focuses on foundation:

1. Complete Phase 1: Setup (T001-T005)
2. Complete Phase 2: Foundational (T006-T012) ← **CRITICAL BLOCKER**
3. Complete Phase 3: User Story 1 (T013-T018)
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo MVP if ready

**Time estimate**: ~2-3 days for MVP

### Incremental Delivery (All Stories)

Add functionality incrementally:

1. Complete Setup + Foundational → Foundation ready (Day 1)
2. Add User Story 1 → Test independently → Demo (MVP!) (Day 2)
3. Add User Stories 2-4 in parallel or sequence → Test independently → Integrate (Days 3-5)
4. Add User Story 5 → Test independently → Integrate (Day 6)
5. Add User Story 6 → Test independently → Integrate (Day 7)
6. Polish & testing → Final validation (Day 8-9)

**Each story**: independently completable, testable, deployable

### Parallel Team Strategy (6 developers, 2 weeks)

**Week 1:**
- Everyone: Phase 1 Setup + Phase 2 Foundational (Days 1-2)
- Developer A: User Story 1 (complete by Day 3)
- Developers B-D: User Stories 2-4 in parallel (Days 3-5)
- Developers E-F: Validation/review (Days 4-5)

**Week 2:**
- Developer A: User Story 5 (Days 6-7)
- Developer B: User Story 6 (depends on US5; Days 8-9)
- Developers C-D: Polish & testing (Days 8-9)
- Everyone: Final validation with quickstart (Day 10)

---

## Validation Checkpoints

### After User Story 1 (Current Quantity Display)
- [ ] Current quantity displays on item detail page
- [ ] Value matches backend state
- [ ] Refreshing page updates quantity
- [ ] Responsive on mobile/tablet/desktop

### After User Story 2 (Stock In)
- [ ] Form opens/closes correctly
- [ ] Valid submission updates quantity
- [ ] Invalid quantity rejected with error
- [ ] Toast notification appears on success
- [ ] Current quantity increases as expected

### After User Story 3 (Stock Out)
- [ ] Form opens/closes correctly
- [ ] Valid submission decreases quantity
- [ ] Invalid quantity (exceeds stock) rejected with error
- [ ] Backend validation error displayed
- [ ] Current quantity decreases correctly

### After User Story 4 (Adjustment)
- [ ] Form displays direction field clearly
- [ ] Both increase and decrease directions work
- [ ] Invalid decrease (would go negative) rejected
- [ ] Quantity updates correctly for both directions

### After User Story 5 (History)
- [ ] History modal opens from button click
- [ ] All movements displayed with correct details
- [ ] Movements in chronological order (oldest first)
- [ ] Empty history handled gracefully

### After User Story 6 (Filtering)
- [ ] Date filters appear in history modal
- [ ] Apply filter works correctly
- [ ] Clear filters returns all movements
- [ ] Empty filtered results handled

### After Polish Phase
- [ ] All error messages are user-friendly
- [ ] Responsive design verified (mobile, tablet, desktop)
- [ ] All 11 quickstart scenarios pass
- [ ] No console errors or warnings
- [ ] Performance: form submission < 2 seconds
- [ ] Accessibility: keyboard navigation works

---

## Notes

- [P] = parallelizable tasks (no file conflicts, no cross-task dependencies)
- [US#] = which user story a task belongs to
- Each user story independently testable and deliverable
- Avoid: breaking changes between stories, cross-story tight coupling
- Commit frequently: after each task or logical group
- Can deploy feature incrementally: MVP after US1, more features after each story

---

## Total Task Count: 82 tasks

**Breakdown by Phase**:
- Phase 1 (Setup): 5 tasks
- Phase 2 (Foundational): 7 tasks
- Phase 3 (US1): 6 tasks
- Phase 4 (US2): 10 tasks (+1 decision task = 11 tasks)
- Phase 5 (US3): 9 tasks
- Phase 6 (US4): 10 tasks
- Phase 7 (US5): 12 tasks
- Phase 8 (US6): 9 tasks
- Phase 9 (Polish): 13 tasks

**Parallelizable tasks**: ~26 tasks marked [P]

**Estimated Timeline** (with standard team):
- Solo developer: 10-12 days
- 2 developers: 6-8 days
- 4 developers: 4-5 days
- 6+ developers: 3-4 days (limited by Foundational phase blocker)

**MVP (User Story 1 only)**: 2-3 days for solo developer
