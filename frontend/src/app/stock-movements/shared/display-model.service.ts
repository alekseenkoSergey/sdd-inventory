import { Injectable } from '@angular/core';
import { MovementType, AdjustmentDirection, StockMovement } from '../../models/stock-movement.model';

export interface DisplayMovement {
  id: number;
  itemId: number;
  movementType: string;
  movementTypeLabel: string;
  quantity: number;
  adjustmentDirection?: string;
  adjustmentDirectionLabel?: string;
  reason?: string;
  movementDate: string;
  createdDate: string;
  formattedMovementDate: string;
  formattedCreatedDate: string;
  itemCurrentQuantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class DisplayModelService {
  getMovementTypeLabel(type: MovementType | string): string {
    const labels: { [key: string]: string } = {
      [MovementType.OPENING_BALANCE]: 'Opening Balance',
      [MovementType.STOCK_IN]: 'Stock In',
      [MovementType.STOCK_OUT]: 'Stock Out',
      [MovementType.ADJUSTMENT]: 'Adjustment'
    };
    return labels[type] || type;
  }

  getAdjustmentDirectionLabel(direction: AdjustmentDirection | string): string {
    const labels: { [key: string]: string } = {
      [AdjustmentDirection.INCREASE]: 'Increase',
      [AdjustmentDirection.DECREASE]: 'Decrease'
    };
    return labels[direction] || direction;
  }

  formatDate(dateString: string, format: 'date' | 'datetime' = 'date'): string {
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        return dateString;
      }

      if (format === 'date') {
        return date.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'short',
          day: 'numeric'
        });
      } else {
        return date.toLocaleString('en-US', {
          year: 'numeric',
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      }
    } catch {
      return dateString;
    }
  }

  transformMovementForDisplay(movement: StockMovement): DisplayMovement {
    return {
      id: movement.id,
      itemId: movement.itemId,
      movementType: movement.movementType,
      movementTypeLabel: this.getMovementTypeLabel(movement.movementType),
      quantity: movement.quantity,
      adjustmentDirection: movement.adjustmentDirection,
      adjustmentDirectionLabel: movement.adjustmentDirection
        ? this.getAdjustmentDirectionLabel(movement.adjustmentDirection)
        : undefined,
      reason: movement.reason,
      movementDate: movement.movementDate,
      createdDate: movement.createdDate,
      formattedMovementDate: this.formatDate(movement.movementDate, 'date'),
      formattedCreatedDate: this.formatDate(movement.createdDate, 'datetime'),
      itemCurrentQuantity: movement.itemCurrentQuantity
    };
  }

  transformMovementsForDisplay(movements: StockMovement[]): DisplayMovement[] {
    return movements.map(m => this.transformMovementForDisplay(m));
  }
}
