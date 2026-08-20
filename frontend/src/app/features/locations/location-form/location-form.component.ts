import { Component, Input, Output, EventEmitter, OnInit, NgZone, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LocationService } from '../location.service';
import { Location } from '../location.model';

@Component({
  selector: 'app-location-form',
  templateUrl: './location-form.component.html',
  styleUrls: ['./location-form.component.css'],
  standalone: true,
  imports: [CommonModule, HttpClientModule, ReactiveFormsModule]
})
export class LocationFormComponent implements OnInit {
  @Input() locationId: number | null = null;
  @Output() locationCreated = new EventEmitter<Location>();
  @Output() locationUpdated = new EventEmitter<Location>();
  @Output() closed = new EventEmitter<void>();

  form: FormGroup;
  isSubmitting = false;
  errorMessage: string | null = null;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private locationService: LocationService,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(255)]]
    });
  }

  ngOnInit(): void {
    this.isEditMode = this.locationId !== null && this.locationId !== undefined;
    if (this.isEditMode && this.locationId) {
      this.loadLocation(this.locationId);
    }
  }

  loadLocation(id: number): void {
    this.locationService.getLocation(id).subscribe({
      next: (location) => {
        this.form.patchValue({ name: location.name });
      },
      error: (error) => {
        this.ngZone.run(() => {
          this.errorMessage = this.getErrorMessage(error);
          this.cdr.markForCheck();
        });
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const name = this.form.get('name')?.value.trim();

    if (this.isEditMode && this.locationId) {
      this.locationService.renameLocation(this.locationId, name).subscribe({
        next: (location) => {
          this.isSubmitting = false;
          this.locationUpdated.emit(location);
        },
        error: (error) => {
          this.ngZone.run(() => {
            this.isSubmitting = false;
            this.errorMessage = this.getErrorMessage(error);
            this.cdr.markForCheck();
          });
        }
      });
    } else {
      this.locationService.createLocation(name).subscribe({
        next: (location) => {
          this.isSubmitting = false;
          this.locationCreated.emit(location);
          this.form.reset();
        },
        error: (error) => {
          this.ngZone.run(() => {
            this.isSubmitting = false;
            this.errorMessage = this.getErrorMessage(error);
            this.cdr.markForCheck();
          });
        }
      });
    }
  }

  private getErrorMessage(error: any): string {
    if (!error) {
      return 'An unexpected error occurred';
    }
    if (error instanceof Error) {
      return error.message;
    }
    if (typeof error === 'string') {
      return error;
    }
    if (typeof error === 'object' && error.message) {
      return error.message;
    }
    return 'An unexpected error occurred';
  }

  close(): void {
    this.closed.emit();
  }
}
