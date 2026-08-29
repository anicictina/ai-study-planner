import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { StudyMaterial } from '../../core/models/material.model';
import { Subject } from '../../core/models/subject.model';
import { ConfirmDialogService } from '../../core/services/confirm-dialog.service';
import { MaterialService } from '../../core/services/material.service';
import { QuizService } from '../../core/services/quiz.service';
import { SubjectService } from '../../core/services/subject.service';
import { MaterialFormDialogComponent } from '../material-form-dialog/material-form-dialog.component';
import { MaterialSummaryDialogComponent } from '../material-summary-dialog/material-summary-dialog.component';
import { MaterialUploadDialogComponent } from '../material-upload-dialog/material-upload-dialog.component';

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
  private readonly quizService = inject(QuizService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly snackBar = inject(MatSnackBar);

  readonly subjects = signal<Subject[]>([]);
  readonly selectedSubjectId = signal<number | null>(null);
  readonly materials = signal<StudyMaterial[]>([]);
  readonly loading = signal(true);
  readonly generatingQuizFor = signal<number | null>(null);

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

  openUploadDialog(): void {
    const subjectId = this.selectedSubjectId();
    if (!subjectId) return;

    const ref = this.dialog.open(MaterialUploadDialogComponent, { data: { subjectId } });
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
    this.confirmDialogService
      .confirm({
        title: 'Obriši gradivo',
        message: `Obrisati gradivo "${material.title}"?`,
        confirmLabel: 'Obriši',
        danger: true
      })
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.materialService.delete(material.id).subscribe(() => this.loadMaterials());
      });
  }

  openSummaryDialog(material: StudyMaterial): void {
    this.dialog.open(MaterialSummaryDialogComponent, {
      data: { materialId: material.id, materialTitle: material.title }
    });
  }

  generateQuiz(material: StudyMaterial): void {
    this.generatingQuizFor.set(material.id);
    this.quizService.generate(material.id).subscribe({
      next: (quiz) => {
        this.generatingQuizFor.set(null);
        this.router.navigate(['/quizzes', quiz.id]);
      },
      error: (err) => {
        this.generatingQuizFor.set(null);
        this.snackBar.open(err?.error?.message ?? 'Generisanje kviza nije uspelo.', 'Zatvori', { duration: 6000 });
      }
    });
  }
}
