# Location Management API Contracts

**Date**: 2026-08-19  
**Feature**: Location Management  
**Base Path**: `/locations`

## Overview

RESTful API for managing user-defined storage locations. All endpoints require OAuth2 authentication. All location operations are scoped to the authenticated user.

---

## Create Location

**Endpoint**: `POST /locations`

**Authentication**: Required (OAuth2)

**Request**:
```json
{
  "name": "Home Office"
}
```

**Request Validation**:
- `name`: Required, non-empty, non-whitespace, max 255 characters

**Response** (201 Created):
```json
{
  "id": 1,
  "userId": 42,
  "name": "Home Office",
  "createdAt": "2026-08-19T10:30:00",
  "updatedAt": "2026-08-19T10:30:00"
}
```

**Error Responses**:

| Status | Error Code | Message | Condition |
|--------|-----------|---------|-----------|
| 400 | INVALID_INPUT | Location name is required | name missing or empty |
| 400 | INVALID_INPUT | Location name cannot be empty or whitespace-only | name is whitespace only |
| 400 | INVALID_INPUT | Location name must be between 1 and 255 characters | name exceeds 255 chars |
| 409 | LOCATION_NAME_NOT_UNIQUE | A location with this name already exists in your account | name already exists for user |
| 401 | UNAUTHORIZED | Authentication required. Please login. | Not authenticated |
| 500 | INTERNAL_SERVER_ERROR | An unexpected error occurred. Please try again later. | Unexpected error |

**Spec Reference**: FR-001, FR-002, FR-003, User Story 1

---

## List Locations

**Endpoint**: `GET /locations`

**Authentication**: Required (OAuth2)

**Query Parameters**: None

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "userId": 42,
    "name": "Home Office",
    "createdAt": "2026-08-19T10:30:00",
    "updatedAt": "2026-08-19T10:30:00"
  },
  {
    "id": 2,
    "userId": 42,
    "name": "Warehouse",
    "createdAt": "2026-08-19T11:00:00",
    "updatedAt": "2026-08-19T11:00:00"
  }
]
```

**Error Responses**:

| Status | Error Code | Message | Condition |
|--------|-----------|---------|-----------|
| 401 | UNAUTHORIZED | Authentication required. Please login. | Not authenticated |
| 500 | INTERNAL_SERVER_ERROR | An unexpected error occurred. Please try again later. | Unexpected error |

**Spec Reference**: User Story 1 (verification step)

---

## Get Location by ID

**Endpoint**: `GET /locations/{id}`

**Authentication**: Required (OAuth2)

**Path Parameters**:
- `id`: Location ID (Long)

**Response** (200 OK):
```json
{
  "id": 1,
  "userId": 42,
  "name": "Home Office",
  "createdAt": "2026-08-19T10:30:00",
  "updatedAt": "2026-08-19T10:30:00"
}
```

**Error Responses**:

| Status | Error Code | Message | Condition |
|--------|-----------|---------|-----------|
| 404 | LOCATION_NOT_FOUND | Location not found | ID does not exist or belongs to different user |
| 401 | UNAUTHORIZED | Authentication required. Please login. | Not authenticated |
| 500 | INTERNAL_SERVER_ERROR | An unexpected error occurred. Please try again later. | Unexpected error |

**Spec Reference**: FR-009 (user isolation verification)

---

## Rename Location

**Endpoint**: `PUT /locations/{id}`

**Authentication**: Required (OAuth2)

**Path Parameters**:
- `id`: Location ID (Long)

**Request**:
```json
{
  "name": "Home Office 2"
}
```

**Request Validation**:
- `name`: Required, non-empty, non-whitespace, max 255 characters

**Response** (200 OK):
```json
{
  "id": 1,
  "userId": 42,
  "name": "Home Office 2",
  "createdAt": "2026-08-19T10:30:00",
  "updatedAt": "2026-08-19T10:45:00"
}
```

**Error Responses**:

| Status | Error Code | Message | Condition |
|--------|-----------|---------|-----------|
| 400 | INVALID_INPUT | Location name is required | name missing or empty |
| 400 | INVALID_INPUT | Location name cannot be empty or whitespace-only | name is whitespace only |
| 400 | INVALID_INPUT | Location name must be between 1 and 255 characters | name exceeds 255 chars |
| 404 | LOCATION_NOT_FOUND | Location not found | ID does not exist or belongs to different user |
| 409 | LOCATION_NAME_NOT_UNIQUE | A location with this name already exists in your account | new name already exists for user |
| 409 | OPTIMISTIC_LOCK_FAILED | The location was modified by another request. Please refresh and try again. | Version mismatch (concurrent edit) |
| 401 | UNAUTHORIZED | Authentication required. Please login. | Not authenticated |
| 500 | INTERNAL_SERVER_ERROR | An unexpected error occurred. Please try again later. | Unexpected error |

**Spec Reference**: FR-004, FR-005, User Story 2

---

## Delete Location

**Endpoint**: `DELETE /locations/{id}`

**Authentication**: Required (OAuth2)

**Path Parameters**:
- `id`: Location ID (Long)

**Request**: No body required

**Response** (204 No Content): No body

**Error Responses**:

| Status | Error Code | Message | Condition |
|--------|-----------|---------|-----------|
| 404 | LOCATION_NOT_FOUND | Location not found | ID does not exist or belongs to different user |
| 409 | LOCATION_HAS_ITEMS | Cannot delete location with items. Please remove or reassign items first. (itemCount: 5) | Location contains items |
| 401 | UNAUTHORIZED | Authentication required. Please login. | Not authenticated |
| 500 | INTERNAL_SERVER_ERROR | An unexpected error occurred. Please try again later. | Unexpected error |

**Spec Reference**: FR-006, FR-007, User Story 4

---

## Error Response Format

All error responses follow this standardized format:

```json
{
  "timestamp": "2026-08-19T10:30:00Z",
  "status": 400,
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "path": "/locations"
}
```

**Fields**:
- `timestamp`: ISO 8601 timestamp of the error (Zoned)
- `status`: HTTP status code
- `error`: Machine-readable error code (SCREAMING_SNAKE_CASE)
- `message`: Human-readable error description
- `path`: Request path that caused the error

**Logging**: All error responses are logged via SLF4J at appropriate levels (WARN for user errors, ERROR for server errors).

---

## Logging

All location operations are logged for audit purposes:

| Operation | Log Level | Message Format |
|-----------|-----------|-----------------|
| Create success | INFO | Location created: id={}, userId={}, name={} |
| Create failure | WARN | Failed to create location: userId={}, reason={} |
| Rename success | INFO | Location renamed: id={}, userId={}, oldName={}, newName={} |
| Rename failure | WARN | Failed to rename location: id={}, userId={}, reason={} |
| Delete success | INFO | Location deleted: id={}, userId={}, name={} |
| Delete failure | WARN | Failed to delete location: id={}, userId={}, reason={} |
| Unexpected error | ERROR | Unexpected error in location operation: {} |

---

## Authentication & Authorization

- All endpoints require valid OAuth2 authentication token in Authorization header: `Authorization: Bearer {token}`
- User isolation enforced at service layer: users can only access/modify their own locations
- userId is extracted from authenticated principal; cannot be overridden in requests

---

## Constraints & Performance

- Location name max length: 255 characters
- All operations must complete within 1 second (SC-001, SC-002)
- 95% of operations should succeed under normal load (SC-006)
- Unique name constraint enforced at both application and database level
- Optimistic locking handles concurrent updates without explicit locks
