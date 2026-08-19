import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../services/category.service';

@Component({
  selector: 'app-create-category-dialog',
  templateUrl: './create-category-dialog.component.html',
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class CreateCategoryDialogComponent {
  categoryName = '';
  errorMessage = '';
  loading = false;

  @Output() categoryCreated = new EventEmitter<void>();

  constructor(private categoryService: CategoryService) {}

  onSubmit(): void {
    if (!this.categoryName.trim()) {
      this.errorMessage = 'Category name cannot be empty';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.categoryService.createCategory({ name: this.categoryName }).subscribe({
      next: () => {
        this.categoryName = '';
        this.loading = false;
        this.categoryCreated.emit();
      },
      error: (err) => {
        this.loading = false;
        if (err.error?.error === 'CATEGORY_NAME_NOT_UNIQUE') {
          this.errorMessage = 'Category name already exists';
        } else {
          this.errorMessage = 'Failed to create category. Please try again.';
        }
      }
    });
  }

  onCancel(): void {
    this.categoryName = '';
    this.errorMessage = '';
  }
}
