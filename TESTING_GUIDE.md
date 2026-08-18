# SSO Authentication Testing Guide

Complete guide for testing the SSO-Only Authentication feature against quickstart.md scenarios and database validation.

## Quick Start

### 1. Start Docker Compose (PostgreSQL)
```bash
cd /Users/salieksieienko/IdeaProjects/sdd-inventory
./commands/start-docker.sh
```

**Verify**:
```bash
docker ps | grep postgres
# Should show: sdd-inventory-postgres-1  postgres:17  ... Up
```

### 2. Start Backend (Terminal 1)
```bash
cd /Users/salieksieienko/IdeaProjects/sdd-inventory/backend
./mvnw spring-boot:run
```

**Verify**:
```bash
# Terminal 2 - Check backend health
curl http://localhost:8080/api/auth/login
# Expected: 200 OK (or 302 redirect to Google OAuth)

# Or check logs for:
curl http://localhost:8080/api/auth/user/profile
# Expected: 401 Unauthorized (no session yet - this is correct!)
```

### 3. Start Frontend (Terminal 3)
```bash
cd /Users/salieksieienko/IdeaProjects/sdd-inventory/frontend
npm install
ng serve
```

**Verify**:
- Navigate to http://localhost:4200
- Login page should display with "Login with Google" button

---

## Database Validation Commands

### Check Users Table Schema
```bash
psql -U inventory_user -d sdd_inventory -c "\d users;"
```

**Expected output**:
```
Table "public.users"
Column             | Type                   | Collation | Nullable
-------------------+------------------------+-----------+----------
id                 | integer                |           | not null
provider           | character varying(64)  |           | not null
provider_user_id   | character varying(255) |           | not null
email              | character varying(255) |           | 
display_name       | character varying(255) |           | 
avatar_url         | character varying(2048)|           | 
created_at         | timestamp              |           | not null
updated_at         | timestamp              |           | not null
```

### Check Unique Constraint
```bash
psql -U inventory_user -d sdd_inventory -c "\d users" | grep -i "unique\|constraint"
```

**Expected output**:
```
UNIQUE CONSTRAINT "users_provider_provider_user_id_key" UNIQUE (provider, provider_user_id)
```

### List All Users
```bash
psql -U inventory_user -d sdd_inventory -c "SELECT id, provider, provider_user_id, email, display_name, created_at FROM users;"
```

**Before any login**: Empty table (0 rows)

---

## Testing Scenarios

### Scenario 1: First-Time User SSO Login

**Steps**:

1. **Verify login page loads**
   ```bash
   curl http://localhost:4200
   # Should load without errors
   ```

2. **In browser**: Navigate to http://localhost:4200
   - See "Login with Google" button
   - Click it

3. **Complete OAuth at Google**
   - Login to Google (if not already)
   - Grant permissions

4. **Verify redirected to homepage**
   - Should see user profile (name, email, avatar)
   - "Logout" button visible

5. **Database verification**:
   ```bash
   psql -U inventory_user -d sdd_inventory -c "SELECT COUNT(*) as user_count FROM users;"
   # Expected: 1
   
   psql -U inventory_user -d sdd_inventory -c "SELECT id, provider, email, display_name FROM users;"
   # Expected output example:
   # id | provider | email              | display_name
   # 1  | Google   | user@gmail.com     | User Name
   ```

6. **Profile API test**:
   ```bash
   # Extract session cookie from browser DevTools or curl login
   curl -b "SESSION=<your_session_id>" http://localhost:8080/api/auth/user/profile
   
   # Expected response (200 OK):
   # {
   #   "id": 1,
   #   "provider": "Google",
   #   "email": "user@gmail.com",
   #   "displayName": "User Name",
   #   "avatarUrl": "https://lh3.googleusercontent.com/..."
   # }
   ```

**✅ Acceptance**: User created in database, profile returned

---

### Scenario 2: Returning User Login (Deduplication)

**Prerequisites**: Complete Scenario 1

**Steps**:

1. **Logout** (see Scenario 4)
   - Click "Logout" button
   - Verify: Redirected to login page

2. **Login again with same Google account**
   - Click "Login with Google"
   - (Should be faster if still logged into Google)

3. **Check database count**:
   ```bash
   psql -U inventory_user -d sdd_inventory -c "SELECT COUNT(*) as user_count FROM users WHERE provider='Google';"
   # Expected: 1 (NOT 2!)
   ```

4. **Verify same user ID**:
   ```bash
   psql -U inventory_user -d sdd_inventory -c "SELECT id, provider_user_id FROM users WHERE provider='Google';"
   # Should be the SAME id as Scenario 1
   ```

5. **Verify performance (<10 seconds)**
   - Measure time from "Login with Google" to homepage loaded
   - Expected: < 10 seconds

**✅ Acceptance**: No duplicate created, same user reused, <10s

---

### Scenario 3: Session Persistence Across Refresh

**Prerequisites**: Logged-in user (Scenario 1 or 2)

**Steps**:

1. **Verify logged in**
   - Homepage shows profile
   - "Logout" button visible

2. **Refresh page** (Ctrl+R or Cmd+R)
   - Should NOT redirect to login
   - Profile should still be visible
   - No re-authentication required

3. **Check session cookie**
   - Browser DevTools → Application → Cookies → localhost:8080
   - SESSION cookie should exist
   - Check it's not expired

4. **API call after refresh**:
   ```bash
   curl -b "SESSION=<session_id>" http://localhost:8080/api/auth/user/profile
   # Expected: 200 OK (not 401)
   ```

**✅ Acceptance**: Session persists, no re-auth needed

---

### Scenario 4: Logout

**Prerequisites**: Logged-in user

**Steps**:

1. **Click "Logout" button**
   - Should complete in <1 second
   - Redirected to login page

2. **Verify session terminated**:
   ```bash
   curl -b "SESSION=<old_session_id>" http://localhost:8080/api/auth/user/profile
   # Expected: 401 Unauthorized
   ```

3. **Verify login required**
   - Refresh page
   - Should see "Login with Google" button
   - Cannot access profile without re-login

**✅ Acceptance**: Session terminated, protected endpoints rejected

---

### Scenario 5: Session Expiry After 12 Hours

**Steps** (simulated - don't wait 12 hours):

1. **Login and note time**
   - Complete login flow
   - Note current time

2. **Check session cookie Max-Age**:
   - Browser DevTools → Application → Cookies → SESSION
   - Max-Age should be 43200 (12 hours in seconds)
   - Or Expires should be ~12 hours from now

3. **Simulate expiry**:
   - Manually edit SESSION cookie Max-Age to 1 (expires in 1 second)
   - Wait 2 seconds and refresh

4. **Verify logged out**:
   - Should redirect to login page
   - Profile endpoint should return 401

**✅ Acceptance**: 12-hour timeout enforced, activity doesn't extend

---

### Scenario 6: OAuth Failure Handling

**Steps**:

1. **Test with network disabled**:
   - Click "Login with Google"
   - Disconnect network (DevTools → Network → Offline)
   - Expected: User-friendly error message (not stack trace)

2. **Test denial at Google**:
   - Click "Login with Google"
   - On Google consent screen, click "Cancel"
   - Expected: Redirected back to login page (not error)

3. **Verify app not broken**:
   - "Login with Google" button still clickable
   - Can retry login

**✅ Acceptance**: Errors handled gracefully, can retry

---

### Scenario 7: Logging & Audit Trail

**Steps**:

1. **Enable debug logging** (optional):
   ```yaml
   # In backend/src/main/resources/application.yml
   logging:
     level:
       auth: DEBUG
   ```

2. **Monitor logs during login**:
   ```bash
   # Terminal 4 - Watch backend logs
   tail -f /tmp/spring-boot*.log | grep -i "auth\|user created\|login"
   ```

3. **Complete login flow**
   - Expected log entries:
     ```
     New user created: provider=Google, providerUserId=..., email=...
     User login: provider=Google, providerUserId=..., timestamp=...
     Profile retrieved: userId=1, timestamp=...
     ```

4. **Complete logout**
   - Expected:
     ```
     User logout: userId=1, timestamp=...
     ```

**✅ Acceptance**: Events logged with timestamps

---

## Compilation & Build Verification

### Backend Compilation
```bash
cd /Users/salieksieienko/IdeaProjects/sdd-inventory/backend
./mvnw clean compile
# Expected: BUILD SUCCESS
```

### Frontend Build
```bash
cd /Users/salieksieienko/IdeaProjects/sdd-inventory/frontend
npm run build
# Expected: Build output with no errors
```

---

## Performance Validation

### First-Time Login (<30s)
```bash
# Time from "Login with Google" to homepage loaded
# Use browser DevTools → Performance tab
# Expected: 12-20 seconds (well under 30s)
```

### Return Login (<10s)
```bash
# Time from second "Login with Google" to homepage
# (When already logged into Google)
# Expected: 5-7 seconds
```

### Profile Endpoint (<200ms)
```bash
time curl -b "SESSION=<session_id>" http://localhost:8080/api/auth/user/profile
# Expected: <200ms (watch the time output)
```

---

## Test Result Checklist

- [ ] **Scenario 1**: First-time login works, user created
- [ ] **Scenario 2**: Return login reuses user (count=1), no duplicate
- [ ] **Scenario 3**: Page refresh maintains session
- [ ] **Scenario 4**: Logout terminates session, 401 on protected endpoints
- [ ] **Scenario 5**: Session expires after 12 hours
- [ ] **Scenario 6**: OAuth errors handled gracefully
- [ ] **Scenario 7**: Authentication events logged
- [ ] **Performance**: First <30s, return <10s, profile <200ms
- [ ] **Build**: Backend and frontend compile without errors
- [ ] **Database**: Unique constraint enforced, no duplicates

---

## Troubleshooting

### "Cannot GET /login"
**Issue**: Frontend not serving static files
- Verify frontend is running: `ng serve` output shows "Compiled successfully"
- Check http://localhost:4200 loads

### "401 Unauthorized" on profile endpoint
**Issue**: Session cookie not being sent
- Verify CORS credentials: Frontend should have `withCredentials: true`
- Check browser DevTools → Network → Headers for `Cookie:` line

### "Unique constraint violation" on second login
**Issue**: Deduplication logic not working
- Check CustomOAuth2UserService.loadUser() is being called
- Verify UserRepository.findByProviderAndProviderUserId() method exists

### Backend won't start: "Port 8080 already in use"
```bash
# Kill existing process
lsof -i :8080 | grep -v PID | awk '{print $2}' | xargs kill -9

# Then restart
./mvnw spring-boot:run
```

### Database connection error
```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Verify credentials in application.yml
psql -U inventory_user -d sdd_inventory -c "SELECT version();"
```

---

## Notes

- **Google OAuth Setup**: Requires actual Google OAuth credentials (not included)
- **Session Duration**: 12 hours absolute (not extended by activity)
- **Database**: PostgreSQL with Flyway migrations (auto-applied on startup)
- **Security**: HTTP-only session cookies, CORS with credentials, CSRF protection

---

## Commit History

Recent implementation commits:
- `6189042` - Fix: Correct Spring Security 6.x API calls
- `f24d31b` - Fix: Rename ExceptionHandler to GlobalExceptionHandler
- `955488c` - Implement SSO-Only Authentication Feature

All tests expected to pass with these fixes in place.