# Feature Specification: Stock Movements

**Feature Branch**: `006-stock-movements`

**Created**: 2026-08-20

**Status**: Draft

**Input**: User description: Backend specification for stock movements feature

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Record Opening Balance for New Items (Priority: P1)

When inventory items are created with an initial quantity, the system automatically generates an opening balance stock movement to establish the audit trail.

**Why this priority**: P1 - Opening balance is the foundation of the audit trail; every item must have a clear starting point. Without this, the history of stock changes is incomplete and unmeaningful.

**Independent Test**: Can be fully tested by creating an item with initial quantity > 0 and verifying that an opening balance movement is automatically created with the correct quantity.

**Acceptance Scenarios**:

1. **Given** an item is created with initial quantity 100, **When** the item is persisted, **Then** a stock movement of type "opening balance" is automatically created with quantity 100, movement date set to current date, and created date set to current timestamp.
2. **Given** an item is created with quantity 0, **When** the item is persisted, **Then** no opening balance movement is created (since quantity is not greater than 0).

---

### User Story 2 - Record Stock In Movements (Priority: P1)

Users need to record when stock is received or added to inventory (e.g., supplier deliveries, internal transfers). This creates an auditable record of all inbound stock changes.

**Why this priority**: P1 - Core operational workflow; stock in is the primary mechanism for replenishing inventory.

**Independent Test**: Can be fully tested by creating a stock in movement for an item and verifying that the current quantity is correctly updated and the movement is recorded.

**Acceptance Scenarios**:

1. **Given** an item with current quantity 50, **When** a stock in movement with quantity 30 is recorded with reason "Supplier delivery", **Then** the item's current quantity becomes 80 and the movement is persisted with movement type "stock in", reason "Supplier delivery", and a valid movement date.
2. **Given** a stock in movement is recorded, **When** the movement is queried, **Then** the movement includes: item reference, movement type, quantity, reason/notes, movement date, and created date.

---

### User Story 3 - Record Stock Out Movements (Priority: P1)

Users need to record when stock is removed from inventory (e.g., sales, adjustments, disposal). This creates an auditable record of all outbound stock changes.

**Why this priority**: P1 - Core operational workflow; stock out is essential for tracking inventory depletion and understanding stock usage.

**Independent Test**: Can be fully tested by creating a stock out movement and verifying that the current quantity is correctly reduced and the movement is recorded.

**Acceptance Scenarios**:

1. **Given** an item with current quantity 100, **When** a stock out movement with quantity 30 is recorded with reason "Sales order 12345", **Then** the item's current quantity becomes 70 and the movement is persisted.
2. **Given** an item with current quantity 50, **When** attempting to create a stock out movement with quantity 60, **Then** the operation is rejected with an error (stock out would make quantity negative).
3. **Given** a stock out movement is recorded, **When** the movement is queried, **Then** the system includes the reason/notes field to document the reason for removal.

---

### User Story 4 - Record Adjustment Movements with Direction (Priority: P1)

Users need to record inventory adjustments (e.g., discovered discrepancies, count corrections) with explicit documentation of whether the adjustment increases or decreases stock.

**Why this priority**: P1 - Adjustments are critical for maintaining accurate inventory; the direction (increase/decrease) must be unambiguous to prevent future confusion about historical adjustments.

**Independent Test**: Can be fully tested by creating adjustment movements with both increase and decrease directions and verifying that current quantity is updated correctly and the direction is stored.

**Acceptance Scenarios**:

1. **Given** an item with current quantity 100, **When** an adjustment movement is created with quantity 5, adjustment direction "increase", and reason "Physical count discrepancy", **Then** the item's current quantity becomes 105 and the movement type is recorded as "adjustment" with direction "increase".
2. **Given** an item with current quantity 100, **When** an adjustment movement is created with quantity 5, adjustment direction "decrease", and reason "Inventory shrinkage", **Then** the item's current quantity becomes 95 and the direction "decrease" is recorded.
3. **Given** an item with current quantity 10, **When** attempting to create an adjustment with quantity 15 and direction "decrease", **Then** the operation is rejected (would make quantity negative).
4. **Given** an adjustment movement with direction "decrease" and quantity 5, **When** the movement is queried, **Then** both the direction and quantity are persisted so the intent is unambiguous.

---

### User Story 5 - Query Movement History for an Item (Priority: P2)

Users need to view the complete history of stock movements for an item to audit changes and understand how current quantity was reached.

**Why this priority**: P2 - Essential for auditing but secondary to recording movements; the history query enables post-facto validation and investigation.

**Independent Test**: Can be fully tested by creating multiple movements for an item and querying the history to verify all movements are returned in order.

**Acceptance Scenarios**:

1. **Given** an item with 3 stock movements (opening balance, stock in, adjustment), **When** the movement history is queried for that item, **Then** all 3 movements are returned with complete details (type, quantity, reason, dates).
2. **Given** movements recorded over time, **When** the movement history is queried, **Then** movements are chronologically ordered (oldest first or newest first, consistently).

---

### Edge Cases

- What happens when a movement date is set to the future? (Should be permitted; movement date and created date can differ)
- What if stock out quantity exactly equals current quantity? (Should be permitted; results in zero quantity)
- What if adjustment direction is not provided for a movement type other than adjustment? (Non-adjustment types should not require direction)
- What if quantity is exactly zero in a stock out or adjustment decrease? (Should be rejected; quantity must be > 0)
- What happens if two movements are recorded simultaneously? (Both should be persisted; created date differentiates them using last-write-wins strategy)

## Clarifications

### Session 2026-08-20

- Q: How should concurrent stock movements for the same item be handled? → A: Last-write-wins; both movements are accepted in sequence, with the most recent movement updating the current quantity.
- Q: Can movement date be set to any past date (or future date)? → A: Yes, movement date can be any date; operators can backdate movements to reflect when the business event occurred.
- Q: Are there specific latency or throughput targets for stock movements? → A: No specific targets; optimize for correctness and simplicity assuming typical single-location inventory scale.
- Q: Is reason/notes required for all movement types or optional? → A: Optional for all movement types; operators can record movements without notes if needed.
- Q: Should movement history queries support pagination or return all movements? → A: Return all movements without pagination; complete history accessible in one query.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST automatically create an opening balance stock movement when an item is created with initial quantity greater than 0.
- **FR-002**: System MUST persist stock movements with the following mandatory fields: item reference, movement type, quantity, movement date, and created date.
- **FR-003**: System MUST support four movement types: opening balance, stock in, stock out, and adjustment.
- **FR-004**: System MUST include an optional reason or notes field for all movements; operators may provide context but it is not required.
- **FR-005**: System MUST accept adjustment direction (increase or decrease) only for adjustment-type movements; non-adjustment types MUST NOT require this field.
- **FR-006**: System MUST validate that quantity is always greater than 0 for any stock movement.
- **FR-007**: System MUST reject stock out movements if they would result in a negative current quantity.
- **FR-008**: System MUST reject adjustment movements with direction "decrease" if they would result in a negative current quantity.
- **FR-009**: System MUST update the item's current quantity consistently after every successful stock movement (increase or decrease based on movement type and adjustment direction); when concurrent movements are recorded for the same item, the most recently recorded movement determines the final current quantity (last-write-wins).
- **FR-010**: System MUST maintain complete movement history for each item, allowing queries that return all movements by item without pagination or filtering (unless explicitly filtered by date range or other business criteria).
- **FR-011**: System MUST NOT allow deletion of stock movements (audit trail immutability).
- **FR-012**: Stock quantity MUST be changed exclusively through stock movements; direct editing of current quantity MUST NOT be permitted.

### Key Entities

- **StockMovement**: Represents a change in item quantity. Includes item ID, movement type, quantity, reason/notes, movement date, created date, and (for adjustments) adjustment direction.
- **Item**: Existing entity with at minimum an id, name, and current quantity that is updated by stock movements.
- **MovementType**: Enum with values: opening_balance, stock_in, stock_out, adjustment.
- **AdjustmentDirection**: Enum with values: increase, decrease (used only when MovementType is adjustment).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All stock movements are persisted correctly and auditable; 100% of movements result in persistent records with complete details.
- **SC-002**: Current quantity is always consistent with the sum of all movements for an item (or opening balance + subsequent changes).
- **SC-003**: Stock out and adjustment decrease validations prevent negative inventory; 100% of invalid operations are rejected without partial state changes.
- **SC-004**: Movement history is queryable and returns accurate, chronologically ordered results for any item.
- **SC-005**: Opening balance movements are automatically created for new items with initial quantity > 0; no manual intervention required.

## Assumptions

- Stock movements apply only to the backend API; UI implementation is explicitly out of scope (user-specified).
- Movement date and created date are separate: movement date represents when the business event occurred, created date represents when the record was created in the system (typically the same but may differ for backdated entries).
- The existing Item entity already has a current_quantity field; stock movements will update this field after each successful operation.
- Quantity in all movements is expressed in the same unit as the item (no unit conversion logic needed).
- Access control and authentication for stock movement endpoints are handled by existing auth system and not part of this feature.
- No real-time synchronization of inventory across multiple warehouse locations is required; each location maintains its own inventory.
- Deleting items will cascade or soft-delete their associated movements (handled by existing deletion logic; not part of this feature).
