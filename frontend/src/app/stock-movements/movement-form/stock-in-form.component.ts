import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { StockMovementService } from '../../services/stock-movement.service';
import { NotificationService } from '../../services/notification.service';
import { StockMovementValidators } from '../shared/validators';
import { MovementType, CreateStockMovementRequest, StockMovement } from '../../models/stock-movement.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-stock-in-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="stock-in-form">
      <h2>Record Stock In</h2>

      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="quantity">Quantity *</label>
          <input
            id="quantity"
            type="number"
            formControlName="quantity"
            class="form-control"
            placeholder="Enter quantity"
            [disabled]="loading$ | async"
          />
          <div class="error-message" *ngIf="isFieldInvalid('quantity')">
            {{ getErrorMessage('quantity') }}
          </div>
        </div>

        <div class="form-group">
          <label for="reason">Reason (optional)</label>
          <textarea
            id="reason"
            formControlName="reason"
            class="form-control"
            placeholder="Enter reason for stock in (max 500 characters)"
            rows="3"
            [disabled]="loading$ | async"
          ></textarea>
          <div class="error-message" *ngIf="isFieldInvalid('reason')">
            {{ getErrorMessage('reason') }}
          </div>
        </div>

        <div class="form-group">
          <label for="movementDate">Movement Date (optional)</label>
          <input
            id="movementDate"
            type="date"
            formControlName="movementDate"
            class="form-control"
            [disabled]="loading$ | async"
          />
          <div class="error-message" *ngIf="isFieldInvalid('movementDate')">
            {{ getErrorMessage('movementDate') }}
          </div>
        </div>

        <div class="form-actions">
          <button
            type="submit"
            class="btn btn-primary"
            [disabled]="!form.valid || (loading$ | async)"
          >
            {{ (loading$ | async) ? 'Submitting...' : 'Record Stock In' }}
          </button>
          <button
            type="button"
            class="btn btn-secondary"
            (click)="onCancel()"
            [disabled]="loading$ | async"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .stock-in-form {
      padding: 1.5rem;
      max-width: 500px;
    }

    h2 {
      margin-bottom: 1.5rem;
      font-size: 1.25rem;
    }

    .form-group {
      margin-bottom: 1.5rem;
    }

    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      font-size: 0.95rem;
    }

    .form-control {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 0.95rem;
      font-family: inherit;
    }

    .form-control:focus {
      outline: none;
      border-color: #007bff;
      box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
    }

    .form-control:disabled {
      background-color: #f8f9fa;
      cursor: not-allowed;
    }

    .error-message {
      color: #dc3545;
      font-size: 0.85rem;
      margin-top: 0.25rem;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      margin-top: 2rem;
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 4px;
      font-size: 0.95rem;
      cursor: pointer;
      flex: 1;
      font-weight: 500;
    }

    .btn-primary {
      background-color: #007bff;
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background-color: #0056b3;
    }

    .btn-secondary {
      background-color: #6c757d;
      color: white;
    }

    .btn-secondary:hover:not(:disabled) {
      background-color: #5a6268;
    }

    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  `]
})
export class StockInFormComponent implements OnInit, OnDestroy {
  @Input() itemId!: number;
  @Output() close = new EventEmitter<void>();
  @Output() submit = new EventEmitter<StockMovement>();

  form: FormGroup;
  loading$ = this.stockMovementService.loading$;
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private stockMovementService: StockMovementService,
    private notificationService: NotificationService
  ) {
    this.form = this.fb.group({
      quantity: ['', [Validators.required, StockMovementValidators.quantity()]],
      reason: ['', [StockMovementValidators.reason()]],
      movementDate: ['', [StockMovementValidators.date()]]
    });
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSubmit(): void {
    if (!this.form.valid) {
      return;
    }

    const request: CreateStockMovementRequest = {
      movementType: MovementType.STOCK_IN,
      quantity: parseInt(this.form.get('quantity')?.value, 10),
      reason: this.form.get('reason')?.value || undefined,
      movementDate: this.form.get('movementDate')?.value || undefined
    };

    this.stockMovementService.createMovement(this.itemId, request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (movement) => {
          this.notificationService.success('Stock in recorded successfully');
          this.submit.emit(movement);
        },
        error: (error) => {
          this.notificationService.error(error.message);
        }
      });
  }

  onCancel(): void {
    this.close.emit();
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.form.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (!field || !field.errors) {
      return '';
    }

    const errors = field.errors;

    if (errors['required']) {
      return `${this.getFieldLabel(fieldName)} is required`;
    }
    if (errors['notANumber']) {
      return 'Please enter a valid number';
    }
    if (errors['minValue']) {
      return 'Quantity must be greater than 0';
    }
    if (errors['notAnInteger']) {
      return 'Quantity must be a whole number';
    }
    if (errors['maxLength']) {
      return `${this.getFieldLabel(fieldName)} cannot exceed ${errors['maxLength'].max} characters`;
    }
    if (errors['invalidDateFormat']) {
      return 'Please enter a valid date';
    }
    if (errors['invalidDate']) {
      return 'Please enter a valid date';
    }

    return 'Invalid input';
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      quantity: 'Quantity',
      reason: 'Reason',
      movementDate: 'Movement Date'
    };
    return labels[fieldName] || fieldName;
  }
}
