import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RenameCategoryDialogComponent } from './rename-category-dialog.component';
import { CategoryService } from '../services/category.service';
import { of, throwError } from 'rxjs';
import { Category } from '../models/category.model';

describe('RenameCategoryDialogComponent', () => {
  let component: RenameCategoryDialogComponent;
  let fixture: ComponentFixture<RenameCategoryDialogComponent>;
  let categoryService: jasmine.SpyObj<CategoryService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CategoryService', ['renameCategory']);
    await TestBed.configureTestingModule({
      declarations: [RenameCategoryDialogComponent],
      providers: [{ provide: CategoryService, useValue: spy }]
    }).compileComponents();

    categoryService = TestBed.inject(CategoryService) as jasmine.SpyObj<CategoryService>;
    fixture = TestBed.createComponent(RenameCategoryDialogComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call renameCategory on submit', () => {
    const mockCategory: Category = {
      id: '1',
      name: 'Tools',
      itemCount: 0,
      createdAt: '2026-08-19T10:30:00Z',
      updatedAt: '2026-08-19T11:00:00Z'
    };

    categoryService.renameCategory.and.returnValue(of(mockCategory));
    component.categoryId = '1';
    component.newName = 'Tools';
    component.onSubmit();

    expect(categoryService.renameCategory).toHaveBeenCalledWith('1', { name: 'Tools' });
  });

  it('should handle conflict error (HTTP 409)', () => {
    const errorResponse = { status: 409, error: { error: 'CATEGORY_HAS_ITEMS' } };
    categoryService.renameCategory.and.returnValue(throwError(() => errorResponse));

    component.categoryId = '1';
    component.newName = 'Tools';
    component.onSubmit();

    expect(component.errorMessage).toContain('conflict') || expect(component.errorMessage).toBeTruthy();
  });
});
