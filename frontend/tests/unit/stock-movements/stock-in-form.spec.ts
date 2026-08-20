import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { StockInFormComponent } from '../../../src/app/stock-movements/movement-form/stock-in-form.component';
import { StockMovementService } from '../../../src/app/services/stock-movement.service';
import { NotificationService } from '../../../src/app/services/notification.service';
import { of, throwError } from 'rxjs';
import { StockMovement, MovementType } from '../../../src/app/models/stock-movement.model';

describe('StockInFormComponent', () => {
  let component: StockInFormComponent;
  let fixture: ComponentFixture<StockInFormComponent>;
  let mockStockMovementService: jasmine.SpyObj<StockMovementService>;
  let mockNotificationService: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    mockStockMovementService = jasmine.createSpyObj('StockMovementService', ['createMovement'], {
      loading$: of(false),
      error$: of(null)
    });

    mockNotificationService = jasmine.createSpyObj('NotificationService', ['success', 'error']);

    await TestBed.configureTestingModule({
      imports: [StockInFormComponent, ReactiveFormsModule],
      providers: [
        { provide: StockMovementService, useValue: mockStockMovementService },
        { provide: NotificationService, useValue: mockNotificationService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StockInFormComponent);
    component = fixture.componentInstance;
    component.itemId = 1;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have invalid form when quantity is empty', () => {
    expect(component.form.invalid).toBeTruthy();
  });

  it('should have valid form when quantity is provided', () => {
    component.form.get('quantity')?.setValue(10);
    expect(component.form.valid).toBeTruthy();
  });

  it('should reject quantity less than 1', () => {
    component.form.get('quantity')?.setValue(0);
    expect(component.form.invalid).toBeTruthy();
  });

  it('should submit form with valid data', () => {
    const mockMovement: StockMovement = {
      id: 1,
      itemId: 1,
      movementType: MovementType.STOCK_IN,
      quantity: 10,
      movementDate: '2026-08-20',
      createdDate: '2026-08-20T10:00:00Z',
      itemCurrentQuantity: 10
    };

    mockStockMovementService.createMovement.and.returnValue(of(mockMovement));

    component.form.get('quantity')?.setValue(10);
    component.form.get('reason')?.setValue('Initial stock');
    component.onSubmit();

    expect(mockStockMovementService.createMovement).toHaveBeenCalledWith(1, jasmine.objectContaining({
      movementType: MovementType.STOCK_IN,
      quantity: 10
    }));

    expect(mockNotificationService.success).toHaveBeenCalled();
  });

  it('should emit close when cancelled', () => {
    spyOn(component.close, 'emit');
    component.onCancel();
    expect(component.close.emit).toHaveBeenCalled();
  });

  it('should handle errors on submission', () => {
    const error = new Error('API Error');
    mockStockMovementService.createMovement.and.returnValue(throwError(() => error));

    component.form.get('quantity')?.setValue(10);
    component.onSubmit();

    expect(mockNotificationService.error).toHaveBeenCalled();
  });
});
