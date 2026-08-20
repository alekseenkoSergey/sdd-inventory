# Quickstart: Search and Filters Validation

**Feature**: Search and Filters  
**Date**: 2026-08-20

This document describes runnable validation scenarios that prove the search and filter feature works end-to-end. Each scenario can be tested independently using the API contract defined in `contracts/search-filter-api.md`.

---

## Prerequisites

1. **Application running**: Backend API server and frontend application both running locally
2. **Test data populated**: Inventory database contains at least 10 items across multiple categories/locations with varying stock levels
3. **Low-stock threshold known**: Identify the system's configured low-stock threshold (e.g., 5 items)
4. **API endpoint accessible**: `GET /api/inventory-items` responds to requests

### Test Data Setup

If not already present, populate database with diverse test data:

```
Category 1: Electronics
  - Item: LED Bulbs (SKU: LED-001), Qty: 45
  - Item: Resistor Pack (SKU: RES-100), Qty: 3 (low stock)
  - Item: Capacitor Set (SKU: CAP-001), Qty: 0 (out of stock)

Category 2: Hardware
  - Item: Screws (SKU: SCR-001), Qty: 200
  - Item: Bolts (SKU: BOLT-001), Qty: 8 (low stock)

Location 1: Warehouse A
  - LED Bulbs, Resistor Pack, Screws

Location 2: Warehouse B
  - Capacitor Set, Bolts

Status: Some items ACTIVE, some ARCHIVED (mix both to test status filter)
```

---

## Validation Scenarios

### Scenario 1: Basic Search by Name

**Goal**: Verify search returns items matching text in name field

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?search=led&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes LED Bulbs item
- `content` array is not empty
- Response matches contract in `contracts/search-filter-api.md`

**Acceptance**:
- Search "led" (lowercase) matches LED Bulbs (mixed case) ✓
- Case-insensitive matching works ✓

---

### Scenario 2: Basic Search by SKU

**Goal**: Verify search returns items matching text in SKU field

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?search=RES&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes Resistor Pack item (SKU: RES-100)
- Search "RES" matches SKU-based results ✓

**Acceptance**:
- Search parameter searches SKU field ✓
- Multi-field search (name + SKU + description) works correctly ✓

---

### Scenario 3: Search with No Results

**Goal**: Verify empty state when search matches nothing

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?search=NONEXISTENT&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- `content` array is empty
- `totalElements: 0`
- UI should display "No items found" message

**Acceptance**:
- Empty state response is valid and clear ✓
- User understands no items match search ✓

---

### Scenario 4: Filter by Category

**Goal**: Verify category filter narrows results to single category

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?categoryId=1&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes only Electronics items (LED Bulbs, Resistor Pack, Capacitor Set)
- All items have `categoryName: "Electronics"`
- Hardware items excluded

**Acceptance**:
- Category filter works independently ✓
- Results include all items in selected category ✓

---

### Scenario 5: Filter by Location

**Goal**: Verify location filter narrows results to single location

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?locationId=1&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes only Warehouse A items (LED Bulbs, Resistor Pack, Screws)
- All items have `locationName: "Warehouse A"`
- Warehouse B items excluded

**Acceptance**:
- Location filter works independently ✓
- Results accurate for selected location ✓

---

### Scenario 6: Combine Category and Location Filters

**Goal**: Verify AND logic when multiple filters applied

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?categoryId=1&locationId=1&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes only Electronics items in Warehouse A (LED Bulbs, Resistor Pack)
- Results satisfy both filters
- Capacity Set excluded (wrong category), Bolts excluded (wrong location)

**Acceptance**:
- Multiple filters combined with AND logic ✓
- Only items matching all criteria returned ✓

---

### Scenario 7: Filter by Status (Active)

**Goal**: Verify status filter shows only active items

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?status=ACTIVE&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes only items with `status: "ACTIVE"`
- Archived items excluded

**Acceptance**:
- Status filter works ✓
- ACTIVE status correctly filters lifecycle ✓

---

### Scenario 8: Filter by Status (Archived)

**Goal**: Verify can view archived items separately

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?status=ARCHIVED&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes only archived items
- Active items excluded

**Acceptance**:
- ARCHIVED status filtering works ✓

---

### Scenario 9: Filter by Stock State (Low Stock)

**Goal**: Verify stock state filter identifies items below threshold

**Test Command** (assuming low-stock threshold = 5):
```bash
curl -X GET "http://localhost:8080/api/inventory-items?stockState=LOW_STOCK&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes Resistor Pack (qty 3) and Bolts (qty 8 if threshold > 8, otherwise excluded)
- Items with qty > threshold excluded
- Out-of-stock items excluded
- All returned items have `isLowStock: true`

**Acceptance**:
- Low stock classification works correctly ✓
- Stock state filter identifies items in range (0, threshold] ✓

---

### Scenario 10: Filter by Stock State (Out of Stock)

**Goal**: Verify out-of-stock filter shows zero-quantity items

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?stockState=OUT_OF_STOCK&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes Capacitor Set (qty 0)
- All returned items have `quantity: 0`
- Low-stock and in-stock items excluded

**Acceptance**:
- Out-of-stock classification works ✓
- Stock state filter correctly identifies zero inventory ✓

---

### Scenario 11: Filter by Stock State (In Stock)

**Goal**: Verify in-stock filter shows items above threshold

**Test Command** (assuming low-stock threshold = 5):
```bash
curl -X GET "http://localhost:8080/api/inventory-items?stockState=IN_STOCK&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes LED Bulbs (qty 45) and Screws (qty 200)
- Low-stock and out-of-stock items excluded
- All returned items have `quantity > threshold`

**Acceptance**:
- In-stock classification works ✓
- Stock state filter correctly identifies healthy inventory ✓

---

### Scenario 12: Combine All Filters

**Goal**: Verify complex query with search + all filter dimensions

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?search=bulb&categoryId=1&locationId=1&status=ACTIVE&stockState=IN_STOCK&page=0&size=20"
```

**Expected Result**:
- HTTP 200 OK
- Response includes only LED Bulbs (matches all criteria)
- Other items filtered out for not matching one or more dimensions
- AND logic applies across all filters

**Acceptance**:
- Complex filter combination works ✓
- All dimensions apply correctly ✓
- Query executes efficiently (< 500ms) ✓

---

### Scenario 13: Pagination

**Goal**: Verify pagination splits large result sets

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?page=0&size=2"
```

**Expected Result**:
- HTTP 200 OK
- `content` array contains exactly 2 items
- `pageSize: 2`
- `totalPages > 1` (if 10+ items in inventory)
- `hasNext: true`

**Follow-up Command** (page 1):
```bash
curl -X GET "http://localhost:8080/api/inventory-items?page=1&size=2"
```

**Expected Result**:
- Different items from page 0
- `currentPage: 1`
- `hasPrevious: true`

**Acceptance**:
- Pagination offset works ✓
- Page metadata accurate ✓

---

### Scenario 14: Invalid Filter Parameter

**Goal**: Verify error handling for invalid enum values

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?stockState=OVERSTOCKED"
```

**Expected Result**:
- HTTP 400 Bad Request
- Error code: `INVALID_FILTER`
- Message indicates valid values: `OUT_OF_STOCK, LOW_STOCK, IN_STOCK, ALL`

**Acceptance**:
- Invalid parameter rejected ✓
- Error message helps user correct input ✓

---

### Scenario 15: Non-Existent Category

**Goal**: Verify error handling for invalid foreign key reference

**Test Command**:
```bash
curl -X GET "http://localhost:8080/api/inventory-items?categoryId=9999"
```

**Expected Result**:
- HTTP 404 Not Found
- Error code: `CATEGORY_NOT_FOUND`
- Clear message that category 9999 does not exist

**Acceptance**:
- Invalid reference detected and reported ✓

---

## Frontend Validation Checklist

After backend API is validated, test frontend integration:

- [ ] Search box appears in main inventory view with a Search button or Enter key submission
- [ ] User can type search text and submit (via Enter or Search button) to trigger API call
- [ ] Results update only after search is submitted, not on every keystroke
- [ ] Filter dropdowns for category, location, status, stock state visible and functional
- [ ] Applying filters updates displayed items with AND logic
- [ ] Clearing filters restores full inventory list
- [ ] Empty state message displays when no items match
- [ ] Empty state message is clear and suggests next action
- [ ] Multiple filters can be applied simultaneously
- [ ] Pagination controls visible and functional for large result sets
- [ ] UI reflects all filter dimensions (search + 4 filters)

---

## Success Criteria Validation

| Success Criterion | Test Scenario | Validation |
|------------------|---------------|-----------|
| SC-001: Find item within 10 seconds | Scenarios 1-3 | User locates item via search/filters in < 10 seconds ✓ |
| SC-002: Results within 500ms | Scenario 12 | API response time for complex query measured ✓ |
| SC-003: All filter combinations work | Scenarios 4-12 | Each combination tested and accurate ✓ |
| SC-004: Empty state clear | Scenario 3 | Empty state message prevents user confusion ✓ |
| SC-005: 95% first-attempt success | Manual usability test | Users successfully apply filters without help |
| SC-006: Search/filter UI discoverable | Frontend validation | Controls visible and accessible on main view ✓ |

---

## Done When

✅ All 15 API validation scenarios pass  
✅ All frontend validation checklist items complete  
✅ All success criteria validated  
✅ Performance targets met (500ms response time)  
✅ Error handling robust and user-friendly
