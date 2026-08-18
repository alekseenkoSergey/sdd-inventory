# Feature Specification: SSO-Only Authentication

**Feature Branch**: `001-sso-auth`

**Created**: 2026-08-18

**Status**: Draft

**Input**: User description: "Accounts and authentication (SSO only)
1) Authentication must be implemented via SSO only
2) The app must support Google OAuth / OpenID Connect
3) The app must support logout
4) Authentication must persist across page refresh
5) On first successful SSO sign-in, the backend must create a local user record automatically
6) The backend must persist at least the following user profile data
    - provider
    - provider_user_id
    - email if provided by the provider
    - display_name
    - avatar_url as optional"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - First-Time SSO Login (Priority: P1)

A new user visits the application and sees a login screen. They click the "Login with Google" button, are redirected to Google's authentication service, grant permission, and are redirected back to the application. The backend automatically creates a user record with their Google profile information. They are now logged in and can use the application.

**Why this priority**: This is the core authentication flow and the entry point for all users. Without this working, the system cannot function.

**Independent Test**: Can be fully tested by: visiting the app unauthenticated, clicking login, completing Google OAuth flow, and verifying the user is logged in and profile data is stored in the backend.

**Acceptance Scenarios**:

1. **Given** an unauthenticated user visits the application, **When** they click "Login with Google", **Then** they are redirected to Google's OAuth consent screen.
2. **Given** a user has granted consent on Google's OAuth screen, **When** they are redirected back to the application, **Then** a backend user record is created with their Google profile data.
3. **Given** a user has completed the first-time SSO flow, **When** the page reloads, **Then** they remain logged in without re-authenticating.
4. **Given** Google provides email and display name during OAuth, **When** the user is created, **Then** these fields are persisted in the backend user record.

---

### User Story 2 - Returning User Login (Priority: P1)

A returning user who has already signed in once revisits the application. They see the login screen again. They click "Login with Google", complete a faster Google authentication (often a single click if already logged into Google), and are immediately logged into the application using their existing backend user record.

**Why this priority**: This enables the core repeatable authentication experience and ensures session management works across visits.

**Independent Test**: Can be fully tested by: creating a user via first-time login, logging out, then logging in again and verifying the existing user record is used.

**Acceptance Scenarios**:

1. **Given** an existing user is logged out, **When** they click "Login with Google", **Then** they complete the OAuth flow and are logged in.
2. **Given** a user completes the OAuth flow on a return visit, **When** the backend receives their authentication, **Then** the existing user record (by provider + provider_user_id) is used rather than creating a duplicate.
3. **Given** a user is returned to the application after OAuth, **When** their session is established, **Then** they can access authenticated endpoints without re-authenticating.

---

### User Story 3 - Logout (Priority: P1)

A logged-in user clicks a "Logout" button or link in the application. Their session is terminated, they are redirected to the login screen, and attempting to access any authenticated endpoint fails, requiring them to log in again.

**Why this priority**: Logout is a critical security feature and required for any authentication system.

**Independent Test**: Can be fully tested by: logging in a user, clicking logout, and verifying they are redirected to the login screen and cannot access protected resources.

**Acceptance Scenarios**:

1. **Given** a logged-in user, **When** they click "Logout", **Then** their session is terminated.
2. **Given** a user has logged out, **When** they attempt to access the application, **Then** they see the login screen.
3. **Given** a logged-out user, **When** they attempt to access a protected endpoint directly (e.g., via URL), **Then** the request fails or redirects to login.

---

### User Story 4 - Session Persistence (Priority: P2)

A logged-in user refreshes the browser page or navigates away and back to the application. They remain logged in without seeing the login screen or requiring re-authentication, and their session context is fully restored.

**Why this priority**: Session persistence across page refreshes is essential for a seamless user experience. This is part of the core requirement but tested separately from initial login.

**Independent Test**: Can be fully tested by: logging in a user, performing a page refresh, and verifying they remain logged in with their session intact.

**Acceptance Scenarios**:

1. **Given** a logged-in user, **When** they refresh the page, **Then** they remain logged in.
2. **Given** a logged-in user on any route, **When** they close and reopen the browser, **Then** their session is restored (browser session storage / token mechanism allows this).
3. **Given** a user with a valid session token, **When** they make requests to protected endpoints, **Then** the backend accepts the token and serves content.

---

### User Story 5 - Profile Data Retrieval (Priority: P2)

A logged-in user can request their profile information from the backend. The backend returns the persisted SSO profile data (provider, provider_user_id, email, display_name, avatar_url) so the frontend can display it or use it for personalization.

**Why this priority**: Enables the frontend to display user information and supports personalization. Important for UX but not blocking core authentication.

**Independent Test**: Can be fully tested by: logging in a user and calling a profile endpoint to retrieve stored SSO data.

**Acceptance Scenarios**:

1. **Given** a logged-in user, **When** they request their profile endpoint, **Then** the backend returns their SSO profile data.
2. **Given** a user's profile includes optional data (like avatar_url), **When** the endpoint returns the profile, **Then** all available fields are included.
3. **Given** a user's profile is missing optional fields (e.g., no email from provider), **When** the endpoint returns the profile, **Then** missing fields are either omitted or null, but no error occurs.

---

### Edge Cases

- What happens if Google's OAuth service is unreachable during login?
- How does the system handle if a user signs in with Google using different email addresses (user@gmail.com vs user@company.com) from the same Google account?
- What if the OAuth callback fails after the user has granted consent?
- How does the system handle a provider (Google) returning different data on subsequent logins (e.g., display name changes)?
- What happens if a user attempts to access the application from an incognito/private browsing window where session storage is cleared?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST implement authentication exclusively via OAuth 2.0 / OpenID Connect; no username/password authentication is permitted.
- **FR-002**: System MUST support Google as the OAuth provider with full OAuth 2.0 authorization code flow.
- **FR-003**: System MUST automatically create a backend user record on first successful SSO authentication with the user's OAuth profile data.
- **FR-004**: System MUST persist user profile data including: provider (e.g., "Google"), provider_user_id (OAuth subject ID), email (if provided), display_name (if provided), and avatar_url (optional).
- **FR-005**: System MUST maintain user sessions such that authenticated users remain logged in across browser page refreshes without re-authenticating.
- **FR-006**: System MUST provide a logout mechanism that terminates the user's session and returns them to an unauthenticated state.
- **FR-007**: System MUST prevent unauthenticated users from accessing protected application endpoints; such requests MUST be rejected or redirected to login.
- **FR-008**: System MUST identify returning users by the combination of (provider, provider_user_id) and reuse their existing backend user record rather than creating duplicates.
- **FR-009**: System MUST provide a profile endpoint that returns the authenticated user's persisted profile data.
- **FR-010**: System MUST handle OAuth token refresh transparently so the user does not experience unexpired session loss.
- **FR-011**: System MUST enforce absolute session expiry of 12 hours; sessions MUST NOT be extended by user activity.
- **FR-012**: System MUST log all authentication events (login, logout, token refresh, failures) with user ID, timestamp, outcome, and error reason for audit and troubleshooting.

### Key Entities

- **User**: Represents an authenticated user account. Attributes: id (primary key), provider (string, e.g., "Google"), provider_user_id (string, unique per provider), email (optional string), display_name (optional string), avatar_url (optional string), created_at (timestamp), updated_at (timestamp). The combination of (provider, provider_user_id) is unique and serves as the lookup key for returning users.
- **Session/Token**: Represents an active user session. Managed either via backend session storage (with secure HTTP-only cookies) or JWT tokens. Must be validated on each authenticated request and must expire after a configurable period of inactivity or absolute time.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new user can complete the first-time SSO login flow end-to-end in under 30 seconds (excluding Google's authentication UI).
- **SC-002**: A returning user can complete the login flow in under 10 seconds (including potential single-click Google sign-in).
- **SC-003**: Session persistence is maintained across browser page refreshes; user remains authenticated with zero re-authentication prompts.
- **SC-004**: Logout completes in under 1 second and immediately prevents access to protected resources.
- **SC-005**: The system creates and stores all six user profile fields (provider, provider_user_id, email, display_name, avatar_url, and system-generated id) on first successful authentication.
- **SC-006**: No duplicate user records are created when the same user (provider + provider_user_id) authenticates multiple times.
- **SC-007**: 100% of OAuth authentication errors (network failure, consent denial, callback errors) result in user-friendly error messages and do not break the application state.
- **SC-008**: Profile endpoint returns user data within 200ms in normal conditions.
- **SC-009**: All authentication events (login, logout, token refresh, failures) are logged with user ID, timestamp, outcome, and error reason for audit and debugging purposes.
- **SC-010**: User sessions expire after a maximum absolute time of 12 hours; the session is not extended by user activity.

## Clarifications

### Session 2026-08-18

- Q: How long should a user's session remain valid if inactive, and should active sessions be extended on each request? → A: Sessions expire after a fixed absolute time (12 hours max) regardless of activity.
- Q: What authentication events should the system log or emit metrics for? → A: Log all authentication events (login, logout, token refresh, failures) with user ID, timestamp, outcome, and error reason.
- Q: What is the expected number of concurrent users? → A: Pet project; no specific scalability target required. Design for single-server deployment.

## Assumptions

- **Assumption A**: Google OAuth credentials (Client ID, Client Secret) will be securely configured in the backend environment and are not in scope for this feature. Configuration management is handled separately.
- **Assumption B**: The frontend is a single-page application (Angular per the constitution) capable of handling OAuth redirect URIs and session token management (cookies or local storage).
- **Assumption C**: Session tokens will be stored in HTTP-only secure cookies or managed by a secure backend session store; frontend token storage in localStorage is not considered secure for sensitive authentication data.
- **Assumption D**: Network connectivity during OAuth flow is available; offline authentication is not in scope.
- **Assumption E**: Google's data model (email, display_name, avatar_url) is assumed stable; if these fields are not provided by Google, they are treated as optional/null in the user record.
- **Assumption F**: The backend is running on HTTPS in production to support secure OAuth cookies and token transmission.
- **Assumption G**: Authentication state is assumed to be per-device/browser; single sign-on across multiple devices is not required (though not prohibited).
