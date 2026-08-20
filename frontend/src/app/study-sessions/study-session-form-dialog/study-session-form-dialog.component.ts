import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Subject } from '../../core/models/subject.model';
import { StudySession, StudySessionRequest } from '../../core/models/study-session.model';
import { StudySessionService } from '../../core/services/study-session.service';

export interface StudySessionFormDialogData {
  session?: StudySession;
  subjects: Subject[];
  defaultDate?: Date;
}

function toApiDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

@Component({
  selector: 'app-study-session-form-dialog',
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
  templateUrl: './study-session-form-dialog.component.html',
  styleUrl: './study-session-form-dialog.component.css'
})
export class StudySessionFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly studySessionService = inject(StudySessionService);
  private readonly dialogRef = inject(MatDialogRef<StudySessionFormDialogComponent>);
  private readonly data = inject<StudySessionFormDialogData>(MAT_DIALOG_DATA);

  readonly isEditMode = !!this.data.session;
  readonly subjects = this.data.subjects;
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    subjectId: [this.data.session?.subjectId ?? this.subjects[0]?.id ?? null, [Validators.required]],
    topic: [this.data.session?.topic ?? ''],
    sessionDate: [
      this.data.session ? new Date(this.data.session.sessionDate) : (this.data.defaultDate ?? new Date()),
      [Validators.required]
    ],
    startTime: [this.data.session?.startTime?.slice(0, 5) ?? ''],
    durationMinutes: [this.data.session?.durationMinutes ?? 60, [Validators.required, Validators.min(1)]],
    activityType: [this.data.session?.activityType ?? 'READING']
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    const request: StudySessionRequest = {
      subjectId: raw.subjectId!,
      topic: raw.topic || null,
      sessionDate: toApiDate(raw.sessionDate!),
      startTime: raw.startTime ? `${raw.startTime}:00` : null,
      durationMinutes: raw.durationMinutes!,
      activityType: raw.activityType ?? 'READING'
    };

    const action = this.isEditMode
      ? this.studySessionService.update(this.data.session!.id, request)
      : this.studySessionService.create(request);

    action.subscribe({
      next: (session) => this.dialogRef.close(session),
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Čuvanje sesije nije uspelo.');
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
