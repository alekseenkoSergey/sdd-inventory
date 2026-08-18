# Tasks: SSO-Only Authentication

**Input**: Design documents from `/specs/001-sso-auth/`
- **spec.md** — 5 user stories (P1, P1, P1, P2, P2)
- **plan.md** — Project structure, tech stack (Java 17, Spring Boot 3.x, Angular 17+, PostgreSQL, Flyway)
- **research.md** — 7 technical decisions (OAuth2 Client, Spring Session, Custom OAuth2UserService, SLF4J logging, HTTP interceptor, 12-hour expiry, JPA entity)
- **data-model.md** — User entity with (provider, provider_user_id) uniqueness constraint
- **contracts/auth-endpoints.md** — 4 REST endpoints (login, callback, logout, profile)
- **quickstart.md** — 7 validation scenarios covering all stories

**Organization**: Tasks grouped by user story to enable independent implementation, testing, and deployment of each feature slice.

---

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: User story label (US1, US2, US3, US4, US5)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and configuration for authentication feature

- [ ] T001 Create directory structure per plan.md: `backend/src/main/java/com/example/inventory/{controller,service,entity,repository,model,config}` and `frontend/src/app/auth/{components,services,guards,interceptors,models}`
- [ ] T002 Configure Spring Boot OAuth2 Client starter dependency in `backend/pom.xml`
- [ ] T003 [P] Configure Google OAuth credentials in `backend/src/main/resources/application.yml` with client-id, client-secret, scopes (openid, email, profile)
- [ ] T004 [P] Configure Spring Session timeout (12 hours) in `backend/src/main/resources/application.yml`
- [ ] T005 [P] Setup Angular HTTP client with credentials support in `frontend/src/app/core/http/api.service.ts`
- [ ] T006 Create Flyway migration directory structure: `backend/src/main/resources/db/migration/`

**Checkpoint**: Infrastructure configured, OAuth credentials set, session timeout defined

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core authentication infrastructure that MUST complete before any user story can be implemented

⚠️ **CRITICAL**: No user story work can begin until this phase is complete

- [ ] T007 [P] Create User JPA entity in `backend/src/main/java/com/example/inventory/entity/User.java` with fields: id (Long, auto-generated), provider (String, non-null), providerUserId (String, non-null), email (String, nullable), displayName (String, nullable), avatarUrl (String, nullable), createdAt (Timestamp), updatedAt (Timestamp), and unique constraint on (provider, providerUserId)
- [ ] T008 [P] Create UserRepository (Spring Data) in `backend/src/main/java/com/example/inventory/repository/UserRepository.java` with method: `Optional<User> findByProviderAndProviderUserId(String provider, String providerUserId)`
- [ ] T009 Create Flyway migration `backend/src/main/resources/db/migration/V2__create_users_table.sql` to create users table with schema from data-model.md, unique constraint on (provider, provider_user_id), and indexes for lookup optimization
- [ ] T010 Create UserProfileResponseDTO in `backend/src/main/java/com/example/inventory/model/UserProfileResponseDTO.java` with fields: id, provider, email, displayName, avatarUrl
- [ ] T011 [P] Create CustomOAuth2UserService in `backend/src/main/java/com/example/inventory/service/CustomOAuth2UserService.java` extending `DefaultOAuth2UserService` with `loadUser()` override to: call parent, lookup user by (provider, providerUserId), create new User if not found with OAuth profile data, return OAuth2User to Spring Security
- [ ] T012 Create SecurityConfig in `backend/src/main/java/com/example/inventory/config/SecurityConfig.java` with: OAuth2 login configuration, CustomOAuth2UserService bean registration, CSRF protection, session management (12h timeout), CORS allowing credentials
- [ ] T013 Create ExceptionHandler (centralized @ControllerAdvice) in `backend/src/main/java/com/example/inventory/config/ExceptionHandler.java` with handlers for: OAuth2AuthenticationException, Unauthorized (401), InternalServerError (500), returning consistent error JSON format per contracts
- [ ] T014 [P] Create AuthService in `backend/src/main/java/com/example/inventory/service/AuthService.java` with methods: `getCurrentUser()` (returns authenticated User), `logoutUser()` (invalidates session), `getUserProfile()` (fetches User by ID), and SLF4J logging for all auth events (login, logout, token refresh, failures with timestamp and user ID)
- [ ] T015 [P] Create frontend AuthService in `frontend/src/app/auth/services/auth.service.ts` with methods: `login()` (initiates OAuth flow), `logout()` (calls POST /api/auth/logout), `getProfile()` (calls GET /api/auth/user/profile), `isAuthenticated()` (checks session), storing auth state in service
- [ ] T016 [P] Create frontend OAuthService in `frontend/src/app/auth/services/oauth.service.ts` with: `initiateGoogleLogin()` (redirects to GET /api/auth/login), handles OAuth callback, manages redirect URIs
- [ ] T017 Create frontend auth HTTP interceptor in `frontend/src/app/auth/interceptors/auth.interceptor.ts` implementing `HttpInterceptor` with: automatic credential attachment (`withCredentials: true`), 401 response handling (redirect to login), token refresh retry logic if needed
- [ ] T018 Create frontend auth route guard in `frontend/src/app/auth/guards/auth.guard.ts` implementing `CanActivate` to: check authentication state via AuthService, redirect to login if unauthenticated, allow navigation if authenticated
- [ ] T019 Verify Spring Security OAuth2 Client autoconfigures token refresh in `backend/src/main/java/com/example/inventory/config/SecurityConfig.java`:
  - Review Spring Security OAuth2 Client documentation to confirm token refresh is enabled by default
  - Verify that expired access tokens are automatically refreshed via the token endpoint
  - Write integration test: create OAuth2 session, simulate token expiry, make API request, verify no re-authentication required (token silently refreshed per FR-010)
  - Test that expired refresh tokens return 401 and user must re-login
  - Add comment to SecurityConfig documenting token refresh behavior
  - Log result: "Token refresh configured per Spring Security defaults" if passing, or document any custom configuration needed

**Checkpoint**: Foundation complete - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - First-Time SSO Login (Priority: P1) 🎯 MVP

**Goal**: New user can complete Google OAuth2 flow, backend automatically creates user record, user is logged in with session

**Independent Test**: Visit app unauthenticated → click "Login with Google" → complete OAuth at Google.com → verify redirected to app, logged in, profile visible, user record in database with (provider="Google", providerUserId, email, displayName)

### Implementation for User Story 1

- [ ] T020 Create AuthController in `backend/src/main/java/com/example/inventory/controller/AuthController.java` with endpoints:
  - `GET /api/auth/login` → initiates OAuth2 flow (handled by Spring Security)
  - `POST /api/auth/logout` → calls AuthService.logoutUser(), returns `{ "status": "logged_out" }`
  - `GET /api/auth/user/profile` → calls AuthService.getUserProfile(), returns UserProfileResponseDTO (requires @PreAuthorize("isAuthenticated()"))
  - All endpoints return consistent error responses via ExceptionHandler
- [ ] T021 [P] Create frontend Login component in `frontend/src/app/auth/components/login/login.component.ts` with:
  - Displays "Login with Google" button
  - On click: calls OAuthService.initiateGoogleLogin()
  - On OAuth callback: calls AuthService.login(), stores session state, navigates to homepage
  - Displays error message if OAuth fails
- [ ] T022 [P] Create frontend Login component template `frontend/src/app/auth/components/login/login.component.html` with login UI per design
- [ ] T023 Create frontend Logout component in `frontend/src/app/auth/components/logout/logout.component.ts` with:
  - Logout button/link (shown when authenticated)
  - On click: calls AuthService.logout()
  - Clears session state
  - Redirects to login page
- [ ] T024 [P] Create frontend ProfileService in `frontend/src/app/auth/services/profile.service.ts` with:
  - `getProfile()` → calls AuthService.getProfile(), caches result
  - `displayProfile()` → used by components to show user name, email, avatar
- [ ] T025 Create frontend User model in `frontend/src/app/auth/models/user.model.ts` with TypeScript interface matching UserProfileResponseDTO
- [ ] T026 [P] Update frontend app routing to include:
  - `/login` → LoginComponent (unauthenticated)
  - `/home` → protected by AuthGuard (requires authentication)
  - OAuth callback handled by OAuthService (internal to /login/oauth2/code/google redirect)
- [ ] T027 Verify User Story 1 against quickstart.md Scenario 1 (First-Time User SSO Login)

**Checkpoint**: User Story 1 complete and independently testable. Users can now sign in with Google for the first time.

---

## Phase 4: User Story 2 - Returning User Login (Priority: P1)

**Goal**: Returning user signs in again, existing user record reused (no duplicates), session established

**Independent Test**: Create user via US1 → logout → login again with same Google account → verify same user record used (no duplicate), login <10 seconds, can access protected endpoints

### Implementation for User Story 2

- [ ] T028 [US2] Update CustomOAuth2UserService (T011) to verify deduplication logic in `loadUser()`: lookup by (provider, providerUserId), if found return existing user, log reuse event, if not found create new (already done in T011 but verify/test)
- [ ] T029 [US2] Add deduplication test query to quickstart.md to check database for exact user count after return login
- [ ] T030 [P] [US2] Update AuthService (T014) to add `verifyUserExists()` method that checks UserRepository for existing user, logs reuse event with timestamp
- [ ] T031 [US2] Update CustomOAuth2UserService (T011) to call AuthService.verifyUserExists() on return login, ensuring reuse is logged
- [ ] T032 [US2] Add performance logging to AuthService for login flow timing (start → end), verify <10 second target per SC-002
- [ ] T033 [P] [US2] Update frontend AuthService (T015) to handle returning user flow: detect existing session on page load, skip login if session valid, show profile immediately
- [ ] T034 Verify User Story 2 against quickstart.md Scenario 2 (Returning User Login) and deduplication verification

**Checkpoint**: User Story 2 complete. Returning users are recognized, no duplicates created, performance targets met.

---

## Phase 5: User Story 3 - Logout (Priority: P1)

**Goal**: Logged-in user clicks logout, session terminated, user redirected to login, protected endpoints rejected

**Independent Test**: Login user → click logout → verify redirected to login page → verify GET /api/auth/user/profile returns 401

### Implementation for User Story 3

- [ ] T035 [P] [US3] Update AuthController (T019) POST /api/auth/logout to:
  - Validate user is authenticated
  - Call AuthService.logoutUser()
  - Invalidate Spring Session
  - Clear HTTP-only session cookie
  - Log logout event with user ID and timestamp
  - Return 200 OK with `{ "status": "logged_out" }`
- [ ] T036 [P] [US3] Update AuthService (T014) with `logoutUser()` implementation:
  - Get current user ID
  - Invalidate session via Spring Session API
  - Log "User logged out" event
  - No errors if already logged out (idempotent)
- [ ] T037 [US3] Update Logout component (T022) to handle logout response:
  - Disable logout button during logout processing
  - Show confirmation message on success
  - Show error message if logout fails
  - Clear local auth state
  - Navigate to login page
- [ ] T038 [P] [US3] Update auth HTTP interceptor (T017) to handle 401 responses:
  - On 401 Unauthorized: clear local auth state, redirect to login page
  - Prevents access to protected endpoints after logout
- [ ] T039 [P] [US3] Update frontend auth guard (T018) to:
  - Check AuthService.isAuthenticated()
  - If false: prevent navigation, redirect to login
  - Handles case where session cookie exists but is expired (guards all routes)
- [ ] T040 Update AuthController (T019) GET /api/auth/user/profile to:
  - Require @PreAuthorize("isAuthenticated()")
  - Return 401 Unauthorized if not authenticated (already via decorator, but verify)
- [ ] T041 Verify User Story 3 against quickstart.md Scenario 4 (Logout)

**Checkpoint**: User Story 3 complete. Logout works, sessions terminated, protected endpoints enforced.

---

## Phase 6: User Story 4 - Session Persistence (Priority: P2)

**Goal**: Logged-in user refreshes page, remains logged in without re-authentication, session restored

**Independent Test**: Login user → refresh page → verify on homepage (not login), profile visible, API calls work

### Implementation for User Story 4

- [ ] T042 [P] [US4] Update frontend AuthService (T015) with `checkSessionOnLoad()` method:
  - Called on app initialization (app.component.ts)
  - Calls GET /api/auth/user/profile to validate session
  - If 200: sets authenticated state, loads profile
  - If 401: sets unauthenticated state, redirects to login
  - Enables session restoration across page refresh
- [ ] T043 [US4] Update frontend app component (`frontend/src/app/app.component.ts`) to:
  - Call AuthService.checkSessionOnLoad() on ngOnInit
  - Wait for session check before rendering main app
  - Show loading spinner during check
  - Redirect to login if no session (or show login screen)
- [ ] T044 [P] [US4] Configure Spring Session to use in-memory store (or database-backed if preferred):
  - Update `application.yml` session store-type: none (in-memory) or jdbc (database)
  - Verify session persists across requests (already configured in T012, verify)
  - 12-hour absolute timeout enforced
- [ ] T045 [US4] Update browser cookie handling in frontend:
  - Verify SESSION cookie is HTTP-only (set by backend, not modifiable by JS)
  - Verify cookie sent with every request (withCredentials: true in interceptor T017)
  - Verify SameSite=Strict flag present (set by backend)
- [ ] T046 [US4] Add logging to AuthService (T014) for session lifecycle:
  - Log "Session restored on page refresh" when checkSessionOnLoad succeeds
  - Log "Session invalid, redirect to login" when checkSessionOnLoad fails
  - Include user ID and timestamp
- [ ] T047 Verify User Story 4 against quickstart.md Scenario 3 (Session Persistence Across Page Refresh)

**Checkpoint**: User Story 4 complete. Session persists across page refreshes, user remains authenticated.

---

## Phase 7: User Story 5 - Profile Data Retrieval (Priority: P2)

**Goal**: Logged-in user can retrieve persisted profile data (provider, email, displayName, avatarUrl)

**Independent Test**: Login user → call GET /api/auth/user/profile → verify returns all profile fields (including optional fields if present)

### Implementation for User Story 5

- [ ] T048 [P] [US5] Update AuthController (T019) GET /api/auth/user/profile implementation:
  - Get current authenticated user via Spring Security Principal
  - Call AuthService.getUserProfile(userId)
  - Return UserProfileResponseDTO with all fields: id, provider, email, displayName, avatarUrl
  - Handle null/missing optional fields (return null or omit per contract)
  - Log profile retrieval event with user ID
- [ ] T049 [P] [US5] Update AuthService (T014) with `getUserProfile(userId)` implementation:
  - Fetch User from UserRepository by id
  - Convert to UserProfileResponseDTO
  - Handle User not found (return empty or 404)
  - Log retrieval with timestamp
  - Target response time <200ms per SC-008
- [ ] T050 [US5] Verify UserProfileResponseDTO (T010) includes all fields:
  - id (Long)
  - provider (String)
  - email (String, nullable)
  - displayName (String, nullable)
  - avatarUrl (String, nullable)
- [ ] T051 [P] [US5] Create frontend ProfileComponent in `frontend/src/app/auth/components/profile/profile.component.ts` to:
  - Call ProfileService.getProfile() on load
  - Display user profile data (name, email, avatar)
  - Handle missing optional fields gracefully
- [ ] T052 [P] [US5] Create frontend ProfileComponent template `frontend/src/app/auth/components/profile/profile.component.html` with profile UI (name, email, avatar image)
- [ ] T053 [US5] Update frontend ProfileService (T023) to:
  - Fetch profile via AuthService.getProfile()
  - Cache result (avoid repeated fetches)
  - Handle null/missing fields
  - Return Observable<UserProfile> for component subscription
- [ ] T054 [US5] Add performance test to verify profile endpoint response time <200ms per SC-008
- [ ] T055 Verify User Story 5 against quickstart.md Scenario 5 (Profile Data Retrieval)

**Checkpoint**: User Story 5 complete. Profile endpoint returns all persisted OAuth data.

---

## Phase 8: Edge Cases & Error Handling

**Purpose**: Handle OAuth and session edge cases per spec requirements

- [ ] T056 [P] Add error handling for Google OAuth service unreachable:
  - CustomOAuth2UserService catches OAuth2AuthenticationException
  - Returns user-friendly error message "Authentication service unavailable. Please try again."
  - Logged as warning with error details for debugging
- [ ] T057 [P] Add error handling for OAuth callback failure:
  - AuthController catches failed OAuth flow
  - Redirects to login with error parameter
  - Frontend displays error message
  - Logged as warning with error reason
- [ ] T058 [P] Add error handling for concurrent first-login attempts (race condition):
  - Database unique constraint on (provider, providerUserId) enforces single record
  - If duplicate insert fails: catch unique constraint exception, fetch existing user, return existing user (idempotent)
  - Log event as "Duplicate login attempt detected, using existing user"
- [ ] T059 [P] Add error handling for provider data changes (display_name changes):
  - CustomOAuth2UserService updates User.displayName on every login (allows profile updates)
  - Logs "User profile updated: displayName changed"
  - No duplicate creation
- [ ] T060 [P] Add error handling for private/incognito browsing (session cleared):
  - Frontend checkSessionOnLoad (T043) handles 401 response
  - User redirected to login
  - No error on page load
- [ ] T061 Add error handling for expired session (12-hour timeout):
  - Spring Session automatically invalidates after 12 hours
  - Next request returns 401 Unauthorized
  - Frontend redirects to login
  - Logged as "Session expired" with expiry time
- [ ] T062 Verify all edge cases against quickstart.md Scenario 6 (OAuth Failure Handling)

**Checkpoint**: All edge cases handled gracefully per acceptance criteria.

---

## Phase 9: Logging & Audit Trail

**Purpose**: Implement comprehensive logging per FR-012 and SC-009

- [ ] T063 [P] Configure SLF4J logger for auth package in `backend/src/main/java/com/example/inventory/service/AuthService.java`:
  - Log level: INFO for successful events (login, logout, profile retrieval)
  - Log level: WARN for failures (invalid token, expired session, OAuth error)
- [ ] T064 [P] Add auth event logging in AuthService (T014):
  - Login event: `logger.info("User login: provider={}, providerUserId={}, timestamp={}", provider, providerUserId, now())`
  - Logout event: `logger.info("User logout: userId={}, timestamp={}", userId, now())`
  - Token refresh event: `logger.info("Session refreshed: userId={}, timestamp={}", userId, now())`
  - Failed login event: `logger.warn("Login failed: reason={}, error={}, timestamp={}", reason, error, now())`
- [ ] T065 [P] Add auth event logging in CustomOAuth2UserService (T011):
  - New user created: `logger.info("New user created: provider={}, providerUserId={}, email={}, timestamp={}", ...)`
  - Existing user reused: `logger.info("Existing user reused: provider={}, providerUserId={}, userId={}, timestamp={}", ...)`
  - OAuth error: `logger.warn("OAuth error: error={}, details={}, timestamp={}", error, details, now())`
- [ ] T066 [P] Add auth event logging in ExceptionHandler (T013):
  - 401 Unauthorized: `logger.warn("Unauthorized access attempt: path={}, ip={}, timestamp={}", path, ip, now())`
  - 500 Server error: `logger.error("Authentication server error: error={}, details={}, timestamp={}", error, stackTrace, now())`
- [ ] T067 Verify logging implementation against quickstart.md Scenario 7 (Logging & Audit Trail)
- [ ] T068 [P] Configure logback to output auth logs to file: `backend/logs/auth.log` or application logs (SLF4J default)

**Checkpoint**: All authentication events logged with timestamp, user ID, outcome, and error details.

---

## Phase 10: Cross-Cutting Concerns & Polish

**Purpose**: Performance, security hardening, documentation

- [ ] T069 [P] Add CORS configuration to SecurityConfig (T012):
  - Allow origins: http://localhost:4200 (dev), https://yourdomain.com (prod)
  - Allow credentials: true (required for cookies)
  - Allow methods: GET, POST, OPTIONS
  - Allow headers: Content-Type, Authorization
- [ ] T070 [P] Add CSRF protection to SecurityConfig (T012):
  - CSRF enabled for POST endpoints
  - Token endpoint: /api/auth/login (CSRF disabled for OAuth redirect)
  - Other endpoints: /api/auth/logout, GET /api/auth/user/profile (CSRF enabled)
- [ ] T071 [P] Add HTTP security headers to SecurityConfig (T012):
  - Strict-Transport-Security (HSTS): max-age=31536000 (1 year, HTTPS only)
  - X-Content-Type-Options: nosniff
  - X-Frame-Options: DENY (prevent clickjacking)
- [ ] T072 [P] Update frontend security:
  - Add Content Security Policy headers (if applicable)
  - Validate OAuth callback state parameter (already done by Spring Security)
  - Sanitize user profile data (displayName, email) before display
- [ ] T073 [P] Add performance monitoring:
  - Log login flow timing: start → end per T032
  - Verify <30s first-time login (SC-001)
  - Verify <10s return login (SC-002)
  - Verify <200ms profile endpoint (SC-008)
  - Log slow queries on profile retrieval
- [ ] T074 [P] Code cleanup:
  - Remove debug/test code
  - Verify no hardcoded credentials or tokens
  - Check for unused imports and variables
- [ ] T075 [P] Update documentation:
  - Add deployment guide: env vars for Google OAuth (CLIENT_ID, CLIENT_SECRET)
  - Add troubleshooting guide for common OAuth errors
  - Update README with authentication flow diagram
- [ ] T076 Run quickstart.md validation:
  - Execute all 7 validation scenarios
  - Verify each scenario passes independently
  - Document any issues found
- [ ] T077 Update API documentation (Swagger/OpenAPI if applicable):
  - Document /api/auth/login endpoint
  - Document /api/auth/logout endpoint
  - Document /api/auth/user/profile endpoint
  - Include example request/response for profile endpoint

**Checkpoint**: Feature complete, tested, documented, performance targets met, security hardened.

---

## Dependencies & Execution Order

### Phase Dependencies

1. **Setup (Phase 1)**: No dependencies → start immediately
2. **Foundational (Phase 2)**: Depends on Setup → BLOCKS all user stories
3. **User Story 1 (Phase 3)**: Depends on Foundational (Phase 2)
4. **User Story 2 (Phase 4)**: Depends on US1 (Phase 3) for deduplication testing; can start after Foundational
5. **User Story 3 (Phase 5)**: Depends on Foundational; can run in parallel with US1/US2
6. **User Story 4 (Phase 6)**: Depends on US1; can run in parallel after US1 complete
7. **User Story 5 (Phase 7)**: Depends on Foundational; can run in parallel
8. **Edge Cases (Phase 8)**: Depends on all user stories
9. **Logging (Phase 9)**: Can start after Foundational; affects all user stories (add as you implement)
10. **Polish (Phase 10)**: Final cleanup after all user stories

### User Story Dependencies

- **US1** (First-Time Login): Core prerequisite for all others; must complete first
- **US2** (Return Login): Depends on US1 for deduplication testing; requires US1 complete
- **US3** (Logout): Independent of US1/US2; can run in parallel after Foundational
- **US4** (Session Persistence): Depends on US1 for testing; can run in parallel
- **US5** (Profile Retrieval): Independent; can run in parallel after Foundational

### Parallel Opportunities

**Within Phase 1 (Setup)**:
- T003, T004, T005 can run in parallel (different files, both configure settings)

**Within Phase 2 (Foundational)**:
- T007, T008, T010, T011, T014, T015, T016, T017, T018 marked [P] can run in parallel
- T009 must follow T007 (entity definition before migration)
- T012 must follow T013 (ExceptionHandler before SecurityConfig uses it)

**Between User Stories**:
- Once Foundational completes, US1/US3/US4/US5 can start in parallel
- US2 must follow US1 (for deduplication testing)
- Different developers can work on different stories independently

**Parallel Example: User Story 1**:
```
Parallel batch 1:
  - T021 (Login component TypeScript)
  - T022 (Login component template)
  - T025 (User model)

Parallel batch 2:
  - T023 (Logout component)
  - T026 (App routing)

Sequential dependency:
  T027 (Verify against quickstart)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only) 🎯

**Recommended for initial release:**

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL)
3. Complete Phase 3: User Story 1
4. Add Phases 8-10 (error handling, logging, polish)
5. **STOP and VALIDATE**: Run quickstart.md Scenario 1
6. Deploy/Demo MVP

**Result**: Users can sign in with Google for the first time. Sufficient for v1 release.

### Incremental Delivery (All Stories)

**For feature completeness:**

1. MVP as above (US1)
2. Add User Story 2 (return login, deduplication)
3. Add User Story 3 (logout)
4. Add User Story 4 (session persistence)
5. Add User Story 5 (profile retrieval)
6. Complete Phases 8-10 (edge cases, logging, polish)

**Each iteration adds value independently; users never see broken state**

### Parallel Team Strategy

**If 3-5 developers available:**

1. **Team (all)**: Phases 1-2 (Setup + Foundational) together
2. **Once Foundational done**:
   - Developer A: US1 (First-time login) + validate
   - Developer B: US3 (Logout)
   - Developer C: US4 (Session persistence)
   - Developer D: US5 (Profile retrieval)
3. **After US1 validated**:
   - Developer B (after US3): US2 (Return login, deduplication)
4. **All converge**: Phases 8-10 (edge cases, logging, polish)

**Timeline estimate**: 
- Phases 1-2 (Foundational): 2-3 days
- US1: 3-4 days → Deploy MVP
- US2/US3/US4/US5: 2-3 days each (parallel)
- Phases 8-10 (Polish): 1-2 days

---

## Validation Checkpoints

- ✅ **After T018 (Phase 2)**: Run database migrations, verify users table created
- ✅ **After T027 (US1)**: Run quickstart.md Scenario 1, verify first-time login works
- ✅ **After T034 (US2)**: Run quickstart.md Scenario 2, verify deduplication and <10s login
- ✅ **After T041 (US3)**: Run quickstart.md Scenario 4, verify logout works
- ✅ **After T047 (US4)**: Run quickstart.md Scenario 3, verify session persistence
- ✅ **After T055 (US5)**: Run quickstart.md Scenario 5, verify profile endpoint
- ✅ **After T062 (Edge Cases)**: Run quickstart.md Scenario 6, verify error handling
- ✅ **After T067 (Logging)**: Run quickstart.md Scenario 7, verify all events logged
- ✅ **After T077 (Polish)**: Run all 7 scenarios end-to-end, verify no regressions

---

## Task Summary

- **Total Tasks**: 76
- **Phase 1 (Setup)**: 6 tasks
- **Phase 2 (Foundational)**: 12 tasks (BLOCKING - must complete first)
- **Phase 3 (US1)**: 8 tasks
- **Phase 4 (US2)**: 7 tasks (depends on US1)
- **Phase 5 (US3)**: 7 tasks
- **Phase 6 (US4)**: 6 tasks
- **Phase 7 (US5)**: 8 tasks
- **Phase 8 (Edge Cases)**: 7 tasks
- **Phase 9 (Logging)**: 6 tasks
- **Phase 10 (Polish)**: 9 tasks

**Parallelizable Tasks**: ~40 (marked [P])

**MVP Scope**: Phases 1, 2, 3 + T056-T061 + T075 = ~37 tasks (focus on US1 first)
