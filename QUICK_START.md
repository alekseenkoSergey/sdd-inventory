# Quick Start Guide - OAuth2 with Welcome UI

Get the app running in minutes with complete SSO authentication.

---

## Prerequisites

- Java 17+
- Node.js 18+
- PostgreSQL running locally
- Google OAuth credentials (already configured)

---

## Step 1: Start Backend

```bash
cd backend
mvn clean spring-boot:run
```

**Expected Output:**
```
Started SddInventoryApplication in X seconds
Flyway automatically creates users table
Server running on http://localhost:8080
```

---

## Step 2: Start Frontend

In a new terminal:

```bash
cd frontend
npm install
ng serve
```

**Expected Output:**
```
✔ Compiled successfully.
* Tip: The serve command is good for development and testing...
* Application bundle generated successfully in 12.34 seconds.
✔ Open your browser on http://localhost:4200
```

---

## Step 3: Visit Application

Open browser to: **http://localhost:4200**

You should see:
- Welcome page with purple gradient background
- "Inventory Tracker" title
- "Sign in with Google" button
- Security badge

---

## Step 4: Test OAuth Login

1. **Click "Sign in with Google"**
2. **You'll be redirected to Google**
3. **Log in with your Google account**
4. **Grant permission** to app
5. **App redirects back**

**After successful login, you'll see:**
- Home dashboard page
- Your name from Google profile
- Your email
- Account information card
- Features section

---

## Step 5: Test Logout

1. **Click "Logout"** button in top right
2. **You'll be redirected to login page**
3. **Session is terminated**

---

## Step 6: Verify Session Persistence

1. **Login again**
2. **Press F5** to refresh page
3. **You remain logged in!**

---

## What Happens Behind the Scenes

### First-Time Login Flow
```
User clicks "Sign in with Google"
    ↓
Frontend redirects to /oauth2/authorization/google
    ↓
Google OAuth consent screen
    ↓
User grants permission
    ↓
Google redirects to /login/oauth2/code/google with auth code
    ↓
Backend exchanges code for token
    ↓
Backend calls CustomOAuth2UserService.loadUser()
    ↓
CustomOAuth2UserService:
  1. Looks up user by (provider="Google", providerUserId)
  2. If NOT found:
     - Creates NEW user record in database
     - Logs "New user created"
  3. If found:
     - Updates existing user profile
     - Logs "Existing user reused"
    ↓
Spring Security creates session
    ↓
Sets HTTP-only secure cookie
    ↓
Frontend app redirected to /home
    ↓
App loads profile from /api/auth/user/profile
    ↓
Home dashboard displays user info
```

### Database

Users are stored in PostgreSQL:
```sql
SELECT * FROM users;
```

**Table Structure:**
```
id                (auto-generated)
provider          "Google"
provider_user_id  (OAuth sub claim)
email             user@gmail.com
display_name      "Your Name"
avatar_url        https://lh3.googleusercontent.com/...
created_at        2026-08-19 10:30:00
updated_at        2026-08-19 10:30:00
```

---

## Common Issues & Solutions

### Issue: "Cannot connect to http://localhost:8080"
**Solution:** Backend not started
```bash
# Check if backend is running
curl http://localhost:8080/actuator/health

# If not, start it
cd backend
mvn spring-boot:run
```

### Issue: "ERROR in ng serve"
**Solution:** Dependencies not installed
```bash
cd frontend
npm install
ng serve
```

### Issue: Google login redirects to error page
**Solution:** OAuth credentials not configured
```bash
# Check application.yml for Google OAuth config
cat backend/src/main/resources/application.yml | grep -A 5 "google:"

# Should show:
# client-id: 769868887730-...
# client-secret: GOCSPX-...
```

### Issue: User created twice on login
**Solution:** Check database for duplicate
```sql
SELECT COUNT(*) FROM users WHERE provider_user_id='YOUR_ID';
-- Should be 1, not 2

-- If duplicate exists, delete extra row:
DELETE FROM users WHERE id=2;
```

### Issue: "Session timeout" after refresh
**Solution:** Check cookie settings
1. Open DevTools (F12)
2. Go to Application → Cookies
3. Look for `SESSION` cookie
4. Should have `HttpOnly`, `Secure`, `SameSite=Lax` flags

---

## Key URLs

| URL | Purpose | Auth Required |
|-----|---------|---------------|
| http://localhost:4200 | Home (redirects to /home) | Yes |
| http://localhost:4200/login | Welcome/Login page | No |
| http://localhost:4200/home | Home dashboard | Yes |
| http://localhost:8080/api/auth/user/profile | Get user profile | Yes |
| http://localhost:8080/api/auth/logout | Logout endpoint | Yes |

---

## Testing the API

### Get User Profile
```bash
curl -X GET http://localhost:8080/api/auth/user/profile \
  -H "Cookie: SESSION=your_session_id" \
  --include

# Response (200 OK):
# {
#   "id": 1,
#   "provider": "Google",
#   "email": "user@gmail.com",
#   "displayName": "Your Name",
#   "avatarUrl": "https://..."
# }
```

### Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Cookie: SESSION=your_session_id" \
  --include

# Response (200 OK):
# { "status": "logged_out" }
```

---

## Files to Know

### Frontend
- `frontend/src/app/auth/components/login/login.component.ts` - Welcome page logic
- `frontend/src/app/auth/components/login/login.component.html` - Welcome page template
- `frontend/src/app/auth/components/login/login.component.css` - Welcome page styling
- `frontend/src/app/pages/home/home.component.ts` - Dashboard logic
- `frontend/src/app/pages/home/home.component.html` - Dashboard template
- `frontend/src/app/pages/home/home.component.css` - Dashboard styling
- `frontend/src/app/auth/services/auth.service.ts` - Auth logic
- `frontend/src/app/auth/guards/auth.guard.ts` - Route protection
- `frontend/src/app/auth/interceptors/auth.interceptor.ts` - HTTP interceptor
- `frontend/src/app/app.routes.ts` - Route configuration

### Backend
- `backend/src/main/java/org/example/sddinventory/config/SecurityConfig.java` - OAuth config
- `backend/src/main/java/org/example/sddinventory/controller/AuthController.java` - Auth endpoints
- `backend/src/main/java/org/example/sddinventory/service/AuthService.java` - Auth logic
- `backend/src/main/java/org/example/sddinventory/service/CustomOAuth2UserService.java` - User provisioning
- `backend/src/main/java/org/example/sddinventory/entity/User.java` - User entity
- `backend/src/main/resources/db/migration/V2__create_users_table.sql` - Database schema
- `backend/src/main/resources/application.yml` - Configuration (with Google OAuth credentials)

---

## Visual Overview

### Welcome Page Flow
```
┌─────────────────────────────────────────┐
│                                         │
│     Inventory Tracker                   │
│     Manage your inventory with ease    │
│                                         │
│     ┌───────────────────────────────┐  │
│     │  Sign in with Google          │  │
│     └───────────────────────────────┘  │
│                                         │
│     🔒 Your data is secure             │
│                                         │
└─────────────────────────────────────────┘
```

### Home Dashboard Flow
```
┌─────────────────────────────────────────┐
│ Logo  Inventory Tracker    [ Logout ]   │
├─────────────────────────────────────────┤
│                                         │
│  👤 Welcome, John Doe!                 │
│     john@gmail.com                     │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │  Account Information             │  │
│  │  Name: John Doe                 │  │
│  │  Email: john@gmail.com          │  │
│  │  Provider: Google               │  │
│  └─────────────────────────────────┘  │
│                                         │
│  Features                               │
│  ┌──────────┐ ┌──────────┐            │
│  │ Secure   │ │ Profile  │            │
│  │ SSO      │ │ Mgmt     │            │
│  └──────────┘ └──────────┘            │
│  ┌──────────┐ ┌──────────┐            │
│  │ Sessions │ │ Security │            │
│  │ Mgmt     │ │ Protected│            │
│  └──────────┘ └──────────┘            │
│                                         │
└─────────────────────────────────────────┘
```

---

## Documentation

For more details, see:
- `OAUTH_IMPLEMENTATION_STATUS.md` - Complete implementation details
- `OAUTH_VERIFICATION_GUIDE.md` - Comprehensive testing guide
- `UI_FEATURES.md` - Design system and styling
- `specs/001-sso-auth/spec.md` - Original requirements
- `specs/001-sso-auth/quickstart.md` - Validation scenarios

---

## Deploy to Production

When ready for production:

1. **Set environment variables**
   ```bash
   export GOOGLE_CLIENT_ID=your_prod_client_id
   export GOOGLE_CLIENT_SECRET=your_prod_secret
   ```

2. **Update redirect URI** in Google Cloud Console
   ```
   https://yourdomain.com/login/oauth2/code/google
   ```

3. **Update application.yml** for production domain
   ```yaml
   spring:
     security:
       oauth2:
         client:
           registration:
             google:
               redirect-uri: https://yourdomain.com/login/oauth2/code/google
   ```

4. **Enable HTTPS**
   ```bash
   # Ensure HTTPS is enabled on your server
   ```

5. **Configure session store**
   ```yaml
   spring:
     session:
       store-type: jdbc  # Use database-backed sessions
   ```

---

## Next Steps

✅ **Completed:**
- OAuth2 backend implementation
- Frontend authentication system
- Welcome page UI
- Home dashboard UI
- Session management
- Error handling
- Documentation

**Coming Soon:**
- More dashboard features
- Inventory management screens
- User preferences
- Mobile app support

---

## Get Help

Check the docs:
- 📖 `OAUTH_VERIFICATION_GUIDE.md` for testing steps
- 📖 `UI_FEATURES.md` for design details
- 📖 `OAUTH_IMPLEMENTATION_STATUS.md` for implementation details
- 💬 Check backend/frontend console for error messages
- 🔍 Use DevTools (F12) to debug frontend
- 📋 Check `/logs/application.log` for backend logs

---

**Status**: ✅ Ready to Use
**Last Updated**: 2026-08-19
