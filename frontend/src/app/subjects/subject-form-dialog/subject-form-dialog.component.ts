import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Subject, SubjectRequest } from '../../core/models/subject.model';
import { SubjectService } from '../../core/services/subject.service';

export interface SubjectFormDialogData {
  subject?: Subject;
}

@Component({
  selector: 'app-subject-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './subject-form-dialog.component.html',
  styleUrl: './subject-form-dialog.component.css'
})
export class SubjectFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly subjectService = inject(SubjectService);
  private readonly dialogRef = inject(MatDialogRef<SubjectFormDialogComponent>);
  private readonly data = inject<SubjectFormDialogData>(MAT_DIALOG_DATA);

  readonly isEditMode = !!this.data.subject;
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    name: [this.data.subject?.name ?? '', [Validators.required]],
    description: [this.data.subject?.description ?? ''],
    credits: [this.data.subject?.credits ?? 6, [Validators.required, Validators.min(1)]],
    difficulty: [this.data.subject?.difficulty ?? 'MEDIUM', [Validators.required]],
    priority: [this.data.subject?.priority ?? 'MEDIUM', [Validators.required]],
    knowledgePercent: [this.data.subject?.knowledgePercent ?? 0, [Validators.min(0), Validators.max(100)]],
    color: [this.data.subject?.color ?? '#3F51B5']
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const request = this.form.getRawValue() as SubjectRequest;
    const action = this.isEditMode
      ? this.subjectService.update(this.data.subject!.id, request)
      : this.subjectService.create(request);

    action.subscribe({
      next: (subject) => this.dialogRef.close(subject),
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Čuvanje predmeta nije uspelo.');
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
