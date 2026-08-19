# API Contract: Category Management Endpoints

**Phase 1 Output** | **Date**: 2026-08-19

## Overview

This document defines the REST API contract for category management. All endpoints are authenticated (require valid JWT token or session) and enforce per-user data isolation.

**Base URL**: `/api/categories`

**Authentication**: Bearer token (Authorization header) or session cookie

**Content-Type**: `application/json`

**Response Format**: JSON

## Endpoints

### 1. Create Category

**Endpoint**: `POST /api/categories`

**Description**: Create a new category for the authenticated user.

**Request**:

```
POST /api/categories HTTP/1.1
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "Electronics"
}
```

**Request Body** (CreateCategoryRequestDTO):

| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| `name` | String | YES | @NotBlank, @Size(min=1, max=255) |

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

**Error Responses**:

- **400 Bad Request**: Name is empty, null, or exceeds 255 characters
  ```json
  {
    "status": 400,
    "error": "VALIDATION_ERROR",
    "message": "Name is required and must not exceed 255 characters"
  }
  ```

- **400 Bad Request**: Category name already exists for this user (case-insensitive, whitespace trimmed)
  ```json
  {
    "status": 400,
    "error": "CATEGORY_NAME_NOT_UNIQUE",
    "message": "Category name 'Electronics' already exists. Please choose a different name."
  }
  ```

- **401 Unauthorized**: Missing or invalid authentication token
  ```json
  {
    "status": 401,
    "error": "UNAUTHORIZED",
    "message": "Authentication required"
  }
  ```

- **500 Internal Server Error**: Unexpected server error
  ```json
  {
    "status": 500,
    "error": "INTERNAL_ERROR",
    "message": "An unexpected error occurred. Please try again later."
  }
  ```

---

### 2. List Categories

**Endpoint**: `GET /api/categories`

**Description**: Retrieve all categories for the authenticated user.

**Request**:

```
GET /api/categories HTTP/1.1
Authorization: Bearer <token>
```

**Query Parameters**: None

**Response** (200 OK):

```json
{
  "categories": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Electronics",
      "itemCount": 3,
      "createdAt": "2026-08-19T10:30:00Z",
      "updatedAt": "2026-08-19T10:30:00Z"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Office Supplies",
      "itemCount": 5,
      "createdAt": "2026-08-19T11:00:00Z",
      "updatedAt": "2026-08-19T11:00:00Z"
    }
  ]
}
```

**Error Responses**:

- **401 Unauthorized**: Missing or invalid authentication token

---

### 3. Get Category by ID

**Endpoint**: `GET /api/categories/{categoryId}`

**Description**: Retrieve a single category by ID (must belong to authenticated user).

**Request**:

```
GET /api/categories/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Authorization: Bearer <token>
```

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `categoryId` | UUID | ID of the category to retrieve |

**Response** (200 OK):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Electronics",
  "itemCount": 3,
  "createdAt": "2026-08-19T10:30:00Z",
  "updatedAt": "2026-08-19T10:30:00Z"
}
```

**Error Responses**:

- **404 Not Found**: Category does not exist or does not belong to authenticated user
  ```json
  {
    "status": 404,
    "error": "CATEGORY_NOT_FOUND",
    "message": "Category not found"
  }
  ```

- **401 Unauthorized**: Missing or invalid authentication token

---

### 4. Rename Category

**Endpoint**: `PATCH /api/categories/{categoryId}`

**Description**: Rename an existing category (must belong to authenticated user). All items remain associated.

**Request**:

```
PATCH /api/categories/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "Consumer Electronics"
}
```

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `categoryId` | UUID | ID of the category to rename |

**Request Body** (RenameCategoryRequestDTO):

| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| `name` | String | YES | @NotBlank, @Size(min=1, max=255) |

**Response** (200 OK):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Consumer Electronics",
  "itemCount": 3,
  "createdAt": "2026-08-19T10:30:00Z",
  "updatedAt": "2026-08-19T10:35:00Z"
}
```

**Error Responses**:

- **400 Bad Request**: New name is empty, null, or exceeds 255 characters

- **400 Bad Request**: New name already exists for this user (case-insensitive)
  ```json
  {
    "status": 400,
    "error": "CATEGORY_NAME_NOT_UNIQUE",
    "message": "Category name 'Consumer Electronics' already exists. Please choose a different name."
  }
  ```

- **404 Not Found**: Category does not exist or does not belong to authenticated user

- **409 Conflict**: Category was renamed or deleted by another client (concurrent edit detected)
  ```json
  {
    "status": 409,
    "error": "CONFLICT",
    "message": "Category no longer exists or has been modified. Please refresh your view and try again."
  }
  ```

- **401 Unauthorized**: Missing or invalid authentication token

---

### 5. Delete Category

**Endpoint**: `DELETE /api/categories/{categoryId}`

**Description**: Delete a category (must be empty — no items assigned). User must manually reassign items before deletion.

**Request**:

```
DELETE /api/categories/550e8400-e29b-41d4-a716-446655440000 HTTP/1.1
Authorization: Bearer <token>
```

**Path Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `categoryId` | UUID | ID of the category to delete |

**Response** (204 No Content):

```
HTTP/1.1 204 No Content
```

No response body.

**Error Responses**:

- **409 Conflict**: Category has items assigned; cannot delete (blocking deletion policy)
  ```json
  {
    "status": 409,
    "error": "CATEGORY_HAS_ITEMS",
    "message": "Cannot delete: 5 items assigned. Please reassign items to another category first."
  }
  ```

- **404 Not Found**: Category does not exist or does not belong to authenticated user

- **409 Conflict**: Category was modified or deleted by another client (concurrent edit detected)
  ```json
  {
    "status": 409,
    "error": "CONFLICT",
    "message": "Category no longer exists or has been modified. Please refresh your view and try again."
  }
  ```

- **401 Unauthorized**: Missing or invalid authentication token

---

## Common Error Codes

| HTTP Status | Error Code | Meaning |
|-------------|-----------|---------|
| 400 | `VALIDATION_ERROR` | Request body failed validation |
| 400 | `CATEGORY_NAME_NOT_UNIQUE` | Category name already exists for this user |
| 401 | `UNAUTHORIZED` | Missing or invalid authentication |
| 403 | `FORBIDDEN` | User does not have permission (e.g., accessing another user's category) |
| 404 | `CATEGORY_NOT_FOUND` | Category does not exist |
| 409 | `CATEGORY_HAS_ITEMS` | Cannot delete category with items assigned |
| 409 | `CONFLICT` | Concurrent edit detected (optimistic lock failed) |
| 500 | `INTERNAL_ERROR` | Unexpected server error |

## Concurrency & Conflict Handling

**Scenario**: User A renames a category in Tab 1. User A views the same category in Tab 2 (stale data). User A attempts to rename it in Tab 2.

**Behavior**:
1. Tab 2 sends PATCH request with old (stale) version information
2. Server detects version mismatch (optimistic lock conflict)
3. Server returns HTTP 409 with message: "Category no longer exists or has been modified. Please refresh your view and try again."
4. Frontend automatically refreshes category list (GET /api/categories)
5. User sees updated category name; can retry rename if desired

## User Data Isolation

**All endpoints enforce per-user isolation**:
- User can only see their own categories (GET returns only their categories)
- User can only modify their own categories (PATCH/DELETE check user ownership)
- User cannot access another user's category (404 returned if category belongs to different user)

**Enforcement**: Service layer verifies authenticated user ID matches category userId before returning or modifying data.

## Rate Limiting (Optional, Implementation TBD)

Recommended for production:
- POST /api/categories: 10 requests per minute per user
- GET /api/categories: 60 requests per minute per user
- PATCH /api/categories/{id}: 10 requests per minute per user
- DELETE /api/categories/{id}: 5 requests per minute per user

## Changelog

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-08-19 | Initial API contract |
