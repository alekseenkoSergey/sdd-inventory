import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InventoryItem } from '../../models/inventory-item.model';

@Component({
  selector: 'app-item-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <table class="items-table" *ngIf="items && items.length > 0">
      <thead>
        <tr>
          <th>Name</th>
          <th>SKU</th>
          <th>Category</th>
          <th>Location</th>
          <th>Quantity</th>
          <th>Unit</th>
          <th>Threshold</th>
          <th>Status</th>
          <th>Created</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let item of items" [class.archived]="item.status === 'ARCHIVED'">
          <td>{{ item.name }}</td>
          <td>{{ item.sku || '-' }}</td>
          <td>{{ item.categoryId }}</td>
          <td>{{ item.locationId }}</td>
          <td class="quantity" [class.low]="item.currentQuantity < item.lowStockThreshold">
            {{ item.currentQuantity }}
          </td>
          <td>{{ item.unit }}</td>
          <td>{{ item.lowStockThreshold }}</td>
          <td>
            <span [class]="'status-badge status-' + item.status.toLowerCase()">
              {{ item.status }}
            </span>
          </td>
          <td>{{ item.createdDate | date: 'medium' }}</td>
          <td class="actions">
            <button (click)="onEdit(item)" class="btn-small btn-edit">Edit</button>
            <button
              (click)="onArchiveRestore(item)"
              [class]="'btn-small btn-' + (item.status === 'ARCHIVED' ? 'restore' : 'archive')"
            >
              {{ item.status === 'ARCHIVED' ? 'Restore' : 'Archive' }}
            </button>
            <button (click)="onDelete(item)" class="btn-small btn-delete">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div *ngIf="!items || items.length === 0" class="empty-state">
      <p>No items found. Create your first item to get started.</p>
    </div>
  `,
  styles: [`
    .items-table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 1rem;
    }

    thead {
      background-color: #f5f5f5;
    }

    th {
      padding: 1rem;
      text-align: left;
      font-weight: 600;
      border-bottom: 2px solid #ddd;
      color: #333;
    }

    td {
      padding: 0.75rem 1rem;
      border-bottom: 1px solid #eee;
    }

    tr.archived {
      opacity: 0.6;
    }

    .quantity {
      text-align: center;
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

    .actions {
      display: flex;
      gap: 0.5rem;
    }

    .btn-small {
      padding: 0.4rem 0.8rem;
      border: none;
      border-radius: 3px;
      cursor: pointer;
      font-size: 0.8rem;
      font-weight: 500;
      color: white;
    }

    .btn-edit {
      background-color: #3498db;
    }

    .btn-edit:hover {
      background-color: #2980b9;
    }

    .btn-archive {
      background-color: #f39c12;
    }

    .btn-archive:hover {
      background-color: #e67e22;
    }

    .btn-restore {
      background-color: #27ae60;
    }

    .btn-restore:hover {
      background-color: #229954;
    }

    .btn-delete {
      background-color: #e74c3c;
    }

    .btn-delete:hover {
      background-color: #c0392b;
    }

    .empty-state {
      padding: 3rem 1rem;
      text-align: center;
      color: #999;
      border: 1px dashed #ddd;
      border-radius: 4px;
      margin-top: 1rem;
    }
  `]
})
export class ItemListComponent {
  @Input() items: InventoryItem[] | null = null;
  @Output() edit = new EventEmitter<InventoryItem>();
  @Output() archiveRestore = new EventEmitter<InventoryItem>();
  @Output() delete = new EventEmitter<InventoryItem>();

  onEdit(item: InventoryItem): void {
    this.edit.emit(item);
  }

  onArchiveRestore(item: InventoryItem): void {
    this.archiveRestore.emit(item);
  }

  onDelete(item: InventoryItem): void {
    this.delete.emit(item);
  }
}
