import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InventoryItemsPageComponent } from './inventory-items-page.component';
import { InventoryItemsService } from '../../services/inventory-items.service';
import { of } from 'rxjs';

describe('InventoryItemsPageComponent', () => {
  let component: InventoryItemsPageComponent;
  let fixture: ComponentFixture<InventoryItemsPageComponent>;
  let service: jasmine.SpyObj<InventoryItemsService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('InventoryItemsService', [
      'listItems',
      'loadCategories',
      'loadLocations',
      'createItem',
      'updateItem',
      'deleteItem',
      'archiveItem',
      'restoreItem'
    ]);

    await TestBed.configureTestingModule({
      imports: [InventoryItemsPageComponent],
      providers: [
        { provide: InventoryItemsService, useValue: spy }
      ]
    }).compileComponents();

    service = TestBed.inject(InventoryItemsService) as jasmine.SpyObj<InventoryItemsService>;
    service.listItems.and.returnValue(of({
      content: [],
      totalElements: 0,
      totalPages: 0,
      currentPage: 0,
      pageSize: 20
    }));
    service.loadCategories.and.returnValue(of([]));
    service.loadLocations.and.returnValue(of([]));

    fixture = TestBed.createComponent(InventoryItemsPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load items on init', () => {
    fixture.detectChanges();
    expect(service.listItems).toHaveBeenCalled();
    expect(service.loadCategories).toHaveBeenCalled();
    expect(service.loadLocations).toHaveBeenCalled();
  });

  it('should show create form when button clicked', () => {
    component.showCreateForm();
    expect(component.showForm).toBeTruthy();
    expect(component.editingItem).toBeUndefined();
  });

  it('should close form and reset editing state', () => {
    component.editingItem = { id: 1 } as any;
    component.showForm = true;
    component.closeForm();
    expect(component.showForm).toBeFalsy();
    expect(component.editingItem).toBeUndefined();
  });
});
