import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthUser, LoginResponse } from './auth.models';

const TOKEN_KEY = 'formalearn.token';
const USER_KEY = 'formalearn.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<AuthUser | null>(this.readUser());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isLoggedIn = computed(() => !!this.currentUserSignal() && !!this.token);
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');
  readonly isLearner = computed(() => this.currentUserSignal()?.role === 'APPRENANT');

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  login(email: string, password: string) {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap(response => this.persist(response))
    );
  }

  me() {
    return this.http.get<AuthUser>(`${environment.apiUrl}/auth/me`).pipe(
      tap(user => {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this.currentUserSignal.set(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
    this.router.navigateByUrl('/login');
  }

  homeByRole(): string {
    return this.isAdmin() ? '/admin/dashboard' : '/app/catalogue';
  }

  private persist(response: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    this.currentUserSignal.set(response.user);
  }

  private readUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      return null;
    }
  }
}
