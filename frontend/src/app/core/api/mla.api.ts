import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Formation } from './formation.api';

export type Interest = 'BACKEND' | 'DATA' | 'MANAGEMENT' | 'LANGUAGES' | 'OTHER';
export type EducationLevel = 'LICENCE' | 'INGENIEUR' | 'MASTER';
export type Skill = 'JAVA' | 'SPRING' | 'SQL' | 'PYTHON' | 'MANAGEMENT';

export interface LearnerProfile {
  interest: Interest;
  experienceYears: number;
  educationLevel: EducationLevel;
  skills: Skill[];
}

export interface RecommendedFormation extends Formation {
  score: number;
}

@Injectable({ providedIn: 'root' })
export class MlaApi {
  constructor(private readonly http: HttpClient) {}

  profile() {
    return this.http.get<LearnerProfile>(`${environment.apiUrl}/profil`);
  }

  saveProfile(body: LearnerProfile) {
    return this.http.put<LearnerProfile>(`${environment.apiUrl}/profil`, body);
  }

  recommendations() {
    return this.http.get<RecommendedFormation[]>(`${environment.apiUrl}/mla/recommandations`);
  }
}
