# Implementation Plan: Stock Movements

**Branch**: `006-stock-movements` | **Date**: 2026-08-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-stock-movements/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

**Primary Requirement**: Implement a stock movement feature that records all changes to inventory item quantities through an immutable audit trail. Stock movements are the exclusive mechanism for quantity changes (no direct editing). The system supports four movement types (opening balance, stock in, stock out, adjustment) with validation to prevent negative inventory and concurrent update handling via last-write-wins.

**Technical Approach**: 
- Backend REST API (Spring Boot) with three endpoints: create stock movement, query movement history by item, optionally query movements by date range
- New JPA entity `StockMovement` with fields: item ID (FK), movement type (enum), quantity, adjustment direction (enum, nullable), reason/notes (optional), movement date, created date/timestamp
- Service layer implements validation rules and quantity update logic
- Database migration (Flyway) to create `stock_movement` table with appropriate indexes
- Existing `Item` entity enhanced to ensure stock quantity only changes via movements

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.x)

**Primary Dependencies**: Spring Boot, Spring Data JPA, Flyway, PostgreSQL 15+

**Storage**: PostgreSQL (relational database; existing for project)

**Testing**: JUnit 5 with Spring Boot Test, Testcontainers (PostgreSQL)

**Target Platform**: Web backend service (Linux/Docker)

**Project Type**: Web application (backend API for inventory tracking)

**Performance Goals**: Single-location inventory scale; optimize for correctness and simplicity (no specific throughput targets per Q3 clarification)

**Constraints**: 
- Last-write-wins concurrency strategy (per Q1 clarification)
- Movement history returned without pagination (all movements per query per Q5 clarification)
- Reason/notes optional for all movement types (per Q4 clarification)
- Movement date can be any past or future date (per Q2 clarification)

**Scale/Scope**: 
- Small to medium business inventory (single location)
- Typical inventory size: hundreds to low thousands of items
- Movement volume: dozens to hundreds per day

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Principle I - Simplicity First**: ✅ **PASS**
- Implementing only required behavior (four movement types, quantity updates, validation)
- No additional abstraction layers, event sourcing, or saga patterns
- Straightforward service + repository + controller structure following Spring conventions

**Principle II - Technology Stack**: ✅ **PASS**
- Backend: Java Spring Boot ✓
- Database: PostgreSQL ✓
- Migrations: Flyway ✓
- Persistence: Spring Data JPA repositories ✓
- No new frameworks or infrastructure components introduced

**Principle III - Backend Layered Package Structure**: ✅ **PASS**
- `model` package: DTOs for create/read stock movements (StockMovementRequestDTO, StockMovementResponseDTO)
- `entity` package: JPA entity `StockMovement` with domain state and validation
- `controller` package: REST endpoints for stock movement operations
- `service` package: Business logic (validation, quantity updates, concurrent update handling)
- `repository` package: Spring Data repository for StockMovement persistence
- Dependency flow: controller → service → repository maintained

**Principle IV - Centralized Exception Processing**: ✅ **PASS**
- Exception handling centralized in existing @ControllerAdvice
- Services throw domain exceptions (e.g., `InvalidQuantityException`, `NegativeQuantityException`)
- Advice translates to HTTP error responses (400 for validation, 409 for conflicts)

**Constitution Status**: ✅ **All gates PASS** — No violations. Feature aligns with all four core principles.

## Project Structure

### Documentation (this feature)

```text
specs/006-stock-movements/
├── spec.md                    # Feature specification
├── plan.md                    # This file
├── research.md                # Phase 0 output
├── data-model.md              # Phase 1 output
├── quickstart.md              # Phase 1 output
├── contracts/                 # Phase 1 output
│   └── stock-movement-api.md
└── tasks.md                   # Phase 2 output (via /speckit-tasks)
```

### Source Code (Backend)

```text
backend/src/main/java/org/example/sddinventory/
├── entity/
│   ├── Item.java              # Enhanced: add movement-related methods
│   └── StockMovement.java     # New entity
├── model/
│   ├── StockMovementRequestDTO.java     # New
│   ├── StockMovementResponseDTO.java    # New
│   └── StockMovementType.java           # Enum
├── service/
│   ├── StockMovementService.java        # New: core business logic
│   └── ItemService.java                 # Enhanced: integrate with movements
├── controller/
│   └── StockMovementController.java     # New: REST endpoints
├── repository/
│   └── StockMovementRepository.java     # New: Spring Data JPA
└── exception/
    ├── InvalidQuantityException.java    # New
    └── NegativeQuantityException.java   # New

backend/src/main/resources/db/migration/
├── V{next}/Create_stock_movement_table.sql  # New migration

backend/src/test/java/org/example/sddinventory/
├── service/
│   └── StockMovementServiceTest.java    # New: unit tests
├── controller/
│   └── StockMovementControllerTest.java # New: integration tests
└── repository/
    └── StockMovementRepositoryTest.java # New: persistence tests
```

**Structure Decision**: Web application (backend + frontend). This feature implements the backend API only; UI is out of scope. Backend follows the established layered architecture (controller → service → repository).

## Complexity Tracking

> **No violations to Constitution Check** — All principles satisfied. No complexity justification needed.

---

## Phase 0: Research

*Research and design decisions; populate before Phase 1.*

### Research Topics

1. **Spring Data JPA enum mapping** — Best practices for mapping movement type and adjustment direction enums to PostgreSQL
2. **Optimistic vs. pessimistic concurrency** — Validate last-write-wins approach for inventory updates in Spring Data
3. **Flyway migration strategy** — Version numbering and idempotency for schema changes
4. **REST API pagination patterns** — Document why pagination is skipped (per Q5 clarification)
5. **Spring validation** — Using Jakarta Validation for DTOs and entities

### Findings

**1. Spring Data JPA Enum Mapping**
- Decision: Use `@Enumerated(EnumType.STRING)` on JPA entity fields for MovementType and AdjustmentDirection
- Rationale: Improves database readability; easier to query and audit than ORDINAL; strings are future-proof if enum order changes
- Alternatives considered: EnumType.ORDINAL (smaller storage, but breaks if enum is reordered), custom type mapper (overkill for simple case)

**2. Concurrency Strategy**
- Decision: Implement last-write-wins via unconditional `save()` without optimistic locking
- Rationale: Per clarification Q1, concurrent movements are rare; Spring Data's default behavior is sufficient
- Alternatives considered: Optimistic locking with @Version (adds complexity for edge case), pessimistic locking with database locks (unnecessary overhead)
- Caveat: If concurrent stock updates become problematic post-launch, can add @Version and ConflictException without changing API

**3. Flyway Migration Strategy**
- Decision: Use sequential versioning (V001_Create_stock_movement_table.sql, V002_... etc.)
- Rationale: Aligns with existing project pattern; prevents version conflicts in team environments
- Location: `backend/src/main/resources/db/migration/`

**4. REST API Design (No Pagination)**
- Decision: Return all movements in a single query; no pagination limit
- Rationale: Per Q5 clarification; small-to-medium business scale means items rarely have >1000 movements; single query simplifies API contract
- Future: If pagination becomes necessary, can add optional query parameters (limit, offset) without breaking existing clients

**5. Validation Framework**
- Decision: Use Jakarta Validation (javax.validation) annotations in DTOs and entities
- Rationale: Spring Boot 3.x native support; declarative and testable
- Examples: @NotNull, @Positive, @Size, custom validators for business rules

**Phase 0 Output**: research.md saved with all clarifications resolved.

---

## Phase 1: Design & Contracts

### Data Model

**Entity: StockMovement**

```
StockMovement (JPA Entity)
├── Long id (PK)
├── Long itemId (FK → Item.id)
├── MovementType movementType (Enum: OPENING_BALANCE, STOCK_IN, STOCK_OUT, ADJUSTMENT)
├── Long quantity (validated > 0)
├── AdjustmentDirection adjustmentDirection (Enum: INCREASE, DECREASE; nullable, required only for ADJUSTMENT)
├── String reason (optional; nullable)
├── LocalDate movementDate (any past/future date)
├── LocalDateTime createdDate (set at record creation)
├── Index: (itemId, createdDate) for efficient history queries
```

**Entity: Item** (Enhanced)

```
Item (existing JPA Entity - changes only)
├── ... existing fields ...
├── Long currentQuantity (updated exclusively by StockMovement creation/update logic)
├── Add constraint: NOT NULL on currentQuantity with default 0
├── Add method: updateQuantityFromMovement(StockMovement) - internal method to sync quantity
```

**Enums**

```
MovementType: OPENING_BALANCE, STOCK_IN, STOCK_OUT, ADJUSTMENT
AdjustmentDirection: INCREASE, DECREASE
```

### API Contracts

**Endpoint 1: Create Stock Movement**

```
POST /api/v1/items/{itemId}/movements
Request:
{
  "movementType": "STOCK_IN",  // OPENING_BALANCE, STOCK_IN, STOCK_OUT, ADJUSTMENT
  "quantity": 50,               // Must be > 0
  "reason": "Supplier delivery", // Optional
  "movementDate": "2026-08-20", // Optional; defaults to today
  "adjustmentDirection": null   // Required only if movementType is ADJUSTMENT; values: INCREASE, DECREASE
}

Response (201 Created):
{
  "id": 123,
  "itemId": 456,
  "movementType": "STOCK_IN",
  "quantity": 50,
  "reason": "Supplier delivery",
  "movementDate": "2026-08-20",
  "adjustmentDirection": null,
  "createdDate": "2026-08-20T14:30:15Z",
  "itemCurrentQuantity": 150  // Reflects updated quantity
}

Response (400 Bad Request) - Examples:
- Quantity <= 0: "Quantity must be greater than 0"
- Stock out would make quantity negative: "Stock out of 60 units would make quantity negative (current: 50)"
- Adjustment decrease would make quantity negative: "Adjustment of 100 would make quantity negative (current: 50)"
- Missing adjustmentDirection for ADJUSTMENT type: "adjustmentDirection is required for ADJUSTMENT movements"
```

**Endpoint 2: Query Movement History**

```
GET /api/v1/items/{itemId}/movements[?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD]
Query Parameters (optional):
  - startDate: Filter to movements on or after this date (ISO 8601)
  - endDate: Filter to movements on or before this date (ISO 8601)

Response (200 OK):
[
  {
    "id": 1,
    "itemId": 456,
    "movementType": "OPENING_BALANCE",
    "quantity": 100,
    "reason": null,
    "movementDate": "2026-08-01",
    "adjustmentDirection": null,
    "createdDate": "2026-08-01T09:00:00Z"
  },
  {
    "id": 2,
    "itemId": 456,
    "movementType": "STOCK_IN",
    "quantity": 50,
    "reason": "Supplier delivery",
    "movementDate": "2026-08-15",
    "adjustmentDirection": null,
    "createdDate": "2026-08-15T14:30:15Z"
  },
  ...
]

Response (404 Not Found):
- If itemId does not exist
```

**Endpoint 3: Query Item with Current Quantity** (Existing - Enhanced)

```
GET /api/v1/items/{itemId}
Response (200 OK):
{
  "id": 456,
  "name": "Widget",
  "currentQuantity": 150,        // Updated by movements
  "category": "...",
  ...
}
```

**Exception Mapping** (via existing @ControllerAdvice)

```
InvalidQuantityException (400 Bad Request):
  - Quantity must be greater than 0
  - Adjustment direction missing for ADJUSTMENT type
  
NegativeQuantityException (400 Bad Request):
  - Stock out would result in negative quantity
  - Adjustment would result in negative quantity

ItemNotFoundException (404 Not Found):
  - Item {itemId} not found
```

### Quickstart Validation Guide

**Prerequisites**
- Backend running on http://localhost:8080
- PostgreSQL migration applied (stock_movement table created)
- Sample item exists with ID=1 and currentQuantity=0

**Scenario 1: Create Opening Balance (Automatic)**

```
✅ Expected Behavior: When item created with quantity > 0, opening balance movement auto-created

Setup:
  POST /api/v1/items
  {
    "name": "Test Item",
    "quantity": 100,
    "category": "Test"
  }

Verify:
  GET /api/v1/items/1/movements
  → Returns array with 1 entry of type OPENING_BALANCE, quantity 100
  
  GET /api/v1/items/1
  → currentQuantity = 100
```

**Scenario 2: Record Stock In**

```
✅ Expected Behavior: Stock in movement recorded and current quantity updated

Request:
  POST /api/v1/items/1/movements
  {
    "movementType": "STOCK_IN",
    "quantity": 50,
    "reason": "Supplier delivery",
    "movementDate": "2026-08-20"
  }

Expected Response (201):
  {
    "id": 2,
    "movementType": "STOCK_IN",
    "quantity": 50,
    "itemCurrentQuantity": 150  // 100 + 50
  }

Verify:
  GET /api/v1/items/1
  → currentQuantity = 150
  
  GET /api/v1/items/1/movements
  → Returns 2 movements (opening balance + stock in), ordered by createdDate
```

**Scenario 3: Reject Stock Out (Negative)**

```
✅ Expected Behavior: Stock out rejected if it would make quantity negative

Request:
  POST /api/v1/items/1/movements
  {
    "movementType": "STOCK_OUT",
    "quantity": 200  // Current quantity is 150
  }

Expected Response (400):
  {
    "error": "Stock out of 200 units would make quantity negative (current: 150)"
  }

Verify:
  GET /api/v1/items/1
  → currentQuantity remains 150 (unchanged)
```

**Scenario 4: Record Adjustment (Increase)**

```
✅ Expected Behavior: Adjustment recorded with direction and current quantity updated

Request:
  POST /api/v1/items/1/movements
  {
    "movementType": "ADJUSTMENT",
    "quantity": 10,
    "adjustmentDirection": "INCREASE",
    "reason": "Physical count discrepancy"
  }

Expected Response (201):
  {
    "id": 3,
    "movementType": "ADJUSTMENT",
    "quantity": 10,
    "adjustmentDirection": "INCREASE",
    "itemCurrentQuantity": 160  // 150 + 10
  }
```

**Scenario 5: Record Adjustment (Decrease)**

```
✅ Expected Behavior: Adjustment decrease recorded and quantity reduced

Request:
  POST /api/v1/items/1/movements
  {
    "movementType": "ADJUSTMENT",
    "quantity": 5,
    "adjustmentDirection": "DECREASE",
    "reason": "Inventory shrinkage"
  }

Expected Response (201):
  {
    "id": 4,
    "movementType": "ADJUSTMENT",
    "quantity": 5,
    "adjustmentDirection": "DECREASE",
    "itemCurrentQuantity": 155  // 160 - 5
  }
```

**Scenario 6: Reject Adjustment Decrease (Negative)**

```
✅ Expected Behavior: Adjustment decrease rejected if it would make quantity negative

Request:
  POST /api/v1/items/1/movements
  {
    "movementType": "ADJUSTMENT",
    "quantity": 200,
    "adjustmentDirection": "DECREASE"
  }

Expected Response (400):
  {
    "error": "Adjustment of 200 would make quantity negative (current: 155)"
  }
```

**Scenario 7: Query Movement History with Filter**

```
✅ Expected Behavior: Returns all movements; can filter by date range

Request:
  GET /api/v1/items/1/movements?startDate=2026-08-15&endDate=2026-08-20

Expected Response (200):
  Array of movements recorded within date range (movementDate filter)
```

**Scenario 8: Reject Quantity Validation**

```
✅ Expected Behavior: Movements with quantity <= 0 are rejected

Request:
  POST /api/v1/items/1/movements
  {
    "movementType": "STOCK_IN",
    "quantity": 0
  }

Expected Response (400):
  {
    "error": "Quantity must be greater than 0"
  }
```

---

## Next Steps

Phase 1 design complete. Generated artifacts:
- `research.md` — Research findings and decisions
- `data-model.md` — Entity definitions and database schema
- `contracts/stock-movement-api.md` — API contracts
- `quickstart.md` — Validation scenarios

**Next Command**: `/speckit-tasks` to generate actionable task decomposition

