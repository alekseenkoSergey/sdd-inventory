import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryItemsService } from '../../services/inventory-items.service';
import { InventoryItem, Category, Location, EditItemFormModel } from '../../models/inventory-item.model';
import { LoadingSpinnerComponent } from '../../components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../components/error-message/error-message.component';
import { ItemFormComponent } from '../../components/item-form/item-form.component';
import { StockInFormComponent } from '../../../stock-movements/movement-form/stock-in-form.component';
import { StockOutFormComponent } from '../../../stock-movements/movement-form/stock-out-form.component';
import { AdjustmentFormComponent } from '../../../stock-movements/movement-form/adjustment-form.component';
import { MovementHistoryModalComponent } from '../../../stock-movements/movement-history-modal/movement-history-modal.component';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-item-detail-page',
  standalone: true,
  imports: [
    CommonModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent,
    ItemFormComponent,
    StockInFormComponent,
    StockOutFormComponent,
    AdjustmentFormComponent,
    MovementHistoryModalComponent
  ],
  template: `
    <div class="item-detail-page">
      <button (click)="goBack()" class="btn btn-back">← Back to List</button>

      <app-error-message
        [error]="error$ | async"
        (retry)="onRetry()"
      ></app-error-message>

      <app-loading-spinner *ngIf="loading$ | async"></app-loading-spinner>

      <div *ngIf="!(loading$ | async) && item" class="detail-container">
        <div class="detail-header">
          <h1>{{ item.name }}</h1>
          <div class="detail-actions">
            <button (click)="showEditForm()" class="btn btn-primary">Edit</button>
            <button
              (click)="onArchiveRestore()"
              [class]="'btn btn-' + (item.status === 'ARCHIVED' ? 'success' : 'warning')"
            >
              {{ item.status === 'ARCHIVED' ? 'Restore' : 'Archive' }}
            </button>
            <button (click)="onDelete()" class="btn btn-danger">Delete</button>
            <button (click)="showStockInForm()" class="btn btn-secondary">Record Stock In</button>
            <button (click)="showStockOutForm()" class="btn btn-secondary">Record Stock Out</button>
            <button (click)="showAdjustmentForm()" class="btn btn-secondary">Record Adjustment</button>
            <button (click)="showHistoryModal()" class="btn btn-info">View History</button>
          </div>
        </div>

        <div class="detail-content">
          <div class="detail-section">
            <div class="detail-row">
              <span class="label">ID:</span>
              <span class="value">{{ item.id }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Name:</span>
              <span class="value">{{ item.name }}</span>
            </div>
            <div class="detail-row">
              <span class="label">SKU:</span>
              <span class="value">{{ item.sku || '-' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Description:</span>
              <span class="value">{{ item.description || '-' }}</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-row">
              <span class="label">Category:</span>
              <span class="value">{{ item.categoryId }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Location:</span>
              <span class="value">{{ item.locationId }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Unit:</span>
              <span class="value">{{ item.unit }}</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-row">
              <span class="label">Current Quantity:</span>
              <span class="value quantity" [class.low]="item.currentQuantity < item.lowStockThreshold">
                {{ item.currentQuantity }}
              </span>
            </div>
            <div class="detail-row">
              <span class="label">Low Stock Threshold:</span>
              <span class="value">{{ item.lowStockThreshold }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Status:</span>
              <span [class]="'status-badge status-' + item.status.toLowerCase()">
                {{ item.status }}
              </span>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-row">
              <span class="label">Created:</span>
              <span class="value">{{ item.createdDate | date: 'medium' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">Updated:</span>
              <span class="value">{{ item.updatedDate | date: 'medium' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal for edit form -->
      <div *ngIf="showForm && item" class="modal-overlay" (click)="closeForm()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Edit Item</h2>
            <button (click)="closeForm()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-item-form
              [item]="item"
              [categories]="(categories$ | async) ?? []"
              [locations]="(locations$ | async) ?? []"
              [loading]="(loading$ | async) ?? false"
              (save)="onSave($event)"
              (cancel)="closeForm()"
            ></app-item-form>
          </div>
        </div>
      </div>

      <!-- Modal for stock in form -->
      <div *ngIf="showStockInModal && item" class="modal-overlay" (click)="closeStockInForm()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Record Stock In</h2>
            <button (click)="closeStockInForm()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-stock-in-form
              [itemId]="item.id"
              (close)="closeStockInForm()"
              (submit)="onStockInSubmit()"
            ></app-stock-in-form>
          </div>
        </div>
      </div>

      <!-- Modal for stock out form -->
      <div *ngIf="showStockOutModal && item" class="modal-overlay" (click)="closeStockOutForm()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Record Stock Out</h2>
            <button (click)="closeStockOutForm()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-stock-out-form
              [itemId]="item.id"
              [currentQuantity]="item.currentQuantity"
              (close)="closeStockOutForm()"
              (submit)="onStockOutSubmit()"
            ></app-stock-out-form>
          </div>
        </div>
      </div>

      <!-- Modal for adjustment form -->
      <div *ngIf="showAdjustmentModal && item" class="modal-overlay" (click)="closeAdjustmentForm()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Record Adjustment</h2>
            <button (click)="closeAdjustmentForm()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-adjustment-form
              [itemId]="item.id"
              [currentQuantity]="item.currentQuantity"
              (close)="closeAdjustmentForm()"
              (submit)="onAdjustmentSubmit()"
            ></app-adjustment-form>
          </div>
        </div>
      </div>

      <!-- Modal for movement history -->
      <div *ngIf="showMovementHistoryModal && item" class="modal-overlay" (click)="closeHistoryModal()">
        <div class="modal-content modal-large" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Movement History</h2>
            <button (click)="closeHistoryModal()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-movement-history-modal
              [itemId]="item.id"
              (close)="closeHistoryModal()"
            ></app-movement-history-modal>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .item-detail-page {
      padding: 2rem;
      max-width: 800px;
      margin: 0 auto;
    }

    .btn-back {
      margin-bottom: 2rem;
      padding: 0.5rem 1rem;
      background-color: #95a5a6;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.95rem;
    }

    .btn-back:hover {
      background-color: #7f8c8d;
    }

    .detail-container {
      background-color: white;
      border: 1px solid #eee;
      border-radius: 8px;
      overflow: hidden;
    }

    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 2rem;
      background-color: #f9f9f9;
      border-bottom: 1px solid #eee;
    }

    .detail-header h1 {
      margin: 0;
      color: #333;
      flex: 1;
    }

    .detail-actions {
      display: flex;
      gap: 0.5rem;
    }

    .btn {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.9rem;
      font-weight: 500;
      color: white;
    }

    .btn-primary {
      background-color: #3498db;
    }

    .btn-primary:hover {
      background-color: #2980b9;
    }

    .btn-warning {
      background-color: #f39c12;
    }

    .btn-warning:hover {
      background-color: #e67e22;
    }

    .btn-success {
      background-color: #27ae60;
    }

    .btn-success:hover {
      background-color: #229954;
    }

    .btn-danger {
      background-color: #e74c3c;
    }

    .btn-danger:hover {
      background-color: #c0392b;
    }

    .detail-content {
      padding: 2rem;
    }

    .detail-section {
      margin-bottom: 2rem;
      padding-bottom: 1.5rem;
      border-bottom: 1px solid #eee;
    }

    .detail-section:last-child {
      border-bottom: none;
      margin-bottom: 0;
      padding-bottom: 0;
    }

    .detail-row {
      display: grid;
      grid-template-columns: 200px 1fr;
      gap: 1rem;
      padding: 0.5rem 0;
      align-items: center;
    }

    .label {
      font-weight: 600;
      color: #555;
    }

    .value {
      color: #333;
    }

    .quantity {
      font-weight: 500;
    }

    .quantity.low {
      color: #c33;
      font-weight: 600;
    }

    .status-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.85rem;
      font-weight: 500;
    }

    .status-active {
      background-color: #d4edda;
      color: #155724;
    }

    .status-archived {
      background-color: #e2e3e5;
      color: #383d41;
    }

    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 1000;
    }

    .modal-content {
      background-color: white;
      border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      max-width: 600px;
      width: 90%;
      max-height: 90vh;
      overflow-y: auto;
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid #eee;
    }

    .modal-header h2 {
      margin: 0;
      color: #333;
    }

    .close-button {
      background: none;
      border: none;
      font-size: 2rem;
      cursor: pointer;
      color: #999;
      padding: 0;
      width: 40px;
      height: 40px;
    }

    .close-button:hover {
      color: #333;
    }

    .modal-body {
      padding: 1.5rem;
    }

    .modal-large {
      max-width: 900px;
    }

    .btn-secondary {
      background-color: #6c757d;
    }

    .btn-secondary:hover {
      background-color: #5a6268;
    }

    .btn-info {
      background-color: #17a2b8;
    }

    .btn-info:hover {
      background-color: #138496;
    }

    .detail-actions {
      flex-wrap: wrap;
    }
  `]
})
export class ItemDetailPageComponent implements OnInit {
  item: InventoryItem | null = null;
  showForm: boolean = false;
  showStockInModal: boolean = false;
  showStockOutModal: boolean = false;
  showAdjustmentModal: boolean = false;
  showMovementHistoryModal: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: InventoryItemsService,
    private notificationService: NotificationService
  ) {}

  get loading$() { return this.service.loading$; }
  get error$() { return this.service.error$; }
  get categories$() { return this.service.categories$; }
  get locations$() { return this.service.locations$; }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.service.getItem(parseInt(id, 10)).subscribe(
        item => (this.item = item),
        () => this.router.navigate(['/inventory/items'])
      );
    }
    this.service.loadCategories().subscribe();
    this.service.loadLocations().subscribe();
  }

  goBack(): void {
    this.router.navigate(['/inventory/items']);
  }

  showEditForm(): void {
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
  }

  onSave(data: EditItemFormModel): void {
    if (this.item) {
      this.service.updateItem(this.item.id, data).subscribe(updated => {
        this.item = updated;
        this.closeForm();
      });
    }
  }

  showStockInForm(): void {
    this.showStockInModal = true;
  }

  closeStockInForm(): void {
    this.showStockInModal = false;
  }

  onStockInSubmit(): void {
    this.closeStockInForm();
    this.refreshItem();
  }

  showStockOutForm(): void {
    this.showStockOutModal = true;
  }

  closeStockOutForm(): void {
    this.showStockOutModal = false;
  }

  onStockOutSubmit(): void {
    this.closeStockOutForm();
    this.refreshItem();
  }

  showAdjustmentForm(): void {
    this.showAdjustmentModal = true;
  }

  closeAdjustmentForm(): void {
    this.showAdjustmentModal = false;
  }

  onAdjustmentSubmit(): void {
    this.closeAdjustmentForm();
    this.refreshItem();
  }

  showHistoryModal(): void {
    this.showMovementHistoryModal = true;
  }

  closeHistoryModal(): void {
    this.showMovementHistoryModal = false;
  }

  private refreshItem(): void {
    if (this.item) {
      this.service.getItem(this.item.id).subscribe(
        updated => (this.item = updated),
        () => this.notificationService.error('Failed to refresh item')
      );
    }
  }

  onArchiveRestore(): void {
    if (!this.item) return;

    if (this.item.status === 'ACTIVE') {
      this.service.archiveItem(this.item.id).subscribe(updated => {
        this.item = updated;
      });
    } else {
      this.service.restoreItem(this.item.id).subscribe(updated => {
        this.item = updated;
      });
    }
  }

  onDelete(): void {
    if (!this.item) return;

    if (confirm(`Are you sure you want to permanently delete "${this.item.name}"? This action cannot be undone.`)) {
      this.service.deleteItem(this.item.id).subscribe(() => {
        this.router.navigate(['/inventory/items']);
      });
    }
  }

  onRetry(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.service.getItem(parseInt(id, 10)).subscribe(item => (this.item = item));
    }
  }
}
