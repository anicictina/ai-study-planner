import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MaterialSummary } from '../../core/models/material.model';
import { MaterialService } from '../../core/services/material.service';

export interface MaterialSummaryDialogData {
  materialId: number;
  materialTitle: string;
}

@Component({
  selector: 'app-material-summary-dialog',
  standalone: true,
  imports: [MatButtonModule, MatChipsModule, MatDialogModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './material-summary-dialog.component.html',
  styleUrl: './material-summary-dialog.component.css'
})
export class MaterialSummaryDialogComponent implements OnInit {
  private readonly materialService = inject(MaterialService);
  private readonly dialogRef = inject(MatDialogRef<MaterialSummaryDialogComponent>);
  readonly data = inject<MaterialSummaryDialogData>(MAT_DIALOG_DATA);

  readonly summary = signal<MaterialSummary | null>(null);
  readonly loading = signal(true);
  readonly generating = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.materialService.getSummary(this.data.materialId).subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => {
        this.summary.set(null);
        this.loading.set(false);
      }
    });
  }

  generate(): void {
    this.generating.set(true);
    this.errorMessage.set(null);

    this.materialService.generateSummary(this.data.materialId).subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.generating.set(false);
      },
      error: (err) => {
        this.generating.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Generisanje sažetka nije uspelo. Pokušajte ponovo kasnije.');
      }
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
