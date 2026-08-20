export enum MovementType {
  OPENING_BALANCE = 'OPENING_BALANCE',
  STOCK_IN = 'STOCK_IN',
  STOCK_OUT = 'STOCK_OUT',
  ADJUSTMENT = 'ADJUSTMENT'
}

export enum AdjustmentDirection {
  INCREASE = 'INCREASE',
  DECREASE = 'DECREASE'
}

export interface StockMovement {
  id: number;
  itemId: number;
  movementType: MovementType;
  quantity: number;
  adjustmentDirection?: AdjustmentDirection;
  reason?: string;
  movementDate: string;
  createdDate: string;
  itemCurrentQuantity: number;
}

export interface CreateStockMovementRequest {
  movementType: MovementType;
  quantity: number;
  reason?: string;
  movementDate?: string;
  adjustmentDirection?: AdjustmentDirection;
}

export interface MovementHistoryResponse {
  movements: StockMovement[];
  totalCount: number;
  hasMore: boolean;
}

export interface Item {
  id: number;
  name: string;
  description?: string;
  currentQuantity: number;
}
