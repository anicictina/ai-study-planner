import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Subject, SubjectRequest } from '../models/subject.model';

@Injectable({ providedIn: 'root' })
export class SubjectService {
  private readonly apiUrl = `${environment.apiUrl}/subjects`;

  constructor(private readonly http: HttpClient) {}

  getAll(archived = false): Observable<Subject[]> {
    return this.http.get<Subject[]>(this.apiUrl, { params: { archived } });
  }

  create(request: SubjectRequest): Observable<Subject> {
    return this.http.post<Subject>(this.apiUrl, request);
  }

  update(id: number, request: SubjectRequest): Observable<Subject> {
    return this.http.put<Subject>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  archive(id: number): Observable<Subject> {
    return this.http.patch<Subject>(`${this.apiUrl}/${id}/archive`, {});
  }
}
