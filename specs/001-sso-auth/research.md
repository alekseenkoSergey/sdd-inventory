# Research: SSO-Only Authentication Technical Decisions

**Date**: 2026-08-18 | **Feature**: SSO-Only Authentication

This document consolidates technical research and design decisions for the Google OAuth 2.0 / OpenID Connect authentication feature.

## Decision 1: OAuth2 Flow & Library Implementation

**Decision**: Use Spring Security OAuth2 Client starter (spring-boot-starter-oauth2-client) for server-side OAuth2 handling.

**Rationale**: 
- Spring Boot provides transparent autoconfiguration for OAuth2 Client; minimal boilerplate required.
- Aligns with project's "Simplicity First" principle—no custom authentication framework needed.
- Handles authorization code flow, token exchange, and PKCE automatically.
- Integrates seamlessly with Spring Security for authorization filtering.
- Official Spring support ensures security patches and best practices.

**Alternatives Considered**:
1. **Custom OAuth2 implementation**: Build custom authorization code flow handler from scratch.
   - Rejected: High complexity, security risk, requires extensive testing of token exchange and PKCE handling.
2. **Third-party OAuth library (e.g., OAuthLib)**: General-purpose OAuth library.
   - Rejected: Adds external dependency; Spring Boot's built-in support is more directly integrated with Security.
3. **Keycloak or Auth0**: External identity provider.
   - Rejected: Over-engineering for pet project; violates "Simplicity First" principle.

**Implementation Detail**: Configure `application.yml` with Google OAuth client credentials and redirect URIs. Spring Security automatically redirects unauthenticated users to `/oauth2/authorization/google`, handles the callback at `/login/oauth2/code/google`, and manages token state.

---

## Decision 2: Session Management & Storage

**Decision**: Use Spring Session with HTTP-only secure cookies for session tokens. Store sessions in memory (Spring Session's default in-memory store) suitable for single-server deployment. Design allows future upgrade to Redis or database-backed store without code changes.

**Rationale**:
- **HTTP-only cookies**: Prevents XSS-based session hijacking (meets Assumption C requirement).
- **Secure flag**: Cookies only sent over HTTPS in production, preventing man-in-the-middle attacks.
- **Server-side storage**: Full control over session timeout (12-hour absolute expiry per FR-011, no activity extension).
- **Spring Session integration**: Abstracts session storage; code remains agnostic to backend (memory vs. Redis vs. database).
- **Pet project scale**: In-memory store sufficient; no distributed session complexity needed.

**Alternatives Considered**:
1. **JWT tokens in localStorage**: Frontend stores JWT in browser's localStorage.
   - Rejected: Vulnerable to XSS attacks (localStorage is readable by JavaScript). Does not meet "secure cookie" requirement.
2. **JWT tokens in HTTP-only cookies**: Backend signs JWT, returns in HTTP-only cookie.
   - Viable alternative but adds complexity: requires manual token refresh logic, CSRF protection, and expiry handling. Spring Session is simpler for this project.
3. **Custom session storage**: Roll custom session store with GUID keys.
   - Rejected: Duplicates Spring Session functionality; introduces maintenance burden.

**Implementation Detail**: Configure Spring Session to use `org.springframework.session.FindByIndexNameSessionRepository` with in-memory backend. Session timeout configured to 12 hours in `application.yml`. Frontend never accesses session token directly; Spring handles cookie management.

---

## Decision 3: User Provisioning on First Login

**Decision**: Implement a custom `OAuth2UserService` that intercepts successful OAuth authentication, checks for existing User by (provider, provider_user_id), creates a new User if needed, and stores profile data in PostgreSQL.

**Rationale**:
- **Automatic user creation** (FR-003): On first successful sign-in, backend automatically creates local user record.
- **Deduplication** (FR-008): Lookup by (provider, provider_user_id) ensures returning users reuse existing records.
- **Spring Security integration**: OAuth2UserService is the official extension point; no custom token handling needed.
- **Leverages Spring Data JPA**: Simple repository interface for User persistence.
- **Follows constitution's layered architecture**: Service layer handles business logic, repository handles data access.

**Alternatives Considered**:
1. **Manual controller-level handling**: ApplicationOAuth2UserProvider in controller layer.
   - Rejected: Loses Spring Security's automatic role mapping, authority handling, and OAuth state validation.
2. **Event-driven provisioning**: Publish event on OAuth success; listener creates user asynchronously.
   - Rejected: Over-engineering for pet project; introduces async complexity without benefit.
3. **JPA event listener**: Use `@PrePersist` on OAuth2User domain object.
   - Rejected: Couples provisioning logic to domain entity; violates separation of concerns.

**Implementation Detail**: Create `CustomOAuth2UserService` in `service/` package extending `DefaultOAuth2UserService`. Override `loadUser()` method to call parent, then check UserRepository for (provider, provider_user_id). If not found, create new User entity with profile data (email, displayName, avatarUrl from OAuth2User attributes). Return user to Spring Security for session establishment. Register as bean in `SecurityConfig.java`.

---

## Decision 4: Logging & Audit Trail

**Decision**: Use SLF4J + Logback (Spring Boot default) to log all authentication events. Log events include: timestamp, user ID (if authenticated), event type (login, logout, token refresh, failed attempt), outcome (success/failure), and error reason (if failure). Logs written to application's default log file. No separate audit database table required.

**Rationale**:
- **No extra dependency**: SLF4J + Logback bundled with Spring Boot.
- **Supports debugging**: Logs capture auth flow including failures, aiding troubleshooting.
- **Supports security audits**: Timestamp + user ID + event type sufficient for breach investigation and compliance.
- **Pet project simplicity**: File-based logging sufficient; no need for centralized audit store.
- **Meets FR-012**: All auth events logged with required fields.

**Alternatives Considered**:
1. **Separate audit database table**: Create Audit entity, persist all auth events to database.
   - Rejected: Over-engineering for pet project. File logs sufficient and easier to configure.
2. **External logging (e.g., Application Insights, Splunk)**: Send logs to third-party service.
   - Rejected: Pet project doesn't warrant external infrastructure; adds operational dependency.
3. **Spring Security's built-in logging**: Use Spring Security's AuthenticationSuccessHandler / AuthenticationFailureHandler.
   - Viable but insufficient: Doesn't capture logout, token refresh, or session events. Custom implementation needed.

**Implementation Detail**: Create authentication-specific logger in service layer (`LoggerFactory.getLogger("auth")`). Log at INFO level for successful events (login, logout, session refresh) and WARN level for failures (invalid token, expired session, OAuth error). Include user ID, timestamp (automatic via Logback), and event description.

---

## Decision 5: Frontend Session & Token Management

**Decision**: Frontend uses Angular HTTP interceptor to automatically attach session cookie (managed by browser) to all outgoing requests. On 401 Unauthorized responses, interceptor redirects user to login page. No manual token management required in components.

**Rationale**:
- **HTTP interceptor is Angular standard**: Avoids duplicating token attachment logic across components.
- **Automatic 401 handling**: Provides consistent UX; users see login page after session expiry.
- **Simplicity**: Browser handles session cookie lifecycle; Angular doesn't need custom logic.
- **Aligns with backend**: Backend sets HTTP-only cookie; frontend doesn't need localStorage/sessionStorage.
- **Secure**: Cookies are browser-managed and HTTP-only; no XSS risk.

**Alternatives Considered**:
1. **Manual token attachment per request**: Services explicitly pass token to every HTTP call.
   - Rejected: Duplicates logic across services; maintainability burden.
2. **Silent token refresh on 401**: Interceptor refreshes token on expiry, retries request.
   - Rejected: 12-hour absolute expiry means refresh isn't useful; users can re-login.
3. **LocalStorage for token**: Frontend stores JWT in localStorage for persistence.
   - Rejected: Vulnerable to XSS; doesn't meet security requirements.

**Implementation Detail**: Create `AuthInterceptor` in Angular that implements `HttpInterceptor`. In `intercept()`, check if request is to protected endpoint; if so, include `withCredentials: true` to send cookies. On 401 response, navigate to `/login`. For token refresh scenario (if ever needed), interceptor can call AuthService to refresh and retry.

---

## Decision 6: Session Expiry Strategy

**Decision**: Sessions expire after a fixed 12-hour absolute timeout from creation. User activity does NOT extend the session (per FR-011). After 12 hours, user's session is invalid regardless of recent requests.

**Rationale**:
- **Fixed expiry is simpler**: No need to track last-access time; Spring Session handles fixed timeout.
- **Security**: Limits exposure window if session is compromised; user must re-authenticate daily.
- **Aligns with spec**: FR-011 explicitly requires "absolute session expiry of 12 hours; sessions MUST NOT be extended by user activity."
- **Pet project scale**: No need for sliding-window complexity; users can easily re-login.

**Alternatives Considered**:
1. **Sliding window (activity-based)**: Session extends 12 hours from each request (typical for web apps).
   - Rejected: Violates explicit requirement FR-011 (no activity extension).
2. **Short expiry (1-2 hours)**: Tighter security.
   - Rejected: Annoying UX for pet project; 12 hours is reasonable compromise.
3. **No expiry (session-level only)**: Session lasts until logout or browser closes.
   - Rejected: Security risk; violates FR-011.

**Implementation Detail**: Configure Spring Session timeout in `application.yml`: `server.servlet.session.timeout=12h`. Spring Session enforces absolute expiry automatically. Frontend handles 401 redirects to login after expiry.

---

## Decision 7: Database Schema & Migrations

**Decision**: Define User entity as JPA entity with Flyway migration. Migration creates `users` table with columns for id, provider, provider_user_id, email, display_name, avatar_url, created_at, updated_at. Unique constraint on (provider, provider_user_id).

**Rationale**:
- **Flyway migrations**: Required by constitution (versioned schema changes via Flyway).
- **JPA entity**: Spring Data repositories handle CRUD; no raw SQL needed.
- **Deduplication constraint**: Unique (provider, provider_user_id) ensures no duplicate accounts for same provider + user.
- **Audit fields**: created_at and updated_at support debugging and audit trails.

**Alternatives Considered**:
1. **Embedded user data in session**: Store profile data only in session, not database.
   - Rejected: Loses data on session expiry; violates FR-003 (persistent user record).
2. **Single "accounts" table**: Combine all auth methods (future password auth, future OAuth providers).
   - Rejected: Over-generalization violates "Simplicity First"; add when needed.

**Implementation Detail**: Create `V1__create_users_table.sql` Flyway migration. Define columns with appropriate types (SERIAL for id, VARCHAR for strings, TIMESTAMP for dates). Add NOT NULL constraints on id, provider, provider_user_id. Add UNIQUE constraint on (provider, provider_user_id). Create index on provider_user_id for lookup speed.

---

## Summary: Design Decisions Map

| Decision | Choice | Key Rationale |
|----------|--------|---|
| OAuth2 Library | Spring Security OAuth2 Client | Built-in, minimal boilerplate, integrates with Spring Security |
| Session Storage | Spring Session + HTTP-only cookies | Secure against XSS, server-side control of expiry, scales to Redis if needed |
| User Provisioning | Custom OAuth2UserService | Automatic first-login account creation, deduplication, leverages Spring Data |
| Logging | SLF4J + Logback (default) | No extra dependency, supports auditing, pet project simple |
| Frontend Token | HTTP interceptor + browser cookies | Angular standard, automatic 401 handling, XSS-safe |
| Session Expiry | 12-hour absolute (no activity extension) | Meets explicit requirement FR-011, simpler than sliding window |
| Database | Flyway migration + JPA entity | Respects constitution, deduplication via unique constraint, audit fields |

All decisions respect the Inventory Tracker Constitution's four core principles and can be validated via the quickstart scenarios in `quickstart.md`.
