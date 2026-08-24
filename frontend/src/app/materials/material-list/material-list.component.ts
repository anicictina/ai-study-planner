import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { StudyMaterial } from '../../core/models/material.model';
import { Subject } from '../../core/models/subject.model';
import { MaterialService } from '../../core/services/material.service';
import { SubjectService } from '../../core/services/subject.service';
import { MaterialFormDialogComponent } from '../material-form-dialog/material-form-dialog.component';

const STATUS_LABELS: Record<string, string> = {
  NOT_STARTED: 'Nije početo',
  IN_PROGRESS: 'U toku',
  LEARNED: 'Naučeno'
};

@Component({
  selector: 'app-material-list',
  standalone: true,
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatIconModule,
    MatSelectModule,
    MatTooltipModule
  ],
  templateUrl: './material-list.component.html',
  styleUrl: './material-list.component.css'
})
export class MaterialListComponent implements OnInit {
  private readonly materialService = inject(MaterialService);
  private readonly subjectService = inject(SubjectService);
  private readonly dialog = inject(MatDialog);

  readonly subjects = signal<Subject[]>([]);
  readonly selectedSubjectId = signal<number | null>(null);
  readonly materials = signal<StudyMaterial[]>([]);
  readonly loading = signal(true);

  readonly statusLabels = STATUS_LABELS;

  ngOnInit(): void {
    this.subjectService.getAll(false).subscribe((subjects) => {
      this.subjects.set(subjects);
      if (subjects.length > 0) {
        this.selectedSubjectId.set(subjects[0].id);
        this.loadMaterials();
      } else {
        this.loading.set(false);
      }
    });
  }

  onSubjectChange(subjectId: number): void {
    this.selectedSubjectId.set(subjectId);
    this.loadMaterials();
  }

  loadMaterials(): void {
    const subjectId = this.selectedSubjectId();
    if (!subjectId) return;

    this.loading.set(true);
    this.materialService.getAllForSubject(subjectId).subscribe({
      next: (materials) => {
        this.materials.set(materials);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  openCreateDialog(): void {
    const subjectId = this.selectedSubjectId();
    if (!subjectId) return;

    const ref = this.dialog.open(MaterialFormDialogComponent, { data: { subjectId } });
    ref.afterClosed().subscribe((result) => {
      if (result) this.loadMaterials();
    });
  }

  openEditDialog(material: StudyMaterial): void {
    const ref = this.dialog.open(MaterialFormDialogComponent, {
      data: { material, subjectId: material.subjectId }
    });
    ref.afterClosed().subscribe((result) => {
      if (result) this.loadMaterials();
    });
  }

  changeStatus(material: StudyMaterial, status: string): void {
    this.materialService.updateStatus(material.id, status as StudyMaterial['status']).subscribe(() => {
      this.loadMaterials();
    });
  }

  remove(material: StudyMaterial): void {
    if (!confirm(`Obrisati gradivo "${material.title}"?`)) return;
    this.materialService.delete(material.id).subscribe(() => this.loadMaterials());
  }
}
