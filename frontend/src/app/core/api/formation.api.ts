import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Category } from './category.api';

export interface Formation {
  id: number;
  title: string;
  description: string;
  price: number;
  durationHours: number;
  category: Category;
  status: 'DRAFT' | 'PUBLISHED';
  createdAt: string;
  chapterCount?: number;
}

export interface FormationPayload {
  title: string;
  description: string;
  price: number;
  durationHours: number;
  categoryId: number;
  status: 'DRAFT' | 'PUBLISHED';
}

export interface FormationQuery {
  q?: string;
  categoryId?: number | '';
  minPrice?: number | '';
  maxPrice?: number | '';
}

@Injectable({ providedIn: 'root' })
export class FormationApi {
  constructor(private readonly http: HttpClient) {}

  list(query: FormationQuery = {}) {
    let params = new HttpParams();
    if (query.q) params = params.set('q', query.q);
    if (query.categoryId) params = params.set('categoryId', String(query.categoryId));
    if (query.minPrice !== '' && query.minPrice != null) params = params.set('minPrice', String(query.minPrice));
    if (query.maxPrice !== '' && query.maxPrice != null) params = params.set('maxPrice', String(query.maxPrice));
    return this.http.get<Formation[]>(`${environment.apiUrl}/formations`, { params });
  }

  get(id: number) {
    return this.http.get<Formation>(`${environment.apiUrl}/formations/${id}`);
  }

  create(body: FormationPayload) {
    return this.http.post<Formation>(`${environment.apiUrl}/formations`, body);
  }

  update(id: number, body: FormationPayload) {
    return this.http.put<Formation>(`${environment.apiUrl}/formations/${id}`, body);
  }

  delete(id: number) {
    return this.http.delete(`${environment.apiUrl}/formations/${id}`);
  }
}
