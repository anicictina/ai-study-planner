import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MaterialRequest, StudyMaterial } from '../../core/models/material.model';
import { MaterialService } from '../../core/services/material.service';

export interface MaterialFormDialogData {
  material?: StudyMaterial;
  subjectId: number;
}

@Component({
  selector: 'app-material-form-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule],
  templateUrl: './material-form-dialog.component.html',
  styleUrl: './material-form-dialog.component.css'
})
export class MaterialFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly materialService = inject(MaterialService);
  private readonly dialogRef = inject(MatDialogRef<MaterialFormDialogComponent>);
  private readonly data = inject<MaterialFormDialogData>(MAT_DIALOG_DATA);

  readonly isEditMode = !!this.data.material;
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    title: [this.data.material?.title ?? '', [Validators.required]],
    content: [this.data.material?.content ?? '', [Validators.required]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    const request: MaterialRequest = {
      subjectId: this.data.subjectId,
      title: raw.title!,
      content: raw.content!
    };

    const action = this.isEditMode
      ? this.materialService.update(this.data.material!.id, request)
      : this.materialService.create(request);

    action.subscribe({
      next: (material) => this.dialogRef.close(material),
      error: (err) => {
        this.saving.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Čuvanje gradiva nije uspelo.');
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
