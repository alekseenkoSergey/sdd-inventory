import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pagination-container">
      <button
        (click)="onPrevious()"
        [disabled]="currentPage === 0"
        class="pagination-button"
      >
        Previous
      </button>

      <span class="pagination-info">
        Page {{ currentPage + 1 }} of {{ totalPages }}
      </span>

      <button
        (click)="onNext()"
        [disabled]="currentPage === totalPages - 1"
        class="pagination-button"
      >
        Next
      </button>
    </div>
  `,
  styles: [`
    .pagination-container {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      margin-top: 1rem;
      border-top: 1px solid #eee;
    }

    .pagination-button {
      padding: 0.5rem 1rem;
      background-color: #f0f0f0;
      border: 1px solid #ccc;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.9rem;
    }

    .pagination-button:hover:not(:disabled) {
      background-color: #e0e0e0;
    }

    .pagination-button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .pagination-info {
      min-width: 150px;
      text-align: center;
      color: #666;
    }
  `]
})
export class PaginationComponent {
  @Input() currentPage: number = 0;
  @Input() totalPages: number = 0;
  @Output() pageChange = new EventEmitter<number>();

  onPrevious(): void {
    if (this.currentPage > 0) {
      this.pageChange.emit(this.currentPage - 1);
    }
  }

  onNext(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.pageChange.emit(this.currentPage + 1);
    }
  }
}
