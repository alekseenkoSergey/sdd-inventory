# Feature Specification: Inventory Items User Interface

**Feature Branch**: `005-inventory-items-ui`

**Created**: 2026-08-20

**Status**: Draft

**Input**: Frontend specification for managing inventory items with full CRUD operations, stock management, categorization, and location tracking.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create New Inventory Item (Priority: P1)

A warehouse manager needs to quickly create new inventory items in the system. The creation flow should support setting initial quantity, assigning categories and locations, and optionally providing additional metadata like SKU and description.

**Why this priority**: Item creation is the foundational operation that enables all other inventory management workflows. Without this, users cannot track any inventory.

**Independent Test**: Can be fully tested by creating a new item with required fields (name, category, location, unit) and verifying it appears in the inventory list with the correct details.

**Acceptance Scenarios**:

1. **Given** a user is on the inventory items list page, **When** they click "Create New Item" button, **Then** a creation form modal or page opens with empty fields and focus on the name field
2. **Given** a user fills in all required fields (name, category, location, unit) and sets initial quantity to 100, **When** they click "Create", **Then** the item is created successfully and appears in the list with quantity 100
3. **Given** a user creates an item without specifying initial quantity, **When** the item is created, **Then** it appears in the list with current quantity of 0
4. **Given** a user attempts to create an item with an empty name field, **When** they click "Create", **Then** a validation error appears indicating the name is required
5. **Given** a user creates an item with a duplicate SKU (matching their existing items), **When** they submit the form, **Then** a validation error appears indicating the SKU already exists

---

### User Story 2 - View and Edit Item Details (Priority: P1)

A warehouse manager needs to view complete item information and edit item properties (name, description, SKU, category, location, unit, low-stock threshold). The system must prevent direct editing of stock quantity, which can only change through stock movements.

**Why this priority**: Item maintenance is essential for keeping inventory data accurate. Users must be able to correct mistakes and update organizational metadata without touching stock quantity.

**Independent Test**: Can be fully tested by opening an item detail view, editing various fields, and confirming updates are saved while stock quantity remains unmodifiable.

**Acceptance Scenarios**:

1. **Given** an inventory item exists, **When** a user clicks on the item in the list, **Then** a detail view or modal opens showing all item fields (name, description, SKU, category, location, current quantity, unit, low-stock threshold, status, created date, updated date)
2. **Given** a user is viewing item details, **When** they click an "Edit" button, **Then** an edit form appears with all editable fields pre-populated
3. **Given** a user is editing an item, **When** they modify the item name and save, **Then** the item name is updated and the detail view reflects the change
4. **Given** a user is editing an item, **When** they attempt to interact with the "Current Quantity" field, **Then** the field is read-only or not editable (visually disabled or hidden from edit form)
5. **Given** a user edits an item's category and saves, **When** the save completes, **Then** the item now belongs to the new category and the list view updates accordingly

---

### User Story 3 - Archive and Restore Items (Priority: P1)

A warehouse manager needs to mark items as archived when they are no longer active (discontinued, obsolete) without losing their historical data. Archived items should be visually distinct and users should be able to restore them if needed.

**Why this priority**: Items become inactive over time and need to be removed from the active view without permanent deletion. Archival provides clean organization while preserving history.

**Independent Test**: Can be fully tested by archiving an active item, verifying it no longer appears in the active items list, and then restoring it to confirm it reappears.

**Acceptance Scenarios**:

1. **Given** an active inventory item, **When** a user clicks an "Archive" button or option in the item detail view, **Then** the item's status changes to "Archived" and it is removed from the active items list
2. **Given** an archived item, **When** a user views the items list, **Then** archived items are either not visible by default or clearly marked as archived with visual distinction
3. **Given** a user has archived items, **When** they click a filter option for "Show Archived" or similar, **Then** archived items appear in the list with visual indication of their archived status
4. **Given** an archived item, **When** a user clicks a "Restore" button in the item detail view, **Then** the item's status changes back to "Active" and it reappears in the active items list

---

### User Story 4 - View Items List with Filtering and Pagination (Priority: P1)

A warehouse manager needs to see all their inventory items in a organized list view with the ability to filter by status, category, and navigate through multiple pages as the inventory grows.

**Why this priority**: Users need efficient access to their inventory across potentially hundreds of items. List view with filtering and pagination is essential for daily operations.

**Independent Test**: Can be fully tested by verifying the list displays items, pagination controls work, and status/category filters narrow the results correctly.

**Acceptance Scenarios**:

1. **Given** a user navigates to the inventory items section, **When** the page loads, **Then** a list of active inventory items is displayed with columns for: name, SKU, category, location, quantity, unit, low-stock threshold, status, and created date
2. **Given** a user is viewing the items list, **When** there are more than 20 items, **Then** pagination controls appear allowing the user to navigate to the next/previous page
3. **Given** a user is viewing the items list, **When** they select "ARCHIVED" in a status filter, **Then** the list refreshes to show only archived items
4. **Given** a user is viewing the items list, **When** they select a specific category from a category filter dropdown, **Then** the list refreshes to show only items in that category
5. **Given** a user has applied filters, **When** they click a "Clear Filters" button, **Then** all filters are cleared and the full active items list is displayed again

---

### User Story 5 - Delete Item Permanently (Priority: P2)

A warehouse manager needs the ability to permanently remove items from the system for cleanup (e.g., items created in error or completely obsolete with no need for historical data).

**Why this priority**: Item deletion is useful for housekeeping but less critical than core CRUD operations. Archival is preferred for most scenarios where history matters, so deletion is a secondary capability.

**Independent Test**: Can be fully tested by deleting an item, confirming a confirmation dialog, and verifying the item no longer exists in the system.

**Acceptance Scenarios**:

1. **Given** a user is viewing an item detail view, **When** they click a "Delete" button, **Then** a confirmation dialog appears asking "Are you sure you want to permanently delete this item?"
2. **Given** a confirmation dialog for item deletion is open, **When** the user confirms the deletion, **Then** the item is removed from the system and the user is returned to the items list
3. **Given** a user has deleted an item, **When** they search for or try to access that item, **Then** a "not found" result or message appears

---

### User Story 6 - Move Items Between Categories and Locations (Priority: P2)

A warehouse manager needs to reorganize inventory by reassigning items to different categories or locations when inventory is reorganized or restocked to new locations.

**Why this priority**: Organizational flexibility is valuable but less critical than core CRUD operations. This can be accomplished through the edit form as well.

**Independent Test**: Can be fully tested by editing an item's category and location fields and confirming the changes persist and reflect in the list view.

**Acceptance Scenarios**:

1. **Given** a user is editing an item, **When** they change the category dropdown to a different category, **Then** the category field updates to the new selection
2. **Given** a user is editing an item, **When** they change the location dropdown to a different location, **Then** the location field updates to the new selection
3. **Given** a user has changed an item's category and saved the edit, **When** they view the items list filtered by the original category, **Then** the item no longer appears in that category's filtered view
4. **Given** a user has changed an item's location, **When** they view the item in the list, **Then** the new location is displayed in the location column

---

### Edge Cases

- What happens when a user creates an item with initial quantity 0? → Item is created with current quantity 0 and no stock movement is automatically recorded
- What happens when a user attempts to archive an item that is already archived? → The system handles this gracefully (idempotent operation - either shows no change or displays a message)
- What happens when a user attempts to edit a field for an item that has been deleted by another user? → System returns a not-found error and returns the user to the items list
- What happens when a user attempts to move an item to a category/location owned by a different user? → System prevents this and shows a validation error (user data isolation)
- What happens when a user creates items very rapidly? → System handles concurrent requests gracefully without data loss or duplicate SKU errors

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: UI MUST display a list of inventory items belonging to the authenticated user with columns for: name, SKU, category, location, current quantity, unit, low-stock threshold, status, created date
- **FR-002**: UI MUST provide a "Create New Item" button that opens a form allowing users to enter: name (required), description (optional), SKU (optional), category (required), location (required), unit (required), low-stock threshold (optional), and initial quantity (optional)
- **FR-003**: UI MUST validate that item name is non-empty and display an inline error message if the user attempts to create/save without a name
- **FR-004**: UI MUST validate that initial quantity (if provided) is not negative and display an inline error message if invalid
- **FR-005**: UI MUST validate that low-stock threshold (if provided) is not negative and display an inline error message if invalid
- **FR-006**: UI MUST validate that SKU uniqueness per user and display an error message if the user attempts to use a SKU that already exists for them
- **FR-007**: UI MUST prevent users from directly editing the "Current Quantity" field in the edit form (field must be read-only or not present in edit mode)
- **FR-008**: UI MUST provide edit functionality allowing users to modify any item field except current quantity (name, description, SKU, category, location, unit, low-stock threshold)
- **FR-009**: UI MUST provide an "Archive" action that changes an item's status to "Archived"
- **FR-010**: UI MUST provide a "Restore" action (visible on archived items) that changes an item's status back to "Active"
- **FR-011**: UI MUST provide a "Delete" action with a confirmation dialog that permanently removes an item from the system
- **FR-012**: UI MUST display item details (all fields including created date and updated date) when a user clicks on an item or opens its detail view
- **FR-013**: UI MUST support pagination for the items list with page size of 20 items per page and navigation controls (next, previous, page jump)
- **FR-014**: UI MUST provide a status filter allowing users to view "Active" items, "Archived" items, or "All" items
- **FR-015**: UI MUST provide a category filter (dropdown or select) allowing users to filter items by category
- **FR-016**: UI MUST provide a "Clear Filters" button or control to reset all applied filters
- **FR-017**: UI MUST display category and location as readable names (not just IDs) in both the list view and detail view
- **FR-018**: UI MUST populate category and location dropdowns from the backend API dynamically so users only see categories and locations they own
- **FR-019**: UI MUST display visual distinction for archived items (e.g., strikethrough, different text color, "Archived" badge) in the list and detail views
- **FR-020**: UI MUST show a loading state while data is being fetched from the API (spinner, skeleton, or "Loading..." message)
- **FR-021**: UI MUST display appropriate error messages when API calls fail (e.g., "Failed to load items", "Failed to save item", with option to retry)
- **FR-022**: UI MUST automatically update the list view after create, edit, archive, restore, or delete operations without requiring a full page refresh
- **FR-023**: UI MUST enforce user data isolation - users can only view, edit, archive, restore, and delete their own items (no ability to access other users' items through the UI)
- **FR-024**: UI MUST populate and display all date fields (created date, updated date) in a human-readable format (e.g., "Aug 20, 2026 2:30 PM")

### Key Entities Displayed

- **InventoryItem**: Represented in the UI with fields: id, name, description, SKU, category (name and ID), location (name and ID), current quantity, unit, low-stock threshold, status (ACTIVE/ARCHIVED), createdDate, updatedDate
- **Category**: Represented as a dropdown/select with category names and IDs; filters items in the list
- **Location**: Represented as a dropdown/select with location names and IDs; filters items in the list

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a new inventory item in under 60 seconds (from clicking "Create New" to confirmation)
- **SC-002**: Users can view a paginated list of up to 1000+ items with filter/search completing in under 2 seconds
- **SC-003**: Item edit operations save and reflect changes in the UI within 1 second of clicking "Save"
- **SC-004**: Archive/restore operations are responsive (under 1 second) and idempotent - users can repeat the action without errors
- **SC-005**: User data isolation is 100% enforced - no item belonging to another user is ever visible or accessible through the UI
- **SC-006**: All form validation rules (non-empty name, non-negative quantities/thresholds, SKU uniqueness) are consistently applied across create and edit flows
- **SC-007**: Archived items are clearly visually distinguished from active items and remain accessible via filter
- **SC-008**: 95% of users can complete the primary workflows (create, edit, archive, list) without assistance on first attempt

## Assumptions

- **Authentication is already implemented**: The UI assumes a user is authenticated and the authenticated user context is available (e.g., via JWT token, session cookie, or framework-provided auth state)
- **Backend API is available**: The REST API endpoints as specified in the inventory-items-api.md contract are fully implemented and operational
- **Categories and Locations already exist**: Users have already created categories and locations through a separate feature. The UI fetches these dynamically from the API
- **Responsive design is required**: The UI should be usable on desktop browsers (1920x1080 minimum) and tablets (iPad-like screens). Mobile optimization is out of scope for v1
- **No real-time multi-user updates**: The UI does not require real-time synchronization when other users modify shared categories/locations. Page refresh shows latest data
- **Standard REST client library available**: The project has access to a modern HTTP client (e.g., fetch API, axios, or framework-provided HTTP module) for API communication
- **State management pattern follows project conventions**: The UI uses the project's established state management approach (e.g., React Context, Redux, Zustand, or direct component state)
- **Error handling follows project patterns**: Error messages and retry logic follow the project's established error handling and UX patterns
- **SKU uniqueness is per-user**: When a SKU is provided during create/edit, it must be unique within that user's inventory only (not globally unique)
- **Date/time display uses server-sent timestamps**: All date fields (created date, updated date) are server-generated using the server's timezone, not client time
- **No soft deletes in UI**: Delete operations are permanent; deleted items do not appear in any historical or archived view
- **Validation happens both client-side and server-side**: The UI performs front-end validation for UX responsiveness, but trusts the server to enforce final validation before persisting data
