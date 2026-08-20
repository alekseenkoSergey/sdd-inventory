export interface InventoryItem {
  id: number;
  name: string;
  description?: string;
  sku?: string;
  categoryId: number;
  locationId: number;
  currentQuantity: number;
  unit: string;
  lowStockThreshold: number;
  status: 'ACTIVE' | 'ARCHIVED';
  createdDate: string;
  updatedDate: string;
}

export interface Category {
  id: number;
  name: string;
}

export interface Location {
  id: number;
  name: string;
}

export interface CreateItemFormModel {
  name: string;
  description?: string;
  sku?: string;
  categoryId: number;
  locationId: number;
  unit: string;
  lowStockThreshold?: number;
  initialQuantity?: number;
}

export interface EditItemFormModel {
  name?: string;
  description?: string;
  sku?: string;
  categoryId?: number;
  locationId?: number;
  unit?: string;
  lowStockThreshold?: number;
}

export interface PagedListResponse {
  content: InventoryItem[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface ItemFilters {
  page: number;
  size: number;
  status?: 'ACTIVE' | 'ARCHIVED' | null;
  categoryId?: number | null;
  search?: string | null;
  locationId?: number | null;
  stockState?: string | null;
}

export interface ApiError {
  status: number;
  message: string;
  code?: string;
}
