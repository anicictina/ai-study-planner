import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Quiz, QuizAttemptSummary, QuizResult } from '../models/quiz.model';

@Injectable({ providedIn: 'root' })
export class QuizService {
  private readonly apiUrl = `${environment.apiUrl}/quizzes`;

  constructor(private readonly http: HttpClient) {}

  generate(materialId: number, questionCount = 5): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.apiUrl}/generate`, { materialId, questionCount });
  }

  getOne(id: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/${id}`);
  }

  submit(id: number, answers: { questionId: number; selectedIndex: number }[]): Observable<QuizResult> {
    return this.http.post<QuizResult>(`${this.apiUrl}/${id}/submit`, { answers });
  }

  getAttemptHistory(): Observable<QuizAttemptSummary[]> {
    return this.http.get<QuizAttemptSummary[]>(`${this.apiUrl}/attempts`);
  }
}
