import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../core/services/auth.service';
import { Reminder } from '../../core/models/reminder.model';
import { ReminderService } from '../../core/services/reminder.service';
import { ThemeService } from '../../core/services/theme.service';

const POLL_INTERVAL_MS = 60000;

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    MatBadgeModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatToolbarModule,
    MatTooltipModule
  ],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly reminderService = inject(ReminderService);
  private readonly themeService = inject(ThemeService);
  private intervalId: ReturnType<typeof setInterval> | null = null;

  readonly currentUser = this.authService.currentUser;
  readonly dueReminders = signal<Reminder[]>([]);
  readonly isDarkTheme = this.themeService.isDark;

  ngOnInit(): void {
    this.loadDueReminders();
    this.intervalId = setInterval(() => this.loadDueReminders(), POLL_INTERVAL_MS);
  }

  ngOnDestroy(): void {
    if (this.intervalId) clearInterval(this.intervalId);
  }

  loadDueReminders(): void {
    this.reminderService.getDue().subscribe({
      next: (reminders) => this.dueReminders.set(reminders),
      error: () => this.dueReminders.set([])
    });
  }

  dismiss(reminder: Reminder): void {
    this.reminderService.dismiss(reminder.id).subscribe(() => this.loadDueReminders());
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }

  logout(): void {
    this.authService.logout();
  }
}
