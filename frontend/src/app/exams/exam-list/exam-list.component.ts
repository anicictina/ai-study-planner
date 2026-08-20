import { NgClass } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
import { Exam } from '../../core/models/exam.model';
import { Subject } from '../../core/models/subject.model';
import { ExamService } from '../../core/services/exam.service';
import { SubjectService } from '../../core/services/subject.service';
import { ExamFormDialogComponent } from '../exam-form-dialog/exam-form-dialog.component';

const STATUS_LABELS: Record<string, string> = { PLANNED: 'Planiran', PASSED: 'Položen', FAILED: 'Pao' };

@Component({
  selector: 'app-exam-list',
  standalone: true,
  imports: [NgClass, MatButtonModule, MatChipsModule, MatDialogModule, MatIconModule, MatTableModule, MatTooltipModule],
  templateUrl: './exam-list.component.html',
  styleUrl: './exam-list.component.css'
})
export class ExamListComponent implements OnInit {
  private readonly examService = inject(ExamService);
  private readonly subjectService = inject(SubjectService);
  private readonly dialog = inject(MatDialog);

  readonly exams = signal<Exam[]>([]);
  readonly subjects = signal<Subject[]>([]);
  readonly loading = signal(false);

  readonly statusLabels = STATUS_LABELS;
  readonly displayedColumns = ['subject', 'date', 'location', 'daysRemaining', 'status', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    forkJoin({
      exams: this.examService.getAll(),
      subjects: this.subjectService.getAll(false)
    }).subscribe({
      next: ({ exams, subjects }) => {
        this.exams.set(exams);
        this.subjects.set(subjects);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  urgencyClass(daysRemaining: number): string {
    if (daysRemaining < 0) return 'urgency-past';
    if (daysRemaining <= 3) return 'urgency-critical';
    if (daysRemaining <= 7) return 'urgency-soon';
    return 'urgency-normal';
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(ExamFormDialogComponent, { data: { subjects: this.subjects() } });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  openEditDialog(exam: Exam): void {
    const ref = this.dialog.open(ExamFormDialogComponent, { data: { exam, subjects: this.subjects() } });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  remove(exam: Exam): void {
    if (!confirm(`Obrisati ispit iz predmeta "${exam.subjectName}"?`)) return;
    this.examService.delete(exam.id).subscribe(() => this.load());
  }
}
