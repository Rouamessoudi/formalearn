import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Formation, FormationApi } from '../../core/api/formation.api';
import { SessionApi, TrainingSession } from '../../core/api/session.api';
import { PriceDtPipe } from '../../core/pipes/price-dt.pipe';
import { formatFrDate, sessionFillKind, sessionFillLabel, fillPercent } from '../../core/ui/session-fill';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';

@Component({
  selector: 'app-admin-dashboard-page',
  standalone: true,
  imports: [RouterLink, PriceDtPipe],
  template: `
    <section class="fl-page">
      <div class="fl-hero">
        <h1>Tableau de bord</h1>
        <div class="fl-actions" style="margin-top:1rem">
          <a class="fl-btn fl-btn-primary" routerLink="/admin/formations">Gérer les formations</a>
          <a class="fl-btn fl-btn-ghost" routerLink="/admin/sessions">Gérer les sessions</a>
          <a class="fl-btn fl-btn-ghost" routerLink="/admin/formations" [queryParams]="{ nouvelle: 1 }">Nouvelle formation</a>
          <a class="fl-btn fl-btn-ghost" routerLink="/admin/sessions" [queryParams]="{ nouvelle: 1 }">Nouvelle session</a>
        </div>
      </div>

      @if (loading) {
        <div class="fl-grid">
          <article class="fl-card"><div class="skeleton"></div></article>
          <article class="fl-card"><div class="skeleton"></div></article>
          <article class="fl-card"><div class="skeleton"></div></article>
        </div>
      } @else {
        <div class="fl-grid">
          <article class="fl-card"><h3>Formations</h3><p class="fl-stat">{{ stats.formations }}</p></article>
          <article class="fl-card"><h3>Publiées</h3><p class="fl-stat">{{ stats.published }}</p><span class="fl-badge published">PUBLISHED</span></article>
          <article class="fl-card"><h3>Brouillons</h3><p class="fl-stat">{{ stats.drafts }}</p><span class="fl-badge draft">DRAFT</span></article>
          <article class="fl-card"><h3>Sessions ouvertes</h3><p class="fl-stat">{{ stats.openSessions }}</p><span class="fl-badge open">OPEN</span></article>
          <article class="fl-card"><h3>Inscriptions actives</h3><p class="fl-stat">{{ stats.enrollments }}</p></article>
          <article class="fl-card"><h3>Places disponibles</h3><p class="fl-stat">{{ stats.remaining }}</p></article>
        </div>

        <h2 class="section">Formations récemment créées</h2>
        @if (recentFormations.length === 0) {
          <article class="fl-card empty">Aucune formation.</article>
        } @else {
          <div class="fl-grid">
            @for (item of recentFormations; track item.id) {
              <article class="fl-card">
                <span class="fl-badge" [class.published]="item.status === 'PUBLISHED'" [class.draft]="item.status === 'DRAFT'">{{ item.status }}</span>
                <h3>{{ item.title }}</h3>
                <p>{{ item.category.name }} · {{ item.price | priceDt }} · {{ item.chapterCount ?? 0 }} chapitres</p>
                <a class="link" [routerLink]="['/admin/formations', item.id, 'chapitres']">Ouvrir</a>
              </article>
            }
          </div>
        }

        <h2 class="section">Sessions prochaines</h2>
        @if (upcoming.length === 0) {
          <article class="fl-card empty">Aucune session à venir.</article>
        } @else {
          <div class="fl-grid">
            @for (item of upcoming; track item.id) {
              <article class="fl-card">
                <span class="fl-badge" [class.open]="item.status === 'OPEN'" [class.closed]="item.status === 'CLOSED'">{{ item.status }}</span>
                <h3>{{ item.formationTitle }}</h3>
                <p>{{ formatDate(item.startDate) }} → {{ formatDate(item.endDate) }}</p>
                <p>{{ item.enrolledCount }} / {{ item.capacity }} inscrits · {{ item.remainingPlaces }} places restantes</p>
                <a class="link" [routerLink]="['/admin/sessions', item.id, 'inscriptions']">Participants</a>
              </article>
            }
          </div>
        }

        <h2 class="section">Sessions presque complètes</h2>
        @if (almostFull.length === 0) {
          <article class="fl-card empty">Aucune session proche de la capacité.</article>
        } @else {
          <div class="fl-grid">
            @for (item of almostFull; track item.id) {
              <article class="fl-card">
                <span class="fl-badge" [class]="fillKind(item)">{{ fillLabel(item) }}</span>
                <h3>{{ item.formationTitle }}</h3>
                <div class="fl-progress" [class]="fillKind(item)"><span [style.width.%]="percent(item)"></span></div>
                <p>{{ item.enrolledCount }} / {{ item.capacity }} places occupées</p>
              </article>
            }
          </div>
        }

        <h2 class="section">Activité récente</h2>
        <article class="fl-card">
          @if (activity.length === 0) {
            <p class="muted">Pas encore d’activité à afficher.</p>
          } @else {
            <ul class="activity">
              @for (line of activity; track line) {
                <li>{{ line }}</li>
              }
            </ul>
          }
        </article>
      }
    </section>
  `,
  styles: [`
    .section { margin: 1.6rem 0 .7rem; color: var(--fl-navy); font-size: 1.05rem; }
    .activity { margin: 0; padding-left: 1.1rem; color: var(--fl-muted); line-height: 1.7; }
  `]
})
export class AdminDashboardPageComponent implements OnInit {
  private readonly formationsApi = inject(FormationApi);
  private readonly sessionsApi = inject(SessionApi);
  private readonly toast = inject(ToastService);

  loading = true;
  stats = { formations: 0, published: 0, drafts: 0, openSessions: 0, enrollments: 0, remaining: 0 };
  recentFormations: Formation[] = [];
  upcoming: TrainingSession[] = [];
  almostFull: TrainingSession[] = [];
  activity: string[] = [];
  readonly formatDate = formatFrDate;
  readonly fillKind = sessionFillKind;
  readonly fillLabel = (s: TrainingSession) => sessionFillLabel(sessionFillKind(s));
  readonly percent = fillPercent;

  ngOnInit(): void {
    forkJoin({
      formations: this.formationsApi.list(),
      sessions: this.sessionsApi.list()
    }).subscribe({
      next: ({ formations, sessions }) => {
        const published = formations.filter(f => f.status === 'PUBLISHED').length;
        const open = sessions.filter(s => s.status === 'OPEN');
        this.stats = {
          formations: formations.length,
          published,
          drafts: formations.length - published,
          openSessions: open.length,
          enrollments: sessions.reduce((sum, s) => sum + s.enrolledCount, 0),
          remaining: sessions.reduce((sum, s) => sum + s.remainingPlaces, 0)
        };
        this.recentFormations = [...formations]
          .sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
          .slice(0, 6);
        const today = new Date().toISOString().slice(0, 10);
        this.upcoming = [...sessions]
          .filter(s => s.startDate >= today)
          .sort((a, b) => a.startDate.localeCompare(b.startDate))
          .slice(0, 6);
        this.almostFull = sessions
          .filter(s => sessionFillKind(s) === 'almost' || sessionFillKind(s) === 'full')
          .slice(0, 6);
        this.activity = [
          ...this.recentFormations.slice(0, 4).map(f => `Formation « ${f.title} » (${f.status})`),
          ...this.upcoming.slice(0, 3).map(s => `Session ${s.formationTitle} le ${formatFrDate(s.startDate)} · ${s.remainingPlaces} places`)
        ];
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.toast.error(apiErrorMessage(err, 'Impossible de charger le tableau de bord'));
      }
    });
  }
}
