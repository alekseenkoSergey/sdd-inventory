import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { InventoryItemsService } from '../../services/inventory-items.service';
import { InventoryItem, Category, Location, CreateItemFormModel, ItemFilters } from '../../models/inventory-item.model';
import { LoadingSpinnerComponent } from '../../components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../components/error-message/error-message.component';
import { ItemListComponent } from '../../components/item-list/item-list.component';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { ItemFormComponent } from '../../components/item-form/item-form.component';
import { StockInFormComponent } from '../../../stock-movements/movement-form/stock-in-form.component';
import { StockOutFormComponent } from '../../../stock-movements/movement-form/stock-out-form.component';
import { AdjustmentFormComponent } from '../../../stock-movements/movement-form/adjustment-form.component';
import { MovementHistoryModalComponent } from '../../../stock-movements/movement-history-modal/movement-history-modal.component';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'currentPageExtract',
  standalone: true
})
export class CurrentPageExtractPipe implements PipeTransform {
  transform(filters: ItemFilters | null): number {
    return filters?.page ?? 0;
  }
}

@Component({
  selector: 'app-inventory-items-page',
  standalone: true,
  imports: [
    CommonModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent,
    ItemListComponent,
    PaginationComponent,
    ItemFormComponent,
    StockInFormComponent,
    StockOutFormComponent,
    AdjustmentFormComponent,
    MovementHistoryModalComponent,
    CurrentPageExtractPipe
  ],
  template: `
    <div class="inventory-items-page">
      <header class="page-header">
        <div class="header-content">
          <button class="back-btn" (click)="goBack()" title="Back to Home">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
          </button>
          <h1>Inventory Items</h1>
          <button (click)="showCreateForm()" class="btn btn-primary">
            + Create New Item
          </button>
        </div>
      </header>

      <app-error-message
        [error]="error$ | async"
        (retry)="onRetry()"
      ></app-error-message>

      <app-loading-spinner *ngIf="loading$ | async"></app-loading-spinner>

      <div *ngIf="!(loading$ | async) && (items$ | async) as items">
        <app-item-list
          [items]="items"
          (edit)="showEditForm($event)"
          (archiveRestore)="onArchiveRestore($event)"
          (delete)="onDelete($event)"
          (stockIn)="showStockInForm($event)"
          (stockOut)="showStockOutForm($event)"
          (adjustment)="showAdjustmentForm($event)"
          (history)="showHistoryModal($event)"
        ></app-item-list>

        <app-pagination
          [currentPage]="filters$ | async | currentPageExtract"
          [totalPages]="(totalPages$ | async) ?? 0"
          (pageChange)="onPageChange($event)"
        ></app-pagination>
      </div>

      <!-- Modal for create/edit form -->
      <div *ngIf="showForm" class="modal-overlay" (click)="closeForm()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>{{ editingItem ? 'Edit Item' : 'Create New Item' }}</h2>
            <button (click)="closeForm()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-item-form
              [item]="editingItem"
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
      <div *ngIf="showStockInModal && selectedItem" class="modal-overlay" (click)="closeStockInForm()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Record Stock In</h2>
            <button (click)="closeStockInForm()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-stock-in-form
              [itemId]="selectedItem.id"
              (close)="closeStockInForm()"
              (submit)="onStockInSubmit()"
            ></app-stock-in-form>
          </div>
        </div>
      </div>

      <!-- Modal for stock out form -->
      <div *ngIf="showStockOutModal && selectedItem" class="modal-overlay" (click)="closeStockOutForm()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Record Stock Out</h2>
            <button (click)="closeStockOutForm()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-stock-out-form
              [itemId]="selectedItem.id"
              [currentQuantity]="selectedItem.currentQuantity"
              (close)="closeStockOutForm()"
              (submit)="onStockOutSubmit()"
            ></app-stock-out-form>
          </div>
        </div>
      </div>

      <!-- Modal for adjustment form -->
      <div *ngIf="showAdjustmentModal && selectedItem" class="modal-overlay" (click)="closeAdjustmentForm()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Record Adjustment</h2>
            <button (click)="closeAdjustmentForm()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-adjustment-form
              [itemId]="selectedItem.id"
              [currentQuantity]="selectedItem.currentQuantity"
              (close)="closeAdjustmentForm()"
              (submit)="onAdjustmentSubmit()"
            ></app-adjustment-form>
          </div>
        </div>
      </div>

      <!-- Modal for movement history -->
      <div *ngIf="showMovementHistoryModal && selectedItem" class="modal-overlay" (click)="closeHistoryModal()">
        <div class="modal-content modal-large" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Stock Movements</h2>
            <button (click)="closeHistoryModal()" class="close-button">&times;</button>
          </div>
          <div class="modal-body">
            <app-movement-history-modal
              [itemId]="selectedItem.id"
              (close)="closeHistoryModal()"
            ></app-movement-history-modal>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .inventory-items-page {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
      background-color: #f9fafb;
    }

    .page-header {
      background-color: #ffffff;
      border-bottom: 1px solid #e5e7eb;
      padding: 1rem 0;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }

    .header-content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 2rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .back-btn {
      background: none;
      border: none;
      cursor: pointer;
      padding: 0.5rem;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #6b7280;
      transition: color 0.2s;
    }

    .back-btn:hover {
      color: #374151;
    }

    .back-btn svg {
      width: 24px;
      height: 24px;
    }

    .page-header h1 {
      flex: 1;
      margin: 0;
      padding: 0 2rem;
      font-size: 1.875rem;
      font-weight: 700;
      color: #111827;
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 0.375rem;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 500;
    }

    .btn-primary {
      background-color: #4f46e5;
      color: white;
      transition: background-color 0.2s;
    }

    .btn-primary:hover {
      background-color: #4338ca;
    }

    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background-color: white;
      border-radius: 0.5rem;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
      max-width: 600px;
      width: 90%;
      padding: 0;
    }

    .modal-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1.5rem;
      border-bottom: 1px solid #e5e7eb;
    }

    .modal-header h2 {
      margin: 0;
      font-size: 1.25rem;
      font-weight: 600;
      color: #111827;
    }

    .close-button {
      background: none;
      border: none;
      font-size: 1.5rem;
      cursor: pointer;
      color: #6b7280;
      padding: 0;
      width: 2rem;
      height: 2rem;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: color 0.2s;
    }

    .close-button:hover {
      color: #111827;
    }

    .modal-body {
      padding: 1.5rem;
    }

    .modal-large {
      max-width: 900px;
    }
  `]
})
export class InventoryItemsPageComponent implements OnInit {
  showForm = false;
  editingItem: InventoryItem | undefined;
  showStockInModal = false;
  showStockOutModal = false;
  showAdjustmentModal = false;
  showMovementHistoryModal = false;
  selectedItem: InventoryItem | undefined;

  constructor(
    private service: InventoryItemsService,
    private router: Router
  ) {}

  get items$() { return this.service.items$; }
  get loading$() { return this.service.loading$; }
  get error$() { return this.service.error$; }
  get filters$() { return this.service.filters$; }
  get totalPages$() { return this.service.totalPages$; }
  get categories$() { return this.service.categories$; }
  get locations$() { return this.service.locations$; }

  ngOnInit(): void {
    this.service.listItems().subscribe();
    this.service.loadCategories().subscribe();
    this.service.loadLocations().subscribe();
  }

  showCreateForm(): void {
    this.editingItem = undefined;
    this.showForm = true;
  }

  showEditForm(item: InventoryItem): void {
    this.editingItem = item;
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.editingItem = undefined;
  }

  onSave(data: CreateItemFormModel | any): void {
    if (this.editingItem) {
      this.service.updateItem(this.editingItem.id, data).subscribe(() => {
        this.closeForm();
      });
    } else {
      this.service.createItem(data).subscribe(() => {
        this.closeForm();
      });
    }
  }

  onPageChange(page: number): void {
    this.service.setPage(page);
  }

  onArchiveRestore(item: InventoryItem): void {
    if (item.status === 'ACTIVE') {
      this.service.archiveItem(item.id).subscribe();
    } else {
      this.service.restoreItem(item.id).subscribe();
    }
  }

  onDelete(item: InventoryItem): void {
    if (confirm(`Are you sure you want to permanently delete "${item.name}"? This action cannot be undone.`)) {
      this.service.deleteItem(item.id).subscribe();
    }
  }

  onRetry(): void {
    this.service.listItems().subscribe();
  }

  showStockInForm(item: InventoryItem): void {
    this.selectedItem = item;
    this.showStockInModal = true;
  }

  closeStockInForm(): void {
    this.showStockInModal = false;
    this.selectedItem = undefined;
  }

  onStockInSubmit(): void {
    this.closeStockInForm();
    this.service.listItems().subscribe();
  }

  showStockOutForm(item: InventoryItem): void {
    this.selectedItem = item;
    this.showStockOutModal = true;
  }

  closeStockOutForm(): void {
    this.showStockOutModal = false;
    this.selectedItem = undefined;
  }

  onStockOutSubmit(): void {
    this.closeStockOutForm();
    this.service.listItems().subscribe();
  }

  showAdjustmentForm(item: InventoryItem): void {
    this.selectedItem = item;
    this.showAdjustmentModal = true;
  }

  closeAdjustmentForm(): void {
    this.showAdjustmentModal = false;
    this.selectedItem = undefined;
  }

  onAdjustmentSubmit(): void {
    this.closeAdjustmentForm();
    this.service.listItems().subscribe();
  }

  showHistoryModal(item: InventoryItem): void {
    this.selectedItem = item;
    this.showMovementHistoryModal = true;
  }

  closeHistoryModal(): void {
    this.showMovementHistoryModal = false;
    this.selectedItem = undefined;
  }

  goBack(): void {
    this.router.navigate(['/']);
  }
}
