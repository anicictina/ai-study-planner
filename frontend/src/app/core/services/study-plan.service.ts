import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StudyPlan, StudyPlanGenerateRequest } from '../models/study-plan.model';

@Injectable({ providedIn: 'root' })
export class StudyPlanService {
  private readonly apiUrl = `${environment.apiUrl}/study-plans`;

  constructor(private readonly http: HttpClient) {}

  generate(request: StudyPlanGenerateRequest): Observable<StudyPlan> {
    return this.http.post<StudyPlan>(`${this.apiUrl}/generate`, request);
  }

  recalculate(request: StudyPlanGenerateRequest): Observable<StudyPlan> {
    return this.http.post<StudyPlan>(`${this.apiUrl}/recalculate`, request);
  }

  getCurrent(): Observable<StudyPlan> {
    return this.http.get<StudyPlan>(`${this.apiUrl}/current`);
  }

  getHistory(): Observable<StudyPlan[]> {
    return this.http.get<StudyPlan[]>(this.apiUrl);
  }

  accept(id: number): Observable<StudyPlan> {
    return this.http.post<StudyPlan>(`${this.apiUrl}/${id}/accept`, {});
  }

  discard(id: number): Observable<StudyPlan> {
    return this.http.post<StudyPlan>(`${this.apiUrl}/${id}/discard`, {});
  }
}
