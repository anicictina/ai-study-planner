import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Exam, ExamRequest } from '../models/exam.model';

@Injectable({ providedIn: 'root' })
export class ExamService {
  private readonly apiUrl = `${environment.apiUrl}/exams`;

  constructor(private readonly http: HttpClient) {}

  getAll(subjectId?: number): Observable<Exam[]> {
    return this.http.get<Exam[]>(this.apiUrl, {
      params: subjectId ? { subjectId } : {}
    });
  }

  create(request: ExamRequest): Observable<Exam> {
    return this.http.post<Exam>(this.apiUrl, request);
  }

  update(id: number, request: ExamRequest): Observable<Exam> {
    return this.http.put<Exam>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
