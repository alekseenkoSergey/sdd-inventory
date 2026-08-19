import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CategoryListComponent } from '../components/category-list.component';
import { CreateCategoryDialogComponent } from '../components/create-category-dialog.component';
import { RenameCategoryDialogComponent } from '../components/rename-category-dialog.component';

@Component({
  selector: 'app-categories-page',
  templateUrl: './categories-page.component.html',
  styleUrl: './categories-page.component.css',
  standalone: true,
  imports: [
    CommonModule,
    CategoryListComponent,
    CreateCategoryDialogComponent,
    RenameCategoryDialogComponent
  ]
})
export class CategoriesPageComponent implements OnInit {
  @ViewChild(CategoryListComponent) categoryListComponent!: CategoryListComponent;
  @ViewChild(CreateCategoryDialogComponent) createDialogComponent!: CreateCategoryDialogComponent;

  showCreateDialog = false;
  showRenameDialog = false;
  selectedCategoryId: string = '';
  selectedCategoryName: string = '';

  constructor(private router: Router) {}

  ngOnInit(): void {}

  goBack(): void {
    this.router.navigate(['/home']);
  }

  openCreateDialog(): void {
    this.showCreateDialog = true;
  }

  closeCreateDialog(): void {
    this.showCreateDialog = false;
    this.createDialogComponent.onCancel();
  }

  onCategoryCreated(): void {
    this.closeCreateDialog();
    this.categoryListComponent.refreshCategories();
  }

  openRenameDialog(categoryId: string, categoryName: string): void {
    this.selectedCategoryId = categoryId;
    this.selectedCategoryName = categoryName;
    this.showRenameDialog = true;
  }

  closeRenameDialog(): void {
    this.showRenameDialog = false;
  }

  onCategoryRenamed(): void {
    this.closeRenameDialog();
    this.categoryListComponent.refreshCategories();
  }
}
