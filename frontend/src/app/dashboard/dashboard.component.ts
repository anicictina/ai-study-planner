import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../core/services/auth.service';
import { StatisticsOverview } from '../core/models/statistics.model';
import { StatisticsService } from '../core/services/statistics.service';
import { StudyPlan } from '../core/models/study-plan.model';
import { StudyPlanService } from '../core/services/study-plan.service';
import { SubjectService } from '../core/services/subject.service';

const WEEKDAY_LABELS = ['Pon', 'Uto', 'Sre', 'Čet', 'Pet', 'Sub', 'Ned'];
const FULL_DAY_LABELS = [
  'nedelja',
  'ponedeljak',
  'utorak',
  'sreda',
  'četvrtak',
  'petak',
  'subota'
];

const MOTIVATIONAL_QUOTES = [
  'Napreduj svaki dan, makar malo.',
  'Tvoja jedina granica je tvoj um.',
  'Mali koraci i dalje vode do velikog cilja.',
  'Disciplina danas znači sloboda sutra.',
  'Ne moraš biti savršena, samo dosledna.',
  'Svaki sat učenja te približava cilju.',
  'Budi ponosna na trud koji ulažeš.'
];

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
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly statisticsService = inject(StatisticsService);
  private readonly subjectService = inject(SubjectService);
  private readonly studyPlanService = inject(StudyPlanService);
  private clockIntervalId: ReturnType<typeof setInterval> | null = null;

  readonly currentUser = this.authService.currentUser;
  readonly stats = signal<StatisticsOverview | null>(null);
  readonly activeSubjectCount = signal(0);
  readonly pendingPlan = signal<StudyPlan | null>(null);
  readonly loading = signal(true);
  readonly weekdayLabels = WEEKDAY_LABELS;
  readonly now = signal(new Date());
  readonly quoteOfTheDay = MOTIVATIONAL_QUOTES[Math.floor(Math.random() * MOTIVATIONAL_QUOTES.length)];

  readonly clockTimeLabel = computed(() => {
    const date = this.now();
    return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
  });

  readonly clockDayLabel = computed(() => FULL_DAY_LABELS[this.now().getDay()]);

  readonly hourHandDegrees = computed(() => {
    const date = this.now();
    return (date.getHours() % 12) * 30 + date.getMinutes() * 0.5;
  });

  readonly minuteHandDegrees = computed(() => this.now().getMinutes() * 6);

  readonly todayPlannedMinutes = computed(() => {
    const sessions = this.stats()?.todaySessions ?? [];
    return sessions.reduce((total, session) => total + session.durationMinutes, 0);
  });

  readonly nearestExamDays = computed(() => {
    const exams = this.stats()?.upcomingExams ?? [];
    return exams.length > 0 ? exams[0].daysRemaining : null;
  });

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
    this.clockIntervalId = setInterval(() => this.now.set(new Date()), 30000);

    forkJoin({
      stats: this.statisticsService.getOverview(),
      subjects: this.subjectService.getAll(false),
      currentPlan: this.studyPlanService.getCurrent().pipe(catchError(() => of(null)))
    }).subscribe({
      next: ({ stats, subjects, currentPlan }) => {
        this.stats.set(stats);
        this.activeSubjectCount.set(subjects.length);
        this.pendingPlan.set(currentPlan?.status === 'PENDING' ? currentPlan : null);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  ngOnDestroy(): void {
    if (this.clockIntervalId) clearInterval(this.clockIntervalId);
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
