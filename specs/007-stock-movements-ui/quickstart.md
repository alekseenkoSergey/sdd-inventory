# Quickstart Validation Guide: Stock Movements UI

**Date**: 2026-08-20
**Feature**: Stock Movements UI (007-stock-movements-ui)
**Goal**: Demonstrate that the Stock Movements UI feature works end-to-end

---

## Prerequisites

### Backend
- Spring Boot backend running at `http://localhost:8080`
- Stock Movements API (feature 006-stock-movements) implemented and available
- Sample item created with initial quantity

### Frontend
- Angular development server running at `http://localhost:4200`
- All dependencies installed: `npm install` in `frontend/` directory
- TypeScript compilation successful
- Unit and integration tests passing

### Browser
- Modern browser with development tools (Chrome, Firefox, Safari, Edge)
- JavaScript console accessible for debugging
- Network tab available to inspect API calls

### Data
- At least one item exists in the system
- Item has a known ID (e.g., 456 in examples)
- Item detail page is accessible and displays the item

---

## Setup & Initialization

### 1. Start Backend Server

```bash
cd backend
mvn spring-boot:run
```

**Expected**: Server starts on port 8080, logs show "Started SddInventoryApplication"

### 2. Start Frontend Development Server

```bash
cd frontend
npm start
```

**Expected**: Angular dev server starts on `http://localhost:4200`, browser opens

### 3. Navigate to Item Detail Page

1. Open browser to `http://localhost:4200`
2. Navigate to Items list
3. Click on any item to view its detail page
4. Verify page displays: item name, current quantity, action buttons

---

## Validation Scenarios

### Scenario 1: Display Current Quantity

**Objective**: Verify that the current quantity for an item is displayed correctly, reflecting all stock movements.

**Setup**: 
- Item exists with initial quantity (e.g., 100)
- At least one opening balance movement created

**Steps**:
1. Navigate to item detail page
2. Look for "Current Quantity" or similar label displaying the quantity value
3. Verify the displayed quantity matches the sum of all movements from history
4. Note the item ID (e.g., 456) for subsequent scenarios

**Success Criteria**:
- ✓ Current quantity is prominently displayed
- ✓ Value is numeric and matches backend state
- ✓ Quantity is read-only (no direct edit field visible)

**Example Screenshot Indicators**:
- Quantity displayed near item title or in a highlighted section
- Label like "Current Stock", "In Stock", or "Current Quantity"
- No editable input field for quantity

---

### Scenario 2: Record Stock In Movement

**Objective**: Verify that users can record a stock in movement via a form, and the UI updates correctly.

**Setup**:
- Item detail page is open with current quantity visible
- Current quantity is, say, 100

**Steps**:
1. Click "Record Stock In" button (or similar action button)
2. Verify a form appears (modal or inline) with fields:
   - Quantity (input field, required)
   - Reason (input field, optional)
   - Movement Date (date picker, optional, defaults to today)
3. Enter:
   - Quantity: `50`
   - Reason: `Supplier delivery order #12345`
   - Movement Date: today (or leave blank)
4. Click "Submit" or "Record" button
5. Observe:
   - Form submission button becomes disabled (loading state)
   - Loading spinner or indicator appears
6. Wait for response (should be < 2 seconds)
7. Observe:
   - Form closes or disappears
   - Toast or notification appears: "Stock in recorded" or "Movement recorded successfully"
   - Current quantity on item detail updates to 150 (100 + 50)
8. Open browser developer tools → Network tab
9. Verify POST request was made to `/api/v1/items/456/movements` with request body:
   ```json
   {
     "movementType": "STOCK_IN",
     "quantity": 50,
     "reason": "Supplier delivery order #12345",
     "movementDate": "2026-08-20"
   }
   ```

**Success Criteria**:
- ✓ Form appears when action button clicked
- ✓ Form has quantity, reason, date fields
- ✓ Form disables submit button during submission
- ✓ API call is made with correct payload
- ✓ Form closes on success
- ✓ Toast notification appears
- ✓ Current quantity updates on page (150)

---

### Scenario 3: Form Validation (Invalid Quantity)

**Objective**: Verify that the form validates quantity and prevents submission of invalid values.

**Setup**:
- Item detail page is open
- "Record Stock In" form is closed

**Steps**:
1. Click "Record Stock In" button
2. Form appears
3. In Quantity field, enter: `0` or `-50` or `abc`
4. Try to submit (click Submit button)
5. Observe:
   - Form does NOT submit to backend
   - Error message appears below quantity field: e.g., "Quantity must be greater than 0" or similar
   - Submit button remains enabled (not in loading state)
6. Verify API call was NOT made (check Network tab)
7. Clear quantity field and enter valid value: `25`
8. Submit form
9. Form closes, toast appears, quantity updates

**Success Criteria**:
- ✓ Invalid quantities are rejected (0, negative, non-numeric)
- ✓ Error message is displayed
- ✓ Form does not submit invalid data to backend
- ✓ User can correct and resubmit
- ✓ Valid submission works after correction

---

### Scenario 4: Record Stock Out Movement

**Objective**: Verify that users can record a stock out movement and that the current quantity decreases.

**Setup**:
- Item detail page is open with current quantity at least 50

**Steps**:
1. Click "Record Stock Out" button
2. Form appears with same fields as Stock In (Quantity, Reason, Movement Date)
3. Enter:
   - Quantity: `30`
   - Reason: `Sales order #67890`
   - Movement Date: today
4. Click "Submit"
5. Form closes, notification appears, current quantity decreases by 30
6. Verify: If quantity was 150, it's now 120

**Success Criteria**:
- ✓ Stock out form appears and submits successfully
- ✓ Current quantity decreases (subtracted)
- ✓ API call includes `"movementType": "STOCK_OUT"`

---

### Scenario 5: Stock Out Validation (Exceeds Current Quantity)

**Objective**: Verify that stock out rejects quantities that would make inventory negative.

**Setup**:
- Item detail page is open with current quantity = 50

**Steps**:
1. Click "Record Stock Out" button
2. Enter:
   - Quantity: `60` (exceeds current quantity of 50)
3. Click "Submit"
4. Backend rejects the request with 400 error
5. Form displays error message: "Stock out of 60 units would make quantity negative (current: 50)"
6. Form remains open, submit button re-enabled
7. Modify quantity to `40` (valid, ≤ 50)
8. Submit again
9. Form closes, notification appears, quantity updates to 10

**Success Criteria**:
- ✓ Backend rejects stock out that would make quantity negative
- ✓ Error message is displayed to user
- ✓ Form remains open for correction
- ✓ User can correct and resubmit successfully

---

### Scenario 6: Record Adjustment Movement

**Objective**: Verify that users can record adjustment movements with explicit increase/decrease direction.

**Setup**:
- Item detail page is open with current quantity = 100

**Steps**:
1. Click "Record Adjustment" button
2. Form appears with fields:
   - Quantity (input field)
   - Direction (radio buttons or dropdown: Increase / Decrease)
   - Reason (input field)
   - Movement Date (date picker)
3. Select Direction: `Increase`
4. Enter:
   - Quantity: `10`
   - Reason: `Physical count correction`
   - Movement Date: today
5. Click "Submit"
6. Form closes, notification appears, current quantity increases to 110
7. Click "View Movement History" to verify adjustment recorded

**Success Criteria**:
- ✓ Adjustment form includes direction field
- ✓ Direction is radio buttons or clear dropdown
- ✓ Form submits successfully with direction
- ✓ Current quantity updates correctly (increase)
- ✓ API call includes `"adjustmentDirection": "INCREASE"`

---

### Scenario 7: Adjustment Decrease Validation

**Objective**: Verify that adjustment decrease movements are validated and cannot make quantity negative.

**Setup**:
- Item detail page is open with current quantity = 20

**Steps**:
1. Click "Record Adjustment" button
2. Select Direction: `Decrease`
3. Enter:
   - Quantity: `25` (exceeds current quantity)
   - Reason: `Inventory shrinkage`
4. Click "Submit"
5. Error message appears: "Adjustment of 25 would make quantity negative (current: 20)"
6. Form remains open
7. Modify quantity to `15` (valid, ≤ 20)
8. Click "Submit"
9. Form closes, notification appears, current quantity decreases to 5

**Success Criteria**:
- ✓ Adjustment decrease validates against current quantity
- ✓ Error displayed if would make negative
- ✓ User can correct and resubmit
- ✓ Successful decrease updates quantity correctly

---

### Scenario 8: View Movement History (Modal)

**Objective**: Verify that users can open a modal to view complete movement history for an item.

**Setup**:
- Item detail page is open with at least 3 movements recorded (opening balance, stock in, adjustment, etc.)

**Steps**:
1. Click "View Movement History" button
2. Modal/dialog appears displaying list of all movements
3. Verify each movement shows:
   - Movement type (e.g., "Opening Balance", "Stock In", "Adjustment", "Stock Out")
   - Quantity (numeric value)
   - Direction (if adjustment: "Increase" or "Decrease")
   - Reason (if provided, or empty)
   - Movement Date (formatted date, e.g., "Aug 20, 2026")
   - Created Date (formatted datetime)
4. Verify movements are listed in chronological order (oldest first)
5. Click outside modal or click close button
6. Modal closes, user returns to item detail page

**Success Criteria**:
- ✓ Modal appears with list of movements
- ✓ All movement fields are displayed
- ✓ Movements are in chronological order (oldest first)
- ✓ Modal can be closed (close button or click outside)

---

### Scenario 9: Filter Movement History by Date

**Objective**: Verify that users can filter movement history by date range.

**Setup**:
- Item detail page is open with movements across multiple dates
- Movements span from Aug 1 to Aug 20

**Steps**:
1. Click "View Movement History" button
2. Modal shows all movements (e.g., 5 movements)
3. Locate date filter fields: "Start Date" and "End Date"
4. Enter:
   - Start Date: `2026-08-10`
   - End Date: `2026-08-15`
5. Click "Apply Filter" button
6. Wait for API call to complete
7. History list updates showing only movements between Aug 10 and Aug 15 (e.g., 2 movements)
8. Verify earlier and later movements are hidden
9. Click "Clear Filters" or reset dates
10. History updates showing all movements again

**Success Criteria**:
- ✓ Date filter fields are present in history modal
- ✓ Filter can be applied
- ✓ API call includes `startDate` and `endDate` parameters
- ✓ History list updates to show only filtered movements
- ✓ Filters can be cleared

---

### Scenario 10: History Modal with No Movements

**Objective**: Verify UI handles case when item has no movements or filtered result is empty.

**Setup**:
- Item with no movements recorded (edge case), or
- Apply date filter that matches no movements

**Steps**:
1. Click "View Movement History" button
2. Modal appears but list is empty
3. Verify message is displayed: e.g., "No movements recorded" or "No movements in this date range"

**Success Criteria**:
- ✓ Modal handles empty state gracefully
- ✓ User-friendly message is shown
- ✓ Modal does not crash or appear broken

---

### Scenario 11: API Error Handling

**Objective**: Verify that UI gracefully handles backend API errors.

**Setup**:
- Backend is running normally
- A form is open

**Steps**:
1. Simulate backend error (stop backend server temporarily, OR use network throttling to simulate timeout)
2. Fill form with valid data
3. Click "Submit"
4. Wait for timeout or error response
5. Observe:
   - Submit button becomes re-enabled
   - Error message appears: e.g., "Failed to save movement" or "Network error"
   - Form remains open (does not close)
6. Start backend again (if stopped)
7. Resubmit form
8. Form submits successfully

**Success Criteria**:
- ✓ Backend errors are caught and displayed
- ✓ Error message is user-friendly (no stack traces)
- ✓ Form remains open for user to retry
- ✓ Submit button is re-enabled for retry

---

## API Contract Verification (Developer Check)

### Verify Request Format

In browser developer tools → Network tab, inspect a POST request to `/api/v1/items/{itemId}/movements`:

**Expected Request Headers**:
```
POST /api/v1/items/456/movements HTTP/1.1
Content-Type: application/json
Authorization: Bearer [token]
```

**Expected Request Body** (Stock In example):
```json
{
  "movementType": "STOCK_IN",
  "quantity": 50,
  "reason": "Supplier delivery",
  "movementDate": "2026-08-20"
}
```

### Verify Response Format

**Expected Response (201 Created)**:
```json
{
  "id": 1001,
  "itemId": 456,
  "movementType": "STOCK_IN",
  "quantity": 50,
  "adjustmentDirection": null,
  "reason": "Supplier delivery",
  "movementDate": "2026-08-20",
  "createdDate": "2026-08-20T14:30:15Z",
  "itemCurrentQuantity": 150
}
```

**Expected Response Headers**:
```
HTTP/1.1 201 Created
Content-Type: application/json
```

### Verify Error Response

**Example 400 Bad Request**:
```json
{
  "error": "Stock out of 60 units would make quantity negative (current: 50)",
  "timestamp": "2026-08-20T14:30:15Z",
  "path": "/api/v1/items/456/movements"
}
```

---

## Performance Checks

### Form Submission Time

**Measure**: Time from clicking "Submit" to form close and toast appearance

**Target**: < 2 seconds (from spec SC-001)

**How to measure**:
1. Open browser DevTools → Performance tab
2. Record performance trace
3. Submit form
4. Stop recording
5. Measure: submission button click to form close event

### API Response Time

**Measure**: Time from POST request to response received

**Target**: Typically < 500ms on localhost; up to 2s on slow networks (acceptable)

**How to measure**:
1. Open DevTools → Network tab
2. Filter to XHR/Fetch
3. Submit form
4. Check "POST /movements" timing: look for "Time" column

---

## Full Workflow Test (End-to-End)

**Time**: ~5 minutes
**Steps**:

1. Navigate to item detail page (item #456)
2. Note current quantity (e.g., 100)
3. Record Stock In: +50 → quantity becomes 150
4. Record Stock Out: -30 → quantity becomes 120
5. Record Adjustment Increase: +5 → quantity becomes 125
6. Record Adjustment Decrease: -10 → quantity becomes 115
7. View Movement History modal
8. Verify 4 movements listed (stock in, stock out, 2 adjustments)
9. Verify current quantity on item detail = 115
10. Apply date filter to show only today's movements
11. Verify 4 movements still displayed (all are today)
12. Clear filters
13. Close history modal

**Expected Result**: All 4 movements recorded, current quantity = 115, history accurate

---

## Regression Checks

**Before considering the feature complete, verify**:
- [ ] Existing item detail page still functions (no breaking changes)
- [ ] Item creation still works (opening balance recorded automatically if applicable)
- [ ] Item list displays with correct quantities
- [ ] Navigation between pages is not broken
- [ ] Authentication/authorization still enforced
- [ ] Other features (item search, filtering) not affected

---

## Done When

- [x] All 11 scenarios pass
- [x] API contract verified (requests/responses match spec)
- [x] Performance targets met (< 2 seconds for form submission)
- [x] Error handling tested and working
- [x] Full end-to-end workflow successful
- [x] No regressions in existing features

