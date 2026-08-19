# Quickstart & Validation Guide: Location Management

**Date**: 2026-08-19  
**Feature**: Location Management

## Overview

This guide describes end-to-end validation scenarios that prove the Location Management feature works correctly. These scenarios verify the full feature lifecycle from API perspective.

## Prerequisites

1. Backend running on `http://localhost:8080`
2. Valid OAuth2 authentication token for test user (assume `USER_TOKEN` environment variable or test bearer token)
3. `curl` or HTTP client available for API calls
4. PostgreSQL database with schema initialized (Flyway migrations applied)

## Setup Commands

### Start Backend

```bash
cd backend
./mvnw spring-boot:run
```

Expected: Server starts, Flyway migrations run, schema created including location table.

### Verify Database Schema

```bash
psql -U inventory_user -d inventory_db -c "\d location"
```

Expected output shows location table with columns: id, user_id, name, created_at, updated_at, version.

## Validation Scenario 1: Create and List Locations

**User Story**: User Story 1 — Create a Location (Priority P1)

**Goal**: Verify user can create a location and immediately see it in their list.

### Step 1: Create Location "Home Office"

```bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{
    "name": "Home Office"
  }'
```

**Expected Response** (201 Created):
```json
{
  "id": 1,
  "userId": 42,
  "name": "Home Office",
  "createdAt": "2026-08-19T10:30:00",
  "updatedAt": "2026-08-19T10:30:00"
}
```

**Verify**:
- Response status is 201
- Response includes id, userId, name, timestamps
- Timestamp is current (within ±5 seconds)

### Step 2: List Locations

```bash
curl -X GET http://localhost:8080/locations \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Expected Response** (200 OK):
```json
[
  {
    "id": 1,
    "userId": 42,
    "name": "Home Office",
    "createdAt": "2026-08-19T10:30:00",
    "updatedAt": "2026-08-19T10:30:00"
  }
]
```

**Verify**:
- Response status is 200
- Array includes the location just created
- Location fields match the create response

---

## Validation Scenario 2: Duplicate Name Rejection

**User Story**: User Story 1 (acceptance scenario 3) — System prevents duplicate names

**Goal**: Verify system rejects duplicate location names for the same user.

### Step 1: Create First Location

```bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "Office"}'
```

**Expected**: 201 Created with id=2

### Step 2: Create Duplicate Location with Same Name

```bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "Office"}'
```

**Expected Response** (409 Conflict):
```json
{
  "timestamp": "2026-08-19T10:35:00Z",
  "status": 409,
  "error": "LOCATION_NAME_NOT_UNIQUE",
  "message": "A location with this name already exists in your account",
  "path": "/locations"
}
```

**Verify**:
- Response status is 409 (not 400 or 500)
- Error code is `LOCATION_NAME_NOT_UNIQUE`
- Message clearly explains duplicate name issue

---

## Validation Scenario 3: Rename Location

**User Story**: User Story 2 — Rename a Location (Priority P2)

**Goal**: Verify user can rename a location and see name updated everywhere.

### Step 1: Create Location

```bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "Temp Location"}'
```

**Expected**: 201 Created with id=3

### Step 2: Rename Location

```bash
curl -X PUT http://localhost:8080/locations/3 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "Warehouse"}'
```

**Expected Response** (200 OK):
```json
{
  "id": 3,
  "userId": 42,
  "name": "Warehouse",
  "createdAt": "2026-08-19T10:40:00",
  "updatedAt": "2026-08-19T10:42:00"
}
```

**Verify**:
- Response status is 200
- name field shows new name "Warehouse"
- updatedAt timestamp is newer than createdAt

### Step 3: Verify Updated Name in List

```bash
curl -X GET http://localhost:8080/locations/3 \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Expected**: GET returns location with name="Warehouse"

**Verify**: Name is "Warehouse" in subsequent GET request (FR-004 verified)

---

## Validation Scenario 4: Delete Empty Location

**User Story**: User Story 4 — Delete a Location (Priority P2)

**Goal**: Verify user can delete empty locations.

### Step 1: Create Location

```bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "To Delete"}'
```

**Expected**: 201 Created with id=4

### Step 2: Delete Location

```bash
curl -X DELETE http://localhost:8080/locations/4 \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Expected Response** (204 No Content): Empty response body

**Verify**:
- Response status is 204
- No response body

### Step 3: Verify Location Removed

```bash
curl -X GET http://localhost:8080/locations/4 \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Expected Response** (404 Not Found):
```json
{
  "timestamp": "2026-08-19T10:50:00Z",
  "status": 404,
  "error": "LOCATION_NOT_FOUND",
  "message": "Location not found",
  "path": "/locations/4"
}
```

**Verify**: Deleted location no longer retrievable (FR-006 verified)

---

## Validation Scenario 5: Block Deletion of Non-Empty Location

**User Story**: User Story 4 (acceptance scenario 3) — System blocks deletion with items

**Goal**: Verify system prevents deletion of locations containing items.

### Prerequisites

- An item must exist in the item table assigned to a location
- For this validation, assume a location with id=5 exists and has 2 items assigned

### Step 1: Attempt to Delete Non-Empty Location

```bash
curl -X DELETE http://localhost:8080/locations/5 \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Expected Response** (409 Conflict):
```json
{
  "timestamp": "2026-08-19T11:00:00Z",
  "status": 409,
  "error": "LOCATION_HAS_ITEMS",
  "message": "Cannot delete location with items. Please remove or reassign items first. (itemCount: 2)",
  "path": "/locations/5"
}
```

**Verify**:
- Response status is 409 (not 204, not 404, not 400)
- Error code is `LOCATION_HAS_ITEMS`
- Message includes itemCount so user knows how many items exist
- Message explains next action ("remove or reassign items")

---

## Validation Scenario 6: Input Validation - Empty Name

**User Story**: User Story 1 (acceptance scenario 2, edge case) — System validates name not empty

**Goal**: Verify system rejects empty or whitespace-only location names.

### Step 1: Create Location with Empty Name

```bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": ""}'
```

**Expected Response** (400 Bad Request):
```json
{
  "timestamp": "2026-08-19T11:05:00Z",
  "status": 400,
  "error": "INVALID_INPUT",
  "message": "Location name is required",
  "path": "/locations"
}
```

**Verify**:
- Response status is 400
- Error code is `INVALID_INPUT`
- Message indicates name is required

### Step 2: Create Location with Whitespace-Only Name

```bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "   "}'
```

**Expected Response** (400 Bad Request):
```json
{
  "status": 400,
  "error": "INVALID_INPUT",
  "message": "Location name cannot be empty or whitespace-only",
  "path": "/locations"
}
```

**Verify**: Whitespace-only names rejected with clear message (FR-002 verified)

---

## Validation Scenario 7: User Isolation

**User Story**: System ensures user data isolation (FR-009)

**Goal**: Verify one user cannot access or delete another user's locations.

### Step 1: Create Location as User A

```bash
curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_A_TOKEN" \
  -d '{"name": "User A Private"}'
```

**Expected**: 201 Created with id=6, userId=42

### Step 2: Attempt to Access Location as User B

```bash
curl -X GET http://localhost:8080/locations/6 \
  -H "Authorization: Bearer $USER_B_TOKEN"
```

**Expected Response** (404 Not Found):
```json
{
  "status": 404,
  "error": "LOCATION_NOT_FOUND",
  "message": "Location not found",
  "path": "/locations/6"
}
```

**Verify**:
- User B cannot access User A's location (returns 404, not the location data)
- System enforces data isolation (FR-009 verified)

---

## Validation Scenario 8: Rename to Current Name (No-op)

**User Story**: Edge case from spec — User attempts rename to current name

**Goal**: Verify system accepts rename-to-self as harmless no-op.

### Step 1: Get Current Location Name

```bash
curl -X GET http://localhost:8080/locations/1 \
  -H "Authorization: Bearer $USER_TOKEN"
```

**Expected**: Returns location with name="Home Office"

### Step 2: Rename to Same Name

```bash
curl -X PUT http://localhost:8080/locations/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "Home Office"}'
```

**Expected Response** (200 OK): Returns location unchanged

**Verify**:
- Response status is 200 (not error)
- Location returned with same name
- No error for renaming to current name (edge case handled per spec)

---

## Performance Validation

**Goal**: Verify location operations meet performance targets (SC-001, SC-002)

### Measure Create Latency

```bash
time curl -X POST http://localhost:8080/locations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -d '{"name": "Performance Test"}'
```

**Expected**: Total time < 1 second

**Verify**: Create, list, rename operations complete within 1 second target

---

## Logging Verification

**Goal**: Verify all operations are logged for audit purposes (FR-009 clarification)

### Check Application Logs

```bash
tail -f backend/logs/application.log | grep -i location
```

**Expected log entries**:
- INFO: Location created: id={}, userId={}, name={}
- INFO: Location renamed: id={}, userId={}, oldName={}, newName={}
- INFO: Location deleted: id={}, userId={}, name={}
- WARN: Failed to create location: reason=...
- ERROR: Unexpected error in location operation

**Verify**: All operations produce appropriate log messages (FR-009 requirement met)

---

## Summary

This validation guide covers:
- ✅ Create location (User Story 1)
- ✅ List locations (User Story 1 verification)
- ✅ Rename location (User Story 2)
- ✅ Delete empty location (User Story 4, scenarios 1-2)
- ✅ Block delete non-empty location (User Story 4, scenario 3)
- ✅ Input validation (FR-002)
- ✅ Duplicate name rejection (FR-003, FR-005)
- ✅ User isolation (FR-009)
- ✅ Edge cases (rename-to-self)
- ✅ Performance targets (SC-001, SC-002)
- ✅ Logging (FR-009 clarification)

All core acceptance scenarios and success criteria are covered.

---

## Frontend UI Testing

### Prerequisites

1. Frontend running on `http://localhost:4200`
2. Backend running on `http://localhost:8080`
3. Authenticated user logged in (OAuth2 session established)
4. Chrome/Firefox browser with developer console access

### Validation Scenario FE-1: View Location List

**Goal**: Verify user can view all their locations in a list/table.

**Steps**:
1. Navigate to Locations page (e.g., `http://localhost:4200/locations`)
2. Verify page loads with title "My Locations" or similar
3. Verify table/list displays all user's locations with columns: Name, Created, Actions

**Expected**:
- ✅ Page loads without errors
- ✅ All existing locations displayed
- ✅ Each location shows name, create date, and action buttons (Rename, Delete)
- ✅ "Create Location" button visible

**Acceptance**: User Story 1 (verification step)

---

### Validation Scenario FE-2: Create Location via Form

**Goal**: Verify user can create a new location through the UI form.

**Steps**:
1. From location list, click "Create Location" button
2. Verify LocationFormComponent modal/dialog opens
3. Enter location name: "Garage"
4. Verify form has client-side validation (can't submit empty name)
5. Click "Create" button
6. Verify form closes and list refreshes
7. Verify new location "Garage" appears in the list

**Expected**:
- ✅ Modal opens with single input field for name
- ✅ Client validation prevents empty submission
- ✅ API call made to POST /locations
- ✅ Location appears in list after successful creation
- ✅ No console errors

**Acceptance**: User Story 1 (create via UI)

---

### Validation Scenario FE-3: Rename Location via Form

**Goal**: Verify user can rename a location through the UI.

**Steps**:
1. From location list, click "Rename" button next to a location
2. Verify LocationFormComponent opens with current name pre-filled
3. Clear the name field and enter new name: "Garage Storage"
4. Click "Update" button
5. Verify form closes and list refreshes
6. Verify location name updated in the list

**Expected**:
- ✅ Form modal opens with current name populated
- ✅ Submit button labeled "Update" (not "Create")
- ✅ API call made to PUT /locations/{id}
- ✅ Name updates in the list
- ✅ No console errors

**Acceptance**: User Story 2 (rename via UI)

---

### Validation Scenario FE-4: Delete Location with Confirmation

**Goal**: Verify user can delete an empty location with confirmation.

**Steps**:
1. From location list, click "Delete" button next to an empty location
2. Verify confirmation dialog appears: "Are you sure you want to delete '{name}'?"
3. Click "Cancel" in dialog
4. Verify dialog closes and location still in list
5. Click "Delete" button again
6. Click "Confirm" in dialog
7. Verify API call made to DELETE /locations/{id}
8. Verify location removed from list

**Expected**:
- ✅ Confirmation dialog displays location name
- ✅ Cancel button closes dialog without deletion
- ✅ Confirm button triggers delete
- ✅ Location disappears from list after deletion
- ✅ No console errors

**Acceptance**: User Story 4 (delete via UI)

---

### Validation Scenario FE-5: Delete Blocked for Non-Empty Location

**Goal**: Verify user sees error when trying to delete a location with items.

**Steps**:
1. From location list, locate a location with items assigned
2. Click "Delete" button
3. Verify confirmation dialog appears
4. Click "Confirm" in dialog
5. Verify error message displayed: "Cannot delete location with items. Please remove or reassign items first. (itemCount: X)"
6. Verify location still in list

**Expected**:
- ✅ Error message clearly shown to user
- ✅ Message includes item count
- ✅ Location not deleted
- ✅ User understands next action (remove items first)

**Acceptance**: User Story 4 (block deletion acceptance scenario)

---

### Validation Scenario FE-6: Duplicate Name Error

**Goal**: Verify user sees error when trying to create duplicate location name.

**Steps**:
1. From location list, click "Create Location"
2. Enter name "Home Office" (assuming this already exists)
3. Click "Create"
4. Verify error message displayed in form: "A location with this name already exists. Please choose a different name."
5. Verify form remains open
6. Enter different name "Home Studio"
7. Click "Create"
8. Verify form closes and new location added to list

**Expected**:
- ✅ Duplicate name error displayed in form
- ✅ Error is user-friendly and explains the issue
- ✅ Form remains open for user to correct
- ✅ User can submit with different name after error

**Acceptance**: FR-003, FR-005 (duplicate name prevention)

---

### Validation Scenario FE-7: Form Validation (Empty Name)

**Goal**: Verify client-side form validation prevents empty submissions.

**Steps**:
1. From location list, click "Create Location"
2. Leave name field empty
3. Attempt to click "Create" button
4. Verify button is disabled OR error message appears: "Location name is required"
5. Enter space characters only: "   "
6. Attempt to click "Create"
7. Verify error: "Location name cannot be empty or whitespace-only"

**Expected**:
- ✅ Empty name field prevents submission
- ✅ Whitespace-only name prevented
- ✅ Clear error messages guide user
- ✅ Real-time validation as user types

**Acceptance**: FR-002 (input validation)

---

### Validation Scenario FE-8: Loading States & Error Messages

**Goal**: Verify UI provides clear feedback during operations and on errors.

**Steps**:
1. From location list, click "Create Location"
2. Enter name "Test Location"
3. Click "Create" button
4. Verify button shows loading state (spinner or "Creating...")
5. Wait for response
6. If successful: Button returns to normal, form closes
7. If network error: Error message appears: "Unable to connect. Please check your connection and try again."

**Expected**:
- ✅ Loading state shown during API call
- ✅ User cannot click button multiple times
- ✅ Network errors handled gracefully
- ✅ User can retry on error

**Acceptance**: SC-006 (95% success rate + clear feedback on failure)

---

### Validation Scenario FE-9: List Refresh After Each Operation

**Goal**: Verify location list updates automatically after CRUD operations.

**Steps**:
1. Start with location list showing 3 locations
2. Create new location "New Location"
3. Verify list immediately shows 4 locations
4. Rename a location from "Office" to "Office Updated"
5. Verify list updates name without page refresh
6. Delete an empty location
7. Verify list removes the location

**Expected**:
- ✅ List updates after create (new item appears)
- ✅ List updates after rename (name changes in-place)
- ✅ List updates after delete (item removed)
- ✅ No manual page refresh required
- ✅ Smooth UI updates without flicker

**Acceptance**: SC-001, SC-002 (UI updates within 1 second)

---

### Validation Scenario FE-10: Responsive Design & Accessibility

**Goal**: Verify UI is responsive and accessible to keyboard users.

**Steps**:
1. Resize browser to mobile width (320px)
2. Verify location list still readable and usable
3. Verify buttons still clickable on mobile
4. From location list, press Tab key repeatedly
5. Verify focus moves through all buttons and links
6. Verify form labels have `<label>` tags associated with inputs
7. Test form with keyboard only (no mouse):
   - Tab to "Create" button, press Enter
   - Tab through form fields, Tab to "Create" button, press Enter
8. Verify keyboard navigation works end-to-end

**Expected**:
- ✅ Layout responsive (mobile, tablet, desktop)
- ✅ All interactive elements keyboard-accessible
- ✅ Tab order logical
- ✅ Form submission possible via keyboard
- ✅ No accessibility console warnings

**Acceptance**: WCAG 2.1 AA compliance basics

---

## Browser DevTools Verification

### Console Checks

1. Open browser DevTools (F12)
2. Go to Console tab
3. Perform location CRUD operations
4. Verify no errors/warnings in console (red X marks)
5. Expected: Only informational messages from Angular, no errors

### Network Tab Checks

1. Go to Network tab in DevTools
2. Create a location
3. Verify single POST /locations request
4. Verify 201 response status
5. Verify response includes location object
6. Perform rename
7. Verify single PUT /locations/{id} request
8. Verify 200 response status
9. Perform delete
10. Verify single DELETE /locations/{id} request
11. Verify 204 response status

---

## Summary: Full-Stack Validation

✅ **Backend API** (via curl/Postman):
- Create, read, list, rename, delete operations
- Error handling (409 duplicate, 404 not found, 409 has items)
- User isolation and authentication

✅ **Frontend UI** (via browser):
- Location list display
- Create/rename/delete forms
- Error messages and loading states
- Responsive design and accessibility
- List updates after operations

✅ **End-to-End**:
- User can manage locations via UI
- UI and backend stay in sync
- All acceptance scenarios verified
- Performance targets met
- Logging working on backend
