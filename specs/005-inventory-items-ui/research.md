# Research: Inventory Items UI Design Decisions

**Date**: 2026-08-20  
**Feature**: Inventory Items User Interface (005-inventory-items-ui)  
**Context**: Angular 22 frontend integrating with Java/Spring Boot REST API

---

## Decision 1: Form State Management Pattern

**Decision**: Use Reactive Forms with RxJS Subjects for form state, not external state management library

**Chosen Approach**:
- Angular Reactive Forms (FormBuilder, FormGroup, Validators)
- Form state managed in component with RxJS Subjects for observable state
- Service layer provides data transformation and API calls
- No NgRx, Akita, or other state management library

**Rationale**:
- **Simplicity** (Constitution Principle I): Uses Angular built-in capabilities, no external dependencies
- **Testability**: Reactive forms are type-safe and testable without complex mocking
- **Performance**: Direct change detection is efficient for a single-feature scope
- **Learning curve**: Team already familiar with Angular forms; external store adds overhead
- **Scalability**: For 1000 items scope, built-in forms sufficient; can upgrade if needed

**Alternatives Considered**:
1. **Template-driven forms** - Rejected: Less testable, harder to scale, harder to implement complex validation
2. **NgRx store** - Rejected: Overkill for current scope, introduces boilerplate, violates Principle I
3. **Zustand/Redux** - Rejected: External dependency violates Constitution tech stack principle
4. **MobX** - Rejected: Not aligned with Angular ecosystem conventions

**Implementation Details**:
```typescript
// In inventory-items.service.ts
export class InventoryItemsService {
  private itemsSubject = new BehaviorSubject<InventoryItem[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  
  items$ = this.itemsSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();
  
  listItems(page: number, filters?: ItemFilters): Observable<PagedResponse> {
    this.loadingSubject.next(true);
    return this.api.get('/inventory-items', { params }).pipe(
      tap(response => this.itemsSubject.next(response.content)),
      finalize(() => this.loadingSubject.next(false))
    );
  }
}

// In container component
export class InventoryItemsListComponent implements OnInit {
  items$ = this.service.items$;
  loading$ = this.service.loading$;
  
  ngOnInit() {
    this.service.listItems(0).subscribe();
  }
}
```

---

## Decision 2: API Client Strategy

**Decision**: Create base API service wrapping Angular HttpClient with centralized error handling

**Chosen Approach**:
- `core/services/api.service.ts`: Base HTTP client with interceptor
- `core/interceptors/error.interceptor.ts`: Centralized HTTP error handling
- Feature services (`inventory-items.service.ts`) call API service, return domain models
- Error messages standardized across all API calls

**Rationale**:
- **Consistency**: All API errors handled uniformly (aligns with backend's @ControllerAdvice)
- **Testability**: Mock API service in component tests
- **Separation of Concerns**: Components don't know about HTTP details
- **Maintainability**: Error logic in one place, easy to add auth tokens, caching, etc.

**Alternatives Considered**:
1. **Direct HttpClient in components** - Rejected: Scattered error handling, hard to maintain
2. **Custom HTTP wrapper with retry logic** - Deferred: Can add if needed; start simple

**Implementation Details**:
```typescript
// core/services/api.service.ts
@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient, private router: Router) {}
  
  get<T>(path: string, options?: any): Observable<T> {
    return this.http.get<T>(`/api/v1${path}`, options).pipe(
      catchError(error => this.handleError(error))
    );
  }
  
  post<T>(path: string, body: any, options?: any): Observable<T> {
    return this.http.post<T>(`/api/v1${path}`, body, options).pipe(
      catchError(error => this.handleError(error))
    );
  }
  
  private handleError(error: any): Observable<never> {
    // Transform backend error response to user-friendly message
    const message = error?.error?.message || 'An unexpected error occurred';
    // Store error in service for display in component
    return throwError(() => ({ message, status: error?.status }));
  }
}
```

---

## Decision 3: List View Pagination & Filtering

**Decision**: Server-side pagination with query parameters; cache in service; client-side filter state

**Chosen Approach**:
- Backend provides paginated endpoint: `GET /api/v1/inventory-items?page=0&size=20&status=ACTIVE&categoryId=1`
- Frontend maintains filter state (page, size, status, categoryId) in service
- Each filter change triggers new API call
- List component displays current page + pagination controls
- No infinite scroll; use discrete pagination

**Rationale**:
- **Performance**: Don't load 1000+ items; only fetch page on demand
- **Backend alignment**: API already designed for pagination
- **UX**: Explicit pagination (next/prev/jump) is familiar to users
- **Simplicity**: Don't implement complex client-side caching

**Alternatives Considered**:
1. **Client-side pagination** (load all, paginate in memory) - Rejected: Scales poorly, uses memory
2. **Infinite scroll** - Rejected: Doesn't match user workflow; users need to jump to specific page
3. **Virtual scrolling** - Deferred: Unnecessary for current scope

**Implementation Details**:
```typescript
// inventory-items.service.ts
private filterSubject = new BehaviorSubject<ItemFilters>({
  page: 0,
  size: 20,
  status: null,
  categoryId: null
});

filters$ = this.filterSubject.asObservable();

listItems(): Observable<PagedResponse> {
  const filters = this.filterSubject.value;
  const params = new HttpParams()
    .set('page', String(filters.page))
    .set('size', String(filters.size));
  
  if (filters.status) params = params.set('status', filters.status);
  if (filters.categoryId) params = params.set('categoryId', String(filters.categoryId));
  
  return this.api.get('/inventory-items', { params }).pipe(
    tap(response => {
      this.itemsSubject.next(response.content);
      this.totalPagesSubject.next(response.totalPages);
    })
  );
}

setPage(page: number) {
  this.filterSubject.next({ ...this.filterSubject.value, page });
  this.listItems().subscribe();
}

setStatusFilter(status: string | null) {
  this.filterSubject.next({ ...this.filterSubject.value, status, page: 0 });
  this.listItems().subscribe();
}
```

---

## Decision 4: Create/Edit Form Workflow

**Decision**: Single form component (item-form) used for both create and edit; modal dialog wrapper

**Chosen Approach**:
- `item-form` component: Accepts optional initial data (for edit mode)
- Form validation rules consistent with backend (mirror FR-003 through FR-006)
- Create: Open modal with empty form
- Edit: Open modal with item data pre-populated
- Submit: POST for create, PATCH for edit
- Close modal after success
- Show validation errors inline (below field)

**Rationale**:
- **DRY principle**: One form component reduces duplication
- **Consistency**: Same validation rules, same UX for both flows
- **Testability**: Test form once, use in both contexts
- **Constitution Principle I**: Simpler design, fewer components

**Alternatives Considered**:
1. **Separate create/edit components** - Rejected: Duplication, harder to maintain
2. **Page-based edit** (navigate to /edit/:id) - Accepted as alternative: Could use modals or pages
3. **Inline editing** - Rejected: Complex validation, worse UX for detailed forms

**Implementation Details**:
```typescript
// item-form.component.ts
@Component({
  selector: 'app-item-form',
  templateUrl: './item-form.component.html'
})
export class ItemFormComponent implements OnInit {
  @Input() item?: InventoryItem;  // undefined for create, populated for edit
  @Output() save = new EventEmitter<CreateItemRequest | EditItemRequest>();
  @Output() cancel = new EventEmitter<void>();
  
  form: FormGroup;
  submitted = false;
  
  ngOnInit() {
    this.initForm();
    if (this.item) {
      // Edit mode: pre-populate
      this.form.patchValue(this.item);
    }
  }
  
  private initForm() {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', Validators.maxLength(1000)],
      sku: ['', Validators.maxLength(100)],
      categoryId: [null, Validators.required],
      locationId: [null, Validators.required],
      unit: ['', [Validators.required, Validators.maxLength(50)]],
      lowStockThreshold: [0, [Validators.min(0)]],
      initialQuantity: this.item ? { value: null, disabled: true } : 
                       [0, [Validators.min(0)]]
      // Note: currentQuantity never appears in form; it's read-only
    });
  }
  
  onSubmit() {
    this.submitted = true;
    if (this.form.invalid) return;
    
    if (this.item) {
      // Edit: don't include initialQuantity
      this.save.emit({ ...this.form.getRawValue(), initialQuantity: undefined });
    } else {
      // Create: include initialQuantity
      this.save.emit(this.form.getRawValue());
    }
  }
}
```

---

## Decision 5: Archive/Restore Idempotent Behavior

**Decision**: Archive/restore are idempotent actions; clicking multiple times is safe, no error if already in target state

**Chosen Approach**:
- Archive button: Changes status to ARCHIVED; clicking again does nothing (button disabled or hidden)
- Restore button: Changes status to ACTIVE; clicking again does nothing
- No "already archived" error message; operation succeeds silently
- UI reflects current state after each operation

**Rationale**:
- **UX**: Users shouldn't worry about double-clicks
- **API design**: Backend archive/restore endpoints are idempotent (per spec)
- **Simplicity**: Don't need special handling for idempotent edge case
- **Safety**: No unintended side effects from repeat clicks

**Implementation Details**:
```typescript
// item-detail.component.ts
archiveItem(id: number) {
  this.loading = true;
  this.service.archiveItem(id).subscribe(
    () => {
      this.item = { ...this.item, status: 'ARCHIVED' };
      this.loading = false;
    },
    error => {
      this.error = error.message;
      this.loading = false;
    }
  );
}

// Template
<button (click)="archiveItem(item.id)" [disabled]="item.status === 'ARCHIVED' || loading">
  {{ item.status === 'ARCHIVED' ? 'Already Archived' : 'Archive Item' }}
</button>
```

---

## Decision 6: User Data Isolation Enforcement

**Decision**: Server enforces all user data isolation; UI trust backend, add UI-level guard to prevent accidental cross-user access

**Chosen Approach**:
- Backend validates user ownership before returning data (401/404 if not owned)
- Frontend stores authenticated user ID from auth service
- Component filter: Don't show UI for items from other users (if somehow returned)
- Authentication guard on routing to /inventory/items
- Assume API doesn't return items from other users (trust backend)

**Rationale**:
- **Security**: Backend is source of truth for access control
- **Simplicity**: UI doesn't implement complex permission logic
- **Constitution Principle IV**: Backend centralized @ControllerAdvice handles access violations
- **Defense in depth**: UI-level guard is extra layer, not primary defense

**Alternatives Considered**:
1. **Client-side permission checks** - Deferred: Unnecessary if backend properly validates
2. **Complex access control rules in UI** - Rejected: Duplicates backend logic, hard to maintain

---

## Decision 7: Validation Timing & Messages

**Decision**: Validate on blur (immediate feedback) and on submit (final gate); show inline error messages

**Chosen Approach**:
- Form fields show validation errors after blur event (when user leaves field)
- Submit button disabled if form is invalid
- Error messages appear below field in red
- Clear message text: "Name is required" not "name.required"
- Match backend error messages where possible

**Rationale**:
- **UX**: Blur validation provides immediate feedback without annoying the user during typing
- **Accessibility**: Inline error messages stay near field
- **Consistency**: Messages match backend validation rules
- **Responsiveness**: Client-side validation is instant; server validates final submission

**Implementation Details**:
```typescript
// item-form.component.html
<div class="form-group">
  <label for="name">Name <span class="required">*</span></label>
  <input 
    id="name" 
    formControlName="name"
    (blur)="onFieldBlur('name')"
    class="form-control"
  >
  <div *ngIf="form.get('name')?.invalid && (form.get('name')?.touched || submitted)" 
       class="error-message">
    <span *ngIf="form.get('name')?.errors?.['required']">Name is required</span>
    <span *ngIf="form.get('name')?.errors?.['maxlength']">Name must not exceed 255 characters</span>
  </div>
</div>
```

---

## Decision 8: Loading & Error States

**Decision**: Show loading spinner during API calls; show error message with retry option on failure

**Chosen Approach**:
- Loading spinner overlays list/form during async operations
- Skeleton loaders for list rows (optional enhancement)
- Error message displays full API error text + "Retry" button
- Retry button re-attempts failed operation
- Clear error message on successful operation

**Rationale**:
- **UX**: Users know system is working; not hung
- **Feedback**: Clear error messages help users understand what went wrong
- **Recovery**: Retry button doesn't require page refresh
- **Simplicity**: Use standard UI patterns

**Implementation Details**:
```typescript
// inventory-items-list.component.html
<div *ngIf="loading$ | async" class="loading-spinner">
  <app-spinner></app-spinner>
</div>

<div *ngIf="error$ | async as error" class="error-alert">
  <p>{{ error.message }}</p>
  <button (click)="retryLoad()">Retry</button>
</div>

<app-item-list 
  [items]="items$ | async" 
  [totalPages]="totalPages$ | async"
  (page)="onPageChange($event)"
  (edit)="onEdit($event)"
  (archive)="onArchive($event)"
  (delete)="onDelete($event)"
></app-item-list>
```

---

## Decision 9: Date/Time Display Format

**Decision**: Display all timestamps in human-readable format (e.g., "Aug 20, 2026 2:30 PM") using Angular locale pipe

**Chosen Approach**:
- Use Angular's built-in `DatePipe` with format: `short` or `medium`
- Display timestamps in user's browser timezone (local time)
- Server sends ISO-8601 UTC timestamps (e.g., "2026-08-20T14:30:00Z")
- Frontend converts to local time using date pipe

**Rationale**:
- **Usability**: Human-readable format better than ISO-8601 for users
- **Internationalization**: DatePipe respects browser locale (US format, etc.)
- **Built-in**: No external date library needed (no moment.js or date-fns)
- **Consistency**: Matches spec requirement (FR-024)

**Alternatives Considered**:
1. **ISO-8601 display** - Rejected: Not user-friendly
2. **date-fns library** - Rejected: External dependency, DatePipe sufficient
3. **Relative time** ("2 hours ago") - Deferred: Nice to have, not required

**Implementation Details**:
```typescript
// item-list.component.html
<td>{{ item.createdDate | date:'medium' }}</td>

// Result: Aug 20, 2026, 2:30 PM (US locale)
//         or appropriate format for user's locale
```

---

## Decision 10: Component Organization (Smart vs. Dumb)

**Decision**: Use container/presentational component split; containers handle state/API, presentational components are dumb

**Chosen Approach**:
- **Container Components** (in `containers/`):
  - `inventory-items-list/`: Manages list state, calls service, handles pagination
  - `inventory-items-detail/`: Manages item detail state, handles archive/restore
- **Presentational Components** (in `components/`):
  - `item-list/`: Displays items table, pure @Input/@Output
  - `item-form/`: Displays form, pure @Input/@Output
  - `item-detail/`: Displays item detail, pure @Input/@Output
  - `filter-toolbar/`: Displays filters, emits filter changes

**Rationale**:
- **Testability**: Presentational components don't need service mocks
- **Reusability**: Presentational components can be used in different contexts
- **Maintainability**: Clear separation of concerns
- **Architecture**: Aligns with Angular best practices

**Alternatives Considered**:
1. **Everything in smart components** - Rejected: Hard to test, not reusable
2. **Custom state management layer** - Rejected: Too complex, violates Principle I

---

## Decision 11: Delete Confirmation Dialog

**Decision**: Delete requires confirmation modal; non-destructive operations (edit, archive) don't

**Chosen Approach**:
- Delete button → shows modal: "Are you sure you want to permanently delete this item? This action cannot be undone."
- Confirm/Cancel buttons in modal
- Only proceed if user confirms
- No undo/recovery after deletion (per spec: permanent)

**Rationale**:
- **Safety**: Prevents accidental data loss
- **Clarity**: Emphasizes permanence of deletion
- **Consistency**: Spec explicitly requires confirmation (FR-011)
- **UX**: Archive is preferred; delete is secondary

---

## Decision 12: Dropdown Population (Categories/Locations)

**Decision**: Load categories and locations once per session; cache in service; update dropdown from cache

**Chosen Approach**:
- Service fetches categories + locations on app init (or first navigation to feature)
- Store in BehaviorSubject (categories$, locations$)
- Components subscribe to observables
- Dropdowns populated from cached data
- No re-fetch on form open (use cached)
- If categories/locations change, user can refresh page or refresh manually

**Rationale**:
- **Performance**: Don't re-fetch on every form open
- **UX**: Instant dropdown population
- **Simplicity**: No complex cache invalidation
- **Assumption**: Categories/locations rarely change during session

**Alternatives Considered**:
1. **Fetch on every form open** - Rejected: Unnecessary API calls
2. **Real-time cache updates** - Deferred: Not required; users can refresh

---

## Summary of Design Decisions

| Decision | Chosen | Key Benefit |
|----------|--------|------------|
| Form State | RxJS + Angular Forms | Simplicity, testability |
| API Client | Base service + interceptor | Consistency, maintainability |
| Pagination | Server-side + query params | Performance |
| Create/Edit | Single form component | DRY principle |
| Archive/Restore | Idempotent actions | Safe UX |
| User Isolation | Trust backend | Simplicity |
| Validation | Blur + submit | Responsive feedback |
| Loading/Error | Spinner + retry | Clear feedback |
| Dates | DatePipe + local time | User-friendly |
| Components | Smart/dumb split | Testability |
| Delete | Confirmation modal | Safety |
| Dropdowns | Cached once per session | Performance |

All decisions follow **Constitution Principle I (Simplicity)** and align with **Angular best practices**.
