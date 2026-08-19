# Tasks: Location Management

**Input**: Design documents from `/specs/003-location-management/`

**Prerequisites**: plan.md (Spring Boot + Angular stack), spec.md (4 user stories: Create, Rename, Delete), data-model.md, contracts/locations-api.md, contracts/frontend-ui.md

**Stack**: Java 17 + Spring Boot 3.x (backend), Angular 16+ (frontend), PostgreSQL, Flyway, SLF4J/Logback

**Organization**: Tasks grouped by user story (US1, US2, US3, US4) to enable independent implementation and testing of each story

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and shared structure

- [ ] T001 Create Flyway migration directory structure at `backend/src/main/resources/db/migration/`
- [ ] T002 Initialize Location exception classes directory at `backend/src/main/java/org/example/sddinventory/exception/`
- [ ] T003 [P] Initialize Location model DTOs directory at `backend/src/main/java/org/example/sddinventory/model/`
- [ ] T004 [P] Initialize Angular feature module directory at `frontend/src/app/features/locations/`
- [ ] T005 [P] Initialize Angular feature subdirectories: location-list/, location-form/, services/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Create Flyway migration V001__add_location_table.sql with location table schema, composite unique constraint (user_id, name), indexes at `backend/src/main/resources/db/migration/V001__add_location_table.sql`
- [ ] T007 Create Location entity with @Entity, @Table, @Version, @GeneratedValue annotations matching Category entity pattern at `backend/src/main/java/org/example/sddinventory/entity/Location.java`
- [ ] T008 Create LocationRepository extending Spring Data JpaRepository with query methods (findByUserId, findByIdAndUserId, existsByUserIdAndName) at `backend/src/main/java/org/example/sddinventory/repository/LocationRepository.java`
- [ ] T009 [P] Create LocationNameNotUniqueException extending RuntimeException with message constructor at `backend/src/main/java/org/example/sddinventory/exception/LocationNameNotUniqueException.java`
- [ ] T010 [P] Create LocationNotFoundException extending RuntimeException at `backend/src/main/java/org/example/sddinventory/exception/LocationNotFoundException.java`
- [ ] T011 [P] Create LocationHasItemsException extending RuntimeException with itemCount field at `backend/src/main/java/org/example/sddinventory/exception/LocationHasItemsException.java`
- [ ] T012 Add exception handlers for LocationNameNotUniqueException, LocationNotFoundException, LocationHasItemsException to GlobalExceptionHandler with proper status codes (409, 404, 409) at `backend/src/main/java/org/example/sddinventory/config/GlobalExceptionHandler.java`
- [ ] T013 Create LocationService stub with @Service, @Transactional, UserRepository injection at `backend/src/main/java/org/example/sddinventory/service/LocationService.java`
- [ ] T014 Create LocationController stub with @RestController, @RequestMapping("/locations"), UserRepository injection at `backend/src/main/java/org/example/sddinventory/controller/LocationController.java`
- [ ] T015 [P] Create CreateLocationRequestDTO with @NotBlank name field at `backend/src/main/java/org/example/sddinventory/model/CreateLocationRequestDTO.java`
- [ ] T016 [P] Create RenameLocationRequestDTO with @NotBlank name field at `backend/src/main/java/org/example/sddinventory/model/RenameLocationRequestDTO.java`
- [ ] T017 [P] Create LocationResponseDTO with id, userId, name, createdAt, updatedAt fields at `backend/src/main/java/org/example/sddinventory/model/LocationResponseDTO.java`
- [ ] T018 [P] Create Angular LocationModel interface with id, userId, name, createdAt, updatedAt in `frontend/src/app/features/locations/location.model.ts`
- [ ] T019 [P] Create Angular LocationService stub with HttpClient injection at `frontend/src/app/features/locations/location.service.ts`
- [ ] T020 [P] Create Angular LocationListComponent stub with template reference in `frontend/src/app/features/locations/location-list/location-list.component.ts` and HTML template
- [ ] T021 [P] Create Angular LocationFormComponent stub with ReactiveFormsModule in `frontend/src/app/features/locations/location-form/location-form.component.ts` and HTML template
- [ ] T022 Create Angular LocationsModule with declarations, imports (CommonModule, ReactiveFormsModule, HttpClientModule), providers (LocationService) at `frontend/src/app/features/locations/locations.module.ts`

**Checkpoint**: All infrastructure in place - user story implementation can now begin

---

## Phase 3: User Story 1 - Create a Location (Priority: P1) 🎯 MVP

**Goal**: Users can create locations with unique names per user

**Independent Test**: User can create location, see it in list, verify uniqueness error on duplicate

### Backend Implementation for US1

- [ ] T023 Implement LocationService.createLocation(userId, name) with validation (non-empty, non-whitespace, uniqueness check via repository, log INFO) at `backend/src/main/java/org/example/sddinventory/service/LocationService.java`
  - Throw LocationNameNotUniqueException if duplicate
  - Return saved Location entity
- [ ] T024 Implement LocationController POST /locations endpoint with @PostMapping, extract userId from principal, call service, return LocationResponseDTO with 201 Created at `backend/src/main/java/org/example/sddinventory/controller/LocationController.java`
- [ ] T025 Implement LocationService.getAllLocations(userId) returning List<Location> for authenticated user at `backend/src/main/java/org/example/sddinventory/service/LocationService.java`
- [ ] T026 Implement LocationController GET /locations endpoint returning List<LocationResponseDTO> at `backend/src/main/java/org/example/sddinventory/controller/LocationController.java`
- [ ] T027 Create backend unit tests for LocationService.createLocation covering: valid creation, duplicate name rejection, whitespace validation, user isolation in `backend/src/test/java/org/example/sddinventory/service/LocationServiceTest.java`
- [ ] T028 Create backend integration tests for POST/GET /locations endpoints covering: create + list workflow, error responses, in `backend/src/test/java/org/example/sddinventory/controller/LocationControllerTest.java`

### Frontend Implementation for US1

- [ ] T029 Implement LocationService.createLocation(name): Observable<Location> HTTP POST call at `frontend/src/app/features/locations/location.service.ts`
- [ ] T030 Implement LocationService.getLocations(): Observable<Location[]> HTTP GET call at `frontend/src/app/features/locations/location.service.ts`
- [ ] T031 Implement LocationService error transformation logic (409 LOCATION_NAME_NOT_UNIQUE → user message) at `frontend/src/app/features/locations/location.service.ts`
- [ ] T032 Implement LocationListComponent ngOnInit to load locations via LocationService at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T033 Implement LocationFormComponent with Reactive Form (name field, validation, submit button) at `frontend/src/app/features/locations/location-form/location-form.component.ts`
- [ ] T034 Implement LocationFormComponent submit handler calling LocationService.createLocation at `frontend/src/app/features/locations/location-form/location-form.component.ts`
- [ ] T035 Update LocationListComponent to display create button, open LocationFormComponent modal on click at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T036 Implement LocationListComponent list refresh after successful create (subscription to form submit event) at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T037 Create Angular unit tests for LocationService.createLocation, error handling, observables in `frontend/src/app/features/locations/location.service.spec.ts`
- [ ] T038 Create Angular template for LocationListComponent with table showing locations, Create button in `frontend/src/app/features/locations/location-list/location-list.component.html`
- [ ] T039 Create Angular template for LocationFormComponent with name input, validation errors, Create button in `frontend/src/app/features/locations/location-form/location-form.component.html`

**Checkpoint**: User Story 1 complete - users can create and list locations. Test independently using quickstart.md scenario FE-2 and Validation Scenario 1

---

## Phase 4: User Story 2 - Rename a Location (Priority: P2)

**Goal**: Users can rename locations; uniqueness enforced; items remain assigned

**Independent Test**: User can rename location, name updates everywhere, verify uniqueness error

### Backend Implementation for US2

- [ ] T040 Implement LocationService.renameLocation(userId, locationId, newName) with uniqueness check, throw exceptions as needed at `backend/src/main/java/org/example/sddinventory/service/LocationService.java`
  - Throw LocationNameNotUniqueException if duplicate
  - Throw LocationNotFoundException if not found or belongs to different user
  - Log INFO on success
  - Update updatedAt timestamp
- [ ] T041 Implement LocationController PUT /locations/{id} endpoint extracting userId from principal, call service, return LocationResponseDTO at `backend/src/main/java/org/example/sddinventory/controller/LocationController.java`
- [ ] T042 Implement LocationService.getLocation(userId, locationId) returning single Location with user isolation check at `backend/src/main/java/org/example/sddinventory/service/LocationService.java`
- [ ] T043 Implement LocationController GET /locations/{id} endpoint returning single LocationResponseDTO at `backend/src/main/java/org/example/sddinventory/controller/LocationController.java`
- [ ] T044 Create backend unit tests for LocationService.renameLocation covering: valid rename, duplicate name rejection, location not found, user isolation in `backend/src/test/java/org/example/sddinventory/service/LocationServiceTest.java`
- [ ] T045 Create backend integration tests for PUT /locations/{id} endpoint covering: rename workflow, error scenarios in `backend/src/test/java/org/example/sddinventory/controller/LocationControllerTest.java`

### Frontend Implementation for US2

- [ ] T046 Implement LocationService.renameLocation(id, name): Observable<Location> HTTP PUT call at `frontend/src/app/features/locations/location.service.ts`
- [ ] T047 Implement LocationFormComponent edit mode (mode: 'edit', pre-fill name, submit button text 'Update') at `frontend/src/app/features/locations/location-form/location-form.component.ts`
- [ ] T048 Update LocationListComponent to show Rename button, open LocationFormComponent in edit mode with existing location data at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T049 Implement LocationListComponent list refresh after successful rename at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T050 Add client-side validation preventing empty/whitespace-only names in form at `frontend/src/app/features/locations/location-form/location-form.component.ts`
- [ ] T051 Create Angular unit tests for LocationService.renameLocation, form validation, error handling in `frontend/src/app/features/locations/location.service.spec.ts`
- [ ] T052 Update LocationListComponent template to add Rename button with click handler for each location in `frontend/src/app/features/locations/location-list/location-list.component.html`

**Checkpoint**: User Story 2 complete - users can rename locations. Test independently using quickstart.md scenario FE-3

---

## Phase 5: User Story 3 & 4 - Delete a Location (Priority: P2)

**Goal**: Users can delete empty locations; system prevents deletion of non-empty locations with clear error

**Independent Test**: User can delete empty location, see error when attempting to delete non-empty location, verify deletion removal from list

### Backend Implementation for US3/US4

- [ ] T053 Implement LocationService.deleteLocation(userId, locationId) with:
  - Throw LocationNotFoundException if not found or belongs to different user
  - Check if location has items via ItemService/Repository
  - Throw LocationHasItemsException with itemCount if non-empty (block deletion strategy)
  - Delete location if empty
  - Log INFO on success, WARN on failure
  at `backend/src/main/java/org/example/sddinventory/service/LocationService.java`
- [ ] T054 Implement LocationController DELETE /locations/{id} endpoint extracting userId from principal, call service, return 204 No Content at `backend/src/main/java/org/example/sddinventory/controller/LocationController.java`
- [ ] T055 Create backend unit tests for LocationService.deleteLocation covering: empty location deletion, non-empty location rejection, location not found, user isolation in `backend/src/test/java/org/example/sddinventory/service/LocationServiceTest.java`
- [ ] T056 Create backend integration tests for DELETE /locations/{id} endpoint covering: delete workflow, error scenarios, verification post-deletion in `backend/src/test/java/org/example/sddinventory/controller/LocationControllerTest.java`

### Frontend Implementation for US3/US4

- [ ] T057 Implement LocationService.deleteLocation(id): Observable<void> HTTP DELETE call at `frontend/src/app/features/locations/location.service.ts`
- [ ] T058 Update LocationListComponent to show Delete button for each location at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T059 Implement confirmation dialog before delete (confirm: "Are you sure?" → proceed, cancel → abort) at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T060 Implement LocationListComponent delete handler calling LocationService.deleteLocation with error handling at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T061 Implement LocationListComponent list refresh/removal after successful delete at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T062 Implement error message display for "Cannot delete location with items" (409 LOCATION_HAS_ITEMS) with item count shown at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T063 Create Angular unit tests for LocationService.deleteLocation, confirmation flow, error handling in `frontend/src/app/features/locations/location.service.spec.ts`
- [ ] T064 Update LocationListComponent template to add Delete button with confirmation dialog for each location in `frontend/src/app/features/locations/location-list/location-list.component.html`

**Checkpoint**: User Stories 3 & 4 complete - users can delete empty locations, system prevents deletion of non-empty locations. Test independently using quickstart.md scenarios FE-4, FE-5

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final enhancements, optimization, and comprehensive testing

- [ ] T065 [P] Add loading state spinner to LocationListComponent during API calls at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T066 [P] Add error alert display above location list for failed operations at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T067 [P] Implement refresh button on location list to manually reload locations at `frontend/src/app/features/locations/location-list/location-list.component.ts`
- [ ] T068 Add loading state to LocationFormComponent submit button during API call at `frontend/src/app/features/locations/location-form/location-form.component.ts`
- [ ] T069 Add error display below form for backend validation errors (duplicate name) at `frontend/src/app/features/locations/location-form/location-form.component.ts`
- [ ] T070 Update LocationFormComponent to auto-focus on name input for UX at `frontend/src/app/features/locations/location-form/location-form.component.ts`
- [ ] T071 [P] Add CSS styling for responsive layout (buttons, table, modal) in `frontend/src/app/features/locations/location-list/location-list.component.css`
- [ ] T072 [P] Add CSS styling for form (input, button, error message) in `frontend/src/app/features/locations/location-form/location-form.component.css`
- [ ] T073 [P] Add ARIA labels and accessibility attributes to all form inputs and buttons in `frontend/src/app/features/locations/` (all HTML templates)
- [ ] T074 [P] Implement keyboard navigation (Tab, Enter) for form submission in LocationFormComponent
- [ ] T075 Verify responsive design: test layout at mobile (320px), tablet (768px), desktop (1024px) breakpoints
- [ ] T076 [P] Create comprehensive end-to-end test scenarios in `frontend/tests/e2e/locations.e2e.spec.ts` (Cypress/Playwright) covering: create, rename, delete workflows
- [ ] T077 Run and validate quickstart.md validation scenarios (FE-1 through FE-10) for backend API and frontend UI
- [ ] T078 Run DevTools verification: Console checks (no errors), Network tab checks (correct status codes, payloads)
- [ ] T079 Verify logging: Check backend logs for INFO entries for all create/rename/delete operations, WARN for failures
- [ ] T080 Performance validation: Verify all location operations complete within 1 second (SC-001, SC-002)
- [ ] T081 Code review: Verify backend follows Spring Boot layered architecture (controller → service → repository)
- [ ] T082 Code review: Verify frontend follows Angular best practices (services, components, reactive forms)
- [ ] T083 Code review: Verify Constitution Principle compliance (Simplicity First, Technology Stack, Layered Architecture, Centralized Exception Handling)
- [ ] T084 Documentation: Update README.md with Location Management feature overview and endpoints
- [ ] T085 Documentation: Ensure all design documents (plan.md, spec.md, data-model.md, contracts/) are consistent with implementation

**Checkpoint**: All user stories complete and polished. Feature ready for production

---

## Parallel Execution Opportunities

### Setup (Phase 1): All [P] tasks in parallel
```
T002, T003, T004, T005 can run simultaneously (different directories)
```

### Foundational (Phase 2): All [P] exception classes in parallel
```
T009, T010, T011 can run simultaneously (different exception classes)
T015, T016, T017 can run simultaneously (different DTOs)
T018, T019, T020, T021 can run simultaneously (different frontend components)
```

### User Story 1 (Phase 3): Backend and frontend can start in parallel after Foundational complete
```
Backend: T023 → T024 → T025 → T026 (sequential: service before controller)
Frontend: T029 → T030 → T031 → T032 → T033 → T034 → T035 → T036 (service first)
Tests: T027, T037, T038, T039 can run parallel after implementation
```

### User Story 2 (Phase 4): Backend and frontend in parallel
```
Backend: T040 → T041 (service before controller)
Frontend: T046 → T047 → T048 → T049 (service before component)
Tests: T044, T045, T051, T052 in parallel after implementation
```

### User Story 3/4 (Phase 5): Backend and frontend in parallel
```
Backend: T053 → T054 (service before controller)
Frontend: T057 → T058 → T059 → T060 → T061 → T062 (service before components)
Tests: T055, T056, T063, T064 in parallel after implementation
```

### Polish (Phase 6): Many tasks parallelizable
```
Frontend loading/error/styling: T065, T066, T067, T068, T069, T070, T071, T072
Accessibility/styling: T073, T074, T075
Tests: T076, T077, T078, T079
Code review: T081, T082, T083
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)
1. Complete Phase 1: Setup (infrastructure)
2. Complete Phase 2: Foundational (database, entities, exceptions)
3. Complete Phase 3: User Story 1 (create + list)
4. **STOP and VALIDATE**: Test against quickstart.md Scenario 1
5. Deploy MVP if ready

### Incremental Delivery
1. Setup + Foundational → Foundation ready
2. Add User Story 1 → Test → Deploy (MVP!)
3. Add User Story 2 → Test → Deploy
4. Add User Story 3/4 → Test → Deploy
5. Add Polish → Final validation → Production release

### Parallel Team (3 Developers)
1. Team: Setup + Foundational together
2. Once Foundational complete:
   - Dev A: User Story 1 (backend + frontend)
   - Dev B: User Story 2 (backend + frontend)
   - Dev C: User Story 3/4 (backend + frontend)
3. All stories complete in parallel
4. Team: Polish + final testing

---

## Validation Checkpoints

After each phase/story, validate independently:

- **Phase 1**: Directories created, files compiled
- **Phase 2**: Flyway migration runs, entities/DTOs compile, no errors
- **Phase 3 (US1)**: Create location via API (curl POST), list locations (curl GET), test duplicate error
- **Phase 4 (US2)**: Rename location via API (curl PUT), verify name updates, verify uniqueness error
- **Phase 5 (US3/US4)**: Delete empty location (curl DELETE 204), verify delete blocked for non-empty (curl DELETE 409)
- **Phase 6**: All quickstart.md scenarios pass, performance targets met, no console errors

---

## Task Summary

| Phase | Tasks | Purpose |
|-------|-------|---------|
| 1: Setup | T001-T005 (5 tasks) | Initialize project structure |
| 2: Foundational | T006-T022 (17 tasks) | Database, entities, services, controllers, DTOs, UI components, modules |
| 3: US1 | T023-T039 (17 tasks) | Create + List functionality (MVP) |
| 4: US2 | T040-T052 (13 tasks) | Rename functionality |
| 5: US3/US4 | T053-T064 (12 tasks) | Delete functionality |
| 6: Polish | T065-T085 (21 tasks) | Styling, testing, validation, documentation |
| **TOTAL** | **85 tasks** | Full-stack implementation |

---

## Key Metrics

- **Total Tasks**: 85 (24 backend, 38 frontend, 23 cross-cutting)
- **Parallelizable Tasks**: ~40 (marked [P])
- **User Story 1 (MVP)**: 39 tasks (56% of total)
- **User Story 2**: 13 tasks
- **User Story 3/4**: 12 tasks
- **Polish**: 21 tasks

**MVP Scope**: Phase 1 + Phase 2 + Phase 3 = 39 tasks for fully working create + list feature

---

## Notes

- Tests are optional but recommended (TDD approach included)
- Each task includes exact file paths for clarity
- Backend follows Spring Boot layered architecture (entity → repository → service → controller)
- Frontend follows Angular best practices (service → component → template)
- All tasks can be committed independently for atomic progress tracking
- Use quickstart.md validation scenarios as acceptance criteria
- Performance targets: All operations < 1 second, 95% success rate
