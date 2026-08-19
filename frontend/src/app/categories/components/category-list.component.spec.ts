import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CategoryListComponent } from './category-list.component';
import { CategoryService } from '../services/category.service';
import { of } from 'rxjs';
import { Category } from '../models/category.model';

describe('CategoryListComponent', () => {
  let component: CategoryListComponent;
  let fixture: ComponentFixture<CategoryListComponent>;
  let categoryService: jasmine.SpyObj<CategoryService>;

  const mockCategories: Category[] = [
    {
      id: '1',
      name: 'Electronics',
      itemCount: 3,
      createdAt: '2026-08-19T10:30:00Z',
      updatedAt: '2026-08-19T10:30:00Z'
    },
    {
      id: '2',
      name: 'Tools',
      itemCount: 0,
      createdAt: '2026-08-19T11:00:00Z',
      updatedAt: '2026-08-19T11:00:00Z'
    }
  ];

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

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load categories on init', () => {
    categoryService.listCategories.and.returnValue(of(mockCategories));
    component.ngOnInit();
    expect(component.categories).toEqual(mockCategories);
  });

  it('should display categories', () => {
    component.categories = mockCategories;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const categoryElements = compiled.querySelectorAll('.category-item');
    expect(categoryElements.length).toBe(2);
  });
});
