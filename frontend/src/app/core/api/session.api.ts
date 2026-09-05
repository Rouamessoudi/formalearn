import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface TrainingSession {
  id: number;
  formationId: number;
  formationTitle: string;
  startDate: string;
  endDate: string;
  capacity: number;
  status: 'OPEN' | 'CLOSED';
  enrolledCount: number;
  remainingPlaces: number;
}

export interface SessionPayload {
  formationId: number;
  startDate: string;
  endDate: string;
  capacity: number;
  status: 'OPEN' | 'CLOSED';
}

@Injectable({ providedIn: 'root' })
export class SessionApi {
  constructor(private readonly http: HttpClient) {}

  list() {
    return this.http.get<TrainingSession[]>(`${environment.apiUrl}/sessions`);
  }

  listByFormation(formationId: number) {
    return this.http.get<TrainingSession[]>(`${environment.apiUrl}/formations/${formationId}/sessions`);
  }

  get(id: number) {
    return this.http.get<TrainingSession>(`${environment.apiUrl}/sessions/${id}`);
  }

  create(body: SessionPayload) {
    return this.http.post<TrainingSession>(`${environment.apiUrl}/sessions`, body);
  }

  update(id: number, body: SessionPayload) {
    return this.http.put<TrainingSession>(`${environment.apiUrl}/sessions/${id}`, body);
  }

  delete(id: number) {
    return this.http.delete(`${environment.apiUrl}/sessions/${id}`);
  }
}
