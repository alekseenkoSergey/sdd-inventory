import { AbstractControl, ValidationErrors, ValidatorFn, AsyncValidatorFn } from '@angular/forms';

export class StockMovementValidators {
  static quantity(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (value === null || value === undefined || value === '') {
        return { required: true };
      }

      const num = Number(value);

      if (isNaN(num)) {
        return { notANumber: true };
      }

      if (num <= 0) {
        return { minValue: { min: 0, actual: num } };
      }

      if (!Number.isInteger(num)) {
        return { notAnInteger: true };
      }

      return null;
    };
  }

  static quantityMax(maxValue: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (value === null || value === undefined || value === '') {
        return null;
      }

      const num = Number(value);

      if (isNaN(num)) {
        return null;
      }

      if (num > maxValue) {
        return { maxValue: { max: maxValue, actual: num } };
      }

      return null;
    };
  }

  static reason(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (value === null || value === undefined || value === '') {
        return null;
      }

      if (typeof value !== 'string') {
        return { invalidType: true };
      }

      if (value.length > 500) {
        return { maxLength: { max: 500, actual: value.length } };
      }

      return null;
    };
  }

  static date(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (value === null || value === undefined || value === '') {
        return null;
      }

      const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
      if (!dateRegex.test(value)) {
        return { invalidDateFormat: true };
      }

      const date = new Date(value);
      if (isNaN(date.getTime())) {
        return { invalidDate: true };
      }

      return null;
    };
  }

  static adjustmentDirection(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (value === null || value === undefined || value === '') {
        return { required: true };
      }

      const validDirections = ['INCREASE', 'DECREASE'];
      if (!validDirections.includes(value)) {
        return { invalidDirection: true };
      }

      return null;
    };
  }
}
