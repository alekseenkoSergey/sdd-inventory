# Quickstart: Category Management Validation

**Phase 1 Output** | **Date**: 2026-08-19

This document provides runnable validation scenarios to confirm the Inventory Categories feature works end-to-end. Use these tests to verify implementation readiness before full integration testing.

## Prerequisites

- **Backend API**: Running on `http://localhost:8080`
- **Authentication**: User account with valid JWT token or session
- **Database**: PostgreSQL with category table created via Flyway migration (V001)
- **Frontend**: Angular app running on `http://localhost:4200` (optional for UI validation)
- **Test Tools**: `curl` (for API testing) or Postman collection
- **Sample User**: Two test users to verify user isolation

## Setup

### 1. Create Test Users

```bash
# User 1: alice@example.com
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}'

# User 2: bob@example.com
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@example.com","password":"password123"}'
```

### 2. Obtain Authentication Tokens

```bash
# Login as Alice
ALICE_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' \
  | jq -r '.token')

# Login as Bob
BOB_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@example.com","password":"password123"}' \
  | jq -r '.token')

echo "Alice Token: $ALICE_TOKEN"
echo "Bob Token: $BOB_TOKEN"
```

---

## Scenario 1: Create a Category

**User Story**: Alice creates a category named "Electronics".

**Command**:

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"name":"Electronics"}'
```

**Expected Response** (201 Created):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Electronics",
  "itemCount": 0,
  "createdAt": "2026-08-19T10:30:00Z",
  "updatedAt": "2026-08-19T10:30:00Z"
}
```

**Validation**:
- ✅ HTTP 201 status code
- ✅ Response includes `id`, `name`, `itemCount`, `createdAt`, `updatedAt`
- ✅ Category name is "Electronics"
- ✅ itemCount is 0 (no items assigned yet)
- ✅ Timestamps are ISO8601 format

**Store for Later Use**:

```bash
ELECTRONICS_ID="550e8400-e29b-41d4-a716-446655440000"
```

---

## Scenario 2: Create Duplicate Category (Should Fail)

**User Story**: Alice attempts to create another category named "Electronics" (case-insensitive). System rejects with 400 error.

**Command**:

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"name":"electronics"}'
```

**Expected Response** (400 Bad Request):

```json
{
  "status": 400,
  "error": "CATEGORY_NAME_NOT_UNIQUE",
  "message": "Category name 'electronics' already exists. Please choose a different name."
}
```

**Validation**:
- ✅ HTTP 400 status code
- ✅ Error code is `CATEGORY_NAME_NOT_UNIQUE`
- ✅ Message indicates name already exists
- ✅ Case-insensitive check works (lowercase "electronics" rejected)

---

## Scenario 3: List Categories

**User Story**: Alice lists all her categories. Should show "Electronics" created in Scenario 1.

**Command**:

```bash
curl -X GET http://localhost:8080/api/categories \
  -H "Authorization: Bearer $ALICE_TOKEN"
```

**Expected Response** (200 OK):

```json
{
  "categories": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Electronics",
      "itemCount": 0,
      "createdAt": "2026-08-19T10:30:00Z",
      "updatedAt": "2026-08-19T10:30:00Z"
    }
  ]
}
```

**Validation**:
- ✅ HTTP 200 status code
- ✅ Response includes array of categories
- ✅ "Electronics" category is in the list
- ✅ Only Alice's categories are returned (user isolation)

---

## Scenario 4: Create Category with Whitespace (Should Trim)

**User Story**: Alice creates a category with leading/trailing whitespace. System trims it and stores as "Office Supplies".

**Command**:

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"name":"  Office Supplies  "}'
```

**Expected Response** (201 Created):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Office Supplies",
  "itemCount": 0,
  "createdAt": "2026-08-19T10:35:00Z",
  "updatedAt": "2026-08-19T10:35:00Z"
}
```

**Validation**:
- ✅ HTTP 201 status code
- ✅ Category name is trimmed: "Office Supplies" (no leading/trailing spaces)
- ✅ Future requests to create "  Office Supplies  " would fail as duplicate

---

## Scenario 5: Rename Category

**User Story**: Alice renames "Electronics" to "Consumer Electronics".

**Command**:

```bash
curl -X PATCH http://localhost:8080/api/categories/$ELECTRONICS_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"name":"Consumer Electronics"}'
```

**Expected Response** (200 OK):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Consumer Electronics",
  "itemCount": 0,
  "createdAt": "2026-08-19T10:30:00Z",
  "updatedAt": "2026-08-19T10:37:00Z"
}
```

**Validation**:
- ✅ HTTP 200 status code
- ✅ Category name changed to "Consumer Electronics"
- ✅ `updatedAt` timestamp is newer
- ✅ `id` and `createdAt` unchanged
- ✅ itemCount unchanged

---

## Scenario 6: Rename to Duplicate Name (Should Fail)

**User Story**: Alice tries to rename "Consumer Electronics" to "Office Supplies" (which already exists). System rejects with 400.

**Command**:

```bash
curl -X PATCH http://localhost:8080/api/categories/$ELECTRONICS_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"name":"Office Supplies"}'
```

**Expected Response** (400 Bad Request):

```json
{
  "status": 400,
  "error": "CATEGORY_NAME_NOT_UNIQUE",
  "message": "Category name 'Office Supplies' already exists. Please choose a different name."
}
```

**Validation**:
- ✅ HTTP 400 status code
- ✅ Error indicates name conflict
- ✅ Original category name unchanged (verified via GET)

---

## Scenario 7: Delete Empty Category

**User Story**: Alice creates an empty category "Tools", then deletes it. Should succeed (204 No Content).

**Command** (Create):

```bash
TOOLS_ID=$(curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"name":"Tools"}' | jq -r '.id')
```

**Command** (Delete):

```bash
curl -X DELETE http://localhost:8080/api/categories/$TOOLS_ID \
  -H "Authorization: Bearer $ALICE_TOKEN"
```

**Expected Response** (204 No Content):

```
HTTP/1.1 204 No Content
```

No response body.

**Validation**:
- ✅ HTTP 204 status code
- ✅ No response body
- ✅ Category no longer appears in GET /api/categories

---

## Scenario 8: Delete Category with Items (Should Fail)

**User Story**: Alice has a category with items assigned. Attempting to delete fails with 409 error including item count.

**Prerequisite**: Items exist in a category (requires item creation feature to be implemented first)

**Command**:

```bash
# Assume "Office Supplies" category has 5 items assigned
curl -X DELETE http://localhost:8080/api/categories/$OFFICE_SUPPLIES_ID \
  -H "Authorization: Bearer $ALICE_TOKEN"
```

**Expected Response** (409 Conflict):

```json
{
  "status": 409,
  "error": "CATEGORY_HAS_ITEMS",
  "message": "Cannot delete: 5 items assigned. Please reassign items to another category first."
}
```

**Validation**:
- ✅ HTTP 409 status code
- ✅ Error code is `CATEGORY_HAS_ITEMS`
- ✅ Message includes item count (5)
- ✅ Category still exists (verified via GET)

---

## Scenario 9: User Isolation — Bob Cannot Access Alice's Categories

**User Story**: Bob logs in and lists categories. Should be empty (Alice's categories not visible).

**Command**:

```bash
curl -X GET http://localhost:8080/api/categories \
  -H "Authorization: Bearer $BOB_TOKEN"
```

**Expected Response** (200 OK):

```json
{
  "categories": []
}
```

**Validation**:
- ✅ HTTP 200 status code
- ✅ Categories array is empty (Alice's categories hidden)
- ✅ Bob cannot see "Electronics", "Office Supplies", etc.

**Further Test**: Bob attempts to access Alice's category by ID directly.

```bash
curl -X GET http://localhost:8080/api/categories/$ELECTRONICS_ID \
  -H "Authorization: Bearer $BOB_TOKEN"
```

**Expected Response** (404 Not Found):

```json
{
  "status": 404,
  "error": "CATEGORY_NOT_FOUND",
  "message": "Category not found"
}
```

**Validation**:
- ✅ HTTP 404 status code
- ✅ Bob cannot access Alice's category even by ID (user isolation enforced)

---

## Scenario 10: Concurrent Edit Detection

**User Story**: Alice opens two browser tabs with the same category. Tab 1 renames it; Tab 2 (with stale data) attempts to rename it. System detects conflict and returns 409.

**Simulate with cURL** (requires manual version tracking):

```bash
# Step 1: Get category (note version from response)
CATEGORY=$(curl -s -X GET http://localhost:8080/api/categories/$ELECTRONICS_ID \
  -H "Authorization: Bearer $ALICE_TOKEN")
VERSION=$(echo $CATEGORY | jq '.version')

# Step 2: Modify category in Tab 1 (updates version)
curl -X PATCH http://localhost:8080/api/categories/$ELECTRONICS_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"name":"Premium Electronics"}'

# Step 3: Attempt to rename in Tab 2 using old version (should fail)
curl -X PATCH http://localhost:8080/api/categories/$ELECTRONICS_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -d '{"name":"High-End Electronics"}'
```

**Expected Response** (409 Conflict):

```json
{
  "status": 409,
  "error": "CONFLICT",
  "message": "Category no longer exists or has been modified. Please refresh your view and try again."
}
```

**Validation**:
- ✅ HTTP 409 status code
- ✅ Concurrent modification detected
- ✅ Error message instructs user to refresh

**Manual Verification**: Refresh category (GET) and confirm name is "Premium Electronics" (Step 2's change won).

---

## Frontend UI Validation (Optional)

If Angular frontend is implemented, validate through UI:

1. **Create Category**: Navigate to categories page, click "Create", enter name, verify appears in list
2. **Rename Category**: Click rename action, change name, verify updated immediately
3. **Delete Empty**: Create and delete empty category, verify removed from list
4. **Delete with Items**: Attempt to delete category with items, verify error message
5. **Concurrent Edits**: Open same category in two browser tabs, rename in one tab, attempt action in other, verify auto-refresh

---

## Test Checklist

- [ ] Create category (Scenario 1)
- [ ] Duplicate name rejected (Scenario 2)
- [ ] List categories shows all user's categories (Scenario 3)
- [ ] Whitespace trimmed on create (Scenario 4)
- [ ] Rename category (Scenario 5)
- [ ] Rename to duplicate rejected (Scenario 6)
- [ ] Delete empty category (Scenario 7)
- [ ] Delete with items blocked (Scenario 8)
- [ ] User isolation enforced (Scenario 9)
- [ ] Concurrent edits detected (Scenario 10)

---

## Troubleshooting

**401 Unauthorized on all requests**:
- Verify token is valid: `curl http://localhost:8080/api/auth/verify -H "Authorization: Bearer $TOKEN"`
- Re-login if expired: obtain new token

**400 Bad Request on valid input**:
- Check request format (JSON syntax, required fields)
- Verify @NotBlank and @Size validation rules are met

**404 Category Not Found**:
- Verify category ID is correct
- Verify authenticated user owns the category (not another user's)

**409 Conflict on rename**:
- Check if new name already exists (case-insensitive)
- If during concurrent test, refresh category and retry

---

## References

- [API Contract](./contracts/category-api.md) — Full endpoint documentation
- [Data Model](./data-model.md) — Entity and DTO specifications
- [Feature Specification](./spec.md) — Requirements and acceptance criteria
