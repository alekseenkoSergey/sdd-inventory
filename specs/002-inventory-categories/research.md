# Research & Technical Decisions: Inventory Categories

**Phase 0 Output** | **Date**: 2026-08-19

## Executive Summary

No critical technical unknowns exist for this feature. The Inventory Tracker Constitution (v1.0.0) predefined all major technology choices and architectural patterns. This research validates those choices and documents their application to category management.

## Technology Stack Validation

### Backend: Java 17+ with Spring Boot 3.x

**Decision**: Mandated by Constitution Principle II (Technology Stack)

**Rationale**:
- Spring Boot provides built-in support for REST API, dependency injection, and transaction management
- Spring Data JPA simplifies database access with repository pattern
- Spring Security integrates seamlessly for multi-tenant user isolation
- Mature ecosystem with battle-tested concurrency handling (optimistic locking, versioning)

**Validation for This Feature**:
- Category CRUD operations map cleanly to Spring Data repository methods
- Business logic (validation, user isolation) fits naturally into service layer
- REST endpoints for category operations align with existing API patterns

### Database: PostgreSQL with Flyway

**Decision**: Mandated by Constitution Principle II (Technology Stack)

**Rationale**:
- PostgreSQL provides robust foreign key constraints for data integrity
- Supports composite unique indexes (required for case-insensitive name uniqueness per user)
- ACID transactions ensure consistency during concurrent operations
- Flyway versioning ensures schema reproducibility and rollback safety

**Validation for This Feature**:
- Composite unique index on `(userId, LOWER(name))` enforces business rule at database layer
- Foreign key on Item.categoryId prevents orphaned references
- Version column (for optimistic locking) is standard PostgreSQL pattern

### Frontend: Angular with TypeScript

**Decision**: Mandated by Constitution Principle II (Technology Stack)

**Rationale**:
- Angular provides robust component model for UI organization
- Built-in dependency injection and lifecycle hooks simplify state management
- Observable-based RxJS integrates seamlessly with REST API calls
- Reactive forms validation matches backend DTO validation

**Validation for This Feature**:
- Category list component can bind to observable from CategoryService
- Dialog components (create/rename) pair naturally with form validation
- Auto-refresh on conflict (HTTP 409) implemented via observable error handling

## Architectural Pattern: Layered Backend

**Decision**: Mandated by Constitution Principle III (Backend Layered Package Structure)

**Pattern**:
```
HTTP Request
    ↓
Controller (request validation, routing)
    ↓
Service (business logic, transactions)
    ↓
Repository (persistence queries)
    ↓
Database
```

**Validation for Category Feature**:
- **Controller**: CategoryController handles `/api/categories` endpoints, maps DTOs, delegates to service
- **Service**: CategoryService implements validation (uniqueness, deletion guards, user isolation), coordinates with repository
- **Repository**: CategoryRepository uses Spring Data for query generation (no custom SQL needed)
- **DTO Layer**: Separate request/response objects (CreateCategoryRequestDTO, CategoryResponseDTO) for API contract clarity

**Benefit**: Clear separation of concerns prevents business logic from leaking into HTTP layer or persistence layer, simplifying testing and maintenance.

## Exception Handling: Centralized @ControllerAdvice

**Decision**: Mandated by Constitution Principle IV (Centralized Exception Processing)

**Pattern**:
```
Service throws domain exception (e.g., CategoryNameNotUniqueException)
    ↓
GlobalExceptionHandler (@ControllerAdvice) catches exception
    ↓
Translates to HTTP response (status code, error DTO)
    ↓
Returned to client
```

**Validation for Category Feature**:
- Service layer throws custom exceptions (CategoryNameNotUniqueException, CategoryHasItemsException, CategoryNotFoundException)
- No try/catch in controllers; exceptions propagate to centralized handler
- Consistent error response format across all endpoints
- Error messages are user-friendly and include actionable guidance (e.g., "Cannot delete: 5 items assigned. Please reassign items first.")

**Benefit**: Eliminates duplicate error-handling code in controllers; enables consistent error response format; simplifies adding new exception types in the future.

## Concurrency & Data Consistency

**Decision**: Optimistic Locking via JPA @Version

**Rationale**:
- Pessimistic locking (row-level locks) would block concurrent reads, hurting performance for typical category rename/delete operations
- Optimistic locking detects conflicts without locks; retries are application-level responsibility
- For category feature, conflicts are rare (user typically edits one category at a time) but must be detected and communicated clearly

**Implementation**:
- Add `@Version` Long field to Category entity
- On conflict (version mismatch), Spring throws `OptimisticLockException`
- GlobalExceptionHandler catches and returns HTTP 409 (Conflict) with message indicating conflict type
- Frontend receives 409, shows error, refreshes category list automatically

**Testing**:
- Unit test simulates concurrent updates in separate transactions
- Integration test verifies version increment and conflict detection

## Name Uniqueness: Case-Insensitive, Per-User

**Decision**: Database constraint + Application-layer trimming

**Rationale**:
- Database constraint enforces business rule at persistence layer (data integrity guarantee)
- Application layer trims whitespace before checking/storing (improves UX; prevents " Electronics" and "Electronics" being treated as different)
- Case-insensitive uniqueness uses SQL LOWER() function in composite index

**Implementation**:
1. Application layer (CategoryService):
   - Request DTO receives user input with potential whitespace
   - Service trims name: `trimmedName = name.trim()`
   - Service checks uniqueness via repository method: `findByUserIdAndNameIgnoreCase(userId, trimmedName)`
   - If duplicate exists, throw CategoryNameNotUniqueException

2. Database layer:
   - Migration creates unique index: `CREATE UNIQUE INDEX idx_category_user_name ON category(user_id, LOWER(TRIM(name)))`
   - This constraint is database-enforced fallback (prevents race conditions between application checks)

**Testing**:
- Unit test: service rejects "Electronics", " ELECTRONICS ", " electronics " as duplicates (all same after trim + case-insensitive)
- Integration test: two concurrent requests to create "Electronics" and " electronics " — only first succeeds, second gets 400

## User Data Isolation: Spring Security Integration

**Decision**: Service layer filters all queries by authenticated user ID

**Rationale**:
- Spring Security's Authentication object provides current user ID
- Service layer retrieves user ID and includes it in all repository queries
- No raw queries without user context; repository methods enforce filter

**Implementation**:
1. CategoryService receives authenticated user ID (from SecurityContext or method argument)
2. All repository calls include userId filter:
   - `categoryRepository.findByUserIdAndId(userId, categoryId)`
   - `categoryRepository.findAllByUserId(userId)`
   - `categoryRepository.deleteByUserIdAndId(userId, categoryId)`
3. Repository never returns categories belonging to other users

**Testing**:
- Unit test: service method throws exception if user attempts to access category owned by different user
- Integration test: User A creates category, User B cannot see it in their list or modify it

## Item Reassignment: Manual, Blocking Deletion

**Decision**: Delete is blocked if any items reference the category (FK constraint + service validation)

**Rationale**:
- Prevents silent data loss (items orphaned if category deleted)
- Requires explicit user action on each item (reassign to new category or delete item)
- Aligns with "Simplicity First" principle: no automatic migrations, no "Uncategorized" category

**Implementation**:
1. CategoryService.deleteCategory(userId, categoryId) checks item count:
   - `itemCount = itemRepository.countByCategoryId(categoryId)`
   - If itemCount > 0, throw CategoryHasItemsException with count included
   - Otherwise, delete category

2. Database FK constraint (optional but recommended for safety):
   - `ALTER TABLE item ADD CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES category(id)`
   - On delete, MySQL/PostgreSQL would reject deletion if items exist (unless ON DELETE CASCADE, which we explicitly don't use)

**Testing**:
- Unit test: service throws exception with item count when deleting category with items
- Integration test: user attempts to delete category with items — receives 409 Conflict response with item count

## Alternatives Considered & Rejected

### 1. "Uncategorized" Auto-Category on Delete

**Rejected Because**:
- Adds complexity: system must create and manage a reserved category
- Contradicts "Simplicity First" principle (Principle I of Constitution)
- User expectation unclear: does delete move ALL future items to "Uncategorized" too?
- Requires special-casing "Uncategorized" (cannot delete, cannot rename, etc.)

**Decision**: Explicit block deletion was chosen in `/speckit-clarify`.

### 2. Pessimistic Locking for Concurrency

**Rejected Because**:
- Serializes concurrent updates: poor performance for frequent operations
- Deadlock risk increases with number of concurrent users
- Not needed: category conflicts are rare in typical usage
- Optimistic locking with clear conflict messaging is better UX

### 3. Case-Sensitive Name Uniqueness

**Rejected Because**:
- Confuses users: "Electronics" and "electronics" both exist
- Creates data quality issues
- Violates user expectations (most systems treat category names case-insensitively)

### 4. Automatic Item Deletion on Category Delete

**Rejected Because**:
- High data loss risk
- User may accidentally delete inventory items
- Contradicts data integrity best practices

## Conclusion

All design decisions for the Inventory Categories feature align with the Inventory Tracker Constitution and established best practices for Spring Boot, Angular, and PostgreSQL applications. No novel patterns, frameworks, or architectural choices are needed. Implementation can proceed directly to Phase 1 (design artifacts).
