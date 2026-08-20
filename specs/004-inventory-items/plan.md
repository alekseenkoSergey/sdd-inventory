# Implementation Plan: Inventory Items Management

**Branch**: `004-inventory-items` | **Date**: 2026-08-20 | **Spec**: [spec.md](spec.md)

**Input**: Backend specification for inventory item management backend with CRUD operations, stock movement integration, archival, and user data isolation.

**Note**: This document describes the technical approach, architecture decisions, and design artifacts for the Inventory Items Management feature.

## Summary

The feature implements a complete backend API for inventory item management, enabling users to create, read, update, archive, and delete inventory items with automatic opening balance stock movement creation. The design emphasizes simplicity, Spring Boot layered architecture compliance, and strong user data isolation.

Core capabilities: item creation with optional opening balance, field editing (excluding direct quantity modification), archival/restoration, category/location reassignment, and deletion. All operations enforce user data isolation and validate business rules at the service layer.

## Technical Context

**Language/Version**: Java 17 with Spring Boot 3.x

**Primary Dependencies**: Spring Boot (web, data-jpa), Spring Security, PostgreSQL JDBC driver, Flyway for migrations

**Storage**: PostgreSQL (relational database)

**Testing**: JUnit 5, Mockito, @SpringBootTest for integration tests

**Target Platform**: Linux server (REST API backend)

**Project Type**: Web service (REST API backend)

**Performance Goals**: All CRUD operations complete in under 500ms (SC-001)

**Constraints**: Archive/restore operations must be idempotent (SC-002); opening balance stock movements must be reliably created (SC-003); archived items cannot receive stock movements (SC-004)

**Scale/Scope**: Small to mid-size personal/business inventory systems. Initial scope: single-user isolation enforced, categories and locations pre-exist (implemented in separate features), no soft deletes

## Constitution Check

**Principles Verified**:

✅ **Principle I - Simplicity First**
- Feature uses only Spring Boot built-in capabilities (Spring Data JPA, Spring Security, Spring MVC)
- No additional architectural patterns (no repository pattern wrapper, no service layer abstraction beyond needs)
- No speculative functionality; only capabilities required by spec are implemented

✅ **Principle II - Technology Stack**
- Backend: Java with Spring Boot ✓
- Database: PostgreSQL ✓
- Migrations: Flyway ✓
- Persistence: Spring Data repositories ✓
- No new frameworks introduced

✅ **Principle III - Layered Package Structure**
- Will follow: `model` (DTOs) → `entity` (domain) → `controller` (API) → `service` (logic) → `repository` (persistence)
- Single entity class per domain concept (InventoryItem, no separate persistence model)
- Controllers handle HTTP, services handle business logic, repositories handle data access

✅ **Principle IV - Centralized Exception Processing**
- All HTTP exception handling will route through single `@ControllerAdvice`
- Services throw domain exceptions, advice translates to HTTP responses
- No duplicate try/catch blocks in controllers

**Gate Status**: ✅ PASS - No violations. Feature aligns with all constitutional principles.

## Project Structure

### Documentation (this feature)

```text
specs/004-inventory-items/
├── spec.md                   # Feature specification
├── plan.md                   # This file
├── research.md               # Phase 0: research findings
├── data-model.md            # Phase 1: entity definitions and schema
├── quickstart.md            # Phase 1: validation scenarios and test guide
├── contracts/               # Phase 1: REST API contracts
│   └── inventory-items-api.md
└── checklists/
    └── requirements.md
```

### Source Code (repository root)

This feature adds to the existing backend structure:

```text
backend/
├── src/main/java/com/inventory/
│   ├── model/
│   │   ├── InventoryItemRequestDTO.java
│   │   ├── InventoryItemResponseDTO.java
│   │   └── InventoryItemPatchDTO.java
│   ├── entity/
│   │   └── InventoryItem.java
│   ├── controller/
│   │   └── InventoryItemController.java
│   ├── service/
│   │   └── InventoryItemService.java
│   └── repository/
│       └── InventoryItemRepository.java
│
├── src/main/resources/db/migration/
│   └── V[N]__Create_inventory_items_table.sql
│
└── src/test/java/com/inventory/
    ├── unit/
    │   └── service/
    │       └── InventoryItemServiceTest.java
    ├── integration/
    │   └── controller/
    │       └── InventoryItemControllerTest.java
    └── contract/
        └── InventoryItemApiContractTest.java
```

**Structure Decision**: Single backend project (Spring Boot monolith). Frontend is separate (out of scope per spec). InventoryItem data model is simple with no separate persistence entity needed. All operations route through standard layered architecture (controller → service → repository → database).

## Design Decisions

### Opening Balance Stock Movement

**Decision**: When an item is created with initial quantity > 0, automatically create a StockMovement record of type "OPENING_BALANCE".

**Rationale**: Maintains audit trail for all quantity changes. Users don't need to manually create a separate transaction. Aligns with spec requirement FR-002.

**Implementation**: InventoryItemService.createItem() checks initial quantity and delegates to StockMovementService if needed. Uses transactional boundary at service level.

### Direct Quantity Edit Prevention

**Decision**: API never accepts `currentQuantity` in update requests. Prevent at DTOs + service validation.

**Rationale**: Enforces audit trail integrity per FR-005. Stock changes must go through stock movement operations.

**Implementation**: InventoryItemPatchDTO does not include currentQuantity field. Service rejects if attempt is made. Clear separation: create sets initial quantity (creating opening balance), updates exclude quantity entirely.

### Archival Idempotency

**Decision**: Archive and restore operations succeed even if item is already in target state (idempotent per SC-002).

**Rationale**: Simplifies client code; reduces error handling complexity.

**Implementation**: Update queries check current status but return success regardless. No "already archived" error thrown.

### SKU Uniqueness Constraint

**Decision**: Enforce database-level unique constraint on (userId, sku) where sku is not null.

**Rationale**: Allows optional SKU (null-safe uniqueness), prevents duplicates, delegated to database for consistency.

**Implementation**: Partial unique index in PostgreSQL: `CREATE UNIQUE INDEX idx_inventory_item_user_sku ON inventory_item(user_id, sku) WHERE sku IS NOT NULL;`

### User Data Isolation

**Decision**: Always filter by authenticated user_id in repository queries. Enforce at service layer before repository calls.

**Rationale**: Prevents accidental cross-user access. Per FR-015 and SC-005.

**Implementation**: Service methods receive user ID from Spring Security context. Repository methods include user_id in WHERE clauses. Category/location validation also checks user_id ownership.

## Complexity Tracking

No constitutional violations. All design decisions favor simplicity and align with Spring Boot layered architecture principles.

---

## Next Steps

Phase 1 design (data-model.md, contracts/, quickstart.md) complete. Ready for Phase 2 task generation via `/speckit-tasks`.
