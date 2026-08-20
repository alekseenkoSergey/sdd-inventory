import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ItemDetailPageComponent } from './item-detail-page.component';
import { ActivatedRoute, Router } from '@angular/router';
import { InventoryItemsService } from '../../services/inventory-items.service';
import { of } from 'rxjs';
import { InventoryItem } from '../../models/inventory-item.model';

describe('ItemDetailPageComponent', () => {
  let component: ItemDetailPageComponent;
  let fixture: ComponentFixture<ItemDetailPageComponent>;
  let service: jasmine.SpyObj<InventoryItemsService>;
  let router: jasmine.SpyObj<Router>;

  const mockItem: InventoryItem = {
    id: 1,
    name: 'Test Item',
    categoryId: 1,
    locationId: 1,
    currentQuantity: 100,
    unit: 'pcs',
    lowStockThreshold: 10,
    status: 'ACTIVE',
    createdDate: '2026-08-20T00:00:00Z',
    updatedDate: '2026-08-20T00:00:00Z'
  };

  beforeEach(async () => {
    const serviceSpy = jasmine.createSpyObj('InventoryItemsService', [
      'getItem',
      'updateItem',
      'deleteItem',
      'archiveItem',
      'restoreItem',
      'loadCategories',
      'loadLocations'
    ]);

    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ItemDetailPageComponent],
      providers: [
        { provide: InventoryItemsService, useValue: serviceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1'
              }
            }
          }
        }
      ]
    }).compileComponents();

    service = TestBed.inject(InventoryItemsService) as jasmine.SpyObj<InventoryItemsService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    service.getItem.and.returnValue(of(mockItem));
    service.loadCategories.and.returnValue(of([]));
    service.loadLocations.and.returnValue(of([]));

    fixture = TestBed.createComponent(ItemDetailPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load item on init', () => {
    fixture.detectChanges();
    expect(service.getItem).toHaveBeenCalledWith(1);
  });

  it('should navigate back to list', () => {
    component.goBack();
    expect(router.navigate).toHaveBeenCalledWith(['/inventory/items']);
  });

  it('should show edit form', () => {
    component.showEditForm();
    expect(component.showForm).toBeTruthy();
  });

  it('should confirm before deleting', () => {
    component.item = mockItem;
    spyOn(window, 'confirm').and.returnValue(true);
    service.deleteItem.and.returnValue(of(void 0));

    component.onDelete();
    expect(service.deleteItem).toHaveBeenCalledWith(1);
  });
});
