# Data Model: SSO-Only Authentication

**Feature**: SSO-Only Authentication | **Date**: 2026-08-18

## Entity: User

Represents an authenticated user account provisioned on first OAuth2 sign-in.

### Database Table: `users`

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    provider VARCHAR(64) NOT NULL,           -- OAuth provider identifier (e.g., "Google")
    provider_user_id VARCHAR(255) NOT NULL,  -- OAuth subject claim (unique per provider)
    email VARCHAR(255),                      -- User's email from provider profile (nullable)
    display_name VARCHAR(255),               -- User's display name from provider profile (nullable)
    avatar_url VARCHAR(2048),                -- User's avatar/profile picture URL (nullable)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Record creation time
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Record last modification time
    UNIQUE(provider, provider_user_id),      -- Prevent duplicate accounts for same provider+user
    INDEX idx_provider_user_id (provider, provider_user_id)  -- Optimize lookups on return login
);
```

### JPA Entity: `com.example.inventory.entity.User`

Located at: `backend/src/main/java/com/example/inventory/entity/User.java`

```java
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String provider;                 // e.g., "Google"

    @Column(nullable = false, length = 255)
    private String providerUserId;          // OAuth subject claim (snake_case in DB, camelCase in Java)

    @Column(nullable = true, length = 255)
    private String email;

    @Column(nullable = true, length = 255)
    private String displayName;             // snake_case in DB via @Column

    @Column(nullable = true, length = 2048)
    private String avatarUrl;               // snake_case in DB via @Column

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors, getters, setters...
}
```

### Attributes

| Attribute | Type | Nullable | Description | Constraints |
|-----------|------|----------|-------------|-----------|
| `id` | Long | No | Primary key, auto-generated | Auto-increment |
| `provider` | String(64) | No | OAuth provider identifier | Must match provider's canonical name (e.g., "Google") |
| `provider_user_id` | String(255) | No | OAuth subject claim (unique user ID from provider) | Maps to OAuth2User's nameAttributeKey; unique per provider |
| `email` | String(255) | Yes | User's email from provider profile | May be absent if provider doesn't return email |
| `display_name` | String(255) | Yes | User's display name from provider profile | May be absent if provider doesn't return name |
| `avatar_url` | String(2048) | Yes | URL to user's profile picture/avatar | May be absent if provider doesn't return picture URL |
| `created_at` | Timestamp | No | Record creation time (UTC) | Set automatically on insert; never updated |
| `updated_at` | Timestamp | No | Record last modification time (UTC) | Set automatically on insert; updated on modify operations |

### Validation Rules

- **provider**: Must be non-empty string, typically one of: "Google", "GitHub" (if added in future)
- **provider_user_id**: Must be non-empty string; should be treated as opaque identifier (no parsing)
- **email**: If present, should be valid email format (validated by OAuth provider)
- **(provider, provider_user_id)**: Unique together; prevents duplicate accounts
- **avatar_url**: If present, should be valid absolute URL (validated by OAuth provider)

### Lifecycle & State Transitions

**User Lifecycle**:

1. **Non-Existent → Existent** (on first OAuth sign-in):
   - Google redirects user to application with authorization code
   - Backend exchanges code for access token
   - Backend calls Google's userinfo endpoint, retrieves user profile
   - Backend checks UserRepository for user with (provider="Google", provider_user_id=<Google subject>)
   - **If not found**: Create new User entity with profile data, persist to database, establish session
   - **If found**: Use existing User, establish session

2. **Active Session** (on subsequent requests):
   - User makes HTTP request with valid session cookie
   - Spring Security validates session, loads User entity (reference only; no additional DB query per request)
   - Request proceeds to protected endpoint

3. **Session Expired → Unauthenticated**:
   - 12-hour absolute timeout elapses
   - User's next request without valid session cookie returns 401 Unauthorized
   - Frontend redirects to login; user must re-authenticate

4. **User Logs Out** (explicit):
   - User clicks Logout button
   - Frontend makes POST /api/auth/logout
   - Backend invalidates session, deletes session cookie
   - User returned to login page

**No Delete Operation**: User records are never deleted (pet project scope). Historical records preserved for audit trail.

### Relationships

- **User ←→ Session**: One-to-many (zero or one active session per user at a time, though multiple sessions from different browsers possible)
  - Managed by Spring Session; not explicit foreign key
  - Session can reference user ID but lifecycle independent

### Indexes

- **Primary Key Index**: On `id` (auto-created)
- **Unique Constraint Index**: On `(provider, provider_user_id)` (for deduplication)
- **Lookup Index**: On `(provider, provider_user_id)` (optimizes return-user login flow)

### Migration History

**V2__create_users_table.sql** (created with this feature):
- Creates `users` table with all columns, constraints, and indexes
- Depends on: V1__initial_schema (core app schema)
- Applied before: Any application startup that requires authentication

---

## Session & Token State

**Managed by**: Spring Session (with in-memory store by default)

Session state is NOT part of the User entity; it's managed separately by Spring Session. However, for completeness:

### Session Attributes

- `SESSION_ID`: Unique identifier for this session
- `userId`: Foreign key reference to User.id
- `createdAt`: Session creation timestamp
- `expiresAt`: Session expiry timestamp (12 hours after creation)
- `lastAccessedTime`: Last request timestamp (used for monitoring; doesn't extend expiry)

### Session Lifecycle

1. **Created**: On successful OAuth authentication (OAuth2UserService or AuthService)
2. **Maintained**: HTTP-only cookie stored on client browser; validated on each request
3. **Expired**: 12 hours after creation; Spring Session automatically removes
4. **Invalidated**: On logout (POST /api/auth/logout) or session timeout

---

## Data Consistency & Constraints

### Unique Constraints

- **(provider, provider_user_id)**: Ensures one user account per OAuth provider account
  - Example: Same person cannot create two User records with provider="Google" and provider_user_id="<Google subject>"

### Referential Integrity

- No foreign keys to other tables in this feature
- Future features may reference User.id for authorization (e.g., user_id in Inventory items)

### Concurrent Access

- **No race condition on first login**: Database unique constraint ensures only one User record created
  - If two concurrent requests attempt to create user with same (provider, provider_user_id), database enforces uniqueness
  - Second request receives unique constraint violation; application handles by fetching existing user instead

### Data Retention

- User records retained indefinitely (pet project scope; no GDPR purge requirement)
- Audit logs (in application logs file) follow system's log retention policy

---

## API Representation

### UserProfileResponseDTO

Frontend-facing representation of User data (returned by GET /api/auth/user/profile):

```json
{
  "id": 123,
  "provider": "Google",
  "email": "user@example.com",
  "displayName": "John Doe",
  "avatarUrl": "https://lh3.googleusercontent.com/a/ABC..."
}
```

**Mapping**:
- `User.id` → `id`
- `User.provider` → `provider`
- `User.email` → `email`
- `User.display_name` → `displayName`
- `User.avatar_url` → `avatarUrl`

**Nullable fields**: If User.email is null, response may omit field or include with null value (backend convention to be defined).

---

## Testing Considerations

### Unit Test Setup

- Mock UserRepository to simulate database queries
- Test User entity JPA annotations (column names, constraints)
- Verify unique constraint on (provider, provider_user_id) via Hibernate validation

### Integration Test Setup

- Use real PostgreSQL test database (or H2 for in-memory testing)
- Create User records and verify unique constraint enforcement
- Test concurrent first-login scenarios to ensure no duplicates
- Verify session creation and expiry behavior

### Example Scenario

```
Given: User "john@google.com" (provider_user_id="12345") exists in database
When: Another request to create user with same provider and provider_user_id
Then: Unique constraint violation is caught; existing user is fetched instead of creating duplicate
And: Session is established for existing user (idempotent behavior)
```
