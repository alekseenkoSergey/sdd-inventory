import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ItemListComponent } from './item-list.component';
import { InventoryItem } from '../../models/inventory-item.model';

describe('ItemListComponent', () => {
  let component: ItemListComponent;
  let fixture: ComponentFixture<ItemListComponent>;

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
    await TestBed.configureTestingModule({
      imports: [ItemListComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ItemListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display items in table', () => {
    component.items = [mockItem];
    fixture.detectChanges();

    const table = fixture.nativeElement.querySelector('table');
    expect(table).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Test Item');
  });

  it('should emit edit event when edit button clicked', (done) => {
    component.items = [mockItem];
    fixture.detectChanges();

    component.edit.subscribe((item) => {
      expect(item.id).toBe(1);
      done();
    });

    component.onEdit(mockItem);
  });

  it('should emit delete event when delete button clicked', (done) => {
    component.items = [mockItem];
    fixture.detectChanges();

    component.delete.subscribe((item) => {
      expect(item.id).toBe(1);
      done();
    });

    component.onDelete(mockItem);
  });

  it('should show empty state when no items', () => {
    component.items = [];
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).toBeTruthy();
  });
});
