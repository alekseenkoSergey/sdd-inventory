import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ItemFormComponent } from './item-form.component';
import { ReactiveFormsModule } from '@angular/forms';

describe('ItemFormComponent', () => {
  let component: ItemFormComponent;
  let fixture: ComponentFixture<ItemFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItemFormComponent, ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(ItemFormComponent);
    component = fixture.componentInstance;
    component.categories = [];
    component.locations = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required validators', () => {
    expect(component.form.get('name')?.hasError('required')).toBeTruthy();
    expect(component.form.get('categoryId')?.hasError('required')).toBeTruthy();
    expect(component.form.get('locationId')?.hasError('required')).toBeTruthy();
  });

  it('should validate name max length', () => {
    const nameControl = component.form.get('name');
    nameControl?.setValue('a'.repeat(256));
    expect(nameControl?.hasError('maxlength')).toBeTruthy();
  });

  it('should emit save event on valid form submission', (done) => {
    component.form.patchValue({
      name: 'Test Item',
      categoryId: 1,
      locationId: 1,
      unit: 'pcs'
    });

    component.save.subscribe((data) => {
      expect(data.name).toBe('Test Item');
      done();
    });

    component.onSubmit();
  });

  it('should emit cancel event', (done) => {
    component.cancel.subscribe(() => {
      done();
    });

    component.onCancel();
  });

  it('should not submit invalid form', () => {
    spyOn(component.save, 'emit');
    component.onSubmit();
    expect(component.save.emit).not.toHaveBeenCalled();
  });
});
