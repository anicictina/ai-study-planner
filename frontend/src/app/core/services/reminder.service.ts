import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Reminder, ReminderRequest } from '../models/reminder.model';

@Injectable({ providedIn: 'root' })
export class ReminderService {
  private readonly apiUrl = `${environment.apiUrl}/reminders`;

  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(this.apiUrl);
  }

  getDue(): Observable<Reminder[]> {
    return this.http.get<Reminder[]>(`${this.apiUrl}/due`);
  }

  create(request: ReminderRequest): Observable<Reminder> {
    return this.http.post<Reminder>(this.apiUrl, request);
  }

  update(id: number, request: ReminderRequest): Observable<Reminder> {
    return this.http.put<Reminder>(`${this.apiUrl}/${id}`, request);
  }

  dismiss(id: number): Observable<Reminder> {
    return this.http.patch<Reminder>(`${this.apiUrl}/${id}/dismiss`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
