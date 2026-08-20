# Phase 0 Research: Inventory Items Management

**Date**: 2026-08-20

This document resolves technical unknowns and documents best practices for the Inventory Items Management backend feature.

## Research Topics

### 1. Spring Boot Best Practices for User Data Isolation

**Topic**: How to properly implement user-scoped data access in Spring Boot REST APIs

**Finding**:
- Use Spring Security's `SecurityContext.getAuthentication()` to retrieve authenticated user in services
- Extract user ID from principal and use as parameter for all repository queries
- Implement custom argument resolver to inject user ID into controller methods (optional convenience)
- Avoid storing user context in static variables; use method parameters instead

**Decision**: Use SecurityContextHolder in service methods to extract user ID, pass to repositories. No custom argument resolver needed for initial implementation (keep simple per constitution).

**Rationale**: Standard Spring Security pattern, minimal overhead, aligns with simplicity principle.

**Alternatives considered**: Custom annotation + argument resolver (more complex), AOP filter (harder to test)

---

### 2. Opening Balance Stock Movement Creation Pattern

**Topic**: How to reliably create opening balance transactions when items are created with initial quantity

**Finding**:
- Opening balance should be created in same transaction as item creation (atomicity)
- Use `@Transactional` at service method level
- Catch and propagate StockMovement service exceptions as is (let @ControllerAdvice handle)
- Test both success and failure paths (item created but stock movement fails → rollback)

**Decision**: InventoryItemService.createItem() is `@Transactional`. Calls StockMovementService within same transaction. Any service exception causes rollback and propagates to controller advice.

**Rationale**: Maintains consistency; prevents orphaned items or missing opening balances.

**Alternatives considered**: Async stock movement creation (no—requires transaction, consistency), event-driven (no—over-engineering)

---

### 3. PostgreSQL Unique Index for Optional SKU

**Topic**: How to enforce uniqueness on an optional (nullable) field in SQL

**Finding**:
- Standard unique constraints in SQL treat NULL as distinct (PostgreSQL allows multiple NULLs)
- Use partial unique index: `CREATE UNIQUE INDEX ON table(field) WHERE field IS NOT NULL;`
- Partial indexes are supported by Spring Data JPA (define in Flyway migration)
- Application-level validation also recommended as defense in depth

**Decision**: Flyway migration creates partial unique index on (user_id, sku). No need for application-level duplicate check for SKU (database enforces).

**Rationale**: Database handles constraint; clean and reliable. Partial index avoids constraint violations when SKU is null.

**Alternatives considered**: Application-level uniqueness check (redundant if DB constraint exists), allow duplicates (violates spec)

---

### 4. Archival/Restore Idempotency

**Topic**: Best practice for idempotent state-change operations

**Finding**:
- Idempotent operations simplify client code and error handling
- Check current state before changing, but don't error if already in target state
- Return success with metadata (e.g., "already archived" flag) or silent success
- Silent success is simpler if no UI distinction needed

**Decision**: Archive operation checks if status == ARCHIVED. If so, returns success silently. Same for restore.

**Rationale**: Simplest implementation. Clients don't need special error handling. Spec allows this interpretation.

**Alternatives considered**: Throw exception if already archived (requires client error handling), return flag indicating state (unnecessary complexity)

---

### 5. REST API Design for Quantity-Protected Fields

**Topic**: How to prevent accidental direct quantity edits in REST PATCH/PUT operations

**Finding**:
- Use separate DTOs for request and response (`SomethingRequestDTO` vs `SomethingResponseDTO`)
- Request DTO omits protected fields (currentQuantity)
- Response DTO includes all fields for visibility
- Document clearly which fields are read-only

**Decision**: Create `InventoryItemPatchDTO` (request) without currentQuantity field. `InventoryItemResponseDTO` includes currentQuantity. Separate create vs update DTOs.

**Rationale**: Type-safe prevention at API boundary. No runtime checks needed (DTO simply doesn't have the field).

**Alternatives considered**: Include field but validate/reject at service (works but less clear), use annotations for read-only (error-prone)

---

### 6. Timestamp Management (createdDate vs updatedDate)

**Topic**: Best practice for managing creation and update timestamps

**Finding**:
- Server generates timestamps, not client (per assumption)
- Use `@CreationTimestamp` and `@UpdateTimestamp` JPA annotations (Hibernate)
- Or manually set in service methods before save
- Store as TIMESTAMP or TIMESTAMP WITH TIMEZONE in database
- Use instant/UTC in Java (no timezone conversion complexity)

**Decision**: Use JPA `@CreationTimestamp` and `@UpdateTimestamp` on entity fields. Spring Data/Hibernate handles automatically.

**Rationale**: Minimal code, consistent with Spring Boot conventions, automatic on insert/update.

**Alternatives considered**: Manual timestamp setting (error-prone), client-supplied timestamps (security/consistency issues)

---

### 7. Category and Location User Ownership Validation

**Topic**: How to validate that updated category/location belong to the same user

**Finding**:
- When moving item to different category/location, verify ownership
- Query category and location separately before updating item
- Throw meaningful exception if category/location not found or owned by different user
- Database foreign keys alone are insufficient (need user-scoped constraint)

**Decision**: In update method, verify category.userId == requestingUserId and location.userId == requestingUserId before updating item.

**Rationale**: Prevents cross-user category/location assignment. Simple application-level check; database FKs don't enforce user scoping.

**Alternatives considered**: Database constraints with user_id in FK (complex), rely only on DB FK (insufficient for multi-user)

---

### 8. Soft Delete vs Hard Delete

**Topic**: Whether to implement soft deletes (mark deleted) or hard deletes (remove from DB)

**Finding**:
- Soft deletes preserve history and support undo/recovery
- Hard deletes remove data permanently
- Spec assumption states "No soft deletes: Delete operations permanently remove items"
- This is intentional per business logic (e.g., error cleanup)

**Decision**: Hard delete via DELETE statement in repository.

**Rationale**: Spec explicitly states no soft deletes. Hard delete is simpler. Users can recover from backups if truly needed.

**Alternatives considered**: Soft delete (violates spec assumption), logical deletion flag (over-engineering)

---

## Summary

All major technical unknowns resolved. Design is ready for Phase 1 artifact generation:
- Data model schema with validated fields and constraints
- REST API contracts with request/response DTOs
- Quickstart validation scenarios to prove feature works

Next: `/speckit-plan` Phase 1 execution generates data-model.md, contracts/, and quickstart.md
