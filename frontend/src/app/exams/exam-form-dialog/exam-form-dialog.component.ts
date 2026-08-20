import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Subject } from '../../core/models/subject.model';
import { Exam, ExamRequest } from '../../core/models/exam.model';
import { ExamService } from '../../core/services/exam.service';

export interface ExamFormDialogData {
  exam?: Exam;
  subjects: Subject[];
}

function toApiDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

@Component({
  selector: 'app-exam-form-dialog',
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
  templateUrl: './exam-form-dialog.component.html',
  styleUrl: './exam-form-dialog.component.css'
})
export class ExamFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly examService = inject(ExamService);
  private readonly dialogRef = inject(MatDialogRef<ExamFormDialogComponent>);
  private readonly data = inject<ExamFormDialogData>(MAT_DIALOG_DATA);

  readonly isEditMode = !!this.data.exam;
  readonly subjects = this.data.subjects;
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    subjectId: [this.data.exam?.subjectId ?? this.subjects[0]?.id ?? null, [Validators.required]],
    examDate: [this.data.exam ? new Date(this.data.exam.examDate) : null, [Validators.required]],
    examTime: [this.data.exam?.examTime?.slice(0, 5) ?? ''],
    location: [this.data.exam?.location ?? ''],
    status: [this.data.exam?.status ?? 'PLANNED']
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    const request: ExamRequest = {
      subjectId: raw.subjectId!,
      examDate: toApiDate(raw.examDate!),
      examTime: raw.examTime ? `${raw.examTime}:00` : null,
      location: raw.location || null,
      status: raw.status ?? 'PLANNED'
    };

    const action = this.isEditMode
      ? this.examService.update(this.data.exam!.id, request)
      : this.examService.create(request);

    action.subscribe({
      next: (exam) => this.dialogRef.close(exam),
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Čuvanje ispita nije uspelo.');
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
