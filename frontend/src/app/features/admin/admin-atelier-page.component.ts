import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/auth/auth.service';
import { FormationApi } from '../../core/api/formation.api';
import { SessionApi, TrainingSession } from '../../core/api/session.api';
import { fillPercent, formatFrDate, sessionFillKind, sessionFillLabel } from '../../core/ui/session-fill';

@Component({
  selector: 'app-admin-atelier-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="fl-page">
      <div class="fl-hero">
        <h1>Atelier avancé — démonstration interactive</h1>
        <p>Chaque action appelle l’API réelle. Utilisez cet écran pendant la soutenance.</p>
      </div>

      <article class="fl-card">
        <h2>1. Authentification JWT</h2>
        <p>Token HS256 décodé (email, rôle, expiration). Profil via GET /api/auth/me : {{ meJson }}</p>
        <pre>{{ jwtJson }}</pre>
        <p class="muted">Rôle actuel : ADMIN. Un apprenant n’a pas ce menu.</p>
      </article>

      <article class="fl-card">
        <h2>2. ADMIN vs APPRENANT</h2>
        <p>Cliquez pour appeler GET /api/categories <strong>sans</strong> JWT : le backend doit répondre 401.</p>
        <button class="fl-btn fl-btn-primary" type="button" (click)="probeUnauthorized()">Tester 401 sans token</button>
        <p class="result" [class.ok]="anonStatus === 401">{{ anonStatus ? ('HTTP ' + anonStatus + ' — ' + anonBody) : 'Pas encore testé' }}</p>
        <p class="why">Si un apprenant colle /admin dans l’URL, le guard le renvoie au catalogue avec « Accès refusé ».</p>
      </article>

      <article class="fl-card">
        <h2>3. DRAFT / PUBLISHED</h2>
        <p>Comptage live : <span class="fl-badge draft">{{ draftCount }} DRAFT</span>
          <span class="fl-badge published">{{ publishedCount }} PUBLISHED</span></p>
        <p>L’apprenant ne voit que les PUBLISHED dans le catalogue.</p>
        <a class="fl-btn fl-btn-primary" routerLink="/admin/formations">Gérer les formations</a>
      </article>

      <article class="fl-card">
        <h2>4. Chapitres — ordre réel</h2>
        <p>Monter / Descendre : PATCH position (permutation en base).</p>
        <a class="fl-btn fl-btn-primary" [routerLink]="chapterLink">Ouvrir les chapitres</a>
      </article>

      <article class="fl-card">
        <h2>5. Capacité, inscrits, places restantes</h2>
        @if (demoSession) {
          <p><strong>{{ demoSession.formationTitle }}</strong></p>
          <p>Capacité {{ demoSession.capacity }} · Inscrits {{ demoSession.enrolledCount }} · Restantes {{ demoSession.remainingPlaces }}</p>
          <span class="fl-badge" [class]="fillKind(demoSession)">{{ fillLabel(demoSession) }}</span>
          <div class="fl-progress" [class]="fillKind(demoSession)"><span [style.width.%]="percent(demoSession)"></span></div>
          <p class="muted">{{ formatDate(demoSession.startDate) }} → {{ formatDate(demoSession.endDate) }}</p>
        } @else {
          <p class="muted">Aucune session.</p>
        }
        <button class="fl-btn" type="button" (click)="reloadSessions()">Actualiser les places</button>
        <a class="fl-btn fl-btn-primary" routerLink="/admin/sessions">Gérer les sessions</a>
      </article>

      <article class="fl-card">
        <h2>6. Inscriptions : doublon, complet, fermé</h2>
        <ul>
          <li>⚠️ Déjà inscrit → HTTP 409 « Vous êtes déjà inscrit à cette session »</li>
          <li>🔴 Session complète → HTTP 409</li>
          <li>🔒 Session CLOSED → HTTP 409</li>
          <li>Annulation apprenant → POST /api/inscriptions/&#123;id&#125;/annuler (place libérée)</li>
        </ul>
        <p>À montrer connecté en APPRENANT : fiche formation → S'inscrire deux fois.</p>
      </article>

      <article class="fl-card">
        <h2>7. Recommandations MLA</h2>
        <p>GET /api/mla/recommandations (rôle APPRENANT) → FastAPI Random Forest. Scores jamais hardcodés.</p>
        <p class="muted">Déconnectez-vous, connectez-vous en apprenant, ouvrez « Mes recommandations ».</p>
      </article>
    </section>
  `,
  styles: [`
    .fl-card { margin-bottom: 1rem; }
    pre { background: #0f172a; color: #e2e8f0; padding: 1rem; border-radius: 12px; overflow: auto; font-size: .85rem; }
    .result { margin-top: .6rem; color: var(--fl-muted); }
    .result.ok { color: #166534; font-weight: 700; }
    .fl-btn { margin: .6rem .4rem 0 0; }
    ul { color: var(--fl-muted); }
  `]
})
export class AdminAtelierPageComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly formationsApi = inject(FormationApi);
  private readonly sessionsApi = inject(SessionApi);

  jwtJson = '';
  meJson = '';
  anonStatus: number | null = null;
  anonBody = '';
  draftCount = 0;
  publishedCount = 0;
  demoSession: TrainingSession | null = null;
  chapterLink: (string | number)[] = ['/admin/formations'];
  readonly formatDate = formatFrDate;
  readonly fillKind = sessionFillKind;
  readonly fillLabel = (s: TrainingSession) => sessionFillLabel(sessionFillKind(s));
  readonly percent = fillPercent;

  ngOnInit(): void {
    this.jwtJson = JSON.stringify(this.decodeJwt(), null, 2);
    this.auth.me().subscribe({
      next: user => this.meJson = `${user.email} · ${user.role}`,
      error: () => this.meJson = 'auth/me indisponible'
    });
    this.formationsApi.list().subscribe(data => {
      this.draftCount = data.filter(f => f.status === 'DRAFT').length;
      this.publishedCount = data.filter(f => f.status === 'PUBLISHED').length;
      const spring = data.find(f => f.title.toLowerCase().includes('spring'));
      if (spring) {
        this.chapterLink = ['/admin/formations', spring.id, 'chapitres'];
      } else if (data[0]) {
        this.chapterLink = ['/admin/formations', data[0].id, 'chapitres'];
      }
    });
    this.reloadSessions();
  }

  reloadSessions(): void {
    this.sessionsApi.list().subscribe(data => {
      this.demoSession = data.find(s => s.remainingPlaces <= 3 && s.capacity > 0)
        || data.find(s => s.status === 'OPEN')
        || data[0]
        || null;
    });
  }

  decodeJwt(): unknown {
    const token = this.auth.token;
    if (!token) {
      return { error: 'Pas de token' };
    }
    try {
      const payload = token.split('.')[1];
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(json);
    } catch {
      return { error: 'JWT illisible' };
    }
  }

  async probeUnauthorized(): Promise<void> {
    const url = `${environment.apiUrl}/categories`;
    const response = await fetch(url, { headers: { Accept: 'application/json' } });
    this.anonStatus = response.status;
    this.anonBody = (await response.text()).slice(0, 180);
  }
}
