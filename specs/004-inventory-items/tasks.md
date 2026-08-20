# Tasks: Inventory Items Management

**Input**: Design documents from `/specs/004-inventory-items/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Test tasks are OPTIONAL and included in the specification. Each user story phase includes integration and unit tests.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/main/java/com/inventory/`
- **Tests**: `backend/src/test/java/com/inventory/`
- **Database migrations**: `backend/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create InventoryItem entity class at `backend/src/main/java/com/inventory/entity/InventoryItem.java` with all fields from data-model.md
- [ ] T002 Create ItemStatus enum at `backend/src/main/java/com/inventory/entity/ItemStatus.java` with ACTIVE and ARCHIVED values
- [ ] T003 [P] Create Flyway migration V5 at `backend/src/main/resources/db/migration/V5__Create_inventory_items_table.sql` with full schema from data-model.md
- [ ] T004 Create InventoryItemRepository interface at `backend/src/main/java/com/inventory/repository/InventoryItemRepository.java` extending Spring Data JPA
- [ ] T005 [P] Configure @ControllerAdvice exception handler at `backend/src/main/java/com/inventory/controller/GlobalExceptionHandler.java` for centralized error handling

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Create InventoryItemService interface at `backend/src/main/java/com/inventory/service/InventoryItemService.java` with method signatures from plan.md
- [ ] T007 Create InventoryItemService implementation at `backend/src/main/java/com/inventory/service/InventoryItemServiceImpl.java` with user isolation enforcement
- [ ] T008 [P] Create InventoryItemRequestDTO at `backend/src/main/java/com/inventory/model/InventoryItemRequestDTO.java` with all required fields
- [ ] T009 [P] Create InventoryItemResponseDTO at `backend/src/main/java/com/inventory/model/InventoryItemResponseDTO.java` with all response fields
- [ ] T010 [P] Create InventoryItemPatchDTO at `backend/src/main/java/com/inventory/model/InventoryItemPatchDTO.java` WITHOUT currentQuantity field
- [ ] T011 Implement user data isolation in InventoryItemRepository—add methods to filter by userId in all queries
- [ ] T012 Implement user ownership validation in InventoryItemService—verify category and location belong to same user before updates
- [ ] T013 Add @CreationTimestamp and @UpdateTimestamp annotations to InventoryItem entity for automatic timestamp management
- [ ] T014 Create base InventoryItemController at `backend/src/main/java/com/inventory/controller/InventoryItemController.java` with routing structure

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Create Inventory Item with Opening Balance (Priority: P1) 🎯 MVP

**Goal**: Enable users to create new inventory items with optional initial quantity that automatically creates opening balance stock movements.

**Independent Test**: Can be fully tested by creating an item with initial quantity and verifying it exists with correct quantity and opening balance stock movement is created.

### Tests for User Story 1

- [ ] T015 [P] [US1] Write unit test for InventoryItemService.createItem() in `backend/src/test/java/com/inventory/service/InventoryItemServiceTest.java`—test with and without initial quantity
- [ ] T016 [P] [US1] Write integration test for POST /api/v1/inventory-items endpoint in `backend/src/test/java/com/inventory/controller/InventoryItemControllerTest.java`
- [ ] T017 [P] [US1] Write contract test validating API response matches InventoryItemResponseDTO schema in `backend/src/test/java/com/inventory/contract/InventoryItemApiContractTest.java`
- [ ] T018 [US1] Test that opening balance stock movement is created when initialQuantity > 0—verify StockMovement table has record
- [ ] T019 [US1] Test that no stock movement is created when initialQuantity = 0 or omitted
- [ ] T020 [US1] Test validation: empty name rejected with 400 Bad Request
- [ ] T021 [US1] Test validation: negative initialQuantity rejected with 400 Bad Request
- [ ] T022 [US1] Test validation: categoryId not found returns 404
- [ ] T023 [US1] Test validation: locationId not found returns 404
- [ ] T024 [US1] Test user data isolation: item created by User1 inaccessible to User2

### Implementation for User Story 1

- [ ] T025 [US1] Implement InventoryItemService.createItem() method to accept CreateItemRequest, validate inputs, create InventoryItem entity with userId from SecurityContext
- [ ] T026 [US1] Implement call to StockMovementService.createOpeningBalance() when initialQuantity > 0, wrapped in @Transactional
- [ ] T027 [P] [US1] Implement POST /api/v1/inventory-items endpoint in InventoryItemController that accepts InventoryItemRequestDTO and returns InventoryItemResponseDTO with HTTP 201
- [ ] T028 [P] [US1] Implement InventoryItemRepository.findByIdAndUserId() for user-scoped queries
- [ ] T029 [US1] Implement SKU uniqueness validation—check for existing (userId, sku) before creating item, throw exception if duplicate
- [ ] T030 [US1] Implement category ownership check in InventoryItemService—verify categoryId belongs to same user
- [ ] T031 [US1] Implement location ownership check in InventoryItemService—verify locationId belongs to same user
- [ ] T032 [US1] Add input validation to InventoryItemRequestDTO using Jakarta Validation annotations (@NotEmpty for name, @NotNull for required fields, @PositiveOrZero for quantities)
- [ ] T033 [US1] Add exception mapping in GlobalExceptionHandler for ValidationError, SKU_DUPLICATE, CATEGORY_NOT_FOUND, LOCATION_NOT_FOUND
- [ ] T034 [US1] Test User Story 1 end-to-end using scenarios from quickstart.md (Scenario 1 and 2)

**Checkpoint**: User Story 1 complete and independently functional. MVP is ready for testing.

---

## Phase 4: User Story 2 - Edit Item Fields (Excluding Stock Quantity) (Priority: P1)

**Goal**: Allow users to update item information while preventing direct quantity edits, maintaining audit trail integrity.

**Independent Test**: Can be fully tested by editing various item fields and verifying updates are applied, while confirming stock quantity cannot be directly modified.

### Tests for User Story 2

- [ ] T035 [P] [US2] Write unit test for InventoryItemService.updateItem() in InventoryItemServiceTest.java—test each field update
- [ ] T036 [P] [US2] Write integration test for PATCH /api/v1/inventory-items/{id} endpoint in InventoryItemControllerTest.java
- [ ] T037 [US2] Test that currentQuantity field is ignored in PATCH request—verify it remains unchanged
- [ ] T038 [US2] Test that updating to duplicate SKU within same user is rejected with 400
- [ ] T039 [US2] Test that all editable fields can be updated independently: name, description, sku, categoryId, locationId, unit, lowStockThreshold
- [ ] T040 [US2] Test validation: empty name rejected if provided
- [ ] T041 [US2] Test validation: negative lowStockThreshold rejected
- [ ] T042 [US2] Test that updatedDate changes on update but createdDate remains unchanged
- [ ] T043 [US2] Test user data isolation: cannot update item belonging to different user

### Implementation for User Story 2

- [ ] T044 [US2] Implement InventoryItemService.updateItem() method accepting InventoryItemPatchDTO and verifying user ownership
- [ ] T045 [P] [US2] Implement PATCH /api/v1/inventory-items/{id} endpoint in InventoryItemController accepting InventoryItemPatchDTO
- [ ] T046 [P] [US2] Implement InventoryItemRepository.findByIdAndUserId() for user-scoped retrieval before update
- [ ] T047 [US2] Verify InventoryItemPatchDTO does NOT include currentQuantity field—ensure type safety prevents accidental quantity edits
- [ ] T048 [US2] Implement validation in service: if categoryId provided, verify it belongs to same user; if locationId provided, verify it belongs to same user
- [ ] T049 [US2] Implement SKU uniqueness check for updates—allow same SKU if unchanged, reject if new SKU already exists for user
- [ ] T050 [US2] Add @UpdateTimestamp logic to automatically update updatedDate on entity save
- [ ] T051 [US2] Add exception mapping in GlobalExceptionHandler for update-specific errors (SKU_DUPLICATE, CATEGORY_NOT_FOUND, LOCATION_NOT_FOUND, ITEM_NOT_FOUND)
- [ ] T052 [US2] Test User Story 2 end-to-end using scenarios from quickstart.md (Scenario 6 and 7)

**Checkpoint**: User Stories 1 and 2 both work independently. Create and update operations complete.

---

## Phase 5: User Story 3 - Archive and Restore Items (Priority: P1)

**Goal**: Enable users to archive inactive items and restore them, with idempotent operations and prevention of stock movements on archived items.

**Independent Test**: Can be fully tested by archiving an active item, verifying it's marked as archived, and then restoring it.

### Tests for User Story 3

- [ ] T053 [P] [US3] Write unit test for InventoryItemService.archiveItem() in InventoryItemServiceTest.java—test idempotency
- [ ] T054 [P] [US3] Write unit test for InventoryItemService.restoreItem() in InventoryItemServiceTest.java—test idempotency
- [ ] T055 [P] [US3] Write integration test for POST /api/v1/inventory-items/{id}/archive and /restore endpoints in InventoryItemControllerTest.java
- [ ] T056 [US3] Test that archive operation idempotently succeeds even if item already archived
- [ ] T057 [US3] Test that restore operation idempotently succeeds even if item already active
- [ ] T058 [US3] Test that archived items appear only in list when filtered by status=ARCHIVED
- [ ] T059 [US3] Test that archived item cannot receive stock movements—verify StockMovement service checks item.status
- [ ] T060 [US3] Test that updatedDate changes on archive/restore
- [ ] T061 [US3] Test user data isolation: cannot archive/restore item belonging to different user

### Implementation for User Story 3

- [ ] T062 [US3] Implement InventoryItemService.archiveItem() method setting status=ARCHIVED, verifying user ownership, idempotent
- [ ] T063 [US3] Implement InventoryItemService.restoreItem() method setting status=ACTIVE, verifying user ownership, idempotent
- [ ] T064 [P] [US3] Implement POST /api/v1/inventory-items/{id}/archive endpoint in InventoryItemController
- [ ] T065 [P] [US3] Implement POST /api/v1/inventory-items/{id}/restore endpoint in InventoryItemController
- [ ] T066 [US3] Update InventoryItemRepository.findByIdAndUserId() to include user_id filter for archive/restore operations
- [ ] T067 [US3] Add validation in StockMovement service: check item.status == ACTIVE before creating movements, throw exception if archived
- [ ] T068 [US3] Add exception mapping in GlobalExceptionHandler for ITEM_NOT_FOUND on archive/restore attempts
- [ ] T069 [US3] Update InventoryItemResponseDTO to include status field in all responses
- [ ] T070 [US3] Test User Story 3 end-to-end using scenarios from quickstart.md (Scenario 9 and 10)

**Checkpoint**: Core P1 stories (1, 2, 3) complete. All create, update, archive, and restore operations work independently.

---

## Phase 6: User Story 4 - Move Items Between Categories and Locations (Priority: P2)

**Goal**: Allow users to reorganize inventory by reassigning items to different categories or locations.

**Independent Test**: Can be fully tested by moving an item to a different location and verifying the location updates without affecting quantity.

### Tests for User Story 4

- [ ] T071 [P] [US4] Write unit test for category/location update in InventoryItemService in InventoryItemServiceTest.java
- [ ] T072 [P] [US4] Write integration test for moving items between categories and locations in InventoryItemControllerTest.java
- [ ] T073 [US4] Test that moving to new category updates categoryId without affecting quantity
- [ ] T074 [US4] Test that moving to new location updates locationId without affecting quantity
- [ ] T075 [US4] Test that moving to same category/location is idempotent (succeeds with no error)
- [ ] T076 [US4] Test that moving to category/location belonging to different user is rejected
- [ ] T077 [US4] Test that moving to non-existent category/location is rejected with 404

### Implementation for User Story 4

- [ ] T078 [US4] Extend InventoryItemService.updateItem() to handle categoryId and locationId changes (uses existing PATCH endpoint)
- [ ] T079 [P] [US4] Verify PATCH /api/v1/inventory-items/{id} already supports categoryId and locationId updates
- [ ] T080 [US4] Implement category ownership validation: verify new categoryId belongs to same user as item
- [ ] T081 [US4] Implement location ownership validation: verify new locationId belongs to same user as item
- [ ] T082 [US4] Test User Story 4 end-to-end using scenarios from quickstart.md (Scenario 6 - category/location moves)

**Checkpoint**: P2 story complete. Items can be reorganized across categories and locations.

---

## Phase 7: User Story 5 - Delete Inventory Item (Priority: P3)

**Goal**: Enable users to permanently remove items from the system (hard delete, no soft delete).

**Independent Test**: Can be fully tested by deleting an item and confirming it no longer exists in the system.

### Tests for User Story 5

- [ ] T083 [P] [US5] Write unit test for InventoryItemService.deleteItem() in InventoryItemServiceTest.java
- [ ] T084 [P] [US5] Write integration test for DELETE /api/v1/inventory-items/{id} endpoint in InventoryItemControllerTest.java
- [ ] T085 [US5] Test that deleted item no longer exists in database—verify no soft delete flag
- [ ] T086 [US5] Test that deleting non-existent item returns 404
- [ ] T087 [US5] Test user data isolation: cannot delete item belonging to different user

### Implementation for User Story 5

- [ ] T088 [US5] Implement InventoryItemService.deleteItem() method performing hard delete via repository.deleteById()
- [ ] T089 [P] [US5] Implement DELETE /api/v1/inventory-items/{id} endpoint in InventoryItemController returning HTTP 204
- [ ] T090 [P] [US5] Implement InventoryItemRepository.deleteByIdAndUserId() for user-scoped deletion
- [ ] T091 [US5] Add exception mapping in GlobalExceptionHandler for delete attempts on non-existent items
- [ ] T092 [US5] Test User Story 5 end-to-end using scenarios from quickstart.md (Scenario 11)

**Checkpoint**: All user stories complete. Full CRUD + archive/restore functionality implemented.

---

## Phase 8: List and Filter Operations

**Purpose**: Enable querying items with pagination and filtering

- [ ] T093 Implement InventoryItemService.listItemsByUser() method with pagination and optional status filter
- [ ] T094 Implement GET /api/v1/inventory-items endpoint in InventoryItemController accepting page, size, and status parameters
- [ ] T095 [P] Implement InventoryItemRepository.findByUserIdAndStatus() for status-filtered queries
- [ ] T096 [P] Implement InventoryItemRepository.findByUserId() for all-items query with pagination
- [ ] T097 Test pagination: verify page, size, totalElements, totalPages, currentPage in response
- [ ] T098 Test filtering by status: verify status=ACTIVE returns only active items, status=ARCHIVED returns only archived items
- [ ] T099 Test filtering by categoryId: verify categoryId filter works in queries
- [ ] T100 Test end-to-end using scenario from quickstart.md (Scenario 8)

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and finalize the feature

- [ ] T101 [P] Add comprehensive Javadoc comments to InventoryItem entity explaining each field
- [ ] T102 [P] Add Javadoc to InventoryItemService interface with method documentation
- [ ] T103 [P] Add Javadoc to all DTOs explaining required vs optional fields
- [ ] T104 Add logging in InventoryItemService for all operations (create, update, archive, restore, delete)
- [ ] T105 [P] Create unit test suite runner in `backend/src/test/java/com/inventory/InventoryItemTestSuite.java` for all tests
- [ ] T106 [P] Create integration test suite in `backend/src/test/java/com/inventory/InventoryItemIntegrationTestSuite.java`
- [ ] T107 Run all tests from quickstart.md (Scenarios 1-12) to validate complete feature
- [ ] T108 Verify performance: all endpoints complete in < 500ms per SC-001
- [ ] T109 Verify user data isolation: run isolation tests from quickstart.md (Scenario 12)
- [ ] T110 Run Flyway migration V5 successfully against test database
- [ ] T111 Code review: verify all DTOs follow naming convention (SomethingRequestDTO, SomethingResponseDTO)
- [ ] T112 Code review: verify service layer contains all business logic, no logic in controllers
- [ ] T113 Code review: verify repository layer has only data access, no business logic
- [ ] T114 Code review: verify all exceptions are caught and mapped in GlobalExceptionHandler
- [ ] T115 [P] Update API documentation (Swagger/OpenAPI if applicable) to document all endpoints
- [ ] T116 Create deployment checklist verifying Flyway migration applied, feature ready for production

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
  - US1, US2, US3 are P1 and can run in parallel after Foundational
  - US4 is P2 and depends on US1-US3 (reads category/location code)
  - US5 is P3 and independent
- **List/Filter (Phase 8)**: Depends on US1 completion (read operations)
- **Polish (Phase 9)**: Depends on all previous phases

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2 (Foundational) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Phase 2 (Foundational) - Builds on US1 entities but independently testable
- **User Story 3 (P1)**: Can start after Phase 2 (Foundational) - Builds on US1 entities but independently testable
- **User Story 4 (P2)**: Can start after Phase 2 (Foundational) - Benefits from US1-US3 but independently testable
- **User Story 5 (P3)**: Can start after Phase 2 (Foundational) - Completely independent

### Within Each User Story

- Tests (T0XX) MUST be written first and FAIL before implementation
- Models/DTOs before service implementation
- Service methods before controller endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] (T001, T002, T003, T004, T005) can run in parallel
- All Foundational tasks marked [P] (T008, T009, T010) can run in parallel within Phase 2
- Once Foundational phase completes, all user story tests can start in parallel (T015-T024 for US1, T035-T043 for US2, etc.)
- Within each user story, all tests marked [P] can run in parallel
- Within each user story, all models/DTOs marked [P] can run in parallel
- Different user stories (US1, US2, US3, US5) can be worked on in parallel by different team members after Phase 2

---

## Parallel Example: User Story 1

```
Team Member A: Test Phase
  - T015: Unit test for createItem()
  - T016: Integration test for POST endpoint
  - T017: Contract test
  
Team Member B: DTO/Model Phase (parallel with tests)
  - T008: InventoryItemRequestDTO (from Phase 2)
  - T009: InventoryItemResponseDTO (from Phase 2)
  
Team Member C: Service Phase (after T008, T009 complete)
  - T025: Implement createItem() service method
  - T026: Add opening balance stock movement
  
Team Member D: Controller Phase (after T025, T026 complete)
  - T027: Implement POST endpoint
  
Merge: All phases complete, run T034 end-to-end validation
```

---

## Parallel Example: Across All P1 User Stories

After Phase 2 (Foundational) complete:

```
Team Member A: User Story 1 (Create Item)
  - T015-T034: All US1 tasks in sequence
  
Team Member B: User Story 2 (Edit Item) - parallel with A
  - T035-T052: All US2 tasks in sequence
  
Team Member C: User Story 3 (Archive/Restore) - parallel with A & B
  - T053-T070: All US3 tasks in sequence
  
Merge: T093+ (List operations) after US1 complete, T101+ (Polish) after all stories complete
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T005)
2. Complete Phase 2: Foundational (T006-T014) - CRITICAL
3. Complete Phase 3: User Story 1 (T015-T034)
4. **STOP and VALIDATE**: Run all US1 tests and scenarios 1-2 from quickstart.md
5. Deploy/demo MVP if ready

### Incremental Delivery (MVP → Feature Complete)

1. Complete Setup + Foundational → Foundation ready (Phases 1-2)
2. Add User Story 1 → Test independently (Scenario 1-2) → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently (Scenario 6-7) → Deploy/Demo
4. Add User Story 3 → Test independently (Scenario 9-10) → Deploy/Demo
5. Add User Story 4 → Test independently (Scenario 6 moves) → Deploy/Demo
6. Add User Story 5 → Test independently (Scenario 11) → Deploy/Demo
7. Complete Phase 8: List operations
8. Each story adds value without breaking previous stories

### Parallel Team Strategy (3-4 developers)

1. Team completes Phase 1-2 together (Setup + Foundational)
2. Once Foundational is done:
   - Developer A: User Story 1 (T015-T034)
   - Developer B: User Story 2 (T035-T052)
   - Developer C: User Story 3 (T053-T070)
3. When P1 stories done, Developer D: User Story 5 (T083-T092)
4. Sequentially after P1: User Story 4 (T071-T082), List/Filter (T093-T100), Polish (T101-T116)

---

## Validation Checkpoints

After completing each phase or story, validate:

### After Phase 1-2 (Setup + Foundational)
- [ ] Flyway migration V5 applied successfully
- [ ] All DTOs have correct fields and validation
- [ ] Entity class properly configured with JPA annotations
- [ ] GlobalExceptionHandler catches all exception types

### After Phase 3 (User Story 1)
- [ ] Scenario 1 passes: Create item with initial quantity, opening balance created
- [ ] Scenario 2 passes: Create item without quantity, currentQuantity = 0
- [ ] All T015-T024 tests pass (9 tests)

### After Phase 4 (User Story 2)
- [ ] Scenario 6 passes: Edit item fields
- [ ] Scenario 7 passes: currentQuantity cannot be edited directly
- [ ] All T035-T043 tests pass (9 tests)
- [ ] Combined US1 + US2 scenarios pass (Scenario 1-2, 6-7)

### After Phase 5 (User Story 3)
- [ ] Scenario 9 passes: Archive item
- [ ] Scenario 10 passes: Restore item
- [ ] All T053-T061 tests pass (9 tests)
- [ ] Combined US1 + US2 + US3 scenarios pass (Scenario 1-2, 6-7, 9-10)

### After Phase 7 (User Story 5)
- [ ] Scenario 11 passes: Delete item
- [ ] All T083-T087 tests pass (5 tests)

### Final Validation
- [ ] All quickstart.md scenarios pass (Scenario 1-12, including Scenario 8 list/filter and Scenario 12 user isolation)
- [ ] All performance tests pass (< 500ms per operation)
- [ ] Code review passed (naming conventions, architecture layers, exception handling)
- [ ] All database migrations applied successfully
- [ ] Feature ready for production deployment

---

## Notes

- [P] tasks = different files, can run in parallel, no blocking dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable (can be deployed separately)
- Write tests FIRST (T0XX), ensure they FAIL before implementing
- Commit after each task or logical group (e.g., after all tests for a story)
- Stop at any checkpoint to validate story works independently
- All operations must complete in < 500ms per SC-001 success criterion
- User data isolation must be enforced on every operation per FR-015 and SC-005
- Avoid: vague tasks, same file conflicts (use [P] to identify parallel-safe tasks), cross-story dependencies that break independence
