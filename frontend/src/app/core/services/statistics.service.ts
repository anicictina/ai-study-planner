import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StatisticsOverview } from '../models/statistics.model';

@Injectable({ providedIn: 'root' })
export class StatisticsService {
  private readonly apiUrl = `${environment.apiUrl}/statistics`;

  constructor(private readonly http: HttpClient) {}

  getOverview(): Observable<StatisticsOverview> {
    return this.http.get<StatisticsOverview>(`${this.apiUrl}/overview`);
  }
}
