# Inventory Categories Feature

## Overview

The Inventory Categories feature enables users to create, rename, and delete named groups (categories) for organizing inventory items. The system enforces category name uniqueness per user (case-insensitive, whitespace-trimmed) and prevents deletion of categories with assigned items.

## Features

### 1. Create Category
- Users can create new categories with unique names
- Names are trimmed of leading/trailing whitespace
- Category name uniqueness is case-insensitive
- Each category belongs to the authenticated user

**API Endpoint**: `POST /api/categories`

**Request**:
```json
{
  "name": "Electronics"
}
```

**Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Electronics",
  "itemCount": 0,
  "createdAt": "2026-08-19T10:30:00Z",
  "updatedAt": "2026-08-19T10:30:00Z"
}
```

### 2. List Categories
- Users can view all their categories
- Each category shows the count of items assigned to it

**API Endpoint**: `GET /api/categories`

**Response** (200 OK):
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Electronics",
    "itemCount": 3,
    "createdAt": "2026-08-19T10:30:00Z",
    "updatedAt": "2026-08-19T10:30:00Z"
  }
]
```

### 3. Rename Category
- Users can rename existing categories
- New name must be unique (case-insensitive)
- Item associations are preserved during rename
- Optimistic locking detects concurrent edits

**API Endpoint**: `PATCH /api/categories/{categoryId}`

**Request**:
```json
{
  "name": "Power Tools"
}
```

**Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Power Tools",
  "itemCount": 3,
  "createdAt": "2026-08-19T10:30:00Z",
  "updatedAt": "2026-08-19T11:00:00Z"
}
```

### 4. Delete Category
- Users can delete empty categories
- Deletion is blocked if items are assigned to the category
- Error message includes count of assigned items

**API Endpoint**: `DELETE /api/categories/{categoryId}`

**Response** (204 No Content):
- On success: Empty response body

**Error Response** (409 Conflict):
```json
{
  "status": 409,
  "error": "CATEGORY_HAS_ITEMS",
  "message": "Cannot delete: 5 items assigned. Please reassign items to another category first.",
  "itemCount": 5,
  "timestamp": "2026-08-19T15:00:00Z"
}
```

## Error Handling

### HTTP Status Codes

| Status | Error | Scenario |
|--------|-------|----------|
| 201 | Created | Category successfully created |
| 200 | OK | Category retrieved, renamed, or list loaded |
| 204 | No Content | Category deleted successfully |
| 400 | Bad Request | Invalid input or duplicate category name |
| 404 | Not Found | Category not found |
| 409 | Conflict | Category has items or concurrent edit detected |

### Error Response Format

```json
{
  "status": 400,
  "error": "CATEGORY_NAME_NOT_UNIQUE",
  "message": "Category name already exists",
  "timestamp": "2026-08-19T15:00:00Z"
}
```

## Deletion Policy

**When Can a Category Be Deleted?**

A category can only be deleted if it has **no items** assigned to it.

**What Happens if I Try to Delete a Category with Items?**

The system will return a **409 Conflict** error with a message showing how many items are assigned. For example:

```
Cannot delete: 5 items assigned. Please reassign items to another category first.
```

**How Do I Delete a Category with Items?**

1. Manually reassign each item to another category
2. Once all items have been reassigned, delete the category
3. The category will now be deletable

## Technical Details

### Database Schema

The category feature creates the following database structure:

- **Table**: `category`
- **Columns**:
  - `id` (UUID, PK)
  - `user_id` (UUID, FK)
  - `name` (VARCHAR 255)
  - `created_at` (TIMESTAMP)
  - `updated_at` (TIMESTAMP)
  - `version` (BIGINT for optimistic locking)

- **Unique Constraint**: `(user_id, LOWER(TRIM(name)))`
- **Indexes**: `idx_category_user_id`, `idx_category_id_user_id`

### Flyway Migration

Migration `V3__Create_category_table.sql` initializes the category table with:
- All required columns and constraints
- Automatic `updated_at` trigger
- Indexes for efficient queries
- Foreign key relationship to user table

### User Isolation

All category operations filter by authenticated user. Users cannot:
- View categories owned by other users
- Rename categories owned by other users
- Delete categories owned by other users

## Frontend Components

### Category List Component
- Displays all user's categories in a table
- Shows item count for each category
- Provides delete button with confirmation
- Auto-refreshes after deletion

### Create Category Dialog
- Form to enter category name
- Validates non-empty input
- Shows duplicate name error
- Clears form on successful creation

### Rename Category Dialog
- Modal to enter new category name
- Detects concurrent edits (HTTP 409)
- Auto-refreshes list on conflict
- Shows duplicate name error

## Testing

### Unit Tests
- `CategoryServiceTest.java` - Service layer logic
- `CategoryServiceRenameTest.java` - Rename operations
- `CategoryServiceDeleteTest.java` - Delete operations
- `CategoryServiceEdgeCasesTest.java` - Edge cases

### Integration Tests
- `CategoryIntegrationTest.java` - End-to-end API testing

### Component Tests
- `category-list.component.spec.ts` - List component
- `create-category-dialog.component.spec.ts` - Create dialog
- `rename-category-dialog.component.spec.ts` - Rename dialog

## Known Limitations

- Category hierarchies (subcategories) are not supported
- Bulk operations (create/delete multiple categories) are not supported
- Category templates/presets are not supported
- Items must be manually reassigned before category deletion

## Future Enhancements

- Category hierarchies with parent/child relationships
- Bulk category operations
- Category templates and presets
- Archiving categories instead of deletion
- Category sharing between users
