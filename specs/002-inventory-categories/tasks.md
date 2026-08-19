# Tasks: Inventory Categories

**Input**: Design documents from `/specs/002-inventory-categories/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Test tasks are included. Write tests FIRST using TDD approach - tests must FAIL before implementation begins.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create database migration V3__Create_category_table.sql in backend/src/main/resources/db/migration/
- [ ] T002 Create CategoryNameNotUniqueException in backend/src/main/java/com/example/inventory/exception/
- [ ] T003 [P] Create CategoryHasItemsException in backend/src/main/java/com/example/inventory/exception/
- [ ] T004 [P] Create CategoryNotFoundException in backend/src/main/java/com/example/inventory/exception/
- [ ] T005 [P] Update GlobalExceptionHandler to map category exceptions in backend/src/main/java/com/example/inventory/handler/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Create Category JPA entity with @Version and validations in backend/src/main/java/com/example/inventory/entity/
- [ ] T007 [P] Create CategoryRepository interface extending Spring Data JPA in backend/src/main/java/com/example/inventory/repository/
- [ ] T008 [P] Create CreateCategoryRequestDTO in backend/src/main/java/com/example/inventory/model/
- [ ] T009 [P] Create RenameCategoryRequestDTO in backend/src/main/java/com/example/inventory/model/
- [ ] T010 [P] Create CategoryResponseDTO in backend/src/main/java/com/example/inventory/model/
- [ ] T011 [P] Create ErrorResponseDTO in backend/src/main/java/com/example/inventory/model/
- [ ] T012 Implement CategoryService business logic layer in backend/src/main/java/com/example/inventory/service/
- [ ] T013 Create category.model.ts TypeScript interface in frontend/src/app/categories/models/
- [ ] T014 Create category.service.ts Angular service in frontend/src/app/categories/services/

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Create a New Category (Priority: P1) 🎯 MVP

**Goal**: Users can create new categories with unique names (case-insensitive, whitespace-trimmed)

**Independent Test**: Create a category, verify it appears in list, verify duplicate names are rejected

### Tests for User Story 1

- [ ] T015 [P] [US1] Create unit test for category name validation (trimming, case-insensitivity) in backend/src/test/java/com/example/inventory/service/
- [ ] T016 [P] [US1] Create unit test for user isolation in CategoryService in backend/src/test/java/com/example/inventory/service/
- [ ] T017 [P] [US1] Create integration test for POST /api/categories happy path in backend/src/test/java/com/example/inventory/integration/
- [ ] T018 [P] [US1] Create integration test for duplicate category name rejection in backend/src/test/java/com/example/inventory/integration/
- [ ] T019 [US1] Create Angular component spec for category-list.component.spec.ts in frontend/src/app/categories/components/
- [ ] T020 [US1] Create Angular component spec for create-category-dialog.component.spec.ts in frontend/src/app/categories/components/

### Implementation for User Story 1

- [ ] T021 [US1] Implement CategoryService.createCategory() with validation and user isolation in backend/src/main/java/com/example/inventory/service/
- [ ] T022 [US1] Implement POST /api/categories endpoint in CategoryController in backend/src/main/java/com/example/inventory/controller/
- [ ] T023 [US1] Implement GET /api/categories endpoint (list all user categories) in CategoryController in backend/src/main/java/com/example/inventory/controller/
- [ ] T024 [P] [US1] Implement category-list.component.ts (display user's categories) in frontend/src/app/categories/components/
- [ ] T025 [P] [US1] Implement category-list.component.html template in frontend/src/app/categories/components/
- [ ] T026 [US1] Implement create-category-dialog.component.ts in frontend/src/app/categories/components/
- [ ] T027 [US1] Implement create-category-dialog.component.html template in frontend/src/app/categories/components/
- [ ] T028 [US1] Implement category.service.ts methods (createCategory, listCategories) in frontend/src/app/categories/services/
- [ ] T029 [US1] Add error handling and user feedback for duplicate category names in frontend/src/app/categories/components/

**Checkpoint**: User Story 1 complete - users can create and list categories independently

---

## Phase 4: User Story 2 - Rename an Existing Category (Priority: P1)

**Goal**: Users can rename existing categories while preserving item associations; new name must be unique

**Independent Test**: Rename a category, verify new name in all views, verify duplicate names rejected, verify items remain associated

### Tests for User Story 2

- [ ] T030 [P] [US2] Create unit test for category rename with uniqueness check in backend/src/test/java/com/example/inventory/service/
- [ ] T031 [P] [US2] Create unit test for version conflict (optimistic locking) in backend/src/test/java/com/example/inventory/service/
- [ ] T032 [P] [US2] Create integration test for PATCH /api/categories/{id} happy path in backend/src/test/java/com/example/inventory/integration/
- [ ] T033 [P] [US2] Create integration test for rename to duplicate name rejection in backend/src/test/java/com/example/inventory/integration/
- [ ] T034 [P] [US2] Create integration test for concurrent edit conflict (HTTP 409) in backend/src/test/java/com/example/inventory/integration/
- [ ] T035 [US2] Create Angular component spec for rename-category-dialog.component.spec.ts in frontend/src/app/categories/components/

### Implementation for User Story 2

- [ ] T036 [US2] Implement CategoryService.renameCategory() with version checking and user isolation in backend/src/main/java/com/example/inventory/service/
- [ ] T037 [US2] Implement PATCH /api/categories/{categoryId} endpoint in CategoryController in backend/src/main/java/com/example/inventory/controller/
- [ ] T038 [P] [US2] Implement rename-category-dialog.component.ts in frontend/src/app/categories/components/
- [ ] T039 [P] [US2] Implement rename-category-dialog.component.html template in frontend/src/app/categories/components/
- [ ] T040 [US2] Implement category.service.ts renameCategory() method in frontend/src/app/categories/services/
- [ ] T041 [US2] Add concurrent edit conflict handling (HTTP 409) in frontend with auto-refresh in frontend/src/app/categories/services/
- [ ] T042 [US2] Add UI feedback for rename operation success/failure in frontend/src/app/categories/components/category-list.component.ts

**Checkpoint**: User Stories 1 & 2 complete - users can create, list, and rename categories independently

---

## Phase 5: User Story 3 - Delete a Category (Priority: P1)

**Goal**: Users can delete empty categories; system blocks deletion if items are assigned and shows count

**Independent Test**: Delete empty category (success), attempt delete with items (blocked with count), verify empty after reassignment

### Tests for User Story 3

- [ ] T043 [P] [US3] Create unit test for deletion with items check in backend/src/test/java/com/example/inventory/service/
- [ ] T044 [P] [US3] Create unit test for successful empty category deletion in backend/src/test/java/com/example/inventory/service/
- [ ] T045 [P] [US3] Create integration test for DELETE /api/categories/{id} success (empty category) in backend/src/test/java/com/example/inventory/integration/
- [ ] T046 [P] [US3] Create integration test for DELETE blocked when items assigned (HTTP 409) in backend/src/test/java/com/example/inventory/integration/
- [ ] T047 [P] [US3] Create integration test for version conflict on delete in backend/src/test/java/com/example/inventory/integration/

### Implementation for User Story 3

- [ ] T048 [US3] Implement CategoryService.deleteCategory() with item count check and user isolation in backend/src/main/java/com/example/inventory/service/
- [ ] T049 [US3] Implement DELETE /api/categories/{categoryId} endpoint in CategoryController in backend/src/main/java/com/example/inventory/controller/
- [ ] T050 [P] [US3] Add delete button and confirmation dialog to category-list.component.ts in frontend/src/app/categories/components/
- [ ] T051 [P] [US3] Update category-list.component.html with delete action in frontend/src/app/categories/components/
- [ ] T052 [US3] Implement category.service.ts deleteCategory() method in frontend/src/app/categories/services/
- [ ] T053 [US3] Add error handling for "Cannot delete: X items assigned" message in frontend in frontend/src/app/categories/components/category-list.component.ts
- [ ] T054 [US3] Add success feedback and list refresh on category deletion in frontend/src/app/categories/components/category-list.component.ts

**Checkpoint**: All user stories complete - full CRUD category management is functional

---

## Phase 6: Cross-Cutting & Polish

**Purpose**: Improvements, documentation, and validation across all user stories

- [ ] T055 [P] Run quickstart.md validation scenarios (10 scenarios) in backend/
- [ ] T056 [P] Add unit test coverage for edge cases (empty names, very long names, special characters) in backend/src/test/java/com/example/inventory/service/
- [ ] T057 [P] Create contract tests for all five API endpoints in backend/src/test/java/com/example/inventory/contract/
- [ ] T058 [P] Add logging for category operations (create, rename, delete, errors) in backend/src/main/java/com/example/inventory/service/
- [ ] T059 Update README.md with category feature documentation and deletion policy explanation
- [ ] T060 Add Angular component unit tests for error scenarios and UI state management in frontend/src/app/categories/components/
- [ ] T061 Add integration testing documentation in backend with test data setup examples
- [ ] T062 Run full integration test suite and verify all tests pass in backend/
- [ ] T063 Run Angular component tests and verify all tests pass in frontend/
- [ ] T064 Code review and cleanup: remove debug statements, verify naming conventions in backend/ and frontend/
- [ ] T065 Database migration rollback test: verify V3 migration can be reverted cleanly in backend/src/test/

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - Can proceed sequentially (P1 → P2 → P3) or in parallel (if team allows)
  - Each story is independently testable and deliverable
- **Polish (Phase 6)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
  - MVP: Stop here after US1 validation for minimum viable product
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - Builds on US1 but independently testable
  - Depends on category creation existing (from US1) but can be tested separately
- **User Story 3 (P1)**: Can start after Foundational (Phase 2) - Builds on US1 but independently testable
  - Depends on category creation existing (from US1) but can be tested separately

### Within Each User Story

1. Tests written first (TDD approach) - tests must FAIL before implementation
2. Models/DTOs created (if needed)
3. Service layer logic implemented
4. Controller endpoints implemented
5. Frontend components implemented
6. Integration between frontend and backend
7. Story validated independently

### Parallel Opportunities

**Phase 1 (Setup)**:
- T002, T003, T004, T005 can run in parallel (different exception classes)

**Phase 2 (Foundational)**:
- T007-T011 can run in parallel (different DTOs and repository)
- T008-T011 are database-independent and parallelizable

**Phase 3 (US1)**:
- T015-T018 can run in parallel (different test files)
- T024-T025 can run in parallel (component implementation)

**Phase 4 (US2)**:
- T030-T034 can run in parallel (different test files)
- T038-T039 can run in parallel (component implementation)

**Phase 5 (US3)**:
- T043-T047 can run in parallel (different test files)
- T050-T051 can run in parallel (component implementation)

**Phase 6 (Polish)**:
- T055-T058, T060, T061 can run in parallel (different test/documentation files)

---

## Parallel Example: Phase 2 Foundational

```bash
# All foundational tasks can start after Phase 1:

Developer A: T006 (Category JPA entity)
Developer B: T007 (CategoryRepository)
Developer C: T008 (CreateCategoryRequestDTO)
Developer D: T009 (RenameCategoryRequestDTO)
Developer E: T010 (CategoryResponseDTO)

# Then wait for all to complete before starting Phase 3
```

---

## Parallel Example: User Story 1

```bash
# Tests and models can start in parallel:

Developer A: T015 (unit tests for validation)
Developer B: T016 (unit tests for isolation)
Developer C: T017 (integration tests happy path)
Developer D: T024 (category-list component)
Developer E: T025 (category-list template)

# Then implement services (T021) after tests are written
# Then implement controller (T022, T023)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

Delivers: Users can create, view, and validate unique category names

1. Complete Phase 1: Setup (migrations, exceptions, DTOs)
2. Complete Phase 2: Foundational (entity, repository, service base)
3. Complete Phase 3: User Story 1 (create and list)
4. **STOP and VALIDATE**: Test scenarios 1-3 from quickstart.md
5. Deploy/demo if ready

**Deployment point**: After Phase 3, users have functional category creation

### Incremental Delivery (All 3 Stories)

Delivers: Complete CRUD category management

1. Phase 1 + 2 + 3 = Users can create categories (MVP)
2. Phase 4 = Users can rename categories
3. Phase 5 = Users can delete categories
4. Phase 6 = Polish and documentation

Each story:
- Independently tests
- Independently deploys
- Adds value without breaking previous stories

### Parallel Team Strategy

With 5 developers:

**Week 1**:
- All: Complete Phase 1 & 2 together (foundations)

**Week 2-3**:
- Developer A: Phase 3 (US1) → create/list
- Developer B: Phase 4 (US2) → rename
- Developer C: Phase 5 (US3) → delete
- Developer D: Phase 6 (tests) → validation
- Developer E: Frontend UI → all stories

All stories complete in parallel by end of week 3.

---

## Task Statistics

| Phase | Tasks | Focus |
|-------|-------|-------|
| Phase 1: Setup | 5 | Migrations, exceptions, error handling |
| Phase 2: Foundational | 9 | Entity, repository, DTOs, service base |
| Phase 3: US1 Create | 14 | Tests + implementation for create/list |
| Phase 4: US2 Rename | 12 | Tests + implementation for rename |
| Phase 5: US3 Delete | 11 | Tests + implementation for delete |
| Phase 6: Polish | 11 | Validation, documentation, code review |
| **TOTAL** | **62** | **Full feature** |

**MVP Scope** (Phase 1 + 2 + 3): 28 tasks

---

## Validation Checkpoints

### After Phase 1: Setup
- [ ] All database migrations created and have correct version numbers (V3)
- [ ] All exception classes compile and are properly mapped

### After Phase 2: Foundational
- [ ] Category entity compiles with all JPA annotations
- [ ] CategoryRepository interface works with Spring Data
- [ ] All DTOs created with Jakarta Validation annotations
- [ ] CategoryService compiles with all required methods

### After Phase 3: User Story 1 (MVP)
- [ ] User can create category via POST /api/categories
- [ ] User sees category in GET /api/categories list
- [ ] Duplicate names rejected with 400 error
- [ ] Frontend shows create dialog and category list
- [ ] Scenario 1-5 from quickstart.md pass

### After Phase 4: User Story 2
- [ ] User can rename category via PATCH /api/categories/{id}
- [ ] Renamed category appears in all views
- [ ] Duplicate names rejected on rename
- [ ] Concurrent edits return 409 with auto-refresh
- [ ] Scenario 5-6 from quickstart.md pass

### After Phase 5: User Story 3
- [ ] User can delete empty category via DELETE /api/categories/{id}
- [ ] Delete blocked when items assigned (409 with count)
- [ ] User can delete after reassigning items
- [ ] Scenario 7-8 from quickstart.md pass

### After Phase 6: Polish
- [ ] All 10 quickstart.md scenarios pass
- [ ] Test coverage > 80% for CategoryService
- [ ] All integration tests pass
- [ ] README updated with deletion policy documentation
- [ ] Code review completed and approved

---

## Testing Approach

### Test Pyramid (per User Story)

- **Unit Tests**: 70% (service logic, validation, user isolation)
  - Example: CategoryService.createCategory() with duplicate names
- **Integration Tests**: 20% (API endpoints, database interaction)
  - Example: POST /api/categories with real database
- **Component Tests**: 10% (Angular components, dialogs)
  - Example: category-list.component interactions

### TDD Approach

1. Write test FIRST (must FAIL)
2. Implement code to make test PASS
3. Refactor for clarity
4. Move to next test

All test tasks (T015-T018, T030-T034, T043-T047) written before implementation tasks.

---

## Success Criteria

✅ **All tasks complete when**:

1. All 62 tasks marked complete
2. All quickstart.md scenarios (10 total) pass without errors
3. Test coverage > 80% for CategoryService
4. README updated with category feature documentation
5. Code review completed
6. Database migrations apply and rollback cleanly

✅ **MVP Success (Phase 1-3 only)**:

1. First 28 tasks complete
2. Scenarios 1-5 from quickstart.md pass
3. User can create, view, and validate unique categories
4. Ready for demo to stakeholders

---

## Notes

- All backend file paths assume `backend/src/main/java/com/example/inventory/`
- All frontend file paths assume `frontend/src/app/categories/`
- All test file paths assume respective test directories (backend/src/test/, frontend/src/)
- Commit after each logical group of tasks (1-3 tasks per commit)
- Run tests after each phase before proceeding to next
- Database migrations must be applied and verified before Phase 3 starts
- Use quickstart.md scenarios for validation at each checkpoint
