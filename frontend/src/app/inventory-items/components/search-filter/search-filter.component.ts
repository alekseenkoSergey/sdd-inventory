import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InventoryItemsService } from '../../services/inventory-items.service';
import { Category, Location } from '../../models/inventory-item.model';

interface FilterParams {
  search?: string;
  categoryId?: number;
  locationId?: number;
  status?: string;
  stockState?: string;
}

@Component({
  selector: 'app-search-filter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-filter.component.html',
  styleUrl: './search-filter.component.css'
})
export class SearchFilterComponent {
  @Input() categories: Category[] = [];
  @Input() locations: Location[] = [];
  @Output() filterApplied = new EventEmitter<FilterParams>();

  searchTerm: string = '';
  selectedCategory: number | null = null;
  selectedLocation: number | null = null;
  selectedStatus: string = 'ALL';
  selectedStockState: string = 'ALL';

  statusOptions = [
    { value: 'ALL', label: 'All Items' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'ARCHIVED', label: 'Archived' }
  ];

  stockStateOptions = [
    { value: 'ALL', label: 'All Stock Levels' },
    { value: 'OUT_OF_STOCK', label: 'Out of Stock' },
    { value: 'LOW_STOCK', label: 'Low Stock' },
    { value: 'IN_STOCK', label: 'In Stock' }
  ];

  constructor(private inventoryService: InventoryItemsService) {}

  onSearch(): void {
    this.applyFilters();
  }

  onSearchKeyup(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.applyFilters();
    }
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    const params: FilterParams = {};

    if (this.searchTerm.trim()) {
      params.search = this.searchTerm.trim();
    }

    if (this.selectedCategory) {
      params.categoryId = this.selectedCategory;
    }

    if (this.selectedLocation) {
      params.locationId = this.selectedLocation;
    }

    if (this.selectedStatus !== 'ALL') {
      params.status = this.selectedStatus;
    }

    if (this.selectedStockState !== 'ALL') {
      params.stockState = this.selectedStockState;
    }

    this.inventoryService.applySearchAndFilters(
      params.search,
      params.categoryId,
      params.locationId,
      params.status,
      params.stockState
    );
    this.filterApplied.emit(params);
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedCategory = null;
    this.selectedLocation = null;
    this.selectedStatus = 'ALL';
    this.selectedStockState = 'ALL';
    this.inventoryService.clearFilters();
    this.filterApplied.emit({});
  }

  clearSearch(): void {
    this.searchTerm = '';
  }

  clearCategoryFilter(): void {
    this.selectedCategory = null;
    this.onFilterChange();
  }

  clearLocationFilter(): void {
    this.selectedLocation = null;
    this.onFilterChange();
  }

  clearStatusFilter(): void {
    this.selectedStatus = 'ALL';
    this.onFilterChange();
  }

  clearStockStateFilter(): void {
    this.selectedStockState = 'ALL';
    this.onFilterChange();
  }

  hasActiveFilters(): boolean {
    return !!(
      this.searchTerm.trim() ||
      this.selectedCategory ||
      this.selectedLocation ||
      this.selectedStatus !== 'ALL' ||
      this.selectedStockState !== 'ALL'
    );
  }
}
