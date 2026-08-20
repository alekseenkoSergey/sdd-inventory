# Quickstart: Inventory Items UI Validation

**Date**: 2026-08-20  
**Feature**: Inventory Items User Interface  
**Purpose**: End-to-end validation scenarios to prove the feature works as specified

---

## Overview

This document describes runnable validation scenarios that exercise the full feature. Each scenario is independently testable and proves a specific user story works end-to-end.

### Prerequisites (Setup)

Before running any scenario, verify:

1. **Backend API running**: 
   - Endpoint: `http://localhost:8080/api/v1/inventory-items`
   - Should return 401 if unauthenticated (expect auth guard)

2. **Frontend running**:
   - Endpoint: `http://localhost:4200`
   - Angular dev server or production build

3. **User authenticated**:
   - Navigate to login page if required
   - Obtain valid JWT token or session
   - Token must be sent with all API requests (Authorization: Bearer {token})

4. **Test data available**:
   - At least 1 Category created in backend
   - At least 1 Location created in backend
   - Clear/empty inventory items list (start fresh for predictable results)

5. **Browser**:
   - Modern browser (Chrome 120+, Firefox 115+, Safari 17+)
   - Desktop or tablet (mobile not required for v1)

**Setup Commands** (from repository root):

```bash
# Terminal 1: Start backend (if not running)
cd backend
./mvnw spring-boot:run

# Terminal 2: Start frontend dev server
cd frontend
npm start
# Frontend will be available at http://localhost:4200

# Verify backend is accessible
curl -H "Authorization: Bearer {your-token}" \
  http://localhost:8080/api/v1/inventory-items
# Should return: {"content":[],"totalPages":0,...} or similar
```

---

## Scenario 1: Create Inventory Item with Initial Quantity

**User Story**: Create New Inventory Item (P1)  
**Acceptance Criteria**: FR-002, FR-003, FR-004

**Goal**: Verify that a user can create an item with initial quantity and it appears in the list.

### Steps

1. **Navigate to Inventory Items page**
   - Open browser to `http://localhost:4200/inventory/items`
   - Expected: List page loads with empty or existing items list

2. **Open Create Form**
   - Click "Create New Item" button
   - Expected: Modal or new page opens with empty form
   - Expected: Name field has focus
   - Expected: Current quantity field not visible (read-only)

3. **Fill Create Form**
   - **Name**: "Widget A" (required)
   - **Category**: Select first category from dropdown
   - **Location**: Select first location from dropdown
   - **Unit**: "pcs" (required)
   - **Description**: "Premium widget variant" (optional)
   - **SKU**: "SKU-001" (optional)
   - **Low-Stock Threshold**: 10 (optional, defaults to 0)
   - **Initial Quantity**: 100 (optional, defaults to 0)

4. **Validate Form**
   - Expected: All required fields populated
   - Expected: Submit button is enabled

5. **Submit Form**
   - Click "Create" button
   - Expected: Loading spinner appears
   - Expected: Modal closes after 1-2 seconds
   - Expected: List page displays and refreshes

6. **Verify Item Created**
   - Expected: "Widget A" appears in list with:
     - Name: "Widget A"
     - SKU: "SKU-001"
     - Category: Category name (not ID)
     - Location: Location name (not ID)
     - Quantity: 100 (not 0)
     - Unit: "pcs"
     - Status: "ACTIVE" (or green badge)
   - Expected: Created date shows today

7. **Verify Opening Balance**
   - Backend check: Query stock movements table
   - Expected: One stock movement record exists for this item with type="OPENING_BALANCE", quantity=100

### Expected Result

✅ **PASS** — Item created with initial quantity 100, appears in list, opening balance recorded

---

## Scenario 2: Create Item Without Initial Quantity

**User Story**: Create New Inventory Item (P1)  
**Acceptance Criteria**: FR-002, FR-003

**Goal**: Verify that an item created without initial quantity has 0 quantity and no stock movement.

### Steps

1. **Open Create Form** (same as Scenario 1, step 2)

2. **Fill Create Form** (partially)
   - **Name**: "Widget B"
   - **Category**: Select first category
   - **Location**: Select first location
   - **Unit**: "kg"
   - Leave **Initial Quantity** empty (defaults to 0)

3. **Submit Form**
   - Click "Create"
   - Expected: Item created successfully

4. **Verify Item in List**
   - Expected: "Widget B" appears with Quantity: 0
   - Expected: No opening balance was created (backend check: no stock movement)

### Expected Result

✅ **PASS** — Item created with 0 quantity, no stock movement recorded

---

## Scenario 3: View Item Details

**User Story**: View and Edit Item Details (P1)  
**Acceptance Criteria**: FR-012

**Goal**: Verify that clicking an item shows all details including read-only current quantity.

### Steps

1. **Click Item in List**
   - Click on "Widget A" (from Scenario 1)
   - Expected: Detail view modal or page opens
   - Expected: All fields displayed: name, description, SKU, category, location, quantity, unit, threshold, status, created date, updated date

2. **Verify Detail Display**
   - Expected: Name: "Widget A"
   - Expected: Quantity: 100 (read-only, styling differs from editable fields)
   - Expected: Status: "ACTIVE" (green badge or label)
   - Expected: Created Date: Today in human-readable format (e.g., "Aug 20, 2026, 2:30 PM")
   - Expected: Category displays as name, not ID

3. **Close Detail View**
   - Click "Close" or "Back" button
   - Expected: Returns to list

### Expected Result

✅ **PASS** — Detail view shows all fields, current quantity is read-only, dates are human-readable

---

## Scenario 4: Edit Item Fields

**User Story**: View and Edit Item Details (P1)  
**Acceptance Criteria**: FR-007, FR-008

**Goal**: Verify that edit form allows changing fields except current quantity.

### Steps

1. **Open Item Detail** (from Scenario 3)

2. **Click Edit Button**
   - Expected: Edit form opens (modal or page)
   - Expected: Form fields pre-populated with current values:
     - Name: "Widget A"
     - Unit: "pcs"
     - Quantity field: NOT present or disabled (read-only)
   - Expected: Submit button says "Save" or "Update"

3. **Edit Fields**
   - **Name**: Change to "Widget A Updated"
   - **Description**: Change to "Updated description"
   - **Low-Stock Threshold**: Change to 20
   - **Unit**: Change to "boxes"
   - **Current Quantity**: Attempt to click → Expected: Field disabled or not visible

4. **Verify Current Quantity is Read-Only**
   - Expected: Current Quantity field is visually disabled (grayed out) or absent from edit form
   - Expected: Cannot type in this field

5. **Save Changes**
   - Click "Save" button
   - Expected: Loading state appears
   - Expected: Form closes after 1-2 seconds

6. **Verify Changes in List**
   - Expected: List refreshes
   - Expected: "Widget A Updated" appears with:
     - Name: "Widget A Updated"
     - Unit: "boxes"
     - Quantity: 100 (unchanged)
   - Expected: Updated date changed to today

### Expected Result

✅ **PASS** — Edit form saves changes, current quantity remains unmodified, updated date reflects change

---

## Scenario 5: Edit Fails with Empty Name

**User Story**: View and Edit Item Details (P1)  
**Acceptance Criteria**: FR-003

**Goal**: Verify that validation prevents saving with empty name.

### Steps

1. **Open Edit Form** (from Scenario 4)

2. **Clear Name Field**
   - Clear the name field (make it empty)
   - Click somewhere else or blur the field
   - Expected: Error message appears: "Name is required"

3. **Attempt to Save**
   - Click "Save" button
   - Expected: Button is disabled or save doesn't proceed
   - Expected: Form remains open
   - Expected: Error message still visible

4. **Fix Validation**
   - Re-enter name: "Widget A Fixed"
   - Expected: Error message disappears
   - Expected: Save button is enabled

5. **Save Successfully**
   - Click "Save"
   - Expected: Form closes, changes saved

### Expected Result

✅ **PASS** — Validation prevents save with empty name, error message displays, can fix and retry

---

## Scenario 6: Create Item with Duplicate SKU

**User Story**: Create New Inventory Item (P1)  
**Acceptance Criteria**: FR-006

**Goal**: Verify that SKU uniqueness per user is enforced.

### Steps

1. **Open Create Form** (from Scenario 1, but different item)

2. **Fill Form with Duplicate SKU**
   - **Name**: "Widget C"
   - **SKU**: "SKU-001" (same as Widget A from Scenario 1)
   - Other required fields: category, location, unit

3. **Submit Form**
   - Click "Create"
   - Expected: Form submission fails
   - Expected: Error message: "SKU already exists for this user" or similar
   - Expected: Form remains open

4. **Fix SKU**
   - Change SKU to "SKU-002" (unique)

5. **Save Successfully**
   - Click "Create"
   - Expected: Item created

### Expected Result

✅ **PASS** — Duplicate SKU rejected, error message shown, can retry with unique SKU

---

## Scenario 7: Archive and Restore Item

**User Story**: Archive and Restore Items (P1)  
**Acceptance Criteria**: FR-009, FR-010

**Goal**: Verify that items can be archived and restored.

### Steps

1. **Open Item Detail** (Widget A)
   - Click "Widget A" in list

2. **Archive Item**
   - Click "Archive" button
   - Expected: Loading state appears
   - Expected: Detail view updates
   - Expected: Status changes to "ARCHIVED" (gray badge or label)
   - Expected: Archive button changes to "Restore" or disappears

3. **Verify Item in List**
   - Close detail view
   - Expected: "Widget A" still visible in list but with ARCHIVED status
   - Expected: Visual distinction (strikethrough, gray text, etc.)

4. **Filter by Archived**
   - Set status filter to "ARCHIVED"
   - Click filter/apply
   - Expected: List refreshes and shows only archived items
   - Expected: "Widget A" visible

5. **Restore Item**
   - Click "Widget A" in list
   - Click "Restore" button
   - Expected: Loading state
   - Expected: Status changes back to "ACTIVE"

6. **Verify Restored**
   - Close detail view
   - Set status filter to "ACTIVE"
   - Click filter/apply
   - Expected: "Widget A" appears with ACTIVE status

### Expected Result

✅ **PASS** — Archive/restore transitions work, list updates, visual distinction applied, filtering works

---

## Scenario 8: List Pagination and Filtering

**User Story**: View Items List with Filtering and Pagination (P1)  
**Acceptance Criteria**: FR-013, FR-014, FR-015, FR-016

**Goal**: Verify pagination and filtering work correctly.

### Steps

1. **Create 25+ Items** (bulk setup)
   - Use create form multiple times or API directly to create 25+ items
   - Distribute across 2-3 different categories

2. **Open List View**
   - Navigate to `/inventory/items`
   - Expected: Page 1 loads with 20 items (default page size)
   - Expected: Pagination controls visible (next, page number, etc.)

3. **Test Pagination**
   - Click "Next" button
   - Expected: Page 2 loads with remaining items (5+)
   - Expected: "Previous" button now enabled
   - Click "Previous"
   - Expected: Back to page 1

4. **Test Status Filter**
   - Set status filter to "ARCHIVED" (from Scenario 7)
   - Click "Apply Filter" or auto-apply
   - Expected: List refreshes
   - Expected: Only archived items visible (Widget A if still archived)
   - Expected: Page counter resets to page 1

5. **Test Category Filter**
   - Set status filter back to "ACTIVE" or "All"
   - Set category filter to first category
   - Expected: List shows only items in that category
   - Expected: Pagination updates to reflect filtered results

6. **Test Clear Filters**
   - Click "Clear Filters" button
   - Expected: All filters reset
   - Expected: List shows all active items again
   - Expected: Pagination resets to page 1

### Expected Result

✅ **PASS** — Pagination works (20 items/page, next/previous), status/category filters work, clear filters resets view

---

## Scenario 9: Delete Item with Confirmation

**User Story**: Delete Item Permanently (P2)  
**Acceptance Criteria**: FR-011

**Goal**: Verify that delete requires confirmation and permanently removes item.

### Steps

1. **Create Test Item** (if needed)
   - Create item: "Widget D" (for deletion testing)

2. **Open Item Detail**
   - Click "Widget D"
   - Expected: Detail view opens

3. **Click Delete Button**
   - Click "Delete" button
   - Expected: Confirmation modal appears
   - Expected: Message: "Are you sure you want to permanently delete this item?"
   - Expected: Confirm and Cancel buttons

4. **Cancel Deletion** (first attempt)
   - Click "Cancel"
   - Expected: Modal closes
   - Expected: Item still in list (not deleted)

5. **Delete Item** (second attempt)
   - Click "Delete" again
   - Modal appears
   - Click "Confirm Delete"
   - Expected: Loading state
   - Expected: Modal closes
   - Expected: Returned to list

6. **Verify Deletion**
   - Expected: "Widget D" no longer in list
   - Try to navigate directly to item (if known ID): `GET /api/v1/inventory-items/{id}`
   - Expected: 404 response (not found)

### Expected Result

✅ **PASS** — Delete requires confirmation, cancellation reverts, confirmed delete removes item permanently

---

## Scenario 10: Move Item to Different Category

**User Story**: Move Items Between Categories and Locations (P2)  
**Acceptance Criteria**: FR-008, FR-009 (accomplished via edit)

**Goal**: Verify that changing category/location updates item and appears in filtered views.

### Steps

1. **Note Original Category**
   - Click "Widget A"
   - Note current category (e.g., "Supplies")

2. **Open Edit Form**
   - Click "Edit"

3. **Change Category**
   - Category dropdown: Select a different category (e.g., "Equipment")
   - Click "Save"
   - Expected: Form closes, detail updates

4. **Verify Category Changed**
   - Detail view should show new category
   - Expected: "Equipment" instead of "Supplies"

5. **Verify in Filtered List**
   - Close detail view
   - Set category filter to "Supplies"
   - Expected: "Widget A" no longer appears (moved to different category)
   - Set category filter to "Equipment"
   - Expected: "Widget A" appears in this category

### Expected Result

✅ **PASS** — Category change persists, item appears in new category's filtered view, original category no longer contains item

---

## Scenario 11: Error Handling - API Failure

**User Story**: All (affects error messaging)  
**Acceptance Criteria**: FR-021

**Goal**: Verify that API errors are handled gracefully with retry option.

### Steps

1. **Stop Backend API** (simulate failure)
   - Kill or pause the backend server
   - Frontend still running

2. **Attempt List Reload**
   - Refresh page or click "Refresh" button
   - Expected: Loading spinner appears
   - Expected: After 2-3 seconds, error message appears
   - Expected: "Failed to load items" or similar message
   - Expected: "Retry" button visible

3. **Restart Backend API**
   - Restart the backend server

4. **Click Retry**
   - Click "Retry" button
   - Expected: Loading spinner
   - Expected: List loads successfully
   - Expected: Error message disappears

### Expected Result

✅ **PASS** — API errors display user-friendly messages, retry button recovers from failure

---

## Scenario 12: Form Validation - Negative Quantities

**User Story**: Create/Edit Item (P1)  
**Acceptance Criteria**: FR-004, FR-005

**Goal**: Verify that negative quantities/thresholds are rejected.

### Steps

1. **Open Create Form**

2. **Enter Negative Initial Quantity**
   - Initial Quantity: "-10"
   - Blur the field (click elsewhere)
   - Expected: Error message: "Initial quantity must not be negative"

3. **Enter Negative Low-Stock Threshold**
   - Low-Stock Threshold: "-5"
   - Blur the field
   - Expected: Error message: "Low-stock threshold must not be negative"

4. **Fix Values**
   - Initial Quantity: "50"
   - Low-Stock Threshold: "5"
   - Expected: Error messages disappear

5. **Submit Successfully**
   - Fill required fields and submit
   - Expected: Form submits (validation passed)

### Expected Result

✅ **PASS** — Negative values rejected with clear error messages, form submits after correction

---

## Scenario 13: Load and Display States

**User Story**: All  
**Acceptance Criteria**: FR-020

**Goal**: Verify that loading and empty states display appropriately.

### Steps

1. **Empty List State**
   - Create a new user (if possible) or clear all items
   - Navigate to `/inventory/items`
   - Expected: List page loads
   - Expected: Empty state message or empty table visible
   - Expected: "Create New Item" button still accessible

2. **Loading State**
   - While list is loading (initial load), open browser DevTools Network tab
   - Slow down network (3G throttle)
   - Navigate to list page
   - Expected: Loading spinner or skeleton loaders appear
   - Expected: List renders after API response completes

3. **After Load**
   - Expected: Spinner disappears
   - Expected: Items display
   - Expected: Pagination controls visible if >20 items

### Expected Result

✅ **PASS** — Empty state handled, loading spinner shown during load, content displays after

---

## Summary of Validation Scenarios

| Scenario | User Story | Coverage |
|----------|-----------|----------|
| 1. Create with quantity | Create Item (P1) | FR-002, initial quantity handling |
| 2. Create without quantity | Create Item (P1) | Default 0 quantity, no stock movement |
| 3. View details | View/Edit (P1) | All fields displayed, read-only quantity |
| 4. Edit fields | View/Edit (P1) | Field updates, quantity protected |
| 5. Edit validation | View/Edit (P1) | Name validation, error display |
| 6. Duplicate SKU | Create Item (P1) | SKU uniqueness enforcement |
| 7. Archive/Restore | Archive/Restore (P1) | Status transitions, visual distinction |
| 8. Pagination/Filter | List (P1) | Pages, status filter, category filter |
| 9. Delete | Delete (P2) | Confirmation modal, permanent removal |
| 10. Move category | Reorganize (P2) | Category change, filter updates |
| 11. Error handling | All | Error messages, retry |
| 12. Validation | Create/Edit | Negative quantities rejected |
| 13. Loading states | All | Spinner, empty state |

**Total Coverage**: 13 scenarios covering all 6 user stories (P1+P2)

---

## Running Scenarios

### Option 1: Manual Testing
1. Follow step-by-step instructions above
2. Verify each expected result
3. Document any deviations

### Option 2: Automated Testing (Cypress/Playwright)
```bash
# Once implementation is complete, create e2e tests matching these scenarios
npm run e2e -- --spec "cypress/e2e/inventory-items.spec.ts"
```

### Option 3: API Contract Testing
```bash
# Verify backend responses match expected shapes
# See contracts/inventory-items-api.md for endpoint details
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/inventory-items
```

---

## Success Criteria

Feature is considered **ready for release** when:

✅ All 13 scenarios pass without errors  
✅ Performance meets targets (create <60s, list <2s, edit <1s)  
✅ No data loss or corruption  
✅ User isolation enforced (no cross-user data visible)  
✅ Error messages are clear and actionable  
✅ Pagination and filtering work correctly  
✅ Archive/restore transitions are smooth  
✅ Form validation prevents invalid submissions  

---

## Related Documents

- **Specification**: [spec.md](./spec.md)
- **Data Model**: [data-model.md](./data-model.md)
- **API Contract**: [contracts/inventory-items-api.md](./contracts/inventory-items-api.md)
- **Implementation Plan**: [plan.md](./plan.md)
