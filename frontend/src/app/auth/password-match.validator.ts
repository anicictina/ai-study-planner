import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function passwordMatchValidator(passwordKey: string, confirmKey: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get(passwordKey)?.value;
    const confirmPassword = group.get(confirmKey)?.value;
    const confirmControl = group.get(confirmKey);

    if (!confirmControl || password === confirmPassword) {
      return null;
    }

    confirmControl.setErrors({ ...confirmControl.errors, passwordMismatch: true });
    return { passwordMismatch: true };
  };
}
