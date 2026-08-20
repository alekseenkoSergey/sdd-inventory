import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError, finalize } from 'rxjs/operators';
import { HttpParams } from '@angular/common/http';
import { ApiService } from '../../core/http/api.service';
import {
  InventoryItem,
  Category,
  Location,
  CreateItemFormModel,
  EditItemFormModel,
  PagedListResponse,
  ItemFilters,
  ApiError
} from '../models/inventory-item.model';

@Injectable({
  providedIn: 'root'
})
export class InventoryItemsService {
  private itemsSubject = new BehaviorSubject<InventoryItem[]>([]);
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private errorSubject = new BehaviorSubject<ApiError | null>(null);
  private filtersSubject = new BehaviorSubject<ItemFilters>({
    page: 0,
    size: 20,
    status: null,
    categoryId: null,
    search: null,
    locationId: null,
    stockState: null
  });
  private totalPagesSubject = new BehaviorSubject<number>(0);
  private categoriesSubject = new BehaviorSubject<Category[]>([]);
  private locationsSubject = new BehaviorSubject<Location[]>([]);

  items$ = this.itemsSubject.asObservable();
  loading$ = this.loadingSubject.asObservable();
  error$ = this.errorSubject.asObservable();
  filters$ = this.filtersSubject.asObservable();
  totalPages$ = this.totalPagesSubject.asObservable();
  categories$ = this.categoriesSubject.asObservable();
  locations$ = this.locationsSubject.asObservable();

  constructor(private api: ApiService) {}

  listItems(): Observable<PagedListResponse> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    const filters = this.filtersSubject.value;
    let params = new HttpParams()
      .set('page', String(filters.page))
      .set('size', String(filters.size));

    if (filters.search) {
      params = params.set('search', filters.search);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.categoryId) {
      params = params.set('categoryId', String(filters.categoryId));
    }
    if (filters.locationId) {
      params = params.set('locationId', String(filters.locationId));
    }
    if (filters.stockState) {
      params = params.set('stockState', filters.stockState);
    }

    return this.api.get<PagedListResponse>(`/v1/inventory-items?${params.toString()}`).pipe(
      tap((response: PagedListResponse) => {
        this.itemsSubject.next(response.content || []);
        this.totalPagesSubject.next(response.totalPages || 0);
      }),
      catchError(error => this.handleError(error)),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  getItem(id: number): Observable<InventoryItem> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.api.get<InventoryItem>(`/v1/inventory-items/${id}`).pipe(
      catchError(error => this.handleError(error)),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  createItem(data: CreateItemFormModel): Observable<InventoryItem> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.api.post<InventoryItem>(`/v1/inventory-items`, data).pipe(
      tap((item: InventoryItem) => {
        const current = this.itemsSubject.value;
        this.itemsSubject.next([item, ...current]);
      }),
      catchError(error => this.handleError(error)),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  updateItem(id: number, data: EditItemFormModel): Observable<InventoryItem> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.api.patch<InventoryItem>(`/v1/inventory-items/${id}`, data).pipe(
      tap((updated: InventoryItem) => {
        const items = this.itemsSubject.value.map(item =>
          item.id === id ? updated : item
        );
        this.itemsSubject.next(items);
      }),
      catchError(error => this.handleError(error)),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  archiveItem(id: number): Observable<InventoryItem> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.api.post<InventoryItem>(`/v1/inventory-items/${id}/archive`, {}).pipe(
      tap((updated: InventoryItem) => {
        const items = this.itemsSubject.value.map(item =>
          item.id === id ? updated : item
        );
        this.itemsSubject.next(items);
      }),
      catchError(error => this.handleError(error)),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  restoreItem(id: number): Observable<InventoryItem> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.api.post<InventoryItem>(`/v1/inventory-items/${id}/restore`, {}).pipe(
      tap((updated: InventoryItem) => {
        const items = this.itemsSubject.value.map(item =>
          item.id === id ? updated : item
        );
        this.itemsSubject.next(items);
      }),
      catchError(error => this.handleError(error)),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  deleteItem(id: number): Observable<void> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.api.delete<void>(`/v1/inventory-items/${id}`).pipe(
      tap(() => {
        const items = this.itemsSubject.value.filter(item => item.id !== id);
        this.itemsSubject.next(items);
      }),
      catchError(error => this.handleError(error)),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  setPage(page: number): void {
    const current = this.filtersSubject.value;
    this.filtersSubject.next({ ...current, page });
    this.listItems().subscribe();
  }

  setStatusFilter(status: 'ACTIVE' | 'ARCHIVED' | null): void {
    const current = this.filtersSubject.value;
    this.filtersSubject.next({ ...current, status, page: 0 });
    this.listItems().subscribe();
  }

  setCategoryFilter(categoryId: number | null): void {
    const current = this.filtersSubject.value;
    this.filtersSubject.next({ ...current, categoryId, page: 0 });
    this.listItems().subscribe();
  }

  clearFilters(): void {
    this.filtersSubject.next({
      page: 0,
      size: 20,
      status: null,
      categoryId: null,
      search: null,
      locationId: null,
      stockState: null
    });
    this.listItems().subscribe();
  }

  applySearchAndFilters(search?: string, categoryId?: number, locationId?: number,
                       statusFilter?: string, stockState?: string): void {
    const current = this.filtersSubject.value;

    let validStatus: 'ACTIVE' | 'ARCHIVED' | null = null;
    if (statusFilter === 'ACTIVE' || statusFilter === 'ARCHIVED') {
      validStatus = statusFilter;
    }

    this.filtersSubject.next({
      page: 0,
      size: current.size,
      status: validStatus,
      categoryId: categoryId || null,
      search: search || null,
      locationId: locationId || null,
      stockState: stockState || null
    });
    this.listItems().subscribe();
  }

  loadCategories(): Observable<Category[]> {
    return this.api.get<Category[]>(`/v1/categories`).pipe(
      tap((categories: Category[]) => this.categoriesSubject.next(categories || [])),
      catchError(error => this.handleError(error))
    );
  }

  loadLocations(): Observable<Location[]> {
    return this.api.get<Location[]>(`/v1/locations`).pipe(
      tap((locations: Location[]) => this.locationsSubject.next(locations || [])),
      catchError(error => this.handleError(error))
    );
  }

  private handleError(error: any): Observable<never> {
    const apiError: ApiError = {
      status: error?.status || 500,
      message: error?.error?.message || 'An unexpected error occurred',
      code: error?.error?.error
    };
    this.errorSubject.next(apiError);
    return throwError(() => apiError);
  }
}
