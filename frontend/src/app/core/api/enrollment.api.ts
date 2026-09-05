import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export type EnrollmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED';

export interface Enrollment {
  id: number;
  status: EnrollmentStatus;
  createdAt: string;
  sessionId: number;
  startDate: string;
  endDate: string;
  sessionStatus: 'OPEN' | 'CLOSED';
  formationId: number;
  formationTitle: string;
  categoryName: string;
  formationPrice: number;
  learnerId: number;
  learnerName: string;
  learnerEmail: string;
  capacity?: number;
  enrolledCount?: number;
  remainingPlaces?: number;
}

@Injectable({ providedIn: 'root' })
export class EnrollmentApi {
  constructor(private readonly http: HttpClient) {}

  enroll(sessionId: number) {
    return this.http.post<Enrollment>(`${environment.apiUrl}/inscriptions`, { sessionId });
  }

  mine() {
    return this.http.get<Enrollment[]>(`${environment.apiUrl}/inscriptions/moi`);
  }

  listBySession(sessionId: number) {
    return this.http.get<Enrollment[]>(`${environment.apiUrl}/sessions/${sessionId}/inscriptions`);
  }

  updateStatus(id: number, status: EnrollmentStatus) {
    return this.http.patch<Enrollment>(`${environment.apiUrl}/inscriptions/${id}/status`, { status });
  }

  cancelMine(id: number) {
    return this.http.post<Enrollment>(`${environment.apiUrl}/inscriptions/${id}/annuler`, {});
  }
}
