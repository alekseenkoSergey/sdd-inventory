import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../services/category.service';

@Component({
  selector: 'app-rename-category-dialog',
  templateUrl: './rename-category-dialog.component.html',
  styleUrl: './rename-category-dialog.component.css',
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class RenameCategoryDialogComponent implements OnInit {
  @Input() categoryId: number = 0;
  @Input() currentName: string = '';
  @Output() categoryRenamed = new EventEmitter<void>();

  newName = '';
  errorMessage = '';
  loading = false;

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.newName = this.currentName;
  }

  onSubmit(): void {
    if (!this.newName.trim()) {
      this.errorMessage = 'Category name cannot be empty';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.categoryService.renameCategory(this.categoryId, { name: this.newName }).subscribe({
      next: () => {
        this.loading = false;
        this.categoryRenamed.emit();
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 409) {
          this.errorMessage = 'Name conflict detected. Please refresh and try again.';
        } else if (err.error?.error === 'CATEGORY_NAME_NOT_UNIQUE') {
          this.errorMessage = 'Category name already exists';
        } else {
          this.errorMessage = 'Failed to rename category. Please try again.';
        }
      }
    });
  }

  onCancel(): void {
    this.newName = this.currentName;
    this.errorMessage = '';
  }
}
