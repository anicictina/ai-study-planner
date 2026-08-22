import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AvailabilitySlot, AvailabilitySlotRequest, PreferredTime } from '../models/profile.model';
import { AuthUser } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly apiUrl = `${environment.apiUrl}/users/me`;

  constructor(private readonly http: HttpClient) {}

  getAvailability(): Observable<AvailabilitySlot[]> {
    return this.http.get<AvailabilitySlot[]>(`${this.apiUrl}/availability`);
  }

  updateAvailability(slots: AvailabilitySlotRequest[]): Observable<AvailabilitySlot[]> {
    return this.http.put<AvailabilitySlot[]>(`${this.apiUrl}/availability`, slots);
  }

  updatePreferredTime(preferredStudyTime: PreferredTime | null): Observable<AuthUser> {
    return this.http.put<AuthUser>(`${this.apiUrl}/preferred-time`, { preferredStudyTime });
  }
}
