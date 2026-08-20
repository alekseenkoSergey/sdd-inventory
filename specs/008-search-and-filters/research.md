# Research: Search and Filters Implementation

**Feature**: Search and Filters  
**Date**: 2026-08-20

## Research Summary

This research resolves technical dependencies and validates implementation approach for search and filter capabilities in the inventory tracker application.

## Key Findings

### 1. Backend Query Strategy

**Decision**: Use Spring Data JPA with custom repository methods for search and filtering

**Rationale**:
- Project constitution mandates Spring Data repositories (Section III)
- Spring Data `@Query` supports complex JPQL/SQL queries needed for multi-field search and AND-logic filters
- Spring Data `Specification` pattern available for dynamic query building if needed
- Integrates cleanly with Spring Boot service layer architecture

**Alternatives Considered**:
- Raw JDBC: Would bypass Spring Data mandate and add boilerplate
- Elasticsearch: Over-engineered for small-to-medium inventory (10k items target)
- SQL concatenation: Risks SQL injection; Spring Data provides parameterized safety

**Implementation Pattern**: Repository with dedicated search/filter methods returning paginated results

### 2. Database Query Optimization

**Decision**: Add database indexes on searchable fields; use ILIKE for PostgreSQL case-insensitive search

**Rationale**:
- PostgreSQL ILIKE operator is standard for case-insensitive substring matching
- Indexes on `name`, `description`, `sku_code` improve search performance to target 500ms on 1000 items
- Indexes on foreign keys (category_id, location_id, status, quantity) support filter queries
- Low-stock threshold comparison can use indexed quantity column

**Alternatives Considered**:
- PostgreSQL LIKE with COLLATE: Less intuitive, more error-prone
- Full-text search (tsvector): Over-engineered for substring matching requirement

**Implementation Pattern**: Flyway migration to create composite indexes on search/filter columns

### 3. Frontend Search/Filter UI Components

**Decision**: Use Angular template-driven forms with submit-based search and filter dropdowns

**Rationale**:
- Project constitution mandates Angular frontend (Section II)
- Straightforward for simple controls (text input, dropdowns)
- Clarification resolved: search submission is submit-based (Enter key or Search button), not real-time keystroke updates
- Reduces backend load; gives users control over when search executes
- Component-based approach aligns with existing Angular architecture

**Alternatives Considered**:
- Real-time search with debounce: Would increase backend load; user has no control over timing
- Reactive Forms: More powerful but overkill for simple filters; template-driven is simpler per constitution
- ngx-bootstrap or Material dropdowns: Consider using existing component library if inventory UI already uses one

**Implementation Pattern**: Single filter component combining search box (with Enter/button submission), filter dropdowns, and clear buttons. Emits search/filter parameters to parent on submission.

### 4. API Contract Design

**Decision**: RESTful endpoint with query parameters for search and filters

**Rationale**:
- REST architecture aligns with existing inventory API (previous features suggest GET endpoints)
- Query parameters (`?search=text&category=1&location=2&status=active&stockState=low`) are stateless
- Supports individual filter changes without page reload
- Easy to test and cache

**Endpoint**: `GET /api/inventory-items` with parameters:
- `search` (string): text to search in name/description/SKU
- `categoryId` (number): filter by category
- `locationId` (number): filter by location
- `status` (enum): ACTIVE, ARCHIVED, ALL
- `stockState` (enum): OUT_OF_STOCK, LOW_STOCK, IN_STOCK, ALL
- `page`, `size`: pagination parameters

**Alternatives Considered**:
- POST with body: Unnecessary for read-only operation; GET is RESTful standard
- Multiple endpoints: Would fragment API surface; single parameterized endpoint is cleaner

### 5. Empty State Handling

**Decision**: Display UI-level empty state message with action suggestion

**Rationale**:
- Spec requires clear empty state for zero search results
- User should distinguish between "no items match filters" and "no items in inventory"
- Suggestion to "adjust search" or "clear filters" helps user recover

**Implementation Pattern**: Angular `*ngIf` conditional rendering for empty state component

### 6. Low-Stock Threshold Source

**Decision**: Query from per-item `lowStockThreshold` attribute in InventoryItem entity

**Rationale**:
- Clarification resolved: low-stock threshold is per-item (already exists in inventory_item table)
- Each item can have its own threshold value; no centralized configuration needed
- Simplifies query logic: join not required; query directly on inventory_item.low_stock_threshold
- Stock state classification computed as: OUT_OF_STOCK (qty=0), LOW_STOCK (qty > 0 AND qty <= low_stock_threshold), IN_STOCK (qty > low_stock_threshold)

**Implementation Pattern**: Direct attribute access in repository query; no configuration lookups needed

## Decisions Justified vs. Constitution

| Constitution Principle | Decision | Compliance |
|------------------------|----------|-----------|
| Simplicity First | Straightforward repository methods, template-driven forms | ✓ No over-engineering |
| Technology Stack | Spring Boot backend, PostgreSQL, Angular frontend | ✓ Aligned |
| Layered Architecture | Controller → Service → Repository | ✓ Follows pattern |
| Centralized Exception Handling | Exceptions bubble to @ControllerAdvice | ✓ No local handling |

## Implementation Approach Summary

- **Backend**: Spring Data repository with custom search/filter methods; service layer orchestrates parameters; controller validates and returns paginated DTO
- **Database**: Add indexes on search/filter columns via Flyway migration; no schema changes (all fields exist)
- **Frontend**: Single Angular filter component with search box (submit-based via Enter/button), filter dropdowns, and clear buttons; binds to service that calls API
- **API**: RESTful GET endpoint with query parameters; returns paginated InventoryItemDTO list
- **Low-Stock Threshold**: Direct query on per-item `inventory_item.low_stock_threshold` attribute; no configuration lookups
- **Empty Category/Location Handling**: Items without category/location included in unfiltered results; naturally excluded from filtered results (NULL values filtered by WHERE clause)
- **Empty State**: Conditional Angular template showing user-friendly message with suggestions to clear filters

## Clarifications Incorporated (2026-08-20)

1. ✅ Low-stock threshold: Per-item attribute (confirmed in InventoryItem table)
2. ✅ Empty category/location: Include in unfiltered; exclude from filtered results
3. ✅ Search submission: Submit-based (Enter or Search button), not real-time keystroke updates
