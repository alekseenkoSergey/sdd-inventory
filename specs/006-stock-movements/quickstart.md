# Quickstart: Stock Movements Feature Validation

**Date**: 2026-08-20  
**Purpose**: Runnable validation scenarios that prove the stock movements feature works end-to-end

## Prerequisites

- **Backend Running**: Spring Boot application running on `http://localhost:8080`
- **Database**: PostgreSQL with migration applied (stock_movement table created)
- **Authentication**: Test user authenticated (session or Bearer token)
- **Sample Data**: At least one item exists (or create via `/items` endpoint)

## Setup Commands

### 1. Start Backend (if not running)

```bash
cd backend/
./mvnw spring-boot:run
# Waits for: "Started Application in X.XXX seconds"
```

### 2. Apply Database Migration

Migrations run automatically on application startup via Flyway. Verify table creation:

```bash
# Connect to PostgreSQL
psql -U postgres -d sdd_inventory

# Check table exists
\dt stock_movement
# Output: public | stock_movement | table | postgres
```

### 3. Create Test Item (if needed)

```bash
curl -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Widget",
    "quantity": 100,
    "categoryId": 1
  }'

# Response (201):
{
  "id": 1,
  "name": "Test Widget",
  "currentQuantity": 100,
  "categoryId": 1
}

# Note the item ID for use in scenarios below
```

---

## Validation Scenarios

### Scenario 1: Opening Balance Auto-Created

**Purpose**: Verify that creating an item with quantity > 0 automatically generates an opening balance movement

**Setup**:
```bash
# Create item with initial quantity
curl -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Auto-Balance Item",
    "quantity": 100,
    "categoryId": 1
  }'
# Response: id=1 (or next ID), currentQuantity=100
```

**Test**:
```bash
# Query movement history for the item
curl -X GET http://localhost:8080/api/v1/items/1/movements \
  -H "Accept: application/json"
```

**Expected Result** ✅:
```json
[
  {
    "id": 1,
    "itemId": 1,
    "movementType": "OPENING_BALANCE",
    "quantity": 100,
    "adjustmentDirection": null,
    "reason": null,
    "movementDate": "2026-08-20",
    "createdDate": "2026-08-20T09:00:00Z",
    "itemCurrentQuantity": 100
  }
]
```

**Validation Criteria**:
- [ ] Exactly 1 movement exists
- [ ] Movement type is `OPENING_BALANCE`
- [ ] Quantity matches initial item quantity
- [ ] `itemCurrentQuantity` equals initial quantity

---

### Scenario 2: Record Stock In

**Purpose**: Verify stock in movement increases quantity correctly

**Setup**: Use item from Scenario 1 (id=1, currentQuantity=100)

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "STOCK_IN",
    "quantity": 50,
    "reason": "Supplier delivery",
    "movementDate": "2026-08-20"
  }'
```

**Expected Result** ✅:
```json
{
  "id": 2,
  "itemId": 1,
  "movementType": "STOCK_IN",
  "quantity": 50,
  "adjustmentDirection": null,
  "reason": "Supplier delivery",
  "movementDate": "2026-08-20",
  "createdDate": "2026-08-20T14:30:15Z",
  "itemCurrentQuantity": 150
}
```

**Validation Criteria**:
- [ ] Movement created (HTTP 201)
- [ ] `movementType` is `STOCK_IN`
- [ ] `itemCurrentQuantity` is 150 (100 + 50)
- [ ] Movement date matches request

**Verify Persistence**:
```bash
# Query item current quantity
curl -X GET http://localhost:8080/api/v1/items/1 \
  -H "Accept: application/json"
# Response: currentQuantity = 150

# Query movement history (should now have 2 entries)
curl -X GET http://localhost:8080/api/v1/items/1/movements \
  -H "Accept: application/json"
# Response: array with 2 movements (opening balance + stock in)
```

---

### Scenario 3: Reject Stock Out (Would Make Quantity Negative)

**Purpose**: Verify that stock out is rejected if it would result in negative quantity

**Setup**: Item has currentQuantity=150 (from Scenario 2)

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "STOCK_OUT",
    "quantity": 200
  }'
```

**Expected Result** ✅:
```json
{
  "error": "Stock out of 200 units would make quantity negative (current: 150)",
  "timestamp": "2026-08-20T14:35:00Z",
  "path": "/api/v1/items/1/movements"
}
```

**HTTP Status**: 400 Bad Request

**Validation Criteria**:
- [ ] Request rejected (HTTP 400)
- [ ] Error message mentions negative quantity
- [ ] Error message shows current quantity (150)
- [ ] Item quantity unchanged (still 150)

**Verify No Side Effects**:
```bash
# Confirm quantity unchanged
curl -X GET http://localhost:8080/api/v1/items/1 \
  -H "Accept: application/json"
# Response: currentQuantity = 150 (unchanged)
```

---

### Scenario 4: Record Stock Out (Valid)

**Purpose**: Verify successful stock out decreases quantity

**Setup**: Item has currentQuantity=150

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "STOCK_OUT",
    "quantity": 50,
    "reason": "Sales order #9999"
  }'
```

**Expected Result** ✅:
```json
{
  "id": 3,
  "itemId": 1,
  "movementType": "STOCK_OUT",
  "quantity": 50,
  "adjustmentDirection": null,
  "reason": "Sales order #9999",
  "movementDate": "2026-08-20",
  "createdDate": "2026-08-20T14:40:00Z",
  "itemCurrentQuantity": 100
}
```

**Validation Criteria**:
- [ ] Movement created (HTTP 201)
- [ ] `movementType` is `STOCK_OUT`
- [ ] `itemCurrentQuantity` is 100 (150 - 50)
- [ ] Reason recorded

---

### Scenario 5: Record Adjustment (Increase)

**Purpose**: Verify adjustment with direction INCREASE correctly increases quantity

**Setup**: Item has currentQuantity=100

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "ADJUSTMENT",
    "quantity": 10,
    "adjustmentDirection": "INCREASE",
    "reason": "Physical count discrepancy - recount found extra units"
  }'
```

**Expected Result** ✅:
```json
{
  "id": 4,
  "itemId": 1,
  "movementType": "ADJUSTMENT",
  "quantity": 10,
  "adjustmentDirection": "INCREASE",
  "reason": "Physical count discrepancy - recount found extra units",
  "movementDate": "2026-08-20",
  "createdDate": "2026-08-20T15:00:00Z",
  "itemCurrentQuantity": 110
}
```

**Validation Criteria**:
- [ ] Movement created (HTTP 201)
- [ ] `movementType` is `ADJUSTMENT`
- [ ] `adjustmentDirection` is `INCREASE`
- [ ] `itemCurrentQuantity` is 110 (100 + 10)

---

### Scenario 6: Record Adjustment (Decrease)

**Purpose**: Verify adjustment with direction DECREASE correctly decreases quantity

**Setup**: Item has currentQuantity=110

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "ADJUSTMENT",
    "quantity": 5,
    "adjustmentDirection": "DECREASE",
    "reason": "Inventory shrinkage - stolen"
  }'
```

**Expected Result** ✅:
```json
{
  "id": 5,
  "itemId": 1,
  "movementType": "ADJUSTMENT",
  "quantity": 5,
  "adjustmentDirection": "DECREASE",
  "reason": "Inventory shrinkage - stolen",
  "movementDate": "2026-08-20",
  "createdDate": "2026-08-20T15:05:00Z",
  "itemCurrentQuantity": 105
}
```

**Validation Criteria**:
- [ ] Movement created (HTTP 201)
- [ ] `adjustmentDirection` is `DECREASE`
- [ ] `itemCurrentQuantity` is 105 (110 - 5)

---

### Scenario 7: Reject Adjustment (Would Make Quantity Negative)

**Purpose**: Verify adjustment decrease rejected if it would make quantity negative

**Setup**: Item has currentQuantity=105

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "ADJUSTMENT",
    "quantity": 150,
    "adjustmentDirection": "DECREASE"
  }'
```

**Expected Result** ✅:
```json
{
  "error": "Adjustment of 150 would make quantity negative (current: 105)",
  "timestamp": "2026-08-20T15:10:00Z",
  "path": "/api/v1/items/1/movements"
}
```

**HTTP Status**: 400 Bad Request

**Validation Criteria**:
- [ ] Request rejected (HTTP 400)
- [ ] Error message shows current quantity

---

### Scenario 8: Reject Invalid Quantity

**Purpose**: Verify quantity must be > 0

**Setup**: Item exists

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "STOCK_IN",
    "quantity": 0
  }'
```

**Expected Result** ✅:
```json
{
  "error": "Quantity must be greater than 0",
  "timestamp": "2026-08-20T15:15:00Z",
  "path": "/api/v1/items/1/movements"
}
```

**HTTP Status**: 400 Bad Request

---

### Scenario 9: Reject Adjustment Without Direction

**Purpose**: Verify adjustmentDirection is required for ADJUSTMENT type

**Setup**: Item exists

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "ADJUSTMENT",
    "quantity": 10
  }'
```

**Expected Result** ✅:
```json
{
  "error": "adjustmentDirection is required for ADJUSTMENT movements",
  "timestamp": "2026-08-20T15:20:00Z",
  "path": "/api/v1/items/1/movements"
}
```

**HTTP Status**: 400 Bad Request

---

### Scenario 10: Query Movement History with Date Filter

**Purpose**: Verify movement history can be filtered by date range

**Setup**: Item has multiple movements recorded (from previous scenarios)

**Test**:
```bash
curl -X GET "http://localhost:8080/api/v1/items/1/movements?startDate=2026-08-15&endDate=2026-08-20" \
  -H "Accept: application/json"
```

**Expected Result** ✅:
```json
[
  {
    "id": 1,
    "movementType": "OPENING_BALANCE",
    "movementDate": "2026-08-20",
    ...
  },
  {
    "id": 2,
    "movementType": "STOCK_IN",
    "movementDate": "2026-08-20",
    ...
  },
  ...
]
```

**Validation Criteria**:
- [ ] Movements returned in chronological order (by createdDate)
- [ ] All movements have movementDate within specified range
- [ ] Complete details included per movement

---

### Scenario 11: Backdate Movement

**Purpose**: Verify movement date can be set to any past date

**Setup**: Item exists

**Test**:
```bash
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "STOCK_IN",
    "quantity": 25,
    "movementDate": "2026-06-15",
    "reason": "Retroactive entry: delivery from June that was not recorded"
  }'
```

**Expected Result** ✅:
```json
{
  "id": 6,
  "itemId": 1,
  "movementType": "STOCK_IN",
  "quantity": 25,
  "movementDate": "2026-06-15",
  "createdDate": "2026-08-20T15:30:00Z",
  "itemCurrentQuantity": 130
}
```

**Validation Criteria**:
- [ ] Movement created (HTTP 201)
- [ ] `movementDate` set to June 15 (past date)
- [ ] `createdDate` set to today (current timestamp)
- [ ] Quantity updated correctly

---

## Full Scenario Walkthrough

Complete workflow from item creation to audit:

```bash
# 1. Create item with opening balance
curl -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "quantity": 100, "categoryId": 1}'
# Response: id=1, currentQuantity=100

# 2. Stock in 50
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{"movementType": "STOCK_IN", "quantity": 50}'
# Response: itemCurrentQuantity=150

# 3. Stock out 30
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{"movementType": "STOCK_OUT", "quantity": 30}'
# Response: itemCurrentQuantity=120

# 4. Adjust down 5
curl -X POST http://localhost:8080/api/v1/items/1/movements \
  -H "Content-Type: application/json" \
  -d '{"movementType": "ADJUSTMENT", "quantity": 5, "adjustmentDirection": "DECREASE"}'
# Response: itemCurrentQuantity=115

# 5. View complete history
curl -X GET http://localhost:8080/api/v1/items/1/movements

# Expected 4 movements:
# 1. OPENING_BALANCE: 100 → qty=100
# 2. STOCK_IN: +50 → qty=150
# 3. STOCK_OUT: -30 → qty=120
# 4. ADJUSTMENT: -5 → qty=115
```

---

## Test Coverage Summary

| Feature | Scenario | Status |
|---------|----------|--------|
| Opening Balance Auto-Create | 1 | ✅ |
| Stock In | 2 | ✅ |
| Stock Out (Reject Negative) | 3 | ✅ |
| Stock Out (Valid) | 4 | ✅ |
| Adjustment Increase | 5 | ✅ |
| Adjustment Decrease | 6 | ✅ |
| Adjustment Reject Negative | 7 | ✅ |
| Quantity Validation | 8 | ✅ |
| Direction Validation | 9 | ✅ |
| History Query w/ Filter | 10 | ✅ |
| Backdate Movement | 11 | ✅ |

---

## Related Documentation

- [Data Model](data-model.md) — Entity definitions and database schema
- [API Contracts](contracts/stock-movement-api.md) — Endpoint specifications
- [Plan](plan.md) — Architecture and design decisions

