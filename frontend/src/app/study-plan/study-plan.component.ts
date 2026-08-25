import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { StudyPlan, StudyPlanItem } from '../core/models/study-plan.model';
import { Subject } from '../core/models/subject.model';
import { StudyPlanService } from '../core/services/study-plan.service';
import { SubjectService } from '../core/services/subject.service';

interface DateGroup {
  date: string;
  items: StudyPlanItem[];
}

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Na čekanju',
  ACCEPTED: 'Prihvaćen',
  DISCARDED: 'Odbačen'
};

function groupByDate(items: StudyPlanItem[]): DateGroup[] {
  const map = new Map<string, StudyPlanItem[]>();
  for (const item of items) {
    const list = map.get(item.itemDate) ?? [];
    list.push(item);
    map.set(item.itemDate, list);
  }
  return Array.from(map.entries())
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([date, groupItems]) => ({ date, items: groupItems }));
}

@Component({
  selector: 'app-study-plan',
  standalone: true,
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './study-plan.component.html',
  styleUrl: './study-plan.component.css'
})
export class StudyPlanComponent implements OnInit {
  private readonly studyPlanService = inject(StudyPlanService);
  private readonly subjectService = inject(SubjectService);

  readonly subjects = signal<Subject[]>([]);
  readonly selectedSubjectIds = signal<Set<number>>(new Set());
  readonly currentPlan = signal<StudyPlan | null>(null);
  readonly history = signal<StudyPlan[]>([]);

  readonly loadingPlan = signal(true);
  readonly generating = signal(false);
  readonly actionInProgress = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly statusLabels = STATUS_LABELS;
  readonly groupedItems = computed(() => groupByDate(this.currentPlan()?.items ?? []));
  readonly hasAcceptedPlanBefore = computed(() => this.history().some((plan) => plan.status === 'ACCEPTED'));

  ngOnInit(): void {
    this.subjectService.getAll(false).subscribe((subjects) => this.subjects.set(subjects));
    this.loadCurrentPlan();
    this.loadHistory();
  }

  loadCurrentPlan(): void {
    this.loadingPlan.set(true);
    this.studyPlanService.getCurrent().subscribe({
      next: (plan) => {
        this.currentPlan.set(plan);
        this.loadingPlan.set(false);
      },
      error: () => {
        this.currentPlan.set(null);
        this.loadingPlan.set(false);
      }
    });
  }

  loadHistory(): void {
    this.studyPlanService.getHistory().subscribe((plans) => this.history.set(plans));
  }

  toggleSubject(subjectId: number): void {
    const current = new Set(this.selectedSubjectIds());
    if (current.has(subjectId)) {
      current.delete(subjectId);
    } else {
      current.add(subjectId);
    }
    this.selectedSubjectIds.set(current);
  }

  isPast(dateStr: string): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return new Date(dateStr) < today;
  }

  generate(): void {
    this.generating.set(true);
    this.errorMessage.set(null);

    const subjectIds = Array.from(this.selectedSubjectIds());
    const request = { subjectIds: subjectIds.length > 0 ? subjectIds : undefined };
    const isRecalculation = this.hasAcceptedPlanBefore();

    const action = isRecalculation
      ? this.studyPlanService.recalculate(request)
      : this.studyPlanService.generate(request);

    action.subscribe({
      next: (plan) => {
        this.currentPlan.set(plan);
        this.generating.set(false);
        this.loadHistory();
      },
      error: (err) => {
        this.generating.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Generisanje plana nije uspelo. Pokušajte ponovo kasnije.');
      }
    });
  }

  accept(): void {
    const plan = this.currentPlan();
    if (!plan) return;

    this.actionInProgress.set(true);
    this.studyPlanService.accept(plan.id).subscribe({
      next: (updated) => {
        this.currentPlan.set(updated);
        this.actionInProgress.set(false);
        this.loadHistory();
      },
      error: () => this.actionInProgress.set(false)
    });
  }

  discard(): void {
    const plan = this.currentPlan();
    if (!plan) return;

    this.actionInProgress.set(true);
    this.studyPlanService.discard(plan.id).subscribe({
      next: (updated) => {
        this.currentPlan.set(updated);
        this.actionInProgress.set(false);
        this.loadHistory();
      },
      error: () => this.actionInProgress.set(false)
    });
  }
}
