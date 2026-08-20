import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StudySession, StudySessionRequest } from '../models/study-session.model';

@Injectable({ providedIn: 'root' })
export class StudySessionService {
  private readonly apiUrl = `${environment.apiUrl}/study-sessions`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<StudySession[]> {
    return this.http.get<StudySession[]>(this.apiUrl);
  }

  create(request: StudySessionRequest): Observable<StudySession> {
    return this.http.post<StudySession>(this.apiUrl, request);
  }

  update(id: number, request: StudySessionRequest): Observable<StudySession> {
    return this.http.put<StudySession>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  complete(id: number): Observable<StudySession> {
    return this.http.patch<StudySession>(`${this.apiUrl}/${id}/complete`, {});
  }
}
