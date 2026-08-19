# Frontend UI Contracts: Location Management

**Date**: 2026-08-19  
**Feature**: Location Management  
**Framework**: Angular 16+

## Component Interfaces & State

### LocationListComponent

**Input/Output**:
```typescript
@Component({
  selector: 'app-location-list',
  templateUrl: './location-list.component.html'
})
export class LocationListComponent implements OnInit {
  locations$: Observable<Location[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  
  onCreateClick(): void
  onRenameClick(location: Location): void
  onDeleteClick(location: Location): void
  onRefresh(): void
}
```

**State Management**:
- `locations$`: Observable of Location[] from LocationService.getLocations()
- `loading$`: True while API request in flight
- `error$`: Error message if operation failed, null otherwise
- Clear error after 5 seconds or when new operation starts

**Component Behavior**:
- On init: Load locations from service
- On create/rename/delete success: Refresh locations list
- On error: Display error message, keep list visible
- Support manual refresh via Refresh button

---

### LocationFormComponent

**Input/Output**:
```typescript
@Component({
  selector: 'app-location-form',
  templateUrl: './location-form.component.html'
})
export class LocationFormComponent {
  @Input() mode: 'create' | 'edit' = 'create';
  @Input() initialName?: string;
  @Output() submit = new EventEmitter<string>();
  @Output() cancel = new EventEmitter<void>();
  
  form: FormGroup;
  loading = false;
  error: string | null = null;
  
  onSubmit(): void
  onCancel(): void
}
```

**Form Validation**:
```typescript
name: [
  '',
  [
    Validators.required,
    Validators.minLength(1),
    Validators.maxLength(255),
    this.whitespaceValidator
  ]
]
```

**Validation Rules**:
- Required: Must have value
- Min length: 1 character
- Max length: 255 characters
- No whitespace-only strings (custom validator)

**Error Messages**:
- "Location name is required" — empty field
- "Location name cannot be empty or whitespace-only" — whitespace only
- "Location name must be between 1 and 255 characters" — length exceeded
- "[Backend error message]" — API error (e.g., duplicate name)

**Behavior**:
- Pre-fill name field if mode='edit' and initialName provided
- Submit button disabled while form invalid or request in flight
- Submit button text: "Create" (mode=create) or "Update" (mode=edit)
- On successful submission: Emit submit event with name string
- On cancel: Emit cancel event, close modal
- Display loading state (spinner) during API call
- Display backend errors below form

---

### LocationService

**API Methods**:
```typescript
@Injectable()
export class LocationService {
  constructor(private http: HttpClient) {}
  
  createLocation(name: string): Observable<Location> {
    // POST /locations with { name }
    // Return Location object on 201
    // Transform error to user-friendly message
  }
  
  getLocations(): Observable<Location[]> {
    // GET /locations
    // Return array of Location objects
  }
  
  getLocation(id: number): Observable<Location> {
    // GET /locations/{id}
    // Return single Location object
  }
  
  renameLocation(id: number, name: string): Observable<Location> {
    // PUT /locations/{id} with { name }
    // Return updated Location object on 200
    // Transform error to user-friendly message
  }
  
  deleteLocation(id: number): Observable<void> {
    // DELETE /locations/{id}
    // Return void on 204
    // Transform error to user-friendly message
  }
}
```

**Error Transformation**:
```typescript
// Transform HTTP errors to user-friendly messages
private handleError(error: HttpErrorResponse): Observable<never> {
  let message = 'An error occurred';
  
  if (error.status === 409) {
    if (error.error?.error === 'LOCATION_NAME_NOT_UNIQUE') {
      message = 'A location with this name already exists. Please choose a different name.';
    } else if (error.error?.error === 'LOCATION_HAS_ITEMS') {
      message = `Cannot delete location with items. Please remove or reassign items first. (itemCount: ${error.error?.itemCount || '?'})`;
    }
  } else if (error.status === 404) {
    message = 'Location not found. It may have been deleted.';
  } else if (error.status === 400) {
    message = error.error?.message || 'Invalid input. Please check your entries.';
  } else if (error.status === 0) {
    message = 'Unable to connect. Please check your connection and try again.';
  }
  
  return throwError(() => new Error(message));
}
```

---

### LocationModel

**TypeScript Interfaces**:
```typescript
export interface Location {
  id: number;
  userId: number;
  name: string;
  createdAt: string;  // ISO 8601 timestamp
  updatedAt: string;  // ISO 8601 timestamp
}

export interface CreateLocationRequest {
  name: string;
}

export interface RenameLocationRequest {
  name: string;
}

export interface LocationResponse extends Location {}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;  // Error code (e.g., 'LOCATION_NAME_NOT_UNIQUE')
  message: string; // Human-readable message
  path: string;   // Request path
}
```

---

### LocationsModule

**Module Definition**:
```typescript
@NgModule({
  declarations: [
    LocationListComponent,
    LocationFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    HttpClientModule,
    // Dialog/Modal module if using Material or other library
  ],
  providers: [
    LocationService
  ],
  exports: [
    LocationListComponent
  ]
})
export class LocationsModule {}
```

**Usage in App**:
```typescript
// In AppModule or routing module
import { LocationsModule } from './features/locations/locations.module';

@NgModule({
  imports: [
    // ... other modules
    LocationsModule
  ]
})
export class AppModule {}
```

---

## UI Layout & Templates

### Location List Template

```html
<div class="locations-container">
  <div class="locations-header">
    <h1>My Locations</h1>
    <button (click)="onCreateClick()" class="btn-primary">
      + Create Location
    </button>
    <button (click)="onRefresh()" class="btn-secondary" [disabled]="(loading$ | async)">
      ↻ Refresh
    </button>
  </div>

  <div *ngIf="(error$ | async) as error" class="alert alert-error">
    {{ error }}
    <button (click)="error$ | async = null" class="close">×</button>
  </div>

  <div *ngIf="(loading$ | async) && !(locations$ | async)?.length" class="spinner">
    Loading locations...
  </div>

  <table *ngIf="(locations$ | async) as locations" class="locations-table">
    <thead>
      <tr>
        <th>Name</th>
        <th>Created</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      <tr *ngFor="let location of locations">
        <td>{{ location.name }}</td>
        <td>{{ location.createdAt | date: 'short' }}</td>
        <td>
          <button (click)="onRenameClick(location)" class="btn-secondary btn-sm">
            Rename
          </button>
          <button (click)="onDeleteClick(location)" class="btn-danger btn-sm">
            Delete
          </button>
        </td>
      </tr>
    </tbody>
  </table>

  <p *ngIf="(locations$ | async)?.length === 0" class="no-data">
    No locations yet. Create one to get started!
  </p>
</div>
```

### Location Form Template

```html
<div class="location-form-modal">
  <div class="modal-header">
    <h2>{{ mode === 'create' ? 'Create Location' : 'Rename Location' }}</h2>
    <button (click)="onCancel()" class="close">×</button>
  </div>

  <form [formGroup]="form" (ngSubmit)="onSubmit()" class="modal-body">
    <div class="form-group">
      <label for="name">Location Name *</label>
      <input
        id="name"
        type="text"
        formControlName="name"
        maxlength="255"
        placeholder="e.g., Home Office, Warehouse"
        class="form-input"
        autofocus
      />
      <div *ngIf="form.get('name')?.invalid && form.get('name')?.touched" class="error-message">
        <span *ngIf="form.get('name')?.errors?.['required']">
          Location name is required
        </span>
        <span *ngIf="form.get('name')?.errors?.['maxlength']">
          Location name must be between 1 and 255 characters
        </span>
      </div>
    </div>

    <div *ngIf="error" class="alert alert-error">
      {{ error }}
    </div>

    <div class="modal-footer">
      <button type="button" (click)="onCancel()" class="btn-secondary">
        Cancel
      </button>
      <button
        type="submit"
        class="btn-primary"
        [disabled]="form.invalid || loading"
      >
        <span *ngIf="loading" class="spinner-inline">⌛</span>
        {{ mode === 'create' ? 'Create' : 'Update' }}
      </button>
    </div>
  </form>
</div>
```

---

## Styling Classes

**Button Classes**:
- `.btn-primary` — Primary action (Create, Update, Confirm)
- `.btn-secondary` — Secondary action (Cancel, Refresh)
- `.btn-danger` — Destructive action (Delete)
- `.btn-sm` — Small button (inline actions in list)
- `[disabled]` — Disabled state

**Alert Classes**:
- `.alert-error` — Error message styling
- `.alert-success` — Success message (optional)

**Form Classes**:
- `.form-input` — Text input field
- `.form-group` — Container for label + input + error
- `.error-message` — Validation error text

**Table Classes**:
- `.locations-table` — Main table styling
- `.spinner` — Loading indicator
- `.no-data` — Empty state message

---

## Reactive Forms Patterns

### FormBuilder Setup

```typescript
constructor(private fb: FormBuilder, private locationService: LocationService) {}

ngOnInit(): void {
  this.form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(255), this.whitespaceValidator]]
  });
  
  if (this.mode === 'edit' && this.initialName) {
    this.form.patchValue({ name: this.initialName });
  }
}

private whitespaceValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  return /^\s+$/.test(control.value) ? { whitespace: true } : null;
}

onSubmit(): void {
  if (this.form.invalid) return;
  
  this.loading = true;
  this.error = null;
  
  const name = this.form.get('name')!.value;
  const operation$ = this.mode === 'create'
    ? this.locationService.createLocation(name)
    : this.locationService.renameLocation(this.locationId, name);
  
  operation$.subscribe({
    next: (result) => {
      this.loading = false;
      this.submit.emit(name);
    },
    error: (err) => {
      this.loading = false;
      this.error = err.message;
    }
  });
}
```

---

## HTTP Interceptor Integration

**Expected**: Application already has HTTP interceptor that:
- Adds Authorization header with OAuth2 token
- Handles response intercepting
- Implements error handling middleware

**LocationService** assumes interceptor handles:
- Token injection (no need for manual headers)
- Standard error responses
- Request/response logging

---

## Accessibility & ARIA

**Form Labels**:
```html
<label for="name" aria-label="Location name">Location Name *</label>
<input id="name" aria-required="true" aria-invalid="false" />
```

**Error Association**:
```html
<input aria-describedby="name-error" />
<div id="name-error" role="alert">Location name is required</div>
```

**Modal Dialog**:
```html
<div role="dialog" aria-modal="true" aria-labelledby="dialog-title">
  <h2 id="dialog-title">Create Location</h2>
</div>
```

---

## Performance Considerations

- **OnPush Change Detection**: Consider using ChangeDetectionStrategy.OnPush in components
- **Unsubscribe**: Use `takeUntil` pattern or async pipe to prevent memory leaks
- **Virtual Scrolling**: If location list exceeds 1000 items, implement CDK virtual scrolling
- **Lazy Loading**: Load LocationsModule only when user navigates to locations page

---

## Testing Requirements

### Unit Tests (Jasmine/Karma)

- LocationService HTTP calls (mock HttpClient)
- LocationListComponent list display logic
- LocationFormComponent form validation
- Error handling and transformation

### E2E Tests (Cypress/Playwright)

- Create location via UI
- Rename location via UI
- Delete empty location
- Verify duplicate name error
- Verify delete blocked for non-empty location
- Verify list updates after CRUD operations

**Example Cypress Test**:
```typescript
it('should create a new location', () => {
  cy.visit('/locations');
  cy.contains('Create Location').click();
  cy.get('input#name').type('My New Location');
  cy.get('button').contains('Create').click();
  cy.contains('My New Location').should('be.visible');
});
```

---

## Styling Guide

Use existing project conventions. Suggested structure:
- Global styles: `styles.css` (already exists)
- Component styles: `location-list.component.css`, `location-form.component.css`
- Responsive breakpoints: Mobile first (320px, 768px, 1024px)
- Dark mode: Follow project's theme handling if applicable

---

## Browser Compatibility

Target modern browsers:
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

Avoid ES2015+ features not supported by target browsers; use Angular's build pipeline for transpilation.
