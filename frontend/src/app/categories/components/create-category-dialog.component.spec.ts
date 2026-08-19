import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateCategoryDialogComponent } from './create-category-dialog.component';
import { CategoryService } from '../services/category.service';
import { of, throwError } from 'rxjs';
import { Category } from '../models/category.model';

describe('CreateCategoryDialogComponent', () => {
  let component: CreateCategoryDialogComponent;
  let fixture: ComponentFixture<CreateCategoryDialogComponent>;
  let categoryService: jasmine.SpyObj<CategoryService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CategoryService', ['createCategory']);
    await TestBed.configureTestingModule({
      declarations: [CreateCategoryDialogComponent],
      providers: [{ provide: CategoryService, useValue: spy }]
    }).compileComponents();

    categoryService = TestBed.inject(CategoryService) as jasmine.SpyObj<CategoryService>;
    fixture = TestBed.createComponent(CreateCategoryDialogComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call createCategory on submit with valid input', () => {
    const mockCategory: Category = {
      id: '1',
      name: 'Electronics',
      itemCount: 0,
      createdAt: '2026-08-19T10:30:00Z',
      updatedAt: '2026-08-19T10:30:00Z'
    };

    categoryService.createCategory.and.returnValue(of(mockCategory));
    component.categoryName = 'Electronics';
    component.onSubmit();

    expect(categoryService.createCategory).toHaveBeenCalledWith({ name: 'Electronics' });
  });

  it('should handle duplicate name error', () => {
    const errorResponse = { error: { error: 'CATEGORY_NAME_NOT_UNIQUE' } };
    categoryService.createCategory.and.returnValue(throwError(() => errorResponse));

    component.categoryName = 'Electronics';
    component.onSubmit();

    expect(component.errorMessage).toBeTruthy();
  });

  it('should clear error message on successful submission', () => {
    const mockCategory: Category = {
      id: '1',
      name: 'Electronics',
      itemCount: 0,
      createdAt: '2026-08-19T10:30:00Z',
      updatedAt: '2026-08-19T10:30:00Z'
    };

    categoryService.createCategory.and.returnValue(of(mockCategory));
    component.errorMessage = 'Previous error';
    component.categoryName = 'Electronics';
    component.onSubmit();

    expect(component.errorMessage).toBeEmpty();
  });
});
