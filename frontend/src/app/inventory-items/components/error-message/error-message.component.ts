import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiError } from '../../models/inventory-item.model';

@Component({
  selector: 'app-error-message',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="error-container" *ngIf="error">
      <div class="error-content">
        <p class="error-message">{{ error.message }}</p>
        <button (click)="onRetry()" class="retry-button">Retry</button>
      </div>
    </div>
  `,
  styles: [`
    .error-container {
      margin-bottom: 1rem;
      padding: 1rem;
      background-color: #fee;
      border: 1px solid #fcc;
      border-radius: 4px;
    }

    .error-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
    }

    .error-message {
      color: #c33;
      margin: 0;
      flex: 1;
    }

    .retry-button {
      padding: 0.5rem 1rem;
      background-color: #c33;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.9rem;
    }

    .retry-button:hover {
      background-color: #a22;
    }
  `]
})
export class ErrorMessageComponent {
  @Input() error: ApiError | null = null;
  @Output() retry = new EventEmitter<void>();

  onRetry(): void {
    this.retry.emit();
  }
}
