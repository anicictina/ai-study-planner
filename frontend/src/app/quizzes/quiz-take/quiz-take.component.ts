import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { Quiz } from '../../core/models/quiz.model';
import { QuizResult } from '../../core/models/quiz.model';
import { QuizService } from '../../core/services/quiz.service';

@Component({
  selector: 'app-quiz-take',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule, MatRadioModule],
  templateUrl: './quiz-take.component.html',
  styleUrl: './quiz-take.component.css'
})
export class QuizTakeComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly quizService = inject(QuizService);

  readonly quiz = signal<Quiz | null>(null);
  readonly result = signal<QuizResult | null>(null);
  readonly selectedAnswers = signal<Map<number, number>>(new Map());
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.quizService.getOne(id).subscribe({
      next: (quiz) => {
        this.quiz.set(quiz);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  selectAnswer(questionId: number, index: number): void {
    const updated = new Map(this.selectedAnswers());
    updated.set(questionId, index);
    this.selectedAnswers.set(updated);
  }

  get allAnswered(): boolean {
    const quiz = this.quiz();
    if (!quiz) return false;
    return quiz.questions.every((q) => this.selectedAnswers().has(q.id));
  }

  submit(): void {
    const quiz = this.quiz();
    if (!quiz) return;

    this.submitting.set(true);
    this.errorMessage.set(null);

    const answers = Array.from(this.selectedAnswers().entries()).map(([questionId, selectedIndex]) => ({
      questionId,
      selectedIndex
    }));

    this.quizService.submit(quiz.id, answers).subscribe({
      next: (result) => {
        this.result.set(result);
        this.submitting.set(false);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Predaja kviza nije uspela.');
      }
    });
  }

  optionClass(questionId: number, index: number): string {
    const result = this.result();
    if (!result) return '';

    const questionResult = result.results.find((r) => r.questionId === questionId);
    if (!questionResult) return '';

    if (index === questionResult.correctAnswerIndex) return 'option-correct';
    if (index === questionResult.selectedIndex && !questionResult.correct) return 'option-wrong';
    return '';
  }
}
