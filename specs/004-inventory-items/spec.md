# Feature Specification: Inventory Items Management

**Feature Branch**: `004-inventory-items`

**Created**: 2026-08-20

**Status**: Draft

**Input**: Backend specification for inventory item management with support for stock movements, categorization, and location tracking.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create Inventory Item with Opening Balance (Priority: P1)

A warehouse manager needs to create new inventory items in the system and optionally set an initial opening balance. When an item is created with an initial quantity greater than 0, the system must automatically record this as an opening balance stock movement so the user doesn't need to manually create separate transactions.

**Why this priority**: This is the fundamental operation for establishing inventory in the system. Without this capability, users cannot track any inventory items.

**Independent Test**: Can be fully tested by creating an item with initial quantity and verifying the item exists with correct quantity and an opening balance stock movement is created.

**Acceptance Scenarios**:

1. **Given** a user has permission to create items, **When** they create an item with name "Widget A", quantity 100, and unit "pcs", **Then** the item is created with current_quantity=100 and an opening balance stock movement is automatically recorded
2. **Given** a user creates an item without specifying initial quantity, **When** the item is created, **Then** the item is created with current_quantity=0 and no stock movement is recorded
3. **Given** a user creates an item with required fields (name, category, location, unit), **When** all fields are valid, **Then** the item is created successfully

---

### User Story 2 - Edit Item Fields (Excluding Stock Quantity) (Priority: P1)

A warehouse manager needs to update item information such as description, SKU, category, location, or low-stock threshold. However, stock quantity must never be edited directly—it can only be changed through stock movements.

**Why this priority**: Item maintenance is essential for keeping inventory data accurate and consistent. Preventing direct quantity edits ensures stock audit trails remain trustworthy.

**Independent Test**: Can be fully tested by editing various item fields and verifying updates are applied, while confirming stock quantity cannot be directly modified.

**Acceptance Scenarios**:

1. **Given** an existing inventory item, **When** a user edits the description field, **Then** the item is updated and the change is persisted
2. **Given** an existing item with quantity 50, **When** a user attempts to edit the current_quantity field directly, **Then** the system rejects this operation with an appropriate error
3. **Given** an item in one category, **When** a user moves it to another category, **Then** the item's category is updated and quantity remains unchanged

---

### User Story 3 - Archive and Restore Items (Priority: P1)

A warehouse manager needs to archive items that are no longer in active use (discontinued, obsolete) but may need to retain the historical data. Items should be archivable and restorable.

**Why this priority**: Inventory often includes items that become inactive. Archiving provides a way to maintain historical records without cluttering the active inventory view, while allowing restoration if needed.

**Independent Test**: Can be fully tested by archiving an active item, verifying it's marked as archived, and then restoring it.

**Acceptance Scenarios**:

1. **Given** an active inventory item, **When** a user archives it, **Then** the item's status changes to "Archived" and it no longer receives new stock movements
2. **Given** an archived item, **When** a user receives a new stock movement request for it, **Then** the system rejects the operation
3. **Given** an archived item, **When** a user restores it, **Then** the item's status changes back to "Active" and stock movements can be recorded again

---

### User Story 4 - Move Items Between Categories and Locations (Priority: P2)

A warehouse manager needs to reorganize inventory by moving items to different categories or locations. This operation should update the item's metadata without affecting stock quantity.

**Why this priority**: Organizational flexibility is important for business operations, but less critical than core creation and archival functionality.

**Independent Test**: Can be fully tested by moving an item to a different location and verifying the location updates without affecting quantity.

**Acceptance Scenarios**:

1. **Given** an item in Location A, **When** a user moves it to Location B, **Then** the item's location is updated and current_quantity remains unchanged
2. **Given** an item assigned to Category X, **When** a user reassigns it to Category Y, **Then** the item's category is updated

---

### User Story 5 - Delete Inventory Item (Priority: P3)

A warehouse manager needs to completely remove items from the system, for example if an item was created in error or is completely obsolete and historical data is not needed.

**Why this priority**: Item deletion is necessary for housekeeping but should be used carefully. Archival is preferred for most scenarios where history matters.

**Independent Test**: Can be fully tested by deleting an item and confirming it no longer exists in the system.

**Acceptance Scenarios**:

1. **Given** an existing inventory item, **When** a user deletes it, **Then** the item is removed from the system entirely
2. **Given** a deleted item, **When** a user attempts to reference it, **Then** the system returns a not-found error

---

### Edge Cases

- What happens when an item is created with an initial quantity of 0? → Item is created with quantity 0 and no stock movement is recorded
- What happens when a user attempts to archive an item that is already archived? → The system handles this gracefully (idempotent operation or informative error)
- What happens when a user attempts to edit a field for an item that no longer exists? → System returns not-found error
- What happens when a user attempts to move an item to the same category/location? → Operation succeeds without error (idempotent)
- What happens when editing an item's category or location to a category/location owned by a different user? → System should prevent this to maintain user data isolation

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST create inventory items with the following fields: name, description/notes, SKU (optional), category (required), location (required), unit (required), low-stock threshold, current quantity, status (Active/Archived), created date, updated date
- **FR-002**: System MUST automatically create an opening balance stock movement when an item is created with initial quantity > 0
- **FR-003**: System MUST initialize items with current_quantity = 0 when no initial quantity is provided during creation
- **FR-004**: System MUST allow editing of any item field except current_quantity directly (quantity changes must go through stock movements)
- **FR-005**: System MUST prevent direct modification of current_quantity field through item edit operations
- **FR-006**: System MUST support archiving items, changing their status to "Archived"
- **FR-007**: System MUST support restoring archived items back to "Active" status
- **FR-008**: System MUST prevent archived items from receiving new stock movements
- **FR-009**: System MUST support moving items to different categories
- **FR-010**: System MUST support moving items to different locations
- **FR-011**: System MUST support deleting items entirely from the system
- **FR-012**: System MUST validate that item name is non-empty
- **FR-013**: System MUST validate that current_quantity is never negative
- **FR-014**: System MUST validate that low-stock threshold is never negative
- **FR-015**: System MUST enforce user data isolation—each inventory item belongs to exactly one user and users can only access their own items
- **FR-016**: System MUST enforce that item category and location belong to the same user when updating an item
- **FR-017**: System MUST enforce SKU uniqueness per user when a SKU is provided. Multiple items may have the same name, but each SKU value must be unique within a user's inventory
- **FR-018**: System MUST expose a REST API supporting all CRUD operations on inventory items
- **FR-019**: System MUST automatically update the updated_date timestamp whenever an item is modified
- **FR-020**: System MUST include created_date and updated_date in all item responses

### Key Entities

- **InventoryItem**: Represents a distinct inventory item. Key attributes: id, userId (foreign key to user), name, description, sku, categoryId (foreign key to category), locationId (foreign key to location), currentQuantity, unit, lowStockThreshold, status (Active/Archived), createdDate, updatedDate
- **StockMovement**: Represents a transaction that changes item quantity. Relates to InventoryItem through itemId. Used for all quantity changes to maintain audit trail
- **Category**: Represents an inventory category. Relates to InventoryItem through categoryId. Belongs to a user
- **Location**: Represents a physical or logical storage location. Relates to InventoryItem through locationId. Belongs to a user
- **User**: Represents the account holder. Each InventoryItem, Category, and Location belongs to exactly one user, enforcing data isolation

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All CRUD operations (create, read, update, delete, archive, restore) for inventory items complete in under 500ms at the API level
- **SC-002**: Archive/restore operations are idempotent and do not generate errors on repeated calls with the same state
- **SC-003**: Item creation with opening balance is reliable and opening balance stock movements are consistently created and recorded
- **SC-004**: 100% of archival operations successfully prevent subsequent stock movements from being recorded
- **SC-005**: User data isolation is enforced—no user can access, modify, or delete items belonging to another user through any operation
- **SC-006**: All item field validation rules (non-empty name, non-negative quantity and threshold) are consistently enforced across all operations
- **SC-007**: Uniqueness constraints on name and/or SKU (per the chosen policy) are consistently enforced

## Assumptions

- **User authentication is already implemented**: The system has an authenticated user context available for all requests. Feature assumes Spring Security or equivalent is in place.
- **Categories and Locations already exist**: Users already have created categories and locations. This feature does not implement category or location management.
- **Stock movements are handled separately**: This feature assumes a StockMovement entity and service exist (implemented in a separate specification). Item creation with initial quantity delegates to the stock movement service.
- **Flyway migrations will be used**: All database schema changes will be represented as versioned Flyway migrations per the constitution.
- **REST API is the primary interface**: The feature exposes HTTP endpoints for all operations. No GraphQL or other API styles are in scope.
- **SKU uniqueness is enforced per user**: When a SKU is provided, it must be unique within that user's inventory. Item names may be duplicated across items in the same user's inventory.
- **Data timestamps use server time**: All created_date and updated_date values are set by the server using the current system time, not client-supplied values.
- **No soft deletes**: Delete operations permanently remove items from the database. Historical data about deleted items is not retained (items are not soft-deleted).
