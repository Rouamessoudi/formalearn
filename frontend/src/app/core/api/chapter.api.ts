import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Chapter {
  id: number;
  title: string;
  content?: string;
  position: number;
}

@Injectable({ providedIn: 'root' })
export class ChapterApi {
  constructor(private readonly http: HttpClient) {}

  list(formationId: number) {
    return this.http.get<Chapter[]>(`${environment.apiUrl}/formations/${formationId}/chapitres`);
  }

  create(formationId: number, body: Partial<Chapter>) {
    return this.http.post<Chapter>(`${environment.apiUrl}/formations/${formationId}/chapitres`, body);
  }

  update(formationId: number, id: number, body: Partial<Chapter>) {
    return this.http.put<Chapter>(`${environment.apiUrl}/formations/${formationId}/chapitres/${id}`, body);
  }

  delete(formationId: number, id: number) {
    return this.http.delete(`${environment.apiUrl}/formations/${formationId}/chapitres/${id}`);
  }

  move(formationId: number, id: number, direction: 'up' | 'down') {
    return this.http.patch<Chapter[]>(
      `${environment.apiUrl}/formations/${formationId}/chapitres/${id}/position`,
      {},
      { params: { direction } }
    );
  }
}
