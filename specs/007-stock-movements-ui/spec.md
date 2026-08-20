# Feature Specification: Stock Movements UI

**Feature Branch**: `007-stock-movements-ui`

**Created**: 2026-08-20

**Status**: Draft

**Input**: Frontend specification for stock movements feature (backend API already implemented)

## Clarifications

### Session 2026-08-20

- Q: Which user roles or permission scopes should have access to record stock movements? → A: All authenticated users can record movements (no role restrictions beyond authentication).
- Q: Should movement forms remain open or auto-close after successful submission? → A: Auto-close the form after successful submission; show a brief success notification (e.g., toast) and return to item view.
- Q: How should the movement history view be accessed — inline on the item detail page or on a separate dedicated page? → A: Movement history opens in a modal/dialog when user clicks a "View History" button.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Item Details with Current Stock (Priority: P1)

Users need to see the current stock quantity for items on the item detail view. This shows the live inventory status and is the foundation for all stock movement operations.

**Why this priority**: P1 - Users cannot manage inventory without knowing the current stock level. This is essential for every inventory operation and decision.

**Independent Test**: Can be fully tested by navigating to an item detail page and verifying that the current quantity is displayed and reflects all applied stock movements.

**Acceptance Scenarios**:

1. **Given** a user is viewing an item detail page, **When** the page loads, **Then** the current quantity is displayed prominently, showing the accumulated result of all stock movements.
2. **Given** a stock movement is recorded via the backend API, **When** the user refreshes the item detail page, **Then** the current quantity reflects the change.
3. **Given** an item has never had a stock movement, **When** the user views the item, **Then** the current quantity displays as 0 or shows no movements.

---

### User Story 2 - Record Stock In Movement (Priority: P1)

Users need a UI form to record when stock is received (e.g., from supplier deliveries). This is the primary way inventory is replenished.

**Why this priority**: P1 - Recording stock in is a core daily operational workflow for inventory management.

**Independent Test**: Can be fully tested by submitting a stock in form and verifying the backend API is called correctly and the UI updates to reflect the new quantity.

**Acceptance Scenarios**:

1. **Given** a user is on an item detail page, **When** they click "Record Stock In" or similar action, **Then** a form appears with fields for quantity, reason/notes, and movement date.
2. **Given** the stock in form is displayed, **When** the user enters valid values (quantity > 0) and submits, **Then** the form calls the backend API, closes automatically, displays a brief success notification, returns to the item view, and the current quantity is updated on the page.
3. **Given** the stock in form is displayed, **When** the user enters invalid values (quantity = 0, negative, or non-numeric), **Then** the form shows validation errors and does not submit.
4. **Given** the form submission is in progress, **When** the user waits, **Then** a loading indicator is shown and the submit button is disabled.

---

### User Story 3 - Record Stock Out Movement (Priority: P1)

Users need a UI form to record when stock is removed (e.g., sales, disposal). This tracks inventory depletion and usage.

**Why this priority**: P1 - Recording stock out is essential for tracking how inventory is used and preventing stockouts.

**Independent Test**: Can be fully tested by submitting a stock out form with valid and invalid quantities and verifying backend validation is respected in the UI.

**Acceptance Scenarios**:

1. **Given** a user is on an item detail page, **When** they click "Record Stock Out" or similar action, **Then** a form appears with fields for quantity, reason/notes, and movement date.
2. **Given** an item with current quantity 100, **When** the user attempts to record a stock out of 150 units, **Then** the backend rejects it and the UI displays an error message (e.g., "Cannot stock out 150 units; current quantity is 100").
3. **Given** the stock out form is displayed with current quantity 100, **When** the user enters a valid quantity (≤ 100), submits, **Then** the form closes, a success notification appears, the current quantity updates on the page, and the user returns to the item view.
4. **Given** a stock out is submitted, **When** the backend validates it, **Then** any validation error from the backend is displayed to the user in the form.

---

### User Story 4 - Record Adjustment Movement (Priority: P1)

Users need a UI form to record inventory adjustments (e.g., physical count corrections, shrinkage). The direction (increase/decrease) must be explicit and clear.

**Why this priority**: P1 - Adjustments are critical for maintaining accurate inventory when discrepancies are discovered.

**Independent Test**: Can be fully tested by submitting adjustment forms with increase and decrease directions and verifying the backend is called correctly and the UI updates.

**Acceptance Scenarios**:

1. **Given** a user is on an item detail page, **When** they click "Record Adjustment" or similar action, **Then** a form appears with fields for quantity, adjustment direction (increase/decrease), reason/notes, and movement date.
2. **Given** an adjustment form with direction "decrease" and quantity 5, **When** the item has current quantity 10, and the user submits, **Then** the form closes, a success notification appears, the current quantity becomes 5 on the page, and the user returns to the item view.
3. **Given** an adjustment form with direction "decrease" and quantity 15, **When** the item has current quantity 10, and the user submits, **Then** the backend rejects it and the UI displays an error in the form (would make quantity negative).
4. **Given** an adjustment form, **When** the user selects direction "increase" and submits, **Then** the form closes, a success notification appears, the quantity is added to the current inventory, and the user returns to the item view.

---

### User Story 5 - View Movement History (Priority: P1)

Users need to see a complete audit trail of all stock movements for an item to understand how the current quantity was reached and verify inventory accuracy.

**Why this priority**: P1 - The audit trail is essential for inventory accountability, investigating discrepancies, and understanding inventory changes.

**Independent Test**: Can be fully tested by clicking the "View History" button, verifying the history modal opens, and checking that all movements are displayed with correct details and in correct order.

**Acceptance Scenarios**:

1. **Given** a user is viewing an item detail page, **When** they click a "View Movement History" or "View History" button, **Then** a modal dialog opens displaying all stock movements for that item.
2. **Given** the movement history modal is open with multiple movements (opening balance, stock in, adjustment), **When** the user views the history, **Then** all movements are displayed with: movement type, quantity, adjustment direction (if applicable), reason/notes, movement date, and created date.
3. **Given** a history modal with 3 movements, **When** the user views it, **Then** movements are displayed in chronological order (oldest first).
4. **Given** an item with no movements, **When** the user opens the history modal, **Then** an appropriate message is shown (e.g., "No movements recorded").
5. **Given** the movement history modal is open, **When** the user clicks close or outside the modal, **Then** the modal closes and the user returns to the item detail page.

---

### User Story 6 - Filter Movement History by Date Range (Priority: P2)

Users need to filter movement history by date range to focus on a specific time period for auditing or investigation.

**Why this priority**: P2 - Essential for auditing and investigation, but secondary to viewing complete history; users can scroll through all movements if needed.

**Independent Test**: Can be fully tested by opening the history modal, applying date filters, and verifying the API is called with correct parameters and only matching movements are displayed.

**Acceptance Scenarios**:

1. **Given** the movement history modal is open, **When** the user sees date filter fields (start date and end date), **Then** they can enter dates and apply the filter.
2. **Given** movements exist across different dates in the modal, **When** the user filters by a specific date range, **Then** only movements within that range are displayed.
3. **Given** a date range that contains no movements, **When** the user applies the filter in the modal, **Then** an appropriate message is shown (e.g., "No movements in this date range").

---

### Edge Cases

- What happens when a user attempts to submit a movement with an empty reason field? (Should be allowed; reason is optional)
- What if the backend API is temporarily unavailable when submitting a movement? (UI should show an error message; user can retry)
- What if two users simultaneously record movements for the same item? (Both should be accepted per backend; UI should refresh to show updated quantity)
- What if a user enters a future date for movement date? (Should be accepted per backend; backend allows backdated and future-dated movements)
- What if the quantity field receives non-numeric input? (Form should validate and prevent submission)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The UI MUST display the current quantity for an item on the item detail page.
- **FR-002**: The UI MUST provide a form to record stock in movements with fields: quantity (required), reason (optional), movement date (optional; defaults to today). The form MUST be accessible to all authenticated users.
- **FR-003**: The UI MUST provide a form to record stock out movements with fields: quantity (required), reason (optional), movement date (optional; defaults to today). The form MUST be accessible to all authenticated users.
- **FR-004**: The UI MUST provide a form to record adjustment movements with fields: quantity (required), adjustment direction (required; radio buttons or dropdown for increase/decrease), reason (optional), movement date (optional; defaults to today). The form MUST be accessible to all authenticated users.
- **FR-005**: The UI MUST validate that quantity is a positive integer (> 0) before submitting any movement form.
- **FR-006**: The UI MUST display backend validation errors when a movement is rejected (e.g., "Stock out would make quantity negative").
- **FR-007**: The UI MUST show a success notification (e.g., toast or snackbar) after a movement is successfully recorded, auto-close the movement form, return to the item view, and update the current quantity on the page.
- **FR-008**: The UI MUST provide a "View Movement History" button on the item detail page that opens a modal dialog displaying all movements for that item with: movement type, quantity, adjustment direction (if applicable), reason, movement date, and created date.
- **FR-009**: The UI MUST display movements in chronological order (oldest first) in the history modal.
- **FR-010**: The UI MUST provide optional date range filters (start date, end date) within the history modal to allow users to filter by movement date.
- **FR-011**: The UI MUST disable form submission buttons and show a loading indicator while a movement is being submitted.
- **FR-012**: The UI MUST NOT allow direct editing of the current quantity field; stock quantity changes ONLY through movement forms.
- **FR-013**: The UI MUST handle API errors gracefully and display user-friendly error messages without exposing technical details.

### Key Entities (UI Representation)

- **Item Detail View**: Displays item information including current quantity and action buttons to record movements or view history.
- **Movement Form**: Modal or inline form(s) to record stock in, stock out, or adjustment movements; auto-closes on success.
- **Movement History Modal**: Dialog/modal displaying all movements for an item with filtering capabilities; opens from "View History" button.
- **Movement Record**: Display of individual movement within the modal with all relevant fields (type, quantity, direction, reason, dates).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can record a stock in movement and see the current quantity update within 2 seconds of form submission.
- **SC-002**: All backend validation errors (e.g., negative quantity, stock out exceeds current quantity) are displayed to users in clear, actionable error messages.
- **SC-003**: Movement history displays all movements for an item with 100% accuracy (no missing or duplicate movements).
- **SC-004**: Date range filters work correctly, returning only movements within the specified range.
- **SC-005**: Form validation prevents submission of invalid data (e.g., quantity ≤ 0, non-numeric quantity, missing required fields).
- **SC-006**: Users can complete a typical stock movement workflow (view item, open form, enter data, submit) in under 1 minute.
- **SC-007**: UI is fully responsive and usable on desktop, tablet, and mobile devices.

## Assumptions

- The backend Stock Movements API (as defined in `/specs/006-stock-movements/contracts/stock-movement-api.md`) is fully implemented and available at `http://localhost:8080/api/v1`.
- User authentication and authorization are already in place; the UI will receive a valid bearer token via the existing auth system.
- The item detail page and related navigation structure already exist; this feature adds movement-related UI to existing pages.
- Movement date defaults to today's date if not explicitly provided by the user.
- The UI will use the existing item category and item structure; no changes to item entity are required for the frontend.
- Form submission uses standard HTTP POST/GET methods to call the backend API; no WebSocket or real-time synchronization is required.
- The UI should follow existing design patterns and styling conventions in the application.
- No pagination or virtualization is required for movement history (assumes reasonable number of movements per item, < 1000).
- Concurrent movement submissions by the same user are not expected; UI does not need to handle race conditions from the same session.
