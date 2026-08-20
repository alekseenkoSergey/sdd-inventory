# Tasks: Stock Movements Backend API

**Input**: Design documents from `specs/006-stock-movements/`

**Prerequisites**: plan.md, spec.md, data-model.md, research.md, contracts/

**Format**: `[ID] [P?] [Story] Description with exact file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1, US2, US3, US4, US5)
- **Exact paths**: All paths reference actual files to be created/modified

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and database schema foundation

- [x] T001 Create database migration for stock_movement table in `backend/src/main/resources/db/migration/V{next}__Create_stock_movement_table.sql`
- [x] T002 [P] Create MovementType enum in `backend/src/main/java/org/example/sddinventory/entity/MovementType.java`
- [x] T003 [P] Create AdjustmentDirection enum in `backend/src/main/java/org/example/sddinventory/entity/AdjustmentDirection.java`
- [x] T004 [P] Create validation exceptions in `backend/src/main/java/org/example/sddinventory/exception/InvalidQuantityException.java`
- [x] T005 [P] Create validation exceptions in `backend/src/main/java/org/example/sddinventory/exception/NegativeQuantityException.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core models and infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 Create StockMovement entity in `backend/src/main/java/org/example/sddinventory/entity/StockMovement.java` (with @PrePersist validation)
- [x] T007 Create StockMovementRequestDTO in `backend/src/main/java/org/example/sddinventory/model/StockMovementRequestDTO.java` (with validation annotations)
- [x] T008 Create StockMovementResponseDTO in `backend/src/main/java/org/example/sddinventory/model/StockMovementResponseDTO.java` (with fromEntity factory)
- [x] T009 Enhance Item entity with movement awareness in `backend/src/main/java/org/example/sddinventory/entity/InventoryItem.java` (add updateCurrentQuantityFromMovement and validateMovement methods)
- [x] T010 Create StockMovementRepository in `backend/src/main/java/org/example/sddinventory/repository/StockMovementRepository.java` (Spring Data interface with query methods)

**Checkpoint**: Foundation ready - all models and repositories in place for user stories to begin

---

## Phase 3: User Story 1 - Record Opening Balance (Priority: P1) 🎯 MVP

**Goal**: Automatically create opening balance movement when item is created with quantity > 0

**Independent Test**: Can be fully tested by creating item with initial quantity and verifying opening balance movement exists

### Implementation for User Story 1

- [x] T011 [P] [US1] Create ItemService enhancement in `backend/src/main/java/org/example/sddinventory/service/ItemService.java` - add createOpeningBalanceMovement method (depends on T006)
- [x] T012 [US1] Implement StockMovementService in `backend/src/main/java/org/example/sddinventory/service/StockMovementService.java` - create recordOpeningBalance method
- [x] T013 [US1] Integrate opening balance creation into item creation workflow - modify item creation endpoint to trigger T012 (depends on T012)
- [x] T014 [US1] Add unit tests for opening balance auto-creation in `backend/src/test/java/org/example/sddinventory/service/StockMovementServiceTest.java` (test recordOpeningBalance with quantity > 0 and quantity = 0)
- [ ] T015 [US1] Add integration test for item creation with opening balance in `backend/src/test/java/org/example/sddinventory/controller/ItemControllerTest.java` (verify movement history after item creation)

**Checkpoint**: User Story 1 complete and independently testable - items can be created with automatic opening balance movements

---

## Phase 4: User Story 2 - Record Stock In Movements (Priority: P1)

**Goal**: Record inbound stock and update current quantity

**Independent Test**: Can be fully tested by creating stock in movement and verifying quantity updated and movement persisted

### Implementation for User Story 2

- [x] T016 [P] [US2] Implement recordStockIn in `backend/src/main/java/org/example/sddinventory/service/StockMovementService.java` (depends on T006, T009)
- [x] T017 [P] [US2] Create POST /items/{itemId}/movements endpoint in `backend/src/main/java/org/example/sddinventory/controller/StockMovementController.java` (depends on T012, T010)
- [x] T018 [US2] Add business logic for stock in quantity validation in StockMovementService - ensure quantity > 0 (depends on T016)
- [x] T019 [US2] Add unit tests for recordStockIn in `backend/src/test/java/org/example/sddinventory/service/StockMovementServiceTest.java` (test quantity updates correctly)
- [ ] T020 [US2] Add integration test for stock in endpoint in `backend/src/test/java/org/example/sddinventory/controller/StockMovementControllerTest.java` (POST /items/{id}/movements with STOCK_IN type)
- [x] T021 [US2] Add error handling for invalid stock in (quantity <= 0) - update exception handling in controller (depends on T017)

**Checkpoint**: User Story 2 complete - stock in movements can be recorded and current quantity updated correctly

---

## Phase 5: User Story 3 - Record Stock Out Movements (Priority: P1)

**Goal**: Record outbound stock with validation to prevent negative inventory

**Independent Test**: Can be fully tested by recording stock out and verifying rejection if quantity would go negative

### Implementation for User Story 3

- [x] T022 [P] [US3] Implement recordStockOut in `backend/src/main/java/org/example/sddinventory/service/StockMovementService.java` with negative quantity prevention (depends on T006, T009)
- [x] T023 [US3] Add negative quantity validation in Item.validateMovement for STOCK_OUT type (depends on T009)
- [x] T024 [US3] Handle stock out error responses in controller - return 400 with error message when quantity would go negative (depends on T017)
- [x] T025 [US3] Add unit tests for recordStockOut in `backend/src/test/java/org/example/sddinventory/service/StockMovementServiceTest.java` (test valid stock out and rejection cases)
- [ ] T026 [US3] Add integration test for stock out endpoint in `backend/src/test/java/org/example/sddinventory/controller/StockMovementControllerTest.java` (test valid and invalid stock out scenarios)
- [ ] T027 [US3] Document error response format for negative quantity in `specs/006-stock-movements/contracts/stock-movement-api.md` (update API contract if needed)

**Checkpoint**: User Story 3 complete - stock out movements recorded with proper validation and error handling

---

## Phase 6: User Story 4 - Record Adjustment Movements (Priority: P1)

**Goal**: Record inventory adjustments with explicit direction (increase/decrease) and validate against negative inventory

**Independent Test**: Can be fully tested by recording adjustments with both directions and verifying direction-dependent behavior

### Implementation for User Story 4

- [x] T028 [P] [US4] Implement recordAdjustment in `backend/src/main/java/org/example/sddinventory/service/StockMovementService.java` with direction logic (depends on T006, T009)
- [x] T029 [US4] Add adjustment direction requirement validation - ensure direction present for ADJUSTMENT type (depends on T007)
- [x] T030 [US4] Add negative quantity validation for adjustment decrease in Item.validateMovement (depends on T009)
- [x] T031 [US4] Handle adjustment error responses - return 400 if direction missing or quantity would go negative (depends on T017)
- [x] T032 [US4] Add unit tests for recordAdjustment in `backend/src/test/java/org/example/sddinventory/service/StockMovementServiceTest.java` (test increase, decrease, and error cases)
- [ ] T033 [US4] Add integration test for adjustment endpoint in `backend/src/test/java/org/example/sddinventory/controller/StockMovementControllerTest.java` (test both directions and error scenarios)

**Checkpoint**: User Story 4 complete - adjustments can be recorded with direction and proper validation

---

## Phase 7: User Story 5 - Query Movement History (Priority: P2)

**Goal**: Retrieve complete, chronologically ordered movement history for an item with optional date filtering

**Independent Test**: Can be fully tested by creating multiple movements and verifying history query returns all movements in order

### Implementation for User Story 5

- [x] T034 [P] [US5] Implement getMovementHistory in `backend/src/main/java/org/example/sddinventory/service/StockMovementService.java` with optional date filtering (depends on T006, T010)
- [x] T035 [P] [US5] Create GET /items/{itemId}/movements endpoint in `backend/src/main/java/org/example/sddinventory/controller/StockMovementController.java` (depends on T017, T010)
- [x] T036 [US5] Add query method to StockMovementRepository for finding by itemId and date range (depends on T010)
- [x] T037 [US5] Ensure movements returned in chronological order (by createdDate ascending) in getMovementHistory (depends on T034)
- [ ] T038 [US5] Add unit tests for getMovementHistory in `backend/src/test/java/org/example/sddinventory/service/StockMovementServiceTest.java` (test with/without date filter, ordering)
- [ ] T039 [US5] Add integration test for history endpoint in `backend/src/test/java/org/example/sddinventory/controller/StockMovementControllerTest.java` (GET /items/{id}/movements with optional filters)
- [ ] T040 [US5] Add 404 handling for non-existent item in movement history endpoint (depends on T035)

**Checkpoint**: User Story 5 complete - complete movement history queryable with optional date filtering

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Quality, documentation, and edge case handling

- [ ] T041 [P] Add comprehensive error handling documentation in `specs/006-stock-movements/contracts/stock-movement-api.md` (all error responses with examples)
- [ ] T042 [P] Create StockMovement unit tests for edge cases in `backend/src/test/java/org/example/sddinventory/entity/StockMovementTest.java` (PrePersist validation, enum serialization)
- [ ] T043 [P] Create Item validation tests in `backend/src/test/java/org/example/sddinventory/entity/ItemTest.java` (updateCurrentQuantityFromMovement and validateMovement edge cases)
- [ ] T044 [P] Add logging for stock movements in StockMovementService - log all successful operations and validation failures
- [ ] T045 Create API documentation for all three endpoints in `specs/006-stock-movements/contracts/stock-movement-api.md` (examples, error responses, rate limits)
- [ ] T046 Run quickstart.md validation scenarios against running implementation in `specs/006-stock-movements/quickstart.md` - verify all 11 scenarios pass
- [ ] T047 Add concurrency test for last-write-wins in `backend/src/test/java/org/example/sddinventory/service/StockMovementServiceTest.java` (concurrent movements, verify correct quantity)
- [ ] T048 Add input validation tests for reason field (max 500 chars) in StockMovementServiceTest
- [ ] T049 Code review: Ensure no direct editing of Item.currentQuantity outside of stock movements
- [ ] T050 Final integration test: Complete workflow (create item → stock in → stock out → adjustment → query history) in `backend/src/test/java/org/example/sddinventory/integration/StockMovementWorkflowTest.java`

---

## Dependencies & Execution Order

### Phase Dependencies

```
Phase 1 (Setup) ← Start here
       ↓
Phase 2 (Foundational) ← MUST complete before user stories
       ↓
Phases 3-7 (User Stories) ← Can run in parallel OR sequentially (P1 stories first)
       ↓
Phase 8 (Polish) ← After all desired stories complete
```

### User Story Dependencies

- **US1 (Opening Balance)**: Depends on Phase 2. Can start immediately after foundational.
- **US2 (Stock In)**: Depends on Phase 2 + US1 foundation should exist (but can implement in parallel).
- **US3 (Stock Out)**: Depends on Phase 2 + US2 foundation should exist (but can implement in parallel).
- **US4 (Adjustment)**: Depends on Phase 2 + US3 foundation (but can implement in parallel).
- **US5 (Query History)**: Depends on Phase 2 + all other stories (for complete history testing, but can implement in parallel).

### Within Each User Story

- Service methods before endpoints
- Unit tests before integration tests
- Happy path before error handling

### Parallel Opportunities

**Phase 1 (Setup)** - All marked [P]:
```bash
- T002: MovementType enum
- T003: AdjustmentDirection enum
- T004: InvalidQuantityException
- T005: NegativeQuantityException
# Can all run in parallel - different files
```

**Phase 2 (Foundational)**:
```bash
- T006, T007, T008: Can run in parallel (different DTOs/entities)
- T009, T010: Can run after previous complete
```

**After Phase 2**:
```bash
- US1, US2, US3, US4 implementation can run in parallel (different services, tests)
- T016 + T017 (recordStockIn + endpoint) can run in parallel within US2
- Similar for US3, US4
- US5 can start as soon as repository ready
```

**Phase 8 (Polish)** - All marked [P]:
```bash
- T041, T042, T043, T044: Documentation, tests, logging can run in parallel
```

---

## Implementation Strategy

### MVP First (User Stories 1-4 Only)

**Goal**: Core stock movement functionality without history queries

1. ✅ Complete Phase 1: Setup (enums, exceptions, migrations)
2. ✅ Complete Phase 2: Foundational (models, DTOs, repositories)
3. ✅ Complete Phase 3: US1 (opening balance auto-creation)
4. ✅ Complete Phase 4: US2 (stock in recording)
5. ✅ Complete Phase 5: US3 (stock out with validation)
6. ✅ Complete Phase 6: US4 (adjustments with direction)
7. 🛑 STOP and VALIDATE: Run basic tests, create items, record movements, verify quantities
8. **DEPLOY MVP**: Core movement recording functionality ready

### Incremental Delivery (All Stories)

1. MVP (above) → Deploy
2. Add Phase 7: US5 (movement history queries) → Test independently → Deploy
3. Add Phase 8: Polish (error handling, edge cases, logging) → Final testing → Deploy

### Parallel Team Strategy (if multiple developers)

**With 5 developers after Phase 2 complete**:

```
Developer 1: Phase 3 (US1 - Opening Balance)
Developer 2: Phase 4 (US2 - Stock In)
Developer 3: Phase 5 (US3 - Stock Out)
Developer 4: Phase 6 (US4 - Adjustment)
Developer 5: Phase 7 (US5 - Query History)

Once complete, all integrate and Polish phase begins together
```

---

## Task Count Summary

- **Phase 1 (Setup)**: 5 tasks
- **Phase 2 (Foundational)**: 5 tasks
- **Phase 3 (US1)**: 5 tasks
- **Phase 4 (US2)**: 6 tasks
- **Phase 5 (US3)**: 6 tasks
- **Phase 6 (US4)**: 6 tasks
- **Phase 7 (US5)**: 7 tasks
- **Phase 8 (Polish)**: 10 tasks
- **Total**: 50 tasks

### By User Story

- **US1 (P1)**: 5 tasks
- **US2 (P1)**: 6 tasks
- **US3 (P1)**: 6 tasks
- **US4 (P1)**: 6 tasks
- **US5 (P2)**: 7 tasks

---

## MVP Scope (Recommended)

**Minimum Viable Product** = User Stories 1-4 (opening balance, stock in, stock out, adjustment)

- **What's included**: Full movement recording with validation and quantity updates
- **What's excluded**: Movement history queries (US5)
- **Task count**: 22 tasks (Phases 1, 2, 3, 4, 5, 6)
- **Time estimate**: 2-3 days with 1-2 developers
- **User value**: Core inventory movement tracking with audit trail
- **Business value**: Operators can record all stock changes; quantities stay accurate

**Deliver MVP first**, then add history queries (US5) and polish (Phase 8).

---

## Validation Checkpoints

After each phase, validate independently:

**After Phase 1**: Database migration applies successfully, enums defined, exceptions importable
**After Phase 2**: Models persist/deserialize correctly, repositories query successfully
**After Phase 3**: Items created with quantity automatically generate opening balance (run T014, T015)
**After Phase 4**: Stock in movements recorded, quantities updated (run T019, T020)
**After Phase 5**: Stock out rejected when quantity would go negative (run T025, T026)
**After Phase 6**: Adjustments recorded with direction, validation works (run T032, T033)
**After Phase 7**: Movement history queries return all movements in order (run T038, T039)
**After Phase 8**: All scenarios in quickstart.md pass (run T046)

---

## Related Documentation

- **Specification**: spec.md (user stories, requirements)
- **Design Plan**: plan.md (architecture, decisions)
- **Data Model**: data-model.md (entities, relationships, indexes)
- **API Contracts**: contracts/stock-movement-api.md (endpoint specs)
- **Research**: research.md (design decisions, alternatives)
- **Quickstart**: quickstart.md (11 validation scenarios)

