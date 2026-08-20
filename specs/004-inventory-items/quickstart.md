# Quickstart: Inventory Items Feature Validation

**Date**: 2026-08-20

This document describes how to validate that the Inventory Items Management feature works end-to-end. Use this guide to confirm the feature is implemented correctly before marking the feature complete.

## Prerequisites

1. **Backend running**: Spring Boot application on `http://localhost:8080`
2. **Database ready**: PostgreSQL with Flyway migrations applied
3. **Authentication setup**: User authentication system working (Spring Security)
4. **Dependencies pre-exist**: 
   - A user account (authenticated via Spring Security)
   - At least one Category (created via the Categories feature)
   - At least one Location (created via the Locations feature)
5. **Test client**: curl, Postman, or equivalent HTTP client

## Setup

### Get Authentication Token

Use your project's authentication endpoint to obtain a bearer token. Example (adjust for your auth system):

```bash
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser@example.com","password":"password"}' | jq -r '.token')
echo $TOKEN
```

Store the token in an environment variable for the tests below.

### Identify Test Data IDs

Get your category and location IDs for use in tests:

```bash
# List categories (adjust endpoint per your implementation)
curl -X GET http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" | jq '.content[0].id'

# List locations (adjust endpoint per your implementation)
curl -X GET http://localhost:8080/api/v1/locations \
  -H "Authorization: Bearer $TOKEN" | jq '.content[0].id'
```

Store these in environment variables:

```bash
CATEGORY_ID="550e8400-e29b-41d4-a716-446655440000"
LOCATION_ID="550e8400-e29b-41d4-a716-446655440001"
```

---

## Validation Scenarios

### Scenario 1: Create Item with Opening Balance

**Goal**: Verify item creation with initial quantity and automatic opening balance stock movement.

**Test Steps**:

1. Create an item with initial quantity > 0:

```bash
curl -X POST http://localhost:8080/api/v1/inventory-items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Validation Widget A",
    "description": "Test item for validation",
    "sku": "VAL-SKU-001",
    "categoryId": "'$CATEGORY_ID'",
    "locationId": "'$LOCATION_ID'",
    "unit": "pcs",
    "lowStockThreshold": 5,
    "initialQuantity": 100
  }' | jq '.'
```

2. Capture the response `id` field:

```bash
ITEM_ID=$(curl -s -X POST http://localhost:8080/api/v1/inventory-items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Validation Widget A",
    "description": "Test item for validation",
    "sku": "VAL-SKU-001",
    "categoryId": "'$CATEGORY_ID'",
    "locationId": "'$LOCATION_ID'",
    "unit": "pcs",
    "lowStockThreshold": 5,
    "initialQuantity": 100
  }' | jq -r '.id')
echo $ITEM_ID
```

**Expected Results**:
- ✅ HTTP 201 Created
- ✅ Response includes `id`, `name`, `currentQuantity: 100`, `status: ACTIVE`
- ✅ `createdDate` and `updatedDate` are set to current UTC timestamp
- ✅ Opening balance stock movement is created (verify via stock movements API if available)

---

### Scenario 2: Create Item Without Initial Quantity

**Goal**: Verify item starts with quantity 0 when no initial quantity provided.

**Test Steps**:

```bash
curl -X POST http://localhost:8080/api/v1/inventory-items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Empty Widget B",
    "categoryId": "'$CATEGORY_ID'",
    "locationId": "'$LOCATION_ID'",
    "unit": "boxes"
  }' | jq '.currentQuantity'
```

**Expected Results**:
- ✅ HTTP 201 Created
- ✅ `currentQuantity: 0`
- ✅ No stock movement created

---

### Scenario 3: Validation - Empty Name

**Goal**: Verify name validation rejects empty names.

**Test Steps**:

```bash
curl -X POST http://localhost:8080/api/v1/inventory-items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "categoryId": "'$CATEGORY_ID'",
    "locationId": "'$LOCATION_ID'",
    "unit": "pcs"
  }' | jq '.'
```

**Expected Results**:
- ✅ HTTP 400 Bad Request
- ✅ Error message indicates name validation failure

---

### Scenario 4: Validation - SKU Uniqueness

**Goal**: Verify duplicate SKU within same user is rejected.

**Test Steps**:

1. Create first item with SKU "UNIQUE-SKU-001":

```bash
curl -X POST http://localhost:8080/api/v1/inventory-items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "First Item",
    "sku": "UNIQUE-SKU-001",
    "categoryId": "'$CATEGORY_ID'",
    "locationId": "'$LOCATION_ID'",
    "unit": "pcs"
  }' | jq '.id'
```

2. Attempt to create second item with same SKU:

```bash
curl -X POST http://localhost:8080/api/v1/inventory-items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Second Item",
    "sku": "UNIQUE-SKU-001",
    "categoryId": "'$CATEGORY_ID'",
    "locationId": "'$LOCATION_ID'",
    "unit": "pcs"
  }' | jq '.'
```

**Expected Results**:
- ✅ First creation: HTTP 201 Created
- ✅ Second creation: HTTP 400 Bad Request with SKU_DUPLICATE error

---

### Scenario 5: Read Item

**Goal**: Verify retrieval of item by ID.

**Test Steps**:

```bash
curl -X GET http://localhost:8080/api/v1/inventory-items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.'
```

**Expected Results**:
- ✅ HTTP 200 OK
- ✅ Response includes all item fields with correct values

---

### Scenario 6: Update Item Fields

**Goal**: Verify item field updates (excluding quantity).

**Test Steps**:

```bash
curl -X PATCH http://localhost:8080/api/v1/inventory-items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Widget A - Updated",
    "description": "Updated description",
    "lowStockThreshold": 20
  }' | jq '.name, .description, .lowStockThreshold'
```

**Expected Results**:
- ✅ HTTP 200 OK
- ✅ `name` updated to "Widget A - Updated"
- ✅ `description` updated
- ✅ `lowStockThreshold` updated to 20
- ✅ `currentQuantity` remains unchanged (100 from scenario 1)
- ✅ `updatedDate` is newer than original

---

### Scenario 7: Prevent Direct Quantity Edit

**Goal**: Verify currentQuantity cannot be directly edited.

**Test Steps**:

```bash
curl -X PATCH http://localhost:8080/api/v1/inventory-items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Widget A",
    "currentQuantity": 999
  }' | jq '.'
```

**Expected Results**:
- ✅ HTTP 200 OK (if DTO ignores unknown fields) OR HTTP 400 Bad Request (if strict mapping)
- ✅ `currentQuantity` remains 100 (unchanged)
- ✅ Alternative: API returns error indicating currentQuantity is read-only

---

### Scenario 8: List Items

**Goal**: Verify item listing with pagination.

**Test Steps**:

```bash
# List all items for authenticated user
curl -X GET 'http://localhost:8080/api/v1/inventory-items' \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'

# List with pagination
curl -X GET 'http://localhost:8080/api/v1/inventory-items?page=0&size=10' \
  -H "Authorization: Bearer $TOKEN" | jq '.totalElements, .currentPage'

# List active items only
curl -X GET 'http://localhost:8080/api/v1/inventory-items?status=ACTIVE' \
  -H "Authorization: Bearer $TOKEN" | jq '.content | map(.status) | unique'
```

**Expected Results**:
- ✅ HTTP 200 OK
- ✅ Response includes pagination metadata
- ✅ Items created in previous tests appear in list
- ✅ Status filter works (returns only ACTIVE items when `status=ACTIVE`)

---

### Scenario 9: Archive Item

**Goal**: Verify item archival and idempotency.

**Test Steps**:

```bash
# Archive item
curl -X POST http://localhost:8080/api/v1/inventory-items/$ITEM_ID/archive \
  -H "Authorization: Bearer $TOKEN" | jq '.status'

# Archive again (idempotent - should succeed)
curl -X POST http://localhost:8080/api/v1/inventory-items/$ITEM_ID/archive \
  -H "Authorization: Bearer $TOKEN" | jq '.status'

# Verify status in list
curl -X GET 'http://localhost:8080/api/v1/inventory-items?status=ARCHIVED' \
  -H "Authorization: Bearer $TOKEN" | jq '.content | map(.id) | contains(["'$ITEM_ID'"])'
```

**Expected Results**:
- ✅ First archive: HTTP 200 OK, `status: ARCHIVED`
- ✅ Second archive: HTTP 200 OK (idempotent, no error)
- ✅ Archived item appears in ARCHIVED list

---

### Scenario 10: Restore Item

**Goal**: Verify archived item can be restored.

**Test Steps**:

```bash
# Restore item
curl -X POST http://localhost:8080/api/v1/inventory-items/$ITEM_ID/restore \
  -H "Authorization: Bearer $TOKEN" | jq '.status'

# Restore again (idempotent)
curl -X POST http://localhost:8080/api/v1/inventory-items/$ITEM_ID/restore \
  -H "Authorization: Bearer $TOKEN" | jq '.status'

# Verify status
curl -X GET http://localhost:8080/api/v1/inventory-items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.status'
```

**Expected Results**:
- ✅ First restore: HTTP 200 OK, `status: ACTIVE`
- ✅ Second restore: HTTP 200 OK (idempotent)
- ✅ Item is ACTIVE after restore

---

### Scenario 11: Delete Item

**Goal**: Verify hard deletion of item.

**Test Steps**:

```bash
# Delete item
curl -X DELETE http://localhost:8080/api/v1/inventory-items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN"

# Verify item no longer exists
curl -X GET http://localhost:8080/api/v1/inventory-items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN" | jq '.status'
```

**Expected Results**:
- ✅ Delete: HTTP 204 No Content
- ✅ Get after delete: HTTP 404 Not Found

---

### Scenario 12: User Data Isolation

**Goal**: Verify items cannot be accessed by other users.

**Test Steps**:

1. Create a second test user account and obtain token:

```bash
TOKEN2=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"otheruser@example.com","password":"password"}' | jq -r '.token')
```

2. Attempt to access item created by first user:

```bash
# Create item as User 1
ITEM_ID=$(curl -s -X POST http://localhost:8080/api/v1/inventory-items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "User 1 Item",
    "categoryId": "'$CATEGORY_ID'",
    "locationId": "'$LOCATION_ID'",
    "unit": "pcs"
  }' | jq -r '.id')

# Attempt to access as User 2
curl -X GET http://localhost:8080/api/v1/inventory-items/$ITEM_ID \
  -H "Authorization: Bearer $TOKEN2" | jq '.status'
```

**Expected Results**:
- ✅ HTTP 404 Not Found (treats as if item doesn't exist for unauthorized user)
- ✅ User 2 cannot see User 1's items in their list

---

## Validation Checklist

After running all scenarios above, verify:

- [ ] All create operations succeed with correct data
- [ ] Quantity validation enforced (non-negative)
- [ ] SKU uniqueness enforced per user
- [ ] Opening balance stock movement created automatically
- [ ] Direct quantity edits rejected
- [ ] List operations support pagination and filtering
- [ ] Archive/restore work and are idempotent
- [ ] Delete removes items permanently
- [ ] All timestamps are UTC ISO-8601 format
- [ ] User data isolation enforced (cannot access other users' items)
- [ ] 404 returned for missing items (not 403, maintaining security through obscurity)

---

## Troubleshooting

**Issue**: "Category not found" when creating item
- Verify `CATEGORY_ID` is correct and belongs to authenticated user

**Issue**: "SKU already exists" when creating with null SKU
- Verify you're not trying to create multiple items without providing SKU values

**Issue**: "currentQuantity" field still editable in PATCH
- Check that update DTO (`InventoryItemPatchDTO`) does not include currentQuantity field

**Issue**: Deleted item still appears in list
- Confirm DELETE endpoint returns 204 and verify database rows are removed (not soft-deleted)

**Issue**: Archived item allows stock movements
- Verify stock movement service checks item.status == ACTIVE before creating movements

---

## Performance Validation

For success criterion SC-001 (operations complete in under 500ms):

```bash
# Time a create operation
time curl -X POST http://localhost:8080/api/v1/inventory-items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Perf Test Item",
    "categoryId": "'$CATEGORY_ID'",
    "locationId": "'$LOCATION_ID'",
    "unit": "pcs",
    "initialQuantity": 50
  }' > /dev/null
```

Expected result: < 500ms total response time

---

## Completion Criteria

Feature is ready for handoff when:
1. ✅ All 12 scenarios pass
2. ✅ All validation checklist items verified
3. ✅ Performance metrics met (< 500ms per operation)
4. ✅ Code review passed
5. ✅ Integration tests included in test suite
6. ✅ Flyway migration applied successfully
