import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../core/services/auth.service';
import { StatisticsOverview } from '../core/models/statistics.model';
import { StatisticsService } from '../core/services/statistics.service';

const WEEKDAY_LABELS = ['Pon', 'Uto', 'Sre', 'Čet', 'Pet', 'Sub', 'Ned'];

interface TrendPoint {
  x: number;
  y: number;
  scorePercent: number;
  materialTitle: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatIconModule, MatTooltipModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly statisticsService = inject(StatisticsService);

  readonly currentUser = this.authService.currentUser;
  readonly stats = signal<StatisticsOverview | null>(null);
  readonly loading = signal(true);
  readonly weekdayLabels = WEEKDAY_LABELS;

  readonly maxDayMinutes = computed(() => {
    const byDay = this.stats()?.weeklyStudy.byDay ?? [];
    return Math.max(1, ...byDay.map((d) => d.minutes));
  });

  readonly trendPoints = computed<TrendPoint[]>(() => {
    const attempts = this.stats()?.quizStats.recentAttempts ?? [];
    if (attempts.length === 0) return [];

    const width = 100;
    const height = 100;
    const step = attempts.length > 1 ? width / (attempts.length - 1) : 0;

    return attempts.map((attempt, index) => ({
      x: attempts.length > 1 ? index * step : width / 2,
      y: height - attempt.scorePercent,
      scorePercent: Math.round(attempt.scorePercent),
      materialTitle: attempt.materialTitle
    }));
  });

  readonly trendPath = computed(() => {
    const points = this.trendPoints();
    if (points.length < 2) return '';
    return points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
  });

  ngOnInit(): void {
    this.statisticsService.getOverview().subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  barHeightPercent(minutes: number): number {
    return Math.max(2, (minutes / this.maxDayMinutes()) * 100);
  }

  formatMinutes(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours === 0) return `${mins} min`;
    if (mins === 0) return `${hours} h`;
    return `${hours} h ${mins} min`;
  }
}
