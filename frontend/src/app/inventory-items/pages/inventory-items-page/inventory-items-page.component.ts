import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InventoryItemsService } from '../../services/inventory-items.service';
import { InventoryItem, Category, Location, CreateItemFormModel, ItemFilters } from '../../models/inventory-item.model';
import { LoadingSpinnerComponent } from '../../components/loading-spinner/loading-spinner.component';
import { ErrorMessageComponent } from '../../components/error-message/error-message.component';
import { ItemListComponent } from '../../components/item-list/item-list.component';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { ItemFormComponent } from '../../components/item-form/item-form.component';

@Component({
  selector: 'app-inventory-items-page',
  standalone: true,
  imports: [
    CommonModule,
    LoadingSpinnerComponent,
    ErrorMessageComponent,
    ItemListComponent,
    PaginationComponent,
    ItemFormComponent
  ],
  template: `
    <div class="inventory-items-page">
      <div class="page-header">
        <h1>Inventory Items</h1>
        <button (click)="showCreateForm()" class="btn btn-primary">
          + Create New Item
        </button>
      </div>

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
        ></app-item-list>

        <app-pagination
          [currentPage]="filters$ | async | currentPageExtract"
          [totalPages]="totalPages$ | async"
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
              [categories]="categories$ | async"
              [locations]="locations$ | async"
              [loading]="loading$ | async"
              (save)="onSave($event)"
              (cancel)="closeForm()"
            ></app-item-form>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .inventory-items-page {
      padding: 2rem;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .page-header h1 {
      margin: 0;
      color: #333;
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

    .btn-primary:hover {
      background-color: #2980b9;
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
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .close-button:hover {
      color: #333;
    }

    .modal-body {
      padding: 1.5rem;
    }
  `]
})
export class InventoryItemsPageComponent implements OnInit {
  items$ = this.service.items$;
  loading$ = this.service.loading$;
  error$ = this.service.error$;
  filters$ = this.service.filters$;
  totalPages$ = this.service.totalPages$;
  categories$ = this.service.categories$;
  locations$ = this.service.locations$;

  showForm = false;
  editingItem: InventoryItem | undefined;

  constructor(private service: InventoryItemsService) {}

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
}

// Custom pipe to extract current page from filters
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
