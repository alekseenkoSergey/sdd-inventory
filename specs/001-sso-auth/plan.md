# Implementation Plan: SSO-Only Authentication

**Branch**: `001-sso-auth` | **Date**: 2026-08-18 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-sso-auth/spec.md`

## Summary

Implement Google OAuth 2.0 / OpenID Connect authentication as the sole authentication mechanism for the Inventory Tracker application. On first successful sign-in, automatically create a local user record with profile data (provider, provider_user_id, email, display_name, avatar_url). Maintain session state across browser refreshes with 12-hour absolute expiry. Provide logout functionality and comprehensive audit logging of all authentication events. All changes respect the project constitution's technology stack (Spring Boot backend, Angular frontend, PostgreSQL, Flyway migrations).

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.x), Angular 17+

**Primary Dependencies**: 
- Backend: Spring Boot 3.x, Spring Security 6.x, OAuth2 Client (spring-boot-starter-oauth2-client), Spring Data JPA
- Frontend: Angular, @angular/common/http, browser localStorage/sessionStorage for token management

**Storage**: PostgreSQL with Flyway migrations for schema versioning

**Testing**: 
- Backend: JUnit 5, Mockito, Spring Test
- Frontend: Jasmine, Karma, Angular TestBed

**Target Platform**: Linux server (backend), modern web browsers (frontend)

**Project Type**: Web service (Spring Boot REST API) + single-page application (Angular)

**Performance Goals**: 
- Login flow: <30 seconds end-to-end (excluding Google's UI)
- Return login: <10 seconds
- Profile endpoint: <200ms response time
- Logout: <1 second

**Constraints**: 
- HTTPS required in production (secure OAuth cookies)
- Session tokens stored in HTTP-only secure cookies (not localStorage)
- 12-hour absolute session expiry (no activity extension)
- Single-server deployment (pet project; no distributed session store required)

**Scale/Scope**: 
- Pet project; single-server, <100 concurrent users
- No horizontal scaling required
- No multi-device single sign-on requirement

## Constitution Check

**Gate Status**: ✅ PASS

| Principle | Assessment |
|-----------|-----------|
| **I. Simplicity First** | ✅ PASS — Feature uses Spring Boot's built-in OAuth2 Client starter; no custom frameworks or abstractions. Direct use of Spring Security. No speculative features. |
| **II. Technology Stack** | ✅ PASS — Java + Spring Boot, PostgreSQL, Flyway, Angular. No new major frameworks introduced. OAuth2 Client is a standard Spring Boot dependency. |
| **III. Backend Layered Structure** | ✅ PASS — Authentication controller → service → repository pattern. DTOs for request/response. Entities for User domain model. No deviation from layered architecture. |
| **IV. Centralized Exception Handling** | ✅ PASS — OAuth errors and validation failures handled by centralized @ControllerAdvice. Authentication failures converted to consistent API error responses. |

**No violations. Proceeding to Phase 0 research.**

## Project Structure

### Documentation (this feature)

```text
specs/001-sso-auth/
├── spec.md                # Feature specification
├── plan.md                # This file
├── research.md            # Phase 0 output (technical decisions, integration patterns)
├── data-model.md          # Phase 1 output (entity definitions, migrations)
├── contracts/             # Phase 1 output (OAuth endpoints, API contracts)
│   ├── auth-endpoints.md
│   └── user-profile-api.md
├── quickstart.md          # Phase 1 output (validation and runnable scenarios)
├── checklists/
│   └── requirements.md    # Specification quality checklist
└── tasks.md               # Phase 2 output (breakdown of implementation work)
```

### Source Code Structure (repository layout)

This project follows a monolithic layered architecture with frontend and backend separation:

```text
backend/src/main/java/com/example/inventory/
├── controller/
│   └── AuthController.java          # HTTP endpoints for authentication
├── service/
│   ├── AuthService.java             # Business logic for user provisioning, session management
│   └── CustomOAuth2UserService.java # Custom OAuth2 user provisioning logic
├── model/
│   ├── UserProfileResponseDTO.java  # DTOs for responses
│   └── UserProfileRequestDTO.java   # DTOs for requests
├── entity/
│   └── User.java                    # Domain model (User entity)
├── repository/
│   └── UserRepository.java          # Data access layer for User
├── config/
│   ├── SecurityConfig.java          # Spring Security configuration
│   └── ExceptionHandler.java        # Centralized @ControllerAdvice

backend/src/main/resources/db/migration/
├── V1__initial_schema.sql           # Core app schema
└── V2__create_user_table.sql        # User entity schema (this feature)

backend/src/test/java/com/example/inventory/
├── controller/
│   └── AuthControllerTest.java
├── service/
│   ├── AuthServiceTest.java
│   └── CustomOAuth2UserServiceTest.java
└── repository/
    └── UserRepositoryTest.java

frontend/src/app/
├── auth/
│   ├── components/
│   │   ├── login/
│   │   │   └── login.component.ts
│   │   └── logout/
│   │       └── logout.component.ts
│   ├── guards/
│   │   └── auth.guard.ts            # Route guard to protect authenticated pages
│   ├── services/
│   │   ├── auth.service.ts          # Manages OAuth flow, token lifecycle
│   │   ├── oauth.service.ts         # Handles Google OAuth redirect/callback
│   │   └── profile.service.ts       # Fetches and caches user profile
│   ├── interceptors/
│   │   └── auth.interceptor.ts      # Attaches bearer token to requests
│   └── models/
│       └── user.model.ts            # TypeScript interface for User

frontend/src/app/
├── core/
│   └── http/
│       └── api.service.ts           # HTTP client wrapper

frontend/src/
└── index.html                       # OAuth redirect URI callback page
```

**Structure Decision**: Web application with separate backend (Spring Boot REST API) and frontend (Angular SPA). Backend uses project's standard layered architecture (controller → service → repository) without auth-specific sub-packages. Frontend manages UI flows in auth feature module (components, guards, services, interceptors) for clean separation of concerns.

---

## Phase 0: Research & Technical Decisions

### OAuth2 Flow & Library Choice

**Decision**: Use Spring Security OAuth2 Client starter (spring-boot-starter-oauth2-client) for server-side OAuth2 handling.

**Rationale**: 
- Spring Boot integrates OAuth2 Client handling via autoconfiguration; minimal custom code required.
- Aligns with project's "Simplicity First" principle and Spring Boot standard stack.
- Handles authorization code flow, token exchange, and PKCE automatically.

**Alternatives Considered**:
- Custom OAuth2 implementation: Rejected for complexity and security risk.
- Third-party OAuth libraries (e.g., OAuthLib): Rejected because Spring Boot's built-in support is more directly integrated.

### Session Management Strategy

**Decision**: Use server-side session storage (Spring Session) with HTTP-only secure cookies. Store sessions in a simple in-memory store (suitable for single-server pet project); upgrade to Redis/database-backed if multi-server deployment needed later.

**Rationale**:
- HTTP-only secure cookies prevent XSS-based token theft (meets security requirement in Assumption C).
- Server-side session allows fine-grained control over timeout (12-hour absolute expiry).
- Spring Session integrates cleanly with Spring Security.

**Alternatives Considered**:
- JWT tokens in localStorage: Rejected because it's vulnerable to XSS and doesn't meet "secure cookie" requirement.
- JWT in HTTP-only cookies: Viable but adds complexity for session management; Spring Session is simpler for this project.

### User Provisioning on First Login

**Decision**: Implement a custom OAuth2UserService that intercepts successful OAuth authentication, creates a local User entity if needed, and stores profile data.

**Rationale**:
- Spring Security's OAuth2UserService hook allows custom provisioning logic.
- Automatic user record creation on first sign-in meets requirement FR-003.
- Leverages Spring Data JPA for persistence.

**Alternatives Considered**:
- Manual controller-level OAuth handling: Rejected for loss of Spring Security integration.
- External user provisioning service: Rejected as over-engineering for a pet project.

### Logging Strategy

**Decision**: Use SLF4J + Logback (Spring Boot default) to log all authentication events. Log to application logs with timestamps, user ID, event type, and outcome. Include audit fields per FR-012.

**Rationale**:
- SLF4J + Logback is Spring Boot's default; no extra dependency.
- Centralized logging supports both debugging and security audits.
- Simple string-based logging sufficient for pet project (no complex audit trail DB required).

**Alternatives Considered**:
- Separate audit database table: Rejected as over-engineering; logs sufficient.
- Application Insights / external logging: Rejected for pet project scope.

### Frontend Token Handling

**Decision**: Angular HTTP interceptor automatically attaches JWT or session-managed token to all outgoing requests. Frontend redirects to login on 401 responses.

**Rationale**:
- HTTP interceptor is Angular standard pattern for token attachment.
- Automatic 401 handling prevents manual token refresh logic scattered across components.
- Aligns with Angular security best practices.

**Alternatives Considered**:
- Manual token attachment per request: Rejected for maintainability.
- Silent token refresh on expiry: Rejected because 12-hour absolute expiry makes this unnecessary (users can re-login).

---

## Phase 1: Design Artifacts

### Data Model (data-model.md)

**Entity: User**

```
User (domain entity, persisted in PostgreSQL)
├── id: Long (primary key, auto-generated)
├── provider: String (e.g., "Google") — identifies OAuth provider
├── provider_user_id: String — OAuth subject claim (unique per provider)
├── email: String (nullable) — from provider profile
├── display_name: String (nullable) — from provider profile
├── avatar_url: String (nullable) — from provider profile
├── created_at: Timestamp — record creation time
├── updated_at: Timestamp — record last modified time
└── Unique Constraint: (provider, provider_user_id)
```

**Sessions**

Managed by Spring Session in-memory store (or upgradeable to database/Redis).

```
Session (managed by Spring Session framework)
├── sessionId: String (unique identifier)
├── userId: Long (foreign key to User)
├── createdAt: Timestamp
├── expiresAt: Timestamp (12 hours after creation, no extension)
└── Stored in: in-memory map or persistent store (configurable)
```

### API Contracts (contracts/*.md)

#### Authentication Endpoints

**POST /api/auth/oauth2/callback**
- Purpose: OAuth2 callback endpoint (internal; handled by Spring Security)
- Request: OAuth authorization code + state parameter
- Response: Redirect to homepage on success; error page on failure
- Security: CSRF protected, HTTPS enforced

**GET /api/auth/login**
- Purpose: Initiate OAuth2 login flow
- Request: Optional `redirectUri` query parameter
- Response: Redirect to Google OAuth consent screen
- Security: Anonymous access allowed

**POST /api/auth/logout**
- Purpose: Terminate user session
- Request: Authenticated request (bearer token or session cookie)
- Response: `{ "status": "logged_out" }`
- Side effects: Invalidates session, clears HTTP-only cookie
- Status: 200 OK on success, 401 Unauthorized if not authenticated

**GET /api/auth/user/profile**
- Purpose: Retrieve authenticated user's profile
- Request: Authenticated request
- Response: `{ "id": Long, "provider": String, "email": String, "displayName": String, "avatarUrl": String }`
- Status: 200 OK on success, 401 Unauthorized if not authenticated

#### Error Responses

All endpoints return consistent error format on failure:

```json
{
  "timestamp": "2026-08-18T10:30:00Z",
  "status": 400,
  "error": "OAuth2AuthenticationException",
  "message": "User-friendly error message describing what went wrong",
  "path": "/api/auth/login"
}
```

Common status codes:
- `400 Bad Request`: Invalid OAuth parameters, malformed request
- `401 Unauthorized`: Missing authentication, expired token, invalid token
- `403 Forbidden`: Access to resource not allowed
- `500 Internal Server Error`: OAuth provider unreachable, database error

### Validation & Quickstart (quickstart.md)

**Prerequisites**:
- Google OAuth application credentials (Client ID, Client Secret) configured in environment
- Backend running on `http://localhost:8080`
- Frontend running on `http://localhost:4200`

**Scenario 1: First-Time User SSO Login**

1. Start backend and frontend
2. Navigate to `http://localhost:4200` (login page)
3. Click "Login with Google"
4. Complete Google OAuth consent on Google's login screen
5. Redirected back to application homepage
6. **Verify**: User is logged in, profile displayed
7. **Verify**: `GET /api/auth/user/profile` returns user data with `provider="Google"`, email, display_name
8. **Verify**: Database contains new User record with (provider, provider_user_id) lookup key

**Scenario 2: Return User Login**

1. Logout from previous session
2. Login again with the same Google account
3. **Verify**: Existing User record is reused (no duplicate created)
4. **Verify**: Login completes in <10 seconds
5. **Verify**: Session established without requiring re-authentication

**Scenario 3: Session Persistence**

1. Login a user
2. Refresh browser page (`Ctrl+R` or `Cmd+R`)
3. **Verify**: User remains logged in, no login screen shown
4. **Verify**: API calls succeed without re-authentication

**Scenario 4: Logout**

1. Login a user
2. Click "Logout" button
3. **Verify**: Session terminated, redirected to login page
4. **Verify**: Attempting to access protected endpoint (e.g., `GET /api/auth/user/profile`) returns 401 Unauthorized

**Scenario 5: Session Expiry**

1. Login a user
2. Modify session cookie expire time to current time (or wait for real 12-hour expiry in testing environment)
3. **Verify**: Next request to protected endpoint returns 401 Unauthorized
4. **Verify**: User must re-login

**Scenario 6: OAuth Failure Handling**

1. User clicks "Login with Google"
2. Simulate network failure or user denies consent
3. **Verify**: User-friendly error message displayed
4. **Verify**: Application state not broken; user can retry login

---

## Complexity Tracking

No constitution violations. All design choices respect "Simplicity First" and standard Spring Boot patterns.

---

## Next Phase: Task Generation

Once this plan is approved, run:

```
/speckit-tasks
```

This will decompose the design above into actionable, dependency-ordered implementation tasks.
