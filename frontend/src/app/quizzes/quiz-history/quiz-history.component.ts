import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { QuizAttemptSummary } from '../../core/models/quiz.model';
import { QuizService } from '../../core/services/quiz.service';

@Component({
  selector: 'app-quiz-history',
  standalone: true,
  imports: [MatCardModule, MatChipsModule, MatIconModule],
  templateUrl: './quiz-history.component.html',
  styleUrl: './quiz-history.component.css'
})
export class QuizHistoryComponent implements OnInit {
  private readonly quizService = inject(QuizService);

  readonly attempts = signal<QuizAttemptSummary[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.quizService.getAttemptHistory().subscribe({
      next: (attempts) => {
        this.attempts.set(attempts);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  scoreClass(attempt: QuizAttemptSummary): string {
    const ratio = attempt.correctCount / attempt.totalCount;
    if (ratio >= 0.8) return 'score-good';
    if (ratio >= 0.5) return 'score-medium';
    return 'score-poor';
  }
}
