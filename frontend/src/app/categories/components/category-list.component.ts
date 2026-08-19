import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategoryService } from '../services/category.service';
import { Category } from '../models/category.model';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  standalone: true,
  imports: [CommonModule]
})
export class CategoryListComponent implements OnInit {
  categories: Category[] = [];
  loading = true;
  error: string | null = null;
  successMessage: string | null = null;
  deleting: { [key: string]: boolean } = {};

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = null;
    this.categoryService.listCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load categories';
        this.loading = false;
        console.error(err);
      }
    });
  }

  refreshCategories(): void {
    this.loadCategories();
  }

  deleteCategory(id: string, name: string): void {
    if (confirm(`Are you sure you want to delete "${name}"?`)) {
      this.deleting[id] = true;
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.deleting[id] = false;
          this.successMessage = `Category "${name}" deleted successfully`;
          this.loadCategories();
        },
        error: (err) => {
          this.deleting[id] = false;
          if (err.error?.error === 'CATEGORY_HAS_ITEMS') {
            const itemCount = err.error?.itemCount || 'unknown number of';
            this.error = `Cannot delete: ${itemCount} items assigned. Please reassign items to another category first.`;
          } else {
            this.error = 'Failed to delete category';
          }
          console.error(err);
        }
      });
    }
  }

  closeError(): void {
    this.error = null;
  }

  closeSuccess(): void {
    this.successMessage = null;
  }
}
