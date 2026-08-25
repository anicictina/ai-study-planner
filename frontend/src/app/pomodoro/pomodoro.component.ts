import { DecimalPipe } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Subject } from '../core/models/subject.model';
import { StudySessionRequest } from '../core/models/study-session.model';
import { StudySessionService } from '../core/services/study-session.service';
import { SubjectService } from '../core/services/subject.service';

type Phase = 'IDLE' | 'WORK' | 'SHORT_BREAK' | 'LONG_BREAK';

const PHASE_LABELS: Record<Phase, string> = {
  IDLE: 'Spremno',
  WORK: 'Fokusiran rad',
  SHORT_BREAK: 'Kratka pauza',
  LONG_BREAK: 'Duga pauza'
};

function toApiDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function toApiTime(date: Date): string {
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}:00`;
}

@Component({
  selector: 'app-pomodoro',
  standalone: true,
  imports: [
    DecimalPipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule
  ],
  templateUrl: './pomodoro.component.html',
  styleUrl: './pomodoro.component.css'
})
export class PomodoroComponent implements OnInit, OnDestroy {
  private readonly subjectService = inject(SubjectService);
  private readonly studySessionService = inject(StudySessionService);
  private intervalId: ReturnType<typeof setInterval> | null = null;
  private workPhaseStartedAt: Date | null = null;

  readonly subjects = signal<Subject[]>([]);
  readonly selectedSubjectId = signal<number | null>(null);
  readonly topic = signal('');

  readonly workMinutes = signal(25);
  readonly shortBreakMinutes = signal(5);
  readonly longBreakMinutes = signal(15);
  readonly cyclesBeforeLongBreak = signal(4);

  readonly phase = signal<Phase>('IDLE');
  readonly remainingSeconds = signal(0);
  readonly isRunning = signal(false);
  readonly completedCycles = signal(0);
  readonly totalFocusedMinutesToday = signal(0);

  readonly phaseLabel = computed(() => PHASE_LABELS[this.phase()]);
  readonly minutes = computed(() => Math.floor(this.remainingSeconds() / 60));
  readonly seconds = computed(() => this.remainingSeconds() % 60);
  readonly progressPercent = computed(() => {
    const total = this.totalSecondsForPhase(this.phase());
    if (total === 0) return 0;
    return ((total - this.remainingSeconds()) / total) * 100;
  });

  ngOnInit(): void {
    this.subjectService.getAll(false).subscribe((subjects) => {
      this.subjects.set(subjects);
      if (subjects.length > 0) this.selectedSubjectId.set(subjects[0].id);
    });
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  start(): void {
    if (this.phase() === 'IDLE') {
      this.beginPhase('WORK');
    }
    this.isRunning.set(true);
    this.startTimer();
  }

  pause(): void {
    this.isRunning.set(false);
    this.stopTimer();
  }

  reset(): void {
    this.stopTimer();
    this.isRunning.set(false);
    this.phase.set('IDLE');
    this.remainingSeconds.set(0);
    this.completedCycles.set(0);
    this.workPhaseStartedAt = null;
  }

  skip(): void {
    this.advancePhase();
  }

  private startTimer(): void {
    if (this.intervalId) return;
    this.intervalId = setInterval(() => {
      const remaining = this.remainingSeconds();
      if (remaining <= 1) {
        this.remainingSeconds.set(0);
        this.advancePhase();
      } else {
        this.remainingSeconds.set(remaining - 1);
      }
    }, 1000);
  }

  private stopTimer(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

  private beginPhase(phase: Phase): void {
    this.phase.set(phase);
    this.remainingSeconds.set(this.totalSecondsForPhase(phase));
    if (phase === 'WORK') {
      this.workPhaseStartedAt = new Date();
    }
  }

  private advancePhase(): void {
    const currentPhase = this.phase();

    if (currentPhase === 'WORK') {
      this.logCompletedWorkPhase();
      const nextCount = this.completedCycles() + 1;
      this.completedCycles.set(nextCount);

      const nextPhase = nextCount % this.cyclesBeforeLongBreak() === 0 ? 'LONG_BREAK' : 'SHORT_BREAK';
      this.beginPhase(nextPhase);
    } else {
      this.beginPhase('WORK');
    }
  }

  private logCompletedWorkPhase(): void {
    const subjectId = this.selectedSubjectId();
    if (!subjectId || !this.workPhaseStartedAt) return;

    const request: StudySessionRequest = {
      subjectId,
      topic: this.topic() || null,
      sessionDate: toApiDate(this.workPhaseStartedAt),
      startTime: toApiTime(this.workPhaseStartedAt),
      durationMinutes: this.workMinutes(),
      activityType: 'READING'
    };

    this.studySessionService.create(request).subscribe((session) => {
      this.studySessionService.complete(session.id).subscribe(() => {
        this.totalFocusedMinutesToday.set(this.totalFocusedMinutesToday() + this.workMinutes());
      });
    });
  }

  private totalSecondsForPhase(phase: Phase): number {
    switch (phase) {
      case 'WORK':
        return this.workMinutes() * 60;
      case 'SHORT_BREAK':
        return this.shortBreakMinutes() * 60;
      case 'LONG_BREAK':
        return this.longBreakMinutes() * 60;
      default:
        return this.workMinutes() * 60;
    }
  }
}
