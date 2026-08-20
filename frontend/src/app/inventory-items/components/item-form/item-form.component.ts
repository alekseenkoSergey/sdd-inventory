import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { InventoryItem, Category, Location, CreateItemFormModel, EditItemFormModel } from '../../models/inventory-item.model';

@Component({
  selector: 'app-item-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="item-form">
      <div class="form-group">
        <label for="name">Name <span class="required">*</span></label>
        <input
          id="name"
          type="text"
          formControlName="name"
          class="form-control"
          (blur)="onFieldBlur('name')"
        />
        <div *ngIf="getFieldError('name')" class="error-text">
          {{ getFieldError('name') }}
        </div>
      </div>

      <div class="form-group">
        <label for="description">Description</label>
        <textarea
          id="description"
          formControlName="description"
          class="form-control"
          rows="3"
          (blur)="onFieldBlur('description')"
        ></textarea>
        <div *ngIf="getFieldError('description')" class="error-text">
          {{ getFieldError('description') }}
        </div>
      </div>

      <div class="form-group">
        <label for="sku">SKU</label>
        <input
          id="sku"
          type="text"
          formControlName="sku"
          class="form-control"
          (blur)="onFieldBlur('sku')"
        />
        <div *ngIf="getFieldError('sku')" class="error-text">
          {{ getFieldError('sku') }}
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="categoryId">Category <span class="required">*</span></label>
          <select
            id="categoryId"
            formControlName="categoryId"
            class="form-control"
            (blur)="onFieldBlur('categoryId')"
          >
            <option value="">Select a category</option>
            <option *ngFor="let cat of categories" [value]="cat.id">
              {{ cat.name }}
            </option>
          </select>
          <div *ngIf="getFieldError('categoryId')" class="error-text">
            {{ getFieldError('categoryId') }}
          </div>
        </div>

        <div class="form-group">
          <label for="locationId">Location <span class="required">*</span></label>
          <select
            id="locationId"
            formControlName="locationId"
            class="form-control"
            (blur)="onFieldBlur('locationId')"
          >
            <option value="">Select a location</option>
            <option *ngFor="let loc of locations" [value]="loc.id">
              {{ loc.name }}
            </option>
          </select>
          <div *ngIf="getFieldError('locationId')" class="error-text">
            {{ getFieldError('locationId') }}
          </div>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label for="unit">Unit <span class="required">*</span></label>
          <input
            id="unit"
            type="text"
            formControlName="unit"
            class="form-control"
            placeholder="e.g., pcs, kg, liters"
            (blur)="onFieldBlur('unit')"
          />
          <div *ngIf="getFieldError('unit')" class="error-text">
            {{ getFieldError('unit') }}
          </div>
        </div>

        <div class="form-group">
          <label for="lowStockThreshold">Low Stock Threshold</label>
          <input
            id="lowStockThreshold"
            type="number"
            formControlName="lowStockThreshold"
            class="form-control"
            (blur)="onFieldBlur('lowStockThreshold')"
          />
          <div *ngIf="getFieldError('lowStockThreshold')" class="error-text">
            {{ getFieldError('lowStockThreshold') }}
          </div>
        </div>
      </div>

      <div *ngIf="!item" class="form-group">
        <label for="initialQuantity">Initial Quantity</label>
        <input
          id="initialQuantity"
          type="number"
          formControlName="initialQuantity"
          class="form-control"
          (blur)="onFieldBlur('initialQuantity')"
        />
        <div *ngIf="getFieldError('initialQuantity')" class="error-text">
          {{ getFieldError('initialQuantity') }}
        </div>
      </div>

      <div *ngIf="item" class="form-group">
        <label>Current Quantity (Read-only)</label>
        <input
          type="number"
          [value]="item.currentQuantity"
          class="form-control"
          disabled
        />
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" [disabled]="form.invalid || loading">
          {{ item ? 'Update' : 'Create' }}
        </button>
        <button type="button" class="btn btn-secondary" (click)="onCancel()" [disabled]="loading">
          Cancel
        </button>
      </div>
    </form>
  `,
  styles: [`
    .item-form {
      max-width: 600px;
      margin: 0 auto;
      padding: 1rem;
    }

    .form-group {
      margin-bottom: 1.5rem;
    }

    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }

    label {
      display: block;
      margin-bottom: 0.5rem;
      font-weight: 500;
      color: #333;
    }

    .required {
      color: #c33;
    }

    .form-control {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #ccc;
      border-radius: 4px;
      font-size: 1rem;
    }

    .form-control:focus {
      outline: none;
      border-color: #3498db;
      box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
    }

    .form-control:disabled {
      background-color: #f5f5f5;
      cursor: not-allowed;
    }

    .error-text {
      color: #c33;
      font-size: 0.85rem;
      margin-top: 0.25rem;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      margin-top: 2rem;
      justify-content: center;
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 500;
    }

    .btn-primary {
      background-color: #3498db;
      color: white;
    }

    .btn-primary:hover:not(:disabled) {
      background-color: #2980b9;
    }

    .btn-secondary {
      background-color: #95a5a6;
      color: white;
    }

    .btn-secondary:hover:not(:disabled) {
      background-color: #7f8c8d;
    }

    .btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `]
})
export class ItemFormComponent implements OnInit {
  @Input() item?: InventoryItem;
  @Input() categories: Category[] = [];
  @Input() locations: Location[] = [];
  @Input() loading: boolean = false;
  @Output() save = new EventEmitter<CreateItemFormModel | EditItemFormModel>();
  @Output() cancel = new EventEmitter<void>();

  form!: FormGroup;
  submitted = false;
  touched: Set<string> = new Set();

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.initForm();
    if (this.item) {
      this.form.patchValue(this.item);
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', Validators.maxLength(1000)],
      sku: ['', Validators.maxLength(100)],
      categoryId: [null, Validators.required],
      locationId: [null, Validators.required],
      unit: ['', [Validators.required, Validators.maxLength(50)]],
      lowStockThreshold: [0, [Validators.min(0)]],
      initialQuantity: this.item ? null : [0, [Validators.min(0)]]
    });
  }

  onFieldBlur(field: string): void {
    this.touched.add(field);
  }

  getFieldError(field: string): string {
    const control = this.form.get(field);
    if (!control || !control.errors || (!this.touched.has(field) && !this.submitted)) {
      return '';
    }

    if (control.errors['required']) {
      return `${this.formatFieldName(field)} is required`;
    }
    if (control.errors['maxlength']) {
      return `${this.formatFieldName(field)} must not exceed ${control.errors['maxlength'].requiredLength} characters`;
    }
    if (control.errors['min']) {
      return `${this.formatFieldName(field)} must be at least ${control.errors['min'].min}`;
    }

    return '';
  }

  private formatFieldName(field: string): string {
    return field
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, str => str.toUpperCase())
      .trim();
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.form.invalid) return;

    const data = this.item ? this.form.getRawValue() : this.form.value;
    this.save.emit(data);
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
