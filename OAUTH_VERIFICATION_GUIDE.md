# OAuth2 Implementation Verification Guide

This guide walks through testing all OAuth functionality end-to-end. All features from the spec should now work.

---

## Prerequisites

1. **Backend Running**
   ```bash
   cd backend
   mvn spring-boot:run
   # Backend should start on http://localhost:8080
   ```

2. **Frontend Running** (in separate terminal)
   ```bash
   cd frontend
   ng serve
   # Frontend should start on http://localhost:4200
   ```

3. **Database Ready**
   - Flyway migrations should run automatically on backend startup
   - PostgreSQL should have `users` table created

---

## Test Scenario 1: First-Time SSO Login (US1) ✅ P1

**Goal**: New user logs in with Google, backend creates user record, user is logged in

### Steps

1. Open http://localhost:4200 in fresh incognito window
2. You should see the login page
3. Click "Login with Google"
4. You'll be redirected to Google's OAuth consent screen
5. Log in with a Google account (create one if needed)
6. Grant permission
7. **Expected**: Redirected back to app, see "Loading..." briefly, then home page
8. **Verify**: User is logged in (profile visible if home component shows it)

### Database Verification

```sql
SELECT * FROM users WHERE provider='Google' ORDER BY created_at DESC LIMIT 1;
```

**Expected**: New row with:
- provider: "Google"
- provider_user_id: (OAuth sub claim)
- email: (your Google email)
- display_name: (your name from Google)
- avatar_url: (Google avatar if available)

### Backend Logs

Should see:
```
New user created: provider=Google, providerUserId=..., email=..., timestamp=...
```

---

## Test Scenario 2: Returning User Login (US2) ✅ P1

**Goal**: Same user logs out and logs back in. Existing user is reused (no duplicate).

### Steps

1. Stay on app (after Scenario 1)
2. Click Logout button
3. **Expected**: Session terminated, redirected to login page
4. Click "Login with Google"
5. Complete OAuth again with **same Google account**
6. **Expected**: Redirected back to app within <10 seconds
7. **Verify**: User is logged in (same as before)

### Database Verification

```sql
SELECT COUNT(*) FROM users WHERE provider='Google';
```

**Expected**: Count should still be 1 (no duplicate created)

```sql
SELECT * FROM users WHERE provider='Google';
```

**Expected**: Same user ID, updated_at timestamp should be more recent

### Backend Logs

Should see:
```
Existing user reused: provider=Google, providerUserId=..., userId=..., timestamp=...
```

---

## Test Scenario 3: Session Persistence (US4) ✅ P2

**Goal**: User remains logged in after page refresh without re-authenticating.

### Steps

1. On home page (after login in Scenario 1 or 2)
2. Press F5 or Cmd+R to refresh page
3. **Expected**: Page refreshes, shows "Loading..." briefly, then home page (not login page)
4. **Verify**: User is still logged in, no re-authentication required

### Browser DevTools Verification

1. Open DevTools (F12)
2. Go to Application → Cookies
3. Look for `SESSION` cookie
4. **Expected**: Cookie exists with flag `HttpOnly`, `Secure` (in HTTPS), `SameSite=Lax`
5. Refresh page and verify cookie persists

### Backend Logs

Should see:
```
Session restored on page refresh
```

---

## Test Scenario 4: Logout (US3) ✅ P1

**Goal**: Logout terminates session and blocks access to protected endpoints.

### Steps

1. On home page (logged in)
2. Click Logout button
3. **Expected**: Immediately redirected to login page
4. Open DevTools → Application → Cookies
5. **Expected**: SESSION cookie is gone or expired

### API Test: Protected Endpoint After Logout

1. Still on login page (after logout)
2. Open DevTools → Console
3. Run:
   ```javascript
   fetch('http://localhost:8080/api/auth/user/profile', {
     method: 'GET',
     credentials: 'include'
   })
   .then(r => r.json())
   .then(console.log)
   ```
4. **Expected**: Response should be 401 Unauthorized

### Backend Logs

Should see:
```
User logout: userId=..., timestamp=...
```

---

## Test Scenario 5: OAuth Error Handling (US1 Edge Case)

**Goal**: OAuth errors display user-friendly message.

### Simulate OAuth Denial

1. Go to http://localhost:4200 on fresh incognito window
2. Click "Login with Google"
3. On Google consent screen, click "Cancel" or deny permission
4. Google redirects back to your app with error parameter
5. **Expected**: Back on login page with error message visible

### Manual Error Test

1. Open http://localhost:4200/login?error=access_denied&error_description=User+denied+access
2. **Expected**: Error message displayed on login page

---

## Test Scenario 6: Profile Data Retrieval (US5) ✅ P2

**Goal**: Logged-in user can retrieve profile data from backend.

### API Test

1. Log in (complete Scenario 1)
2. Open DevTools → Console
3. Run:
   ```javascript
   fetch('http://localhost:8080/api/auth/user/profile', {
     method: 'GET',
     credentials: 'include'
   })
   .then(r => r.json())
   .then(d => console.log(JSON.stringify(d, null, 2)))
   ```
4. **Expected**: JSON response with all fields:
   ```json
   {
     "id": 1,
     "provider": "Google",
     "email": "your-email@gmail.com",
     "displayName": "Your Name",
     "avatarUrl": "https://..."
   }
   ```

### Performance Check

1. Measure response time (DevTools → Network tab)
2. **Expected**: <200ms per spec (SC-008)

---

## Test Scenario 7: Protected Routes (US1 Requirement)

**Goal**: Unauthenticated users cannot access protected routes.

### Test Protection

1. On login page (unauthenticated)
2. Try to navigate to http://localhost:4200/home
3. **Expected**: Immediately redirected to /login

### Test After Logout

1. Log in (complete Scenario 1)
2. Click Logout
3. Try to access http://localhost:4200/home
4. **Expected**: Redirected to /login

---

## Test Scenario 8: Session Timeout

**Goal**: Session expires after 12 hours (absolute, no extension).

### Setup (Dev/Test Only)

1. For testing, you can shorten timeout in `application.yml`:
   ```yaml
   spring:
     session:
       timeout: 1m  # Change from 12h to 1 minute for testing
   ```
2. Restart backend
3. Log in
4. Wait 1+ minutes
5. Try to access protected endpoint
6. **Expected**: 401 Unauthorized (session expired)

---

## Test Scenario 9: Concurrent Session Limit

**Goal**: Only 1 session per user is allowed (if already logged in elsewhere).

### Steps

1. Log in on Browser A (http://localhost:4200)
2. Open same app in Browser B
3. Log in on Browser B with same account
4. Go back to Browser A and refresh
5. **Expected**: Session invalid on Browser A (or redirected to login)

---

## Test Scenario 10: CORS & Credentials

**Goal**: HTTP-only cookies are properly sent with requests.

### Test with DevTools

1. Open DevTools → Network tab
2. Do any action that calls API (e.g., get profile)
3. Click the request in Network tab
4. Go to "Headers" section
5. Look for `Cookie` header
6. **Expected**: SESSION cookie is included automatically

### Verify Credentials Flag

1. In DevTools Console:
   ```javascript
   const url = 'http://localhost:8080/api/auth/user/profile';
   const req = new Request(url, { 
     method: 'GET', 
     credentials: 'include'  // <-- This is automatic in interceptor
   });
   console.log(req.credentials);  // Should output: "include"
   ```

---

## Full End-to-End Test Flow

Run this complete sequence to verify everything:

```
1. Start Backend (mvn spring-boot:run)
2. Start Frontend (ng serve)
3. Scenario 1: First-Time Login
   - Open http://localhost:4200
   - Click Login with Google
   - Complete OAuth
   - Verify logged in
4. Scenario 3: Session Persistence
   - Refresh page (F5)
   - Verify still logged in
5. Scenario 5: Profile Retrieval
   - Get /api/auth/user/profile
   - Verify all fields present
6. Scenario 4: Logout
   - Click Logout
   - Verify redirected to login
7. Scenario 2: Returning User
   - Click Login with Google
   - Complete OAuth with same account
   - Verify reused (check DB, only 1 user)
   - Verify login time <10 seconds
8. Scenario 7: Protected Routes
   - Logout
   - Try to access /home
   - Verify redirected to /login
```

**Result**: All scenarios pass ✅ → OAuth implementation complete and ready for production

---

## Troubleshooting

### Issue: Stuck on "Loading..." page

**Cause**: Session check failed
- Check backend logs for errors
- Verify Google OAuth credentials are valid
- Check browser console for errors

**Fix**:
1. Open DevTools → Console
2. Check for error messages
3. Verify backend is running: `curl http://localhost:8080/actuator/health`

### Issue: "Cannot find module" errors in frontend

**Cause**: Dependencies not installed
- Run `npm install` in frontend directory
- Run `ng serve` again

### Issue: 401 Unauthorized when calling API

**Cause**: Session cookie not being sent
- Check DevTools → Network → request headers
- Verify `Cookie` header includes `SESSION=...`
- Check if CORS is allowing credentials (should be in browser console)

**Fix**:
1. Verify interceptor is registered: check app.config.ts
2. Verify backend CORS allows credentials: check SecurityConfig.java
3. Clear browser cookies and try again

### Issue: "Access Denied" from Google

**Cause**: Invalid OAuth credentials or redirect URI mismatch
- Verify Google OAuth Client ID and Secret in application.yml
- Verify redirect URI matches: `http://localhost:8080/login/oauth2/code/google`
- Check Google Cloud Console for correct credentials

### Issue: Database migration fails

**Cause**: V2__create_users_table.sql error
- Check PostgreSQL is running
- Verify database credentials in application.yml
- Check Flyway logs in backend console

**Fix**:
```bash
# Reset database (dev only!)
psql -U inventory_user -d sdd_inventory -c "DROP TABLE IF EXISTS users, flyway_schema_history CASCADE;"
# Restart backend to re-run migrations
```

---

## Performance Checklist

- [ ] First-time login: <30 seconds (excluding Google's UI)
- [ ] Return login: <10 seconds
- [ ] Profile endpoint: <200ms
- [ ] Logout: <1 second
- [ ] Session persistence: <1 second

---

## Security Checklist

- [ ] HTTPS in production (before deploying)
- [ ] Google OAuth credentials not in git (move to env vars)
- [ ] Session cookie has HttpOnly flag
- [ ] CSRF protection enabled (for non-OAuth endpoints)
- [ ] Audit logs are being written
- [ ] 12-hour session timeout enforced

---

## Next Steps After Verification

1. ✅ All scenarios pass → Ready for production
2. Create a home/dashboard component (currently users see login template)
3. Add more user information display to home page
4. Configure production OAuth credentials
5. Set up HTTPS for production
6. Deploy backend and frontend

---

**Last Updated**: 2026-08-19
**OAuth Status**: Implementation Complete, Ready for Testing
