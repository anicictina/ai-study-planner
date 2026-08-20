import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';
import { Exam } from '../core/models/exam.model';
import { StudySession } from '../core/models/study-session.model';
import { ExamService } from '../core/services/exam.service';
import { StudySessionService } from '../core/services/study-session.service';

interface CalendarDay {
  date: Date;
  dateKey: string;
  inMonth: boolean;
  isToday: boolean;
  exams: Exam[];
  sessions: StudySession[];
}

const WEEKDAY_LABELS = ['Pon', 'Uto', 'Sre', 'Čet', 'Pet', 'Sub', 'Ned'];
const MONTH_LABELS = [
  'Januar', 'Februar', 'Mart', 'April', 'Maj', 'Jun',
  'Jul', 'Avgust', 'Septembar', 'Oktobar', 'Novembar', 'Decembar'
];

function dateKey(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function isSameDay(a: Date, b: Date): boolean {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatTooltipModule],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.css'
})
export class CalendarComponent implements OnInit {
  private readonly examService = inject(ExamService);
  private readonly studySessionService = inject(StudySessionService);

  readonly weekdayLabels = WEEKDAY_LABELS;
  readonly exams = signal<Exam[]>([]);
  readonly sessions = signal<StudySession[]>([]);
  readonly cursor = signal(new Date());

  readonly monthLabel = computed(() => {
    const c = this.cursor();
    return `${MONTH_LABELS[c.getMonth()]} ${c.getFullYear()}`;
  });

  readonly weeks = computed(() => this.buildWeeks(this.cursor(), this.exams(), this.sessions()));

  ngOnInit(): void {
    forkJoin({
      exams: this.examService.getAll(),
      sessions: this.studySessionService.getAll()
    }).subscribe(({ exams, sessions }) => {
      this.exams.set(exams);
      this.sessions.set(sessions);
    });
  }

  previousMonth(): void {
    const c = this.cursor();
    this.cursor.set(new Date(c.getFullYear(), c.getMonth() - 1, 1));
  }

  nextMonth(): void {
    const c = this.cursor();
    this.cursor.set(new Date(c.getFullYear(), c.getMonth() + 1, 1));
  }

  goToToday(): void {
    this.cursor.set(new Date());
  }

  private buildWeeks(cursor: Date, exams: Exam[], sessions: StudySession[]): CalendarDay[][] {
    const year = cursor.getFullYear();
    const month = cursor.getMonth();
    const today = new Date();

    const firstOfMonth = new Date(year, month, 1);
    // Monday-first offset: getDay() 0=Sunday..6=Saturday -> shift so Monday=0
    const leadingOffset = (firstOfMonth.getDay() + 6) % 7;
    const gridStart = new Date(year, month, 1 - leadingOffset);

    const examsByDate = new Map<string, Exam[]>();
    for (const exam of exams) {
      const list = examsByDate.get(exam.examDate) ?? [];
      list.push(exam);
      examsByDate.set(exam.examDate, list);
    }

    const sessionsByDate = new Map<string, StudySession[]>();
    for (const session of sessions) {
      const list = sessionsByDate.get(session.sessionDate) ?? [];
      list.push(session);
      sessionsByDate.set(session.sessionDate, list);
    }

    const days: CalendarDay[] = [];
    for (let i = 0; i < 42; i++) {
      const date = new Date(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate() + i);
      const key = dateKey(date);
      days.push({
        date,
        dateKey: key,
        inMonth: date.getMonth() === month,
        isToday: isSameDay(date, today),
        exams: examsByDate.get(key) ?? [],
        sessions: sessionsByDate.get(key) ?? []
      });
    }

    const weeks: CalendarDay[][] = [];
    for (let i = 0; i < days.length; i += 7) {
      weeks.push(days.slice(i, i + 7));
    }
    return weeks;
  }
}
