import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MaterialService } from '../../core/services/material.service';

export interface MaterialUploadDialogData {
  subjectId: number;
}

@Component({
  selector: 'app-material-upload-dialog',
  standalone: true,
  imports: [FormsModule, MatButtonModule, MatDialogModule, MatFormFieldModule, MatIconModule, MatInputModule],
  templateUrl: './material-upload-dialog.component.html',
  styleUrl: './material-upload-dialog.component.css'
})
export class MaterialUploadDialogComponent {
  private readonly materialService = inject(MaterialService);
  private readonly dialogRef = inject(MatDialogRef<MaterialUploadDialogComponent>);
  private readonly data = inject<MaterialUploadDialogData>(MAT_DIALOG_DATA);

  readonly title = signal('');
  readonly selectedFile = signal<File | null>(null);
  readonly uploading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile.set(file);
    this.errorMessage.set(null);
  }

  submit(): void {
    const file = this.selectedFile();
    if (!file) {
      this.errorMessage.set('Izaberi PDF ili TXT fajl.');
      return;
    }

    this.uploading.set(true);
    this.errorMessage.set(null);

    this.materialService.uploadFile(this.data.subjectId, this.title() || null, file).subscribe({
      next: (material) => this.dialogRef.close(material),
      error: (err) => {
        this.uploading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Otpremanje fajla nije uspelo.');
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
