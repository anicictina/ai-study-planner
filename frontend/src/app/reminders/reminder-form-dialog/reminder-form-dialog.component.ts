import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Reminder, ReminderRequest } from '../../core/models/reminder.model';
import { Subject } from '../../core/models/subject.model';
import { ReminderService } from '../../core/services/reminder.service';

export interface ReminderFormDialogData {
  reminder?: Reminder;
  subjects: Subject[];
}

function toApiDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

@Component({
  selector: 'app-reminder-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDatepickerModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './reminder-form-dialog.component.html',
  styleUrl: './reminder-form-dialog.component.css'
})
export class ReminderFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly reminderService = inject(ReminderService);
  private readonly dialogRef = inject(MatDialogRef<ReminderFormDialogComponent>);
  private readonly data = inject<ReminderFormDialogData>(MAT_DIALOG_DATA);

  readonly isEditMode = !!this.data.reminder;
  readonly subjects = this.data.subjects;
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    subjectId: [this.data.reminder?.subjectId ?? null],
    message: [this.data.reminder?.message ?? '', [Validators.required]],
    remindDate: [this.data.reminder ? new Date(this.data.reminder.remindAt) : null, [Validators.required]],
    remindTime: [this.data.reminder ? this.data.reminder.remindAt.slice(11, 16) : '', [Validators.required]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    const request: ReminderRequest = {
      subjectId: raw.subjectId ?? null,
      message: raw.message!.trim(),
      remindAt: `${toApiDate(raw.remindDate!)}T${raw.remindTime}:00`
    };

    const action = this.isEditMode
      ? this.reminderService.update(this.data.reminder!.id, request)
      : this.reminderService.create(request);

    action.subscribe({
      next: (reminder) => this.dialogRef.close(reminder),
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Čuvanje podsetnika nije uspelo.');
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
