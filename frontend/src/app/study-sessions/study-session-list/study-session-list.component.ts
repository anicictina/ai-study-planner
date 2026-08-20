import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
import { StudySession } from '../../core/models/study-session.model';
import { Subject } from '../../core/models/subject.model';
import { StudySessionService } from '../../core/services/study-session.service';
import { SubjectService } from '../../core/services/subject.service';
import { StudySessionFormDialogComponent } from '../study-session-form-dialog/study-session-form-dialog.component';

const ACTIVITY_LABELS: Record<string, string> = {
  READING: 'Čitanje',
  PRACTICE: 'Vežbanje',
  QUIZ: 'Kviz',
  REVISION: 'Ponavljanje',
  HOMEWORK: 'Domaći zadatak'
};

@Component({
  selector: 'app-study-session-list',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatIconModule,
    MatTableModule,
    MatTooltipModule
  ],
  templateUrl: './study-session-list.component.html',
  styleUrl: './study-session-list.component.css'
})
export class StudySessionListComponent implements OnInit {
  private readonly studySessionService = inject(StudySessionService);
  private readonly subjectService = inject(SubjectService);
  private readonly dialog = inject(MatDialog);

  readonly sessions = signal<StudySession[]>([]);
  readonly subjects = signal<Subject[]>([]);
  readonly loading = signal(false);

  readonly activityLabels = ACTIVITY_LABELS;
  readonly displayedColumns = ['completed', 'subject', 'topic', 'date', 'duration', 'activityType', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    forkJoin({
      sessions: this.studySessionService.getAll(),
      subjects: this.subjectService.getAll(false)
    }).subscribe({
      next: ({ sessions, subjects }) => {
        this.sessions.set(sessions);
        this.subjects.set(subjects);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(StudySessionFormDialogComponent, { data: { subjects: this.subjects() } });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  openEditDialog(session: StudySession): void {
    const ref = this.dialog.open(StudySessionFormDialogComponent, {
      data: { session, subjects: this.subjects() }
    });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  toggleComplete(session: StudySession): void {
    if (session.completed) return;
    this.studySessionService.complete(session.id).subscribe(() => this.load());
  }

  remove(session: StudySession): void {
    if (!confirm(`Obrisati sesiju iz predmeta "${session.subjectName}"?`)) return;
    this.studySessionService.delete(session.id).subscribe(() => this.load());
  }
}
