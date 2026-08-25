import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MaterialRequest, MaterialStatus, MaterialSummary, StudyMaterial } from '../models/material.model';

@Injectable({ providedIn: 'root' })
export class MaterialService {
  private readonly apiUrl = `${environment.apiUrl}/materials`;

  constructor(private readonly http: HttpClient) {}

  getAllForSubject(subjectId: number): Observable<StudyMaterial[]> {
    return this.http.get<StudyMaterial[]>(`${this.apiUrl}/subject/${subjectId}`);
  }

  getOne(id: number): Observable<StudyMaterial> {
    return this.http.get<StudyMaterial>(`${this.apiUrl}/${id}`);
  }

  create(request: MaterialRequest): Observable<StudyMaterial> {
    return this.http.post<StudyMaterial>(this.apiUrl, request);
  }

  update(id: number, request: MaterialRequest): Observable<StudyMaterial> {
    return this.http.put<StudyMaterial>(`${this.apiUrl}/${id}`, request);
  }

  updateStatus(id: number, status: MaterialStatus): Observable<StudyMaterial> {
    return this.http.patch<StudyMaterial>(`${this.apiUrl}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  uploadFile(subjectId: number, title: string | null, file: File): Observable<StudyMaterial> {
    const formData = new FormData();
    formData.append('subjectId', String(subjectId));
    if (title) formData.append('title', title);
    formData.append('file', file);

    return this.http.post<StudyMaterial>(`${this.apiUrl}/upload`, formData);
  }

  generateSummary(id: number): Observable<MaterialSummary> {
    return this.http.post<MaterialSummary>(`${this.apiUrl}/${id}/summary`, {});
  }

  getSummary(id: number): Observable<MaterialSummary> {
    return this.http.get<MaterialSummary>(`${this.apiUrl}/${id}/summary`);
  }
}
