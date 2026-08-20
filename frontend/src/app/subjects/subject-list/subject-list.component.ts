import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject } from '../../core/models/subject.model';
import { SubjectService } from '../../core/services/subject.service';
import { SubjectFormDialogComponent } from '../subject-form-dialog/subject-form-dialog.component';

const DIFFICULTY_LABELS: Record<string, string> = { LOW: 'Niska', MEDIUM: 'Srednja', HIGH: 'Visoka' };
const PRIORITY_LABELS: Record<string, string> = { LOW: 'Nizak', MEDIUM: 'Srednji', HIGH: 'Visok' };

@Component({
  selector: 'app-subject-list',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatDialogModule,
    MatIconModule,
    MatProgressBarModule,
    MatSlideToggleModule,
    MatTooltipModule
  ],
  templateUrl: './subject-list.component.html',
  styleUrl: './subject-list.component.css'
})
export class SubjectListComponent implements OnInit {
  private readonly subjectService = inject(SubjectService);
  private readonly dialog = inject(MatDialog);

  readonly subjects = signal<Subject[]>([]);
  readonly showArchived = signal(false);
  readonly loading = signal(false);

  readonly difficultyLabels = DIFFICULTY_LABELS;
  readonly priorityLabels = PRIORITY_LABELS;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.subjectService.getAll(this.showArchived()).subscribe({
      next: (subjects) => {
        this.subjects.set(subjects);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  toggleArchived(): void {
    this.showArchived.set(!this.showArchived());
    this.load();
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(SubjectFormDialogComponent, { data: {} });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  openEditDialog(subject: Subject): void {
    const ref = this.dialog.open(SubjectFormDialogComponent, { data: { subject } });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  archive(subject: Subject): void {
    if (!confirm(`Arhivirati predmet "${subject.name}"?`)) return;
    this.subjectService.archive(subject.id).subscribe(() => this.load());
  }

  remove(subject: Subject): void {
    if (!confirm(`Obrisati predmet "${subject.name}"? Ova akcija se ne može poništiti.`)) return;
    this.subjectService.delete(subject.id).subscribe(() => this.load());
  }
}
