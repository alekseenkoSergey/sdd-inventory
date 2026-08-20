import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { StockMovementService } from '../../services/stock-movement.service';
import { DisplayModelService, DisplayMovement } from '../shared/display-model.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-movement-history-modal',
  standalone: true,
  imports: [CommonModule, DatePipe, ReactiveFormsModule],
  template: `
    <div class="movement-history-modal">
      <h2>Movement History</h2>

      <div class="filters">
        <form [formGroup]="filterForm" (ngSubmit)="onApplyFilter()">
          <div class="filter-row">
            <div class="filter-group">
              <label for="startDate">Start Date</label>
              <input
                id="startDate"
                type="date"
                formControlName="startDate"
                class="form-control"
                [disabled]="loading$ | async"
              />
            </div>
            <div class="filter-group">
              <label for="endDate">End Date</label>
              <input
                id="endDate"
                type="date"
                formControlName="endDate"
                class="form-control"
                [disabled]="loading$ | async"
              />
            </div>
            <div class="filter-actions">
              <button
                type="submit"
                class="btn btn-small btn-primary"
                [disabled]="loading$ | async"
              >
                Apply Filter
              </button>
              <button
                type="button"
                class="btn btn-small btn-secondary"
                (click)="onClearFilter()"
                [disabled]="loading$ | async"
              >
                Clear
              </button>
            </div>
          </div>
        </form>
      </div>

      <div *ngIf="loading$ | async" class="loading">
        <div class="spinner"></div>
        Loading movements...
      </div>

      <div *ngIf="!(loading$ | async)" class="movements-container">
        <div *ngIf="movements.length === 0" class="empty-state">
          <p>{{ hasFilters ? 'No movements in this date range.' : 'No movements recorded for this item.' }}</p>
        </div>

        <table *ngIf="movements.length > 0" class="movements-table">
          <thead>
            <tr>
              <th>Type</th>
              <th>Quantity</th>
              <th>Direction</th>
              <th>Reason</th>
              <th>Movement Date</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let movement of movements" class="movement-row">
              <td class="type-cell">
                <span [class]="'type-badge type-' + movement.movementType.toLowerCase()">
                  {{ movement.movementTypeLabel }}
                </span>
              </td>
              <td class="quantity-cell">{{ movement.quantity }}</td>
              <td class="direction-cell">
                <span *ngIf="movement.adjustmentDirectionLabel" class="direction-badge">
                  {{ movement.adjustmentDirectionLabel }}
                </span>
                <span *ngIf="!movement.adjustmentDirectionLabel" class="text-muted">—</span>
              </td>
              <td class="reason-cell">{{ movement.reason || '—' }}</td>
              <td class="date-cell">{{ movement.formattedMovementDate }}</td>
              <td class="date-cell">{{ movement.formattedCreatedDate }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="modal-actions">
        <button class="btn btn-secondary" (click)="onClose()">
          Close
        </button>
      </div>
    </div>
  `,
  styles: [`
    .movement-history-modal {
      padding: 1.5rem;
      max-width: 900px;
      max-height: 600px;
      display: flex;
      flex-direction: column;
    }

    h2 {
      margin-bottom: 1.5rem;
      font-size: 1.25rem;
    }

    .loading {
      display: flex;
      align-items: center;
      gap: 1rem;
      justify-content: center;
      padding: 2rem;
      color: #666;
    }

    .spinner {
      width: 20px;
      height: 20px;
      border: 3px solid #ddd;
      border-top-color: #007bff;
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      to {
        transform: rotate(360deg);
      }
    }

    .movements-container {
      flex: 1;
      overflow-y: auto;
      margin-bottom: 1rem;
    }

    .empty-state {
      text-align: center;
      padding: 2rem;
      color: #666;
    }

    .movements-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.9rem;
    }

    .movements-table thead {
      background-color: #f8f9fa;
      position: sticky;
      top: 0;
    }

    .movements-table th {
      padding: 0.75rem;
      text-align: left;
      font-weight: 600;
      border-bottom: 2px solid #dee2e6;
    }

    .movements-table tbody tr {
      border-bottom: 1px solid #dee2e6;
    }

    .movements-table tbody tr:hover {
      background-color: #f8f9fa;
    }

    .movements-table td {
      padding: 0.75rem;
    }

    .type-badge {
      display: inline-block;
      padding: 0.25rem 0.75rem;
      border-radius: 4px;
      font-size: 0.85rem;
      font-weight: 500;
      white-space: nowrap;
    }

    .type-stock_in {
      background-color: #d4edda;
      color: #155724;
    }

    .type-stock_out {
      background-color: #f8d7da;
      color: #721c24;
    }

    .type-adjustment {
      background-color: #cce5ff;
      color: #004085;
    }

    .type-opening_balance {
      background-color: #d1ecf1;
      color: #0c5460;
    }

    .direction-badge {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      background-color: #e2e3e5;
      border-radius: 3px;
      font-size: 0.8rem;
      white-space: nowrap;
    }

    .quantity-cell {
      font-weight: 500;
      text-align: right;
    }

    .reason-cell {
      max-width: 150px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .date-cell {
      white-space: nowrap;
      font-size: 0.85rem;
      color: #666;
    }

    .text-muted {
      color: #999;
    }

    .filters {
      background-color: #f8f9fa;
      padding: 1rem;
      border-radius: 4px;
      margin-bottom: 1rem;
    }

    .filter-row {
      display: flex;
      gap: 1rem;
      align-items: flex-end;
      flex-wrap: wrap;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .filter-group label {
      font-size: 0.85rem;
      font-weight: 500;
    }

    .filter-group .form-control {
      padding: 0.5rem;
      font-size: 0.9rem;
    }

    .filter-actions {
      display: flex;
      gap: 0.5rem;
    }

    .btn-small {
      padding: 0.5rem 1rem;
      font-size: 0.9rem;
    }

    .modal-actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
      margin-top: 1rem;
      border-top: 1px solid #dee2e6;
      padding-top: 1rem;
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border: none;
      border-radius: 4px;
      font-size: 0.95rem;
      cursor: pointer;
      font-weight: 500;
    }

    .btn-secondary {
      background-color: #6c757d;
      color: white;
    }

    .btn-secondary:hover {
      background-color: #5a6268;
    }
  `]
})
export class MovementHistoryModalComponent implements OnInit, OnDestroy {
  @Input() itemId!: number;
  @Output() close = new EventEmitter<void>();

  loading$ = this.stockMovementService.loading$;
  movements: DisplayMovement[] = [];
  filterForm: FormGroup;
  hasFilters = false;
  private destroy$ = new Subject<void>();

  constructor(
    private stockMovementService: StockMovementService,
    private displayModelService: DisplayModelService,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      startDate: [''],
      endDate: ['']
    });
  }

  ngOnInit(): void {
    this.loadMovements();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadMovements(): void {
    this.stockMovementService.getMovementHistory(this.itemId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const displayMovements = this.displayModelService.transformMovementsForDisplay(response.movements);
          this.movements = displayMovements.sort((a, b) => {
            return new Date(a.movementDate).getTime() - new Date(b.movementDate).getTime();
          });
        },
        error: (error) => {
          console.error('Failed to load movement history:', error);
          this.movements = [];
        }
      });
  }

  onApplyFilter(): void {
    const startDate = this.filterForm.get('startDate')?.value;
    const endDate = this.filterForm.get('endDate')?.value;

    this.hasFilters = !!(startDate || endDate);

    this.stockMovementService.getMovementHistory(this.itemId, startDate, endDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const displayMovements = this.displayModelService.transformMovementsForDisplay(response.movements);
          this.movements = displayMovements.sort((a, b) => {
            return new Date(a.movementDate).getTime() - new Date(b.movementDate).getTime();
          });
        },
        error: (error) => {
          console.error('Failed to load movement history:', error);
          this.movements = [];
        }
      });
  }

  onClearFilter(): void {
    this.filterForm.reset();
    this.hasFilters = false;
    this.loadMovements();
  }

  onClose(): void {
    this.close.emit();
  }
}
