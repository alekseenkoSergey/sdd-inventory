# Tasks: Inventory Items Management

**Input**: Design documents from `/specs/004-inventory-items/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Test tasks are included and organized by user story. Tests must be written first and fail before implementation (TDD approach).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/main/java/org/example/sddinventory/`
- **Tests**: `backend/src/test/java/org/example/sddinventory/`
- **Database migrations**: `backend/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create InventoryItem entity class at `backend/src/main/java/org/example/sddinventory/entity/InventoryItem.java` with all 12 fields (id, userId, name, description, sku, categoryId, locationId, currentQuantity, unit, lowStockThreshold, status, timestamps)
- [ ] T002 Create ItemStatus enum at `backend/src/main/java/org/example/sddinventory/entity/ItemStatus.java` with ACTIVE and ARCHIVED values
- [ ] T003 [P] Create Flyway migration V5 at `backend/src/main/resources/db/migration/V5__Create_inventory_items_table.sql` with schema: BIGINT id (IDENTITY), Long userId, String name/sku, Long categoryId/locationId, Decimal currentQuantity/lowStockThreshold, VARCHAR status, timestamps, partial unique index on (user_id, sku) WHERE sku IS NOT NULL, FKs to category and location
- [ ] T004 Create InventoryItemRepository interface at `backend/src/main/java/org/example/sddinventory/repository/InventoryItemRepository.java` extending Spring Data JPA with user-scoped methods: findByIdAndUserId(Long id, Long userId), findByUserIdAndStatus(Long userId, ItemStatus status), findByUserId(Long userId, Pageable), deleteByIdAndUserId(Long id, Long userId)
- [ ] T005 [P] Update exception handler at `backend/src/main/java/org/example/sddinventory/config/GlobalExceptionHandler.java` to map new exceptions: SKU_DUPLICATE, CATEGORY_NOT_FOUND, LOCATION_NOT_FOUND, ITEM_NOT_FOUND for inventory items

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Create InventoryItemService interface at `backend/src/main/java/org/example/sddinventory/service/InventoryItemService.java` with method signatures: createItem(Long userId, InventoryItemRequestDTO), getItem(Long userId, Long itemId), listItems(Long userId, int page, int size, ItemStatus status), updateItem(Long userId, Long itemId, InventoryItemPatchDTO), archiveItem(Long userId, Long itemId), restoreItem(Long userId, Long itemId), deleteItem(Long userId, Long itemId)
- [ ] T007 Create InventoryItemService implementation at `backend/src/main/java/org/example/sddinventory/service/InventoryItemServiceImpl.java` with user isolation enforcement in all methods: extract userId from SecurityContext, filter all queries by userId, validate categoryId/locationId belong to same user
- [ ] T008 [P] Create InventoryItemRequestDTO at `backend/src/main/java/org/example/sddinventory/model/InventoryItemRequestDTO.java` with fields: name (@NotEmpty), description, sku, categoryId (@NotNull), locationId (@NotNull), unit (@NotNull), lowStockThreshold (@PositiveOrZero), initialQuantity (@PositiveOrZero)
- [ ] T009 [P] Create InventoryItemResponseDTO at `backend/src/main/java/org/example/sddinventory/model/InventoryItemResponseDTO.java` with all entity fields: id, name, description, sku, categoryId, locationId, currentQuantity, unit, lowStockThreshold, status, createdDate, updatedDate
- [ ] T010 [P] Create InventoryItemPatchDTO at `backend/src/main/java/org/example/sddinventory/model/InventoryItemPatchDTO.java` with optional fields: name, description, sku, categoryId, locationId, unit, lowStockThreshold—EXCLUDES currentQuantity field (read-only)
- [ ] T011 Implement user data isolation in InventoryItemRepository—verify all query methods filter by userId before returning results; add @Query methods for findByIdAndUserId, findByUserIdAndStatus, deleteByIdAndUserId
- [ ] T012 Implement user ownership validation in InventoryItemService—before create/update: validate categoryId exists and belongs to currentUserId using CategoryRepository.findByIdAndUserId(), validate locationId exists and belongs to currentUserId using LocationRepository.findByIdAndUserId(), throw CATEGORY_NOT_FOUND or LOCATION_NOT_FOUND if validation fails
- [ ] T013 Add JPA timestamp annotations to InventoryItem entity: @CreationTimestamp on createdDate, @UpdateTimestamp on updatedDate for automatic server-side timestamp management
- [ ] T014 Create base InventoryItemController at `backend/src/main/java/org/example/sddinventory/controller/InventoryItemController.java` with route prefix /api/v1/inventory-items and dependency injection for InventoryItemService and SecurityContext

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Create Inventory Item with Opening Balance (Priority: P1) 🎯 MVP

**Goal**: Enable users to create new inventory items with optional initial quantity that automatically creates opening balance stock movements.

**Independent Test**: Can be fully tested by creating an item with initial quantity and verifying it exists with correct quantity and opening balance stock movement is created.

### Tests for User Story 1

- [ ] T015 [P] [US1] Write unit test for InventoryItemService.createItem() in `backend/src/test/java/org/example/sddinventory/service/InventoryItemServiceTest.java`—test createItem with initialQuantity > 0 creates item and calls StockMovementService.createOpeningBalance()
- [ ] T016 [P] [US1] Write unit test for InventoryItemService.createItem() with initialQuantity = 0 or null creates item without calling StockMovementService
- [ ] T017 [P] [US1] Write integration test for POST /api/v1/inventory-items endpoint in `backend/src/test/java/org/example/sddinventory/controller/InventoryItemControllerTest.java`—verify HTTP 201 Created response
- [ ] T018 [P] [US1] Write contract test validating API response matches InventoryItemResponseDTO schema in `backend/src/test/java/org/example/sddinventory/contract/InventoryItemApiContractTest.java`
- [ ] T019 [US1] Test validation: empty name in request rejected with 400 Bad Request and error message
- [ ] T020 [US1] Test validation: negative initialQuantity in request rejected with 400 Bad Request
- [ ] T021 [US1] Test validation: categoryId not found returns 404 CATEGORY_NOT_FOUND
- [ ] T022 [US1] Test validation: locationId not found returns 404 LOCATION_NOT_FOUND
- [ ] T023 [US1] Test validation: categoryId belongs to different user returns 404 CATEGORY_NOT_FOUND (user isolation)
- [ ] T024 [US1] Test validation: locationId belongs to different user returns 404 LOCATION_NOT_FOUND (user isolation)
- [ ] T025 [US1] Test user data isolation: item created by User1 cannot be retrieved by User2

### Implementation for User Story 1

- [ ] T026 [US1] Implement InventoryItemService.createItem(Long userId, InventoryItemRequestDTO) method: extract userId from SecurityContext, validate DTO, check SKU uniqueness for (userId, sku), validate categoryId/locationId belong to user, create InventoryItem entity, persist to database, if initialQuantity > 0 call StockMovementService.createOpeningBalance() within @Transactional
- [ ] T027 [US1] Implement InventoryItemRepository.findBySku(Long userId, String sku) for SKU uniqueness check; throw SKU_DUPLICATE exception if duplicate found
- [ ] T028 [P] [US1] Implement POST /api/v1/inventory-items endpoint in InventoryItemController that accepts InventoryItemRequestDTO, calls service.createItem(), returns InventoryItemResponseDTO with HTTP 201 Created
- [ ] T029 [P] [US1] Implement InventoryItemRepository.findByIdAndUserId(Long id, Long userId) for user-scoped retrieval before create
- [ ] T030 [US1] Implement category ownership check in InventoryItemService.validateCategory(Long categoryId, Long userId)—query CategoryRepository.findByIdAndUserId(), throw CATEGORY_NOT_FOUND if null
- [ ] T031 [US1] Implement location ownership check in InventoryItemService.validateLocation(Long locationId, Long userId)—query LocationRepository.findByIdAndUserId(), throw LOCATION_NOT_FOUND if null
- [ ] T032 [US1] Add input validation to InventoryItemRequestDTO: @NotEmpty on name, @NotNull on categoryId/locationId/unit, @PositiveOrZero on quantities, @Size(max=255) on name, @Size(max=100) on sku
- [ ] T033 [US1] Add exception mapping in GlobalExceptionHandler: map ValidationException to 400 with VALIDATION_ERROR code, map SKU_DUPLICATE to 400, map CATEGORY_NOT_FOUND to 404, map LOCATION_NOT_FOUND to 404
- [ ] T034 [US1] Test User Story 1 end-to-end using scenarios from quickstart.md (Scenario 1: create with quantity, Scenario 2: create without quantity)

**Checkpoint**: User Story 1 complete and independently functional. MVP is ready for testing.

---

## Phase 4: User Story 2 - Edit Item Fields (Excluding Stock Quantity) (Priority: P1)

**Goal**: Allow users to update item information while preventing direct quantity edits, maintaining audit trail integrity.

**Independent Test**: Can be fully tested by editing various item fields and verifying updates are applied, while confirming stock quantity cannot be directly modified.

### Tests for User Story 2

- [ ] T035 [P] [US2] Write unit test for InventoryItemService.updateItem() in InventoryItemServiceTest.java—test each field update: name, description, sku, categoryId, locationId, unit, lowStockThreshold
- [ ] T036 [P] [US2] Write integration test for PATCH /api/v1/inventory-items/{id} endpoint in InventoryItemControllerTest.java
- [ ] T037 [P] [US2] Write contract test for PATCH endpoint response schema
- [ ] T038 [US2] Test that currentQuantity field is ignored in PATCH request—verify currentQuantity remains unchanged after update
- [ ] T039 [US2] Test that updating to duplicate SKU within same user is rejected with 400 SKU_DUPLICATE
- [ ] T039b [US2] Test that updating to same SKU (unchanged) succeeds without error
- [ ] T040 [US2] Test that all editable fields can be updated independently: name, description, sku, categoryId, locationId, unit, lowStockThreshold
- [ ] T041 [US2] Test validation: empty name rejected if provided in PATCH
- [ ] T042 [US2] Test validation: negative lowStockThreshold rejected if provided in PATCH
- [ ] T043 [US2] Test that updatedDate changes on update but createdDate remains unchanged
- [ ] T044 [US2] Test user data isolation: cannot update item belonging to different user—returns 404 ITEM_NOT_FOUND

### Implementation for User Story 2

- [ ] T045 [US2] Implement InventoryItemService.updateItem(Long userId, Long itemId, InventoryItemPatchDTO) method: fetch item with findByIdAndUserId(), verify user ownership, apply non-null DTO fields to entity, validate categoryId/locationId if changed, check SKU uniqueness if SKU changed, persist with @Transactional
- [ ] T046 [P] [US2] Implement PATCH /api/v1/inventory-items/{id} endpoint in InventoryItemController accepting InventoryItemPatchDTO, calls service.updateItem(), returns InventoryItemResponseDTO with HTTP 200 OK
- [ ] T047 [P] [US2] Verify InventoryItemPatchDTO does NOT include currentQuantity field—ensure type safety prevents accidental quantity edits at compile time
- [ ] T048 [US2] Implement validation in service: if categoryId provided in PATCH, verify it belongs to same user using validateCategory(); if locationId provided, verify with validateLocation()
- [ ] T049 [US2] Implement SKU uniqueness check for updates—allow same SKU if unchanged, query for other items with new SKU, throw SKU_DUPLICATE if found
- [ ] T050 [US2] Verify @UpdateTimestamp on updatedDate field automatically updates on entity save
- [ ] T051 [US2] Add exception mapping in GlobalExceptionHandler for update-specific errors: SKU_DUPLICATE, CATEGORY_NOT_FOUND, LOCATION_NOT_FOUND, ITEM_NOT_FOUND all return appropriate HTTP status codes
- [ ] T052 [US2] Test User Story 2 end-to-end using scenarios from quickstart.md (Scenario 6: edit item fields, Scenario 7: prevent quantity edit)

**Checkpoint**: User Stories 1 and 2 both work independently. Create and update operations complete.

---

## Phase 5: User Story 3 - Archive and Restore Items (Priority: P1)

**Goal**: Enable users to archive inactive items and restore them, with idempotent operations and prevention of stock movements on archived items.

**Independent Test**: Can be fully tested by archiving an active item, verifying it's marked as archived, and then restoring it.

### Tests for User Story 3

- [ ] T053 [P] [US3] Write unit test for InventoryItemService.archiveItem() in InventoryItemServiceTest.java—test idempotency: archive already-archived item succeeds without error
- [ ] T054 [P] [US3] Write unit test for InventoryItemService.restoreItem() in InventoryItemServiceTest.java—test idempotency: restore already-active item succeeds without error
- [ ] T055 [P] [US3] Write integration test for POST /api/v1/inventory-items/{id}/archive and /restore endpoints in InventoryItemControllerTest.java
- [ ] T056 [P] [US3] Write contract test for archive/restore endpoint response schema
- [ ] T057 [US3] Test that archive operation idempotently succeeds even if item already archived—returns 200 with status=ARCHIVED
- [ ] T058 [US3] Test that restore operation idempotently succeeds even if item already active—returns 200 with status=ACTIVE
- [ ] T059 [US3] Test that archived items do not appear in list when status=ACTIVE filter applied
- [ ] T060 [US3] Test that archived items appear only in list when filtered by status=ARCHIVED
- [ ] T061 [US3] Test that archived item cannot receive stock movements—StockMovement service checks item.status == ACTIVE, throws exception if archived (integration with external service)
- [ ] T062 [US3] Test that updatedDate changes on archive/restore but createdDate unchanged
- [ ] T063 [US3] Test user data isolation: cannot archive/restore item belonging to different user—returns 404 ITEM_NOT_FOUND

### Implementation for User Story 3

- [ ] T064 [US3] Implement InventoryItemService.archiveItem(Long userId, Long itemId) method: fetch item with findByIdAndUserId(), verify user ownership, set status=ARCHIVED, persist with @Transactional, return item—idempotent (no error if already archived)
- [ ] T065 [US3] Implement InventoryItemService.restoreItem(Long userId, Long itemId) method: fetch item with findByIdAndUserId(), verify user ownership, set status=ACTIVE, persist with @Transactional, return item—idempotent (no error if already active)
- [ ] T066 [P] [US3] Implement POST /api/v1/inventory-items/{id}/archive endpoint in InventoryItemController, calls service.archiveItem(), returns InventoryItemResponseDTO with HTTP 200 OK
- [ ] T067 [P] [US3] Implement POST /api/v1/inventory-items/{id}/restore endpoint in InventoryItemController, calls service.restoreItem(), returns InventoryItemResponseDTO with HTTP 200 OK
- [ ] T068 [US3] Update InventoryItemRepository to handle archive/restore: ensure findByIdAndUserId() includes user_id filter for both operations
- [ ] T069 [US3] Integrate with StockMovement service: update StockMovement service to check item.status == ACTIVE before creating movements, throw exception if archived (this is external service responsibility, document requirement)
- [ ] T070 [US3] Add exception mapping in GlobalExceptionHandler for ITEM_NOT_FOUND on archive/restore attempts—returns 404
- [ ] T071 [US3] Verify InventoryItemResponseDTO includes status field in all responses (already included from entity)
- [ ] T072 [US3] Test User Story 3 end-to-end using scenarios from quickstart.md (Scenario 9: archive item, Scenario 10: restore item)

**Checkpoint**: Core P1 stories (1, 2, 3) complete. All create, update, archive, and restore operations work independently.

---

## Phase 6: User Story 4 - Move Items Between Categories and Locations (Priority: P2)

**Goal**: Allow users to reorganize inventory by reassigning items to different categories or locations.

**Independent Test**: Can be fully tested by moving an item to a different location and verifying the location updates without affecting quantity.

### Tests for User Story 4

- [ ] T073 [P] [US4] Write unit test for category/location update in InventoryItemService in InventoryItemServiceTest.java—test moving to new category, new location independently
- [ ] T074 [P] [US4] Write integration test for PATCH /api/v1/inventory-items/{id} with categoryId/locationId changes in InventoryItemControllerTest.java
- [ ] T075 [US4] Test that moving to new category updates categoryId without affecting quantity
- [ ] T076 [US4] Test that moving to new location updates locationId without affecting quantity
- [ ] T077 [US4] Test that moving to same category/location is idempotent (succeeds with no error)
- [ ] T078 [US4] Test that moving to category belonging to different user is rejected with 404 CATEGORY_NOT_FOUND
- [ ] T079 [US4] Test that moving to location belonging to different user is rejected with 404 LOCATION_NOT_FOUND
- [ ] T080 [US4] Test that moving to non-existent category is rejected with 404 CATEGORY_NOT_FOUND
- [ ] T081 [US4] Test that moving to non-existent location is rejected with 404 LOCATION_NOT_FOUND

### Implementation for User Story 4

- [ ] T082 [US4] Verify existing InventoryItemService.updateItem() already handles categoryId and locationId changes through generic field update logic—no new implementation needed, reuses US2 PATCH endpoint
- [ ] T083 [P] [US4] Verify PATCH /api/v1/inventory-items/{id} endpoint from US2 already supports categoryId and locationId updates—no new endpoint needed
- [ ] T084 [US4] Implement category ownership validation in updateItem: if categoryId in DTO, call validateCategory(categoryId, userId) before applying update
- [ ] T085 [US4] Implement location ownership validation in updateItem: if locationId in DTO, call validateLocation(locationId, userId) before applying update
- [ ] T086 [US4] Test User Story 4 end-to-end using scenarios from quickstart.md (Scenario 6: category/location moves)

**Checkpoint**: P2 story complete. Items can be reorganized across categories and locations.

---

## Phase 7: User Story 5 - Delete Inventory Item (Priority: P3)

**Goal**: Enable users to permanently remove items from the system (hard delete, no soft delete).

**Independent Test**: Can be fully tested by deleting an item and confirming it no longer exists in the system.

### Tests for User Story 5

- [ ] T087 [P] [US5] Write unit test for InventoryItemService.deleteItem() in InventoryItemServiceTest.java
- [ ] T088 [P] [US5] Write integration test for DELETE /api/v1/inventory-items/{id} endpoint in InventoryItemControllerTest.java
- [ ] T089 [P] [US5] Write contract test for DELETE endpoint (HTTP 204 response)
- [ ] T090 [US5] Test that deleted item no longer exists in database—query returns empty after delete
- [ ] T091 [US5] Test that deleting non-existent item returns 404 ITEM_NOT_FOUND
- [ ] T092 [US5] Test user data isolation: cannot delete item belonging to different user—returns 404 ITEM_NOT_FOUND

### Implementation for User Story 5

- [ ] T093 [US5] Implement InventoryItemService.deleteItem(Long userId, Long itemId) method: fetch item with findByIdAndUserId(), verify user ownership, delete via repository.deleteByIdAndUserId(itemId, userId), no return value (hard delete)
- [ ] T094 [P] [US5] Implement DELETE /api/v1/inventory-items/{id} endpoint in InventoryItemController, calls service.deleteItem(), returns HTTP 204 No Content (no response body)
- [ ] T095 [P] [US5] Implement InventoryItemRepository.deleteByIdAndUserId(Long id, Long userId) for user-scoped deletion—generates DELETE query with both id and user_id conditions
- [ ] T096 [US5] Add exception mapping in GlobalExceptionHandler for delete attempts on non-existent items—ITEM_NOT_FOUND returns 404
- [ ] T097 [US5] Test User Story 5 end-to-end using scenarios from quickstart.md (Scenario 11: delete item)

**Checkpoint**: All user stories complete. Full CRUD + archive/restore functionality implemented.

---

## Phase 8: List and Filter Operations

**Purpose**: Enable querying items with pagination and filtering

- [ ] T098 Implement InventoryItemService.listItemsByUser(Long userId, int page, int size, ItemStatus status) method with pagination (PageRequest) and optional status filter (null = all statuses)
- [ ] T099 Implement GET /api/v1/inventory-items endpoint in InventoryItemController accepting query parameters: page (default 0), size (default 20), status (optional: ACTIVE, ARCHIVED, or null for all)
- [ ] T100 [P] Implement InventoryItemRepository.findByUserIdAndStatus(Long userId, ItemStatus status, Pageable) for status-filtered queries
- [ ] T101 [P] Implement InventoryItemRepository.findByUserId(Long userId, Pageable) for all-items query with pagination
- [ ] T102 Test pagination: verify response includes page, size, totalElements, totalPages, currentPage metadata
- [ ] T103 Test filtering by status: verify status=ACTIVE returns only active items, status=ARCHIVED returns only archived items, null/missing returns all items
- [ ] T104 Test user data isolation: verify GET /api/v1/inventory-items only returns items belonging to authenticated user
- [ ] T105 Test end-to-end using scenario from quickstart.md (Scenario 8: list and filter)

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and finalize the feature

- [ ] T106 [P] Add comprehensive Javadoc comments to InventoryItem entity explaining each field, validation rules, and relationships
- [ ] T107 [P] Add Javadoc to InventoryItemService interface documenting all method contracts: parameters, return values, exceptions thrown, user isolation guarantees
- [ ] T108 [P] Add Javadoc to all DTOs (Request/Response/Patch) explaining required vs optional fields, validation constraints, and purpose
- [ ] T109 Add logging in InventoryItemService for all operations: create (info), update (debug), archive (info), restore (info), delete (warn), list (debug)
- [ ] T110 [P] Create unit test suite runner in `backend/src/test/java/org/example/sddinventory/InventoryItemTestSuite.java` for all unit tests
- [ ] T111 [P] Create integration test suite in `backend/src/test/java/org/example/sddinventory/InventoryItemIntegrationTestSuite.java`
- [ ] T112 Run all tests from quickstart.md (Scenarios 1-12) to validate complete feature end-to-end
- [ ] T113 Verify performance: measure all endpoints complete in < 500ms per SC-001 using load test or profiler
- [ ] T114 Verify user data isolation: run isolation tests from quickstart.md (Scenario 12) confirming cross-user access prevention
- [ ] T115 Run Flyway migration V5 successfully against test database and verify schema created correctly
- [ ] T116 Code review: verify all DTOs follow naming convention (InventoryItemRequestDTO, InventoryItemResponseDTO, InventoryItemPatchDTO)
- [ ] T117 Code review: verify service layer contains all business logic (validation, user isolation, stock movement integration), no logic in controllers
- [ ] T118 Code review: verify repository layer has only data access queries (@Query methods), no business logic
- [ ] T119 Code review: verify all exceptions are caught and mapped in GlobalExceptionHandler (no unhandled exceptions leak to client)
- [ ] T120 Code review: verify user isolation is enforced in every service method (userId extracted from SecurityContext, used in all queries)
- [ ] T121 [P] Update API documentation (Swagger/OpenAPI if applicable) to document all 8 endpoints: POST create, GET retrieve, GET list, PATCH update, POST archive, POST restore, DELETE remove, with request/response schemas
- [ ] T122 Create deployment checklist verifying: Flyway migration V5 applied to production database, all tests pass, performance validated, user isolation tested, feature ready for production

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - US1, US2, US3 are P1 and can run in parallel after Foundational
  - US4 is P2 and depends on US1-US3 (builds on update logic)
  - US5 is P3 and independent
- **List/Filter (Phase 8)**: Depends on US1 completion (read operations)
- **Polish (Phase 9)**: Depends on all previous phases

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2 (Foundational) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Phase 2 (Foundational) - Builds on US1 entities but independently testable
- **User Story 3 (P1)**: Can start after Phase 2 (Foundational) - Builds on US1 entities but independently testable
- **User Story 4 (P2)**: Can start after Phase 2 (Foundational) - Benefits from US1-US3 logic but independently testable
- **User Story 5 (P3)**: Can start after Phase 2 (Foundational) - Completely independent

### Within Each User Story

- Tests (T0XX) MUST be written first and FAIL before implementation
- Models/DTOs before service implementation
- Service methods before controller endpoints
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] (T001-T005) can run in parallel
- All Foundational tasks marked [P] (T008-T010) can run in parallel within Phase 2
- Once Foundational phase completes, all user story tests can start in parallel (T015-T025 for US1, T035-T044 for US2, etc.)
- Within each user story, all tests marked [P] can run in parallel
- Within each user story, all models/DTOs marked [P] can run in parallel
- Different user stories (US1, US2, US3, US5) can be worked on in parallel by different team members after Phase 2

---

## Parallel Example: User Story 1

```
Developer A: Tests (parallel with others)
  - T015: Unit test for createItem() with/without quantity
  - T016: Unit test for initialQuantity=0
  - T017: Integration test for POST endpoint
  - T018: Contract test for response schema
  
Developer B: Models/DTOs (parallel with A)
  - T008: InventoryItemRequestDTO (Phase 2)
  - T009: InventoryItemResponseDTO (Phase 2)
  
Developer C: Service implementation (after DTOs complete)
  - T026: Implement createItem() service
  - T027: Implement SKU uniqueness check
  - T030: Implement category ownership validation
  - T031: Implement location ownership validation
  
Developer D: Controller (after service complete)
  - T028: Implement POST endpoint
  
Merge: All phases complete, run T034 end-to-end validation
```

---

## Parallel Example: Across All P1 User Stories

After Phase 2 (Foundational) complete:

```
Developer A: User Story 1 (Create Item)
  - T015-T034: All US1 tasks in sequence
  
Developer B: User Story 2 (Edit Item) - parallel with A
  - T035-T052: All US2 tasks in sequence
  
Developer C: User Story 3 (Archive/Restore) - parallel with A & B
  - T053-T072: All US3 tasks in sequence
  
Merge after P1: US4 (Phase 6), List operations (Phase 8), Polish (Phase 9)
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
   - Developer C: User Story 3 (T053-T072)
3. When P1 stories done, Developer D: User Story 5 (T087-T097)
4. Sequentially after P1: User Story 4 (T073-T086), List/Filter (T098-T105), Polish (T106-T122)

---

## Validation Checkpoints

After completing each phase or story, validate:

### After Phase 1-2 (Setup + Foundational)
- [ ] Flyway migration V5 applied successfully to test database
- [ ] All DTOs have correct fields and validation annotations
- [ ] Entity class properly configured with JPA annotations (@CreationTimestamp, @UpdateTimestamp)
- [ ] GlobalExceptionHandler updated with inventory item exception mappings
- [ ] Repository implements user-scoped queries correctly

### After Phase 3 (User Story 1)
- [ ] Scenario 1 passes: Create item with initial quantity, opening balance created, HTTP 201
- [ ] Scenario 2 passes: Create item without quantity, currentQuantity = 0, no stock movement
- [ ] All T015-T025 tests pass (11 tests)
- [ ] Performance validated: < 500ms per SC-001

### After Phase 4 (User Story 2)
- [ ] Scenario 6 passes: Edit item fields update correctly
- [ ] Scenario 7 passes: currentQuantity cannot be edited directly
- [ ] All T035-T044 tests pass (10 tests)
- [ ] Combined US1 + US2 scenarios pass (Scenario 1-2, 6-7)

### After Phase 5 (User Story 3)
- [ ] Scenario 9 passes: Archive item, status = ARCHIVED
- [ ] Scenario 10 passes: Restore item, status = ACTIVE
- [ ] All T053-T063 tests pass (11 tests)
- [ ] Combined US1 + US2 + US3 scenarios pass (Scenario 1-2, 6-7, 9-10)

### After Phase 7 (User Story 5)
- [ ] Scenario 11 passes: Delete item, HTTP 204, item not found after
- [ ] All T087-T092 tests pass (6 tests)

### Final Validation
- [ ] All quickstart.md scenarios pass (Scenario 1-12)
- [ ] All performance tests pass (< 500ms per operation)
- [ ] Code review passed (naming conventions, architecture layers, exception handling)
- [ ] All database migrations applied successfully
- [ ] User isolation verified in all operations
- [ ] Feature ready for production deployment

---

## Notes

- [P] tasks = different files, can run in parallel, no blocking dependencies
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable (can be deployed separately)
- Write tests FIRST (T0XX), ensure they FAIL before implementing (TDD)
- Commit after each task or logical group (e.g., after all tests for a story)
- Stop at any checkpoint to validate story works independently
- All operations must complete in < 500ms per SC-001 success criterion
- User data isolation must be enforced on every operation per FR-015 and SC-005
- Category and Location IDs are type `Long` (not UUID)—validated for existence and user ownership before operations
- Avoid: vague tasks, same file conflicts (use [P] to identify parallel-safe tasks), cross-story dependencies that break independence
