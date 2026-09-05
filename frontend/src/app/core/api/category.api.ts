import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Category {
  id: number;
  name: string;
  description?: string;
  formationCount?: number;
}

@Injectable({ providedIn: 'root' })
export class CategoryApi {
  constructor(private readonly http: HttpClient) {}

  list() {
    return this.http.get<Category[]>(`${environment.apiUrl}/categories`);
  }

  create(body: Partial<Category>) {
    return this.http.post<Category>(`${environment.apiUrl}/categories`, body);
  }

  update(id: number, body: Partial<Category>) {
    return this.http.put<Category>(`${environment.apiUrl}/categories/${id}`, body);
  }

  delete(id: number) {
    return this.http.delete(`${environment.apiUrl}/categories/${id}`);
  }
}
