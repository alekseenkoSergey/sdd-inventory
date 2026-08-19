# Feature Specification: Inventory Categories

**Feature Branch**: `002-inventory-categories`

**Created**: 2026-08-19

**Status**: Draft

**Input**: User description: "Categories - A category represents a group of inventory items. A user can create a category. A user can rename a category. A user can delete a category. Category name must be unique per user. Deleting a category must be handled explicitly by one of these approaches: block deletion if items exist in the category, OR allow deletion and move items to a default category, for example 'Uncategorized'. You must document the chosen approach in README and in the specification."

## Clarifications

### Session 2026-08-19

- Q: When a user deletes a category, what happens to items currently assigned to that category? → A: User must manually reassign items to another category before deletion is allowed. System blocks deletion and shows error message with item count.
- Q: Should the system allow leading/trailing whitespace in category names, or should it be trimmed/rejected? → A: Trim leading/trailing whitespace; enforce uniqueness as case-insensitive. "Electronics" and "electronics" are treated as duplicates.
- Q: If a user renames or deletes a category from one browser tab while viewing it in another tab, how should the system handle stale data? → A: System detects conflict, shows error ("Category no longer exists" or "Name already taken"), and automatically refreshes the category list.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a New Category (Priority: P1)

A user wants to organize their inventory items by grouping them into meaningful categories (e.g., "Electronics", "Office Supplies", "Tools"). They need to be able to create a new category with a descriptive name so they can assign items to it.

**Why this priority**: Creating categories is the foundational action that enables the entire categorization system. Without this, users cannot organize their inventory at all.

**Independent Test**: This can be fully tested by creating a category and verifying it appears in the user's category list and delivers the capability to organize inventory items.

**Acceptance Scenarios**:

1. **Given** a user is viewing the category management interface, **When** they click "Create Category" and enter a unique category name (e.g., "Tools"), **Then** the category is created and appears in their category list immediately.
2. **Given** a user has already created a category named "Electronics", **When** they attempt to create another category with the same name, **Then** the system shows an error message indicating the category name must be unique and prevents creation.
3. **Given** a user enters a category name, **When** they click "Create", **Then** the new category is accessible for assigning items immediately.

---

### User Story 2 - Rename an Existing Category (Priority: P1)

A user realizes they want to change the name of an existing category to better reflect their organizational needs (e.g., renaming "Office" to "Office Supplies"). They need to update the category name while preserving all items currently assigned to that category.

**Why this priority**: Renaming is a critical operation as users often refine their organizational structure after initial setup. It affects workflow efficiency.

**Independent Test**: This can be fully tested by renaming a category and verifying the new name is reflected everywhere the category appears, and all associated items remain grouped under the new name.

**Acceptance Scenarios**:

1. **Given** a user has a category named "Electronics", **When** they select the rename action and change the name to "Tech Devices", **Then** the category is updated with the new name and all items in that category remain associated with it.
2. **Given** a user attempts to rename a category to a name that already exists for another category, **When** they confirm the rename, **Then** the system shows an error indicating the new name conflicts and prevents the rename.
3. **Given** a user renames a category, **When** the rename completes successfully, **Then** the new name is immediately visible in all views where that category appears (category list, item filters, etc.).

---

### User Story 3 - Delete a Category (Priority: P1)

A user wants to remove a category that is no longer needed. The system must handle this deletion according to a clear and consistent policy to prevent data loss or corruption.

**Why this priority**: Managing the category lifecycle (including deletion) is essential for maintaining a clean and organized inventory system. Unclear deletion behavior can cause data integrity issues or user confusion.

**Independent Test**: This can be fully tested by attempting to delete a category under various conditions (empty vs. with items) and verifying the system behaves according to the defined deletion policy.

**Acceptance Scenarios**:

1. **Given** a user has an empty category with no items assigned, **When** they delete it, **Then** the category is removed from the system and no longer appears in the category list.
2. **Given** a user has a category with 5 items assigned, **When** they attempt to delete it, **Then** the system blocks the deletion and displays an error message showing the count of items ("Cannot delete: 5 items assigned. Please reassign items to another category first.").
3. **Given** a user has manually reassigned all items from a category to another category, **When** they delete the now-empty category, **Then** the category is removed successfully.
4. **Given** a category has been deleted, **When** a user views their category list, **Then** the deleted category does not appear and is no longer available for assignment to items.

---

### Edge Cases

- Empty or whitespace-only names are rejected; after trimming, category names must contain at least one non-whitespace character.
- **Concurrent edits from multiple tabs**: When a user attempts to rename/delete a category that has been modified in another tab, the system detects the conflict and shows a clear error message ("Category no longer exists" or "Name already taken"), then automatically refreshes the category list. The user must retry the operation with current data.
- When a user attempts to create more than a reasonable number of categories (implementation may enforce a per-user limit; specific limit to be determined during planning based on database and performance constraints).
- Duplicate category check is case-insensitive and applied after whitespace trimming, so " Electronics " and "ELECTRONICS" are treated as the same category name and will be rejected.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST allow authenticated users to create a new category by providing a name that (after trimming whitespace) contains at least one non-whitespace character.
- **FR-002**: System MUST validate that category names are unique per user. Uniqueness check is case-insensitive and applied after trimming leading/trailing whitespace. For example, "Electronics", "electronics", " ELECTRONICS ", and "  Electronics  " all refer to the same category name.
- **FR-003**: System MUST allow users to rename an existing category while preserving all items assigned to that category.
- **FR-004**: System MUST enforce that a category name cannot be changed to a name that already exists for another category owned by the same user (using case-insensitive comparison after trimming).
- **FR-005**: System MUST prevent deletion of a category if any items are assigned to it. The system MUST show a clear error message that includes the count of items and instructs the user to manually reassign items to another category before deletion is allowed. This approach protects data integrity by requiring explicit user action on each item.
- **FR-006**: System MUST display a user's complete list of categories in the category management interface.
- **FR-007**: System MUST persist all category data in the database and maintain data integrity across all CRUD operations.
- **FR-008**: System MUST enforce user data isolation so that users only see and can modify their own categories.
- **FR-009**: System MUST detect and handle concurrent edits (e.g., category renamed or deleted in another session/tab). When a stale operation is attempted, the system MUST show a clear error message (e.g., "Category no longer exists" or "Name already taken") and automatically refresh the category list to reflect current state.

### Key Entities

- **Category**: Represents a named group of inventory items.
  - Attributes: `id`, `userId` (foreign key to user), `name` (string, unique per user, case-insensitive, trimmed of leading/trailing whitespace), `createdAt` (timestamp), `updatedAt` (timestamp)
  - Relationships: One-to-many with inventory items (items reference this category via `categoryId` foreign key)
  
- **Item** (referenced, not directly part of this feature): Inventory items that belong to categories.
  - Relationship: Each item has an optional `categoryId` foreign key pointing to a Category

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can create a new category in under 30 seconds.
- **SC-002**: Users can rename a category in under 20 seconds.
- **SC-003**: Category creation fails gracefully with a clear error message when the name already exists for that user.
- **SC-004**: Deleted categories are immediately removed from the UI and all category lists.
- **SC-005**: All items remain properly associated with their categories after a rename operation (zero items lost or orphaned).
- **SC-006**: User data isolation is enforced; users cannot see, modify, or delete categories belonging to other users.

## Assumptions

- **Authentication & Authorization**: The system has already implemented user authentication and authorization. Users are already logged in before accessing category management features.
- **User Data Isolation**: The backend already enforces multi-tenant data isolation at the service/database layer. Each request is associated with an authenticated user.
- **Inventory Items Exist**: The system already has an inventory items feature (or will have it) that references categories via a foreign key relationship.
- **Deletion Policy**: The system uses the "Block Deletion" approach. Categories with items cannot be deleted. Users must manually reassign items to another category before deletion is allowed. The system shows an error message with the count of items blocking deletion. This policy is documented in the project README and enforced at the application layer.
- **Name Validation**: Category names are trimmed of leading/trailing whitespace before storage and validation. Uniqueness is enforced as case-insensitive after trimming.
- **Concurrent Edit Handling**: The system detects when a category has been modified (renamed or deleted) by another client/tab. On stale operations, users see a clear error and the category list is automatically refreshed.
- **Database Consistency**: The application will rely on database constraints (foreign keys, unique constraints) to maintain data integrity alongside application-level validation.
- **No Bulk Operations**: Bulk rename/delete/reassign operations (e.g., deleting multiple categories or reassigning multiple items at once) are out of scope for this feature.
