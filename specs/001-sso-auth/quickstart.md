# Quickstart: SSO-Only Authentication Validation

**Feature**: SSO-Only Authentication | **Date**: 2026-08-18

This guide provides runnable validation scenarios that prove the feature works end-to-end. Each scenario is independently testable and verifies specific user stories and requirements.

---

## Prerequisites

Before running validation scenarios, ensure the following is in place:

### 1. Google OAuth Application Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Create OAuth 2.0 credentials (OAuth 2.0 Client ID)
5. Set authorized JavaScript origins: `http://localhost:8080`, `http://localhost:4200`
6. Set authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
7. Copy Client ID and Client Secret

### 2. Backend Configuration

Backend implementation classes:
- `com.example.inventory.controller.AuthController` — OAuth and session endpoints
- `com.example.inventory.service.AuthService` — Authentication business logic
- `com.example.inventory.service.CustomOAuth2UserService` — OAuth2 user provisioning
- `com.example.inventory.repository.UserRepository` — User data access
- `com.example.inventory.entity.User` — User domain model
- `com.example.inventory.config.SecurityConfig` — Spring Security setup
- `com.example.inventory.config.ExceptionHandler` — Central error handling

Configure environment variables or `application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: <YOUR_CLIENT_ID>
            client-secret: <YOUR_CLIENT_SECRET>
            redirect-uri: http://localhost:8080/login/oauth2/code/google
            scope:
              - openid
              - email
              - profile
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://www.googleapis.com/oauth2/v4/token
            user-info-uri: https://www.googleapis.com/oauth2/v1/userinfo
            user-name-attribute: sub
  session:
    timeout: 12h
    store-type: none  # Use in-memory store for single-server deployment
```

### 3. Database Setup

Ensure PostgreSQL is running and Flyway migrations are applied:

```bash
# Backend automatically runs migrations on startup
./mvnw spring-boot:run
# Verify V2__create_users_table.sql was applied:
# psql -U inventory_user -d inventory_db -c "SELECT * FROM users;"
# Should be empty (no users yet)
```

### 4. Start Services

**Backend** (in one terminal):
```bash
cd backend
./mvnw spring-boot:run
# Expected output: "Started Application in X.XXX seconds"
# Verify: curl http://localhost:8080/api/auth/user/profile
#   → Returns 401 Unauthorized (expected, not logged in)
```

**Frontend** (in another terminal):
```bash
cd frontend
npm install
ng serve
# Expected output: "Application bundle generation complete"
# Navigate to http://localhost:4200 in browser
```

---

## Validation Scenarios

### Scenario 1: First-Time User SSO Login (User Story 1, P1)

**Objective**: Verify that a new user can complete OAuth2 login and have a user record automatically created.

**Steps**:

1. **Start both backend and frontend** (see Prerequisites)

2. **Open browser to frontend**:
   ```
   Navigate to http://localhost:4200
   ```
   - Verify: Login page is displayed with "Login with Google" button

3. **Click "Login with Google"**:
   - Verify: Redirected to Google's OAuth consent screen

4. **Complete Google OAuth**:
   - Login with a Google account (or grant consent if already logged in)
   - Grant requested permissions
   - Verify: Redirected back to application homepage

5. **Verify user is logged in**:
   - Verify: Homepage displays user's profile (name, email, avatar)
   - Verify: "Logout" button visible (not "Login with Google")

6. **Verify user record was created in database**:
   ```bash
   psql -U inventory_user -d inventory_db
   SELECT id, provider, provider_user_id, email, display_name FROM users;
   ```
   - Expected output: One row with provider="Google", email and display_name populated

7. **Verify profile endpoint returns data**:
   ```bash
   curl -b "SESSION=<session_cookie>" http://localhost:8080/api/auth/user/profile
   ```
   - Expected response:
   ```json
   {
     "id": 1,
     "provider": "Google",
     "email": "your-email@gmail.com",
     "displayName": "Your Name",
     "avatarUrl": "https://lh3.googleusercontent.com/..."
   }
   ```

**Acceptance Criteria Met**:
- ✅ User redirected to Google OAuth consent screen (FR-002, SC-001)
- ✅ User record created in backend with provider, email, display_name (FR-003, FR-004)
- ✅ User remains logged in after OAuth redirect (FR-005, SC-001)
- ✅ Profile endpoint returns OAuth profile data (FR-009)
- ✅ Session persistence: User stays logged in (SC-003)

---

### Scenario 2: Returning User Login (User Story 2, P1)

**Objective**: Verify that an existing user is recognized and reuses their user record (no duplicates).

**Prerequisite**: Complete Scenario 1 first

**Steps**:

1. **Logout** (see Scenario 4):
   - Click "Logout" button
   - Verify: Redirected to login page

2. **Login again with the same Google account**:
   - Click "Login with Google"
   - Complete OAuth flow (faster if already logged into Google)

3. **Verify user is logged in** with same profile:
   - Verify: Homepage shows same user profile (same name, email)

4. **Verify no duplicate user record created**:
   ```bash
   psql -U inventory_user -d inventory_db
   SELECT COUNT(*) FROM users WHERE provider='Google';
   ```
   - Expected output: `1` (not 2 or more)

5. **Verify existing user was reused**:
   ```bash
   SELECT id, provider_user_id FROM users WHERE provider='Google';
   ```
   - Expected output: Same id and provider_user_id as after Scenario 1

**Acceptance Criteria Met**:
- ✅ Returning user login completes in <10 seconds (SC-002)
- ✅ No duplicate user record created (FR-008, SC-006)
- ✅ Existing user record reused (FR-008, SC-006)
- ✅ Session established without re-authentication (FR-005)

---

### Scenario 3: Session Persistence Across Page Refresh (User Story 4, P2)

**Objective**: Verify that session persists across browser page refreshes without re-authentication.

**Prerequisite**: Logged-in user from Scenario 1 or 2

**Steps**:

1. **Logged in, on homepage**:
   - Verify: User profile visible, "Logout" button shown

2. **Refresh browser page**:
   ```
   Press Ctrl+R (Windows/Linux) or Cmd+R (Mac)
   ```
   - Verify: Homepage reloads without login redirect
   - Verify: User remains logged in, profile still visible

3. **Verify session cookie is valid**:
   ```bash
   # Browser DevTools → Application → Cookies → localhost:8080
   # Verify: SESSION cookie is present and not expired
   ```

4. **Verify API call succeeds without re-authentication**:
   ```bash
   curl -b "SESSION=<session_cookie>" http://localhost:8080/api/auth/user/profile
   ```
   - Expected: 200 OK with user profile

5. **Navigate away and back**:
   - Click link to different page (if exists)
   - Use browser back/forward to return
   - Verify: Still logged in, no login redirect

**Acceptance Criteria Met**:
- ✅ Session persists across page refresh (FR-005, SC-003)
- ✅ Zero re-authentication prompts (SC-003)
- ✅ Session remains valid across navigation (FR-005)

---

### Scenario 4: Logout (User Story 3, P1)

**Objective**: Verify that logout terminates session and requires re-authentication.

**Prerequisite**: Logged-in user from Scenario 1 or 2

**Steps**:

1. **Logged in, on homepage**:
   - Verify: User profile visible, "Logout" button shown

2. **Click "Logout" button**:
   - Frontend calls POST /api/auth/logout
   - Verify: Redirected to login page

3. **Verify user sees login page** (not homepage):
   - Verify: "Login with Google" button visible

4. **Verify session terminated in backend**:
   ```bash
   curl -b "SESSION=<session_cookie>" http://localhost:8080/api/auth/user/profile
   ```
   - Expected: 401 Unauthorized (session cookie is now invalid)

5. **Attempt direct endpoint access**:
   ```bash
   # Without session cookie
   curl http://localhost:8080/api/auth/user/profile
   ```
   - Expected: 401 Unauthorized

6. **Verify logout completed in <1 second**:
   - Measure time from logout click to login page display
   - Expected: <1000ms (SC-004)

**Acceptance Criteria Met**:
- ✅ Session terminated (FR-006)
- ✅ User redirected to login (FR-006)
- ✅ Protected endpoint access fails (FR-007)
- ✅ Logout completes in <1 second (SC-004)

---

### Scenario 5: Session Expiry After 12 Hours (User Story 4, P2)

**Objective**: Verify that sessions expire after 12 hours absolute timeout.

**Steps** (simulated; don't wait 12 hours):

1. **Logged in, on homepage**:
   - Verify: User profile visible

2. **Simulate session expiry**:
   - Browser DevTools → Application → Cookies
   - Find SESSION cookie
   - Edit `Expires/Max-Age` to a past time (e.g., current time - 1 hour)
   - Refresh page

3. **Verify user is logged out** after expiry:
   - Verify: Redirected to login page
   - Verify: "Login with Google" button shown

4. **Verify profile endpoint rejects expired session**:
   ```bash
   curl -b "SESSION=<expired_session_cookie>" http://localhost:8080/api/auth/user/profile
   ```
   - Expected: 401 Unauthorized

5. **Verify time-to-expiry is 12 hours**:
   ```bash
   # After logging in, check SESSION cookie Max-Age:
   # Browser DevTools → Application → Cookies → SESSION
   # Expected: Max-Age ≈ 43200 (12 hours in seconds)
   ```

6. **Verify session does NOT extend on activity**:
   - Log in and note session cookie Max-Age
   - Wait 5 minutes (or simulate wait)
   - Make an API request with the session
   - Check Max-Age again
   - Expected: Max-Age slightly less than original (time passed, but not reset to 12h)

**Acceptance Criteria Met**:
- ✅ Session expires after 12 hours (FR-011, SC-010)
- ✅ Activity does NOT extend session (FR-011)
- ✅ Expired session prevents access (FR-007)

---

### Scenario 6: OAuth Failure Handling (Edge Case)

**Objective**: Verify that authentication errors result in user-friendly messages and don't break the app.

**Steps** (requires manual simulation):

1. **Simulate network failure**:
   - Start login flow (`GET /api/auth/login`)
   - Disconnect network (or use browser DevTools to block Google API calls)
   - Attempt to complete OAuth

2. **Verify user-friendly error message**:
   - Verify: Error page displayed (not blank page or stack trace)
   - Verify: Message like "Authentication failed. Please try again." (not technical error)

3. **Verify application is not broken**:
   - Verify: "Login with Google" button still visible
   - Verify: Can retry login after error

4. **Simulate user denies consent**:
   - Start login flow
   - On Google consent screen, click "Cancel"

5. **Verify user is returned to login** without error:
   - Verify: Redirected back to login page
   - Verify: "Login with Google" button visible (can retry)

**Acceptance Criteria Met**:
- ✅ OAuth errors result in user-friendly messages (FR-007, SC-007)
- ✅ Application state not broken after error (SC-007)
- ✅ Users can retry login after failure

---

### Scenario 7: Logging & Audit Trail (FR-012)

**Objective**: Verify that all authentication events are logged for audit and debugging.

**Steps**:

1. **Configure logging to see auth events**:
   ```yaml
   logging:
     level:
       auth: DEBUG  # Enable debug logs for auth package
   ```

2. **Complete login flow**:
   - Login with Google

3. **Check application logs**:
   ```bash
   # Tail backend logs
   tail -f backend/logs/application.log | grep "auth"
   ```
   - Expected: Entries like:
     ```
     [auth] Login attempt: provider=Google, provider_user_id=12345
     [auth] User created/retrieved: id=1, provider_user_id=12345
     [auth] Session established: sessionId=ABC123, userId=1
     ```

4. **Complete logout flow**:
   - Click Logout

5. **Check logs for logout event**:
   - Expected:
     ```
     [auth] Logout: userId=1, sessionId=ABC123
     ```

6. **Attempt failed login** (simulate invalid token):
   - Make API request with invalid/expired session
   - Check logs for failure event:
     ```
     [auth] Authentication failed: error=InvalidSession, timestamp=...
     ```

**Acceptance Criteria Met**:
- ✅ Login events logged (FR-012)
- ✅ User ID, timestamp, outcome recorded (FR-012)
- ✅ Logout events logged (FR-012)
- ✅ Failure events logged with reason (FR-012)

---

## Performance Validation

### First-Time Login Flow

**Measure**: Time from "Login with Google" click to homepage displayed

1. Open browser DevTools → Performance tab
2. Click "Login with Google"
3. Complete Google OAuth
4. Mark when homepage finishes loading
5. Expected: <30 seconds (SC-001)

**Breakdown**:
- User to Google consent: ~1-2 seconds (redirects)
- Google authentication: ~5-10 seconds (user enters credentials, if needed)
- Google redirect back: ~1-2 seconds
- Backend OAuth processing (token exchange, user creation): ~1-2 seconds
- Frontend page render: ~2-3 seconds
- **Total**: ~12-20 seconds (well under 30s target)

### Return User Login

**Measure**: Time from "Login with Google" click (already logged into Google) to homepage displayed

1. Logout completely
2. Open browser DevTools → Network tab
3. Click "Login with Google"
4. Complete OAuth (should be 1-click if logged into Google)
5. Expected: <10 seconds (SC-002)

**Breakdown**:
- If already logged into Google: ~1-2 seconds redirect
- Backend processing: ~1-2 seconds (no user creation needed)
- Frontend render: ~2-3 seconds
- **Total**: ~5-7 seconds (under 10s target)

### Profile Endpoint Response Time

**Measure**: Time from request to response

```bash
time curl -b "SESSION=<session_cookie>" http://localhost:8080/api/auth/user/profile
```

Expected: <200ms (SC-008)

---

## Test Result Checklist

Use this checklist to track validation results:

- [ ] **Scenario 1**: First-time login flow works, user created, profile returned
- [ ] **Scenario 2**: Return login reuses user, no duplicate, no re-auth
- [ ] **Scenario 3**: Page refresh maintains login, session valid
- [ ] **Scenario 4**: Logout terminates session, requires re-login
- [ ] **Scenario 5**: Session expires after 12 hours, activity doesn't extend
- [ ] **Scenario 6**: OAuth failures handled gracefully, app not broken
- [ ] **Scenario 7**: All auth events logged with timestamps and outcomes
- [ ] **Performance**: First login <30s, return login <10s, profile <200ms
- [ ] **No duplicates**: Database contains only one User record per provider+user
- [ ] **Unique constraint**: (provider, provider_user_id) enforced

---

## Troubleshooting

### Login redirects to login page instead of homepage

**Cause**: OAuth callback failed or session not established

**Debug**:
```bash
# Check backend logs for errors
tail backend/logs/application.log | grep -i "oauth\|auth\|error"

# Verify Google credentials configured
echo $GOOGLE_CLIENT_ID
echo $GOOGLE_CLIENT_SECRET

# Verify database user was created
psql -U inventory_user -d inventory_db -c "SELECT * FROM users;"
```

### Session cookie not persisted across requests

**Cause**: CORS credentials not configured or cookie flags wrong

**Debug**:
- Browser DevTools → Application → Cookies
  - Verify SESSION cookie exists
  - Verify Secure flag (if HTTPS)
  - Verify HttpOnly flag
  - Verify SameSite flag

- Backend CORS config check:
```yaml
spring:
  web:
    cors:
      allowed-origins: http://localhost:4200
      allow-credentials: true
```

### Profile endpoint returns 401 even though logged in

**Cause**: Session validation failing

**Debug**:
```bash
# Check session is valid in Spring Session store
# (If using database store, query sessions table)

# Verify session cookie has not expired
# Browser DevTools → Application → Cookies → SESSION
# Check Expires/Max-Age is in future
```

### User not found after successful OAuth

**Cause**: OAuth2UserService custom logic not executing or database error

**Debug**:
- Add breakpoint in CustomOAuth2UserService.loadUser()
- Verify UserRepository.findByProviderAndProviderUserId() query
- Check database for any User records:
```bash
psql -U inventory_user -d inventory_db -c "SELECT COUNT(*) FROM users;"
```

---

## Next Steps

Once all scenarios pass:

1. **Commit changes**: Branch `001-sso-auth` with implementation
2. **Run full test suite**: `./mvnw test` (backend) + `ng test` (frontend)
3. **Merge to main**: Create PR, await review
4. **Deploy to production**: Configure Google OAuth with production URLs, deploy to server

For any issues or feature requests, see project's issue tracker.
