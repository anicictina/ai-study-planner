import { NgClass } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
import { Reminder } from '../../core/models/reminder.model';
import { Subject } from '../../core/models/subject.model';
import { ReminderService } from '../../core/services/reminder.service';
import { SubjectService } from '../../core/services/subject.service';
import { ReminderFormDialogComponent } from '../reminder-form-dialog/reminder-form-dialog.component';

@Component({
  selector: 'app-reminder-list',
  standalone: true,
  imports: [NgClass, MatButtonModule, MatChipsModule, MatDialogModule, MatIconModule, MatTableModule, MatTooltipModule],
  templateUrl: './reminder-list.component.html',
  styleUrl: './reminder-list.component.css'
})
export class ReminderListComponent implements OnInit {
  private readonly reminderService = inject(ReminderService);
  private readonly subjectService = inject(SubjectService);
  private readonly dialog = inject(MatDialog);

  readonly reminders = signal<Reminder[]>([]);
  readonly subjects = signal<Subject[]>([]);
  readonly loading = signal(false);

  readonly displayedColumns = ['message', 'subject', 'remindAt', 'status', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    forkJoin({
      reminders: this.reminderService.getAll(),
      subjects: this.subjectService.getAll(false)
    }).subscribe({
      next: ({ reminders, subjects }) => {
        this.reminders.set(reminders);
        this.subjects.set(subjects);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  statusClass(reminder: Reminder): string {
    if (reminder.dismissed) return 'status-dismissed';
    if (reminder.due) return 'status-due';
    return 'status-scheduled';
  }

  statusLabel(reminder: Reminder): string {
    if (reminder.dismissed) return 'Odbačen';
    if (reminder.due) return 'Dospeo';
    return 'Zakazan';
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(ReminderFormDialogComponent, { data: { subjects: this.subjects() } });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  openEditDialog(reminder: Reminder): void {
    const ref = this.dialog.open(ReminderFormDialogComponent, {
      data: { reminder, subjects: this.subjects() }
    });
    ref.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  dismiss(reminder: Reminder): void {
    this.reminderService.dismiss(reminder.id).subscribe(() => this.load());
  }

  remove(reminder: Reminder): void {
    if (!confirm('Obrisati ovaj podsetnik?')) return;
    this.reminderService.delete(reminder.id).subscribe(() => this.load());
  }
}
