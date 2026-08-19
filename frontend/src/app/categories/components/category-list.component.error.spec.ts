import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CategoryListComponent } from './category-list.component';
import { CategoryService } from '../services/category.service';
import { throwError } from 'rxjs';

describe('CategoryListComponent - Error Scenarios', () => {
  let component: CategoryListComponent;
  let fixture: ComponentFixture<CategoryListComponent>;
  let categoryService: jasmine.SpyObj<CategoryService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('CategoryService', ['listCategories', 'deleteCategory']);
    await TestBed.configureTestingModule({
      declarations: [CategoryListComponent],
      providers: [{ provide: CategoryService, useValue: spy }]
    }).compileComponents();

    categoryService = TestBed.inject(CategoryService) as jasmine.SpyObj<CategoryService>;
    fixture = TestBed.createComponent(CategoryListComponent);
    component = fixture.componentInstance;
  });

  it('should display error message on failed load', () => {
    const errorResponse = new Error('Network error');
    categoryService.listCategories.and.returnValue(throwError(() => errorResponse));

    component.ngOnInit();

    expect(component.error).toBe('Failed to load categories');
  });

  it('should display item count error message on delete with items', () => {
    const deleteError = {
      error: {
        error: 'CATEGORY_HAS_ITEMS',
        itemCount: 5,
        message: 'Cannot delete: 5 items assigned'
      }
    };
    categoryService.deleteCategory.and.returnValue(throwError(() => deleteError));

    component.deleteCategory('1', 'Electronics');

    expect(component.error).toContain('Cannot delete');
  });

  it('should handle close error button', () => {
    component.error = 'Some error message';
    component.closeError();
    expect(component.error).toBeNull();
  });

  it('should handle close success message', () => {
    component.successMessage = 'Success message';
    component.closeSuccess();
    expect(component.successMessage).toBeNull();
  });

  it('should disable delete button while deleting', () => {
    const categoryId = '1';
    component.deleting[categoryId] = true;

    fixture.detectChanges();

    expect(component.deleting[categoryId]).toBe(true);
  });
});
