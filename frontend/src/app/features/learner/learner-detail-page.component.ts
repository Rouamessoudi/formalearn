import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Chapter, ChapterApi } from '../../core/api/chapter.api';
import { Enrollment, EnrollmentApi } from '../../core/api/enrollment.api';
import { Formation, FormationApi } from '../../core/api/formation.api';
import { SessionApi, TrainingSession } from '../../core/api/session.api';
import { PriceDtPipe } from '../../core/pipes/price-dt.pipe';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';
import { fillPercent, formatFrDate, sessionFillKind, sessionFillLabel } from '../../core/ui/session-fill';

@Component({
  selector: 'app-learner-detail-page',
  standalone: true,
  imports: [RouterLink, PriceDtPipe],
  templateUrl: './learner-detail-page.component.html',
  styleUrl: './learner-detail-page.component.scss'
})
export class LearnerDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly formationsApi = inject(FormationApi);
  private readonly chaptersApi = inject(ChapterApi);
  private readonly sessionsApi = inject(SessionApi);
  private readonly enrollmentsApi = inject(EnrollmentApi);
  private readonly toast = inject(ToastService);

  formation: Formation | null = null;
  chapters: Chapter[] = [];
  sessions: TrainingSession[] = [];
  mine: Enrollment[] = [];
  loading = true;
  enrollingId: number | null = null;
  readonly today = new Date().toISOString().slice(0, 10);
  readonly formatDate = formatFrDate;
  readonly fillKind = sessionFillKind;
  readonly fillLabel = (s: TrainingSession) => sessionFillLabel(sessionFillKind(s));
  readonly percent = fillPercent;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.formation = this.route.snapshot.data['formation'] ?? null;
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    if (this.formation) {
      this.loadRelated(id);
      return;
    }
    this.formationsApi.get(id).subscribe({
      next: formation => {
        this.formation = formation;
        this.loadRelated(id);
      },
      error: err => {
        this.loading = false;
        this.toast.error(apiErrorMessage(err, 'Formation introuvable'));
      }
    });
  }

  private loadRelated(id: number): void {
    forkJoin({
      chapters: this.chaptersApi.list(id),
      sessions: this.sessionsApi.listByFormation(id),
      mine: this.enrollmentsApi.mine()
    }).subscribe({
      next: data => {
        this.chapters = [...data.chapters].sort((a, b) => a.position - b.position);
        this.sessions = data.sessions;
        this.mine = data.mine;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.toast.error(apiErrorMessage(err, 'Chargement impossible'));
      }
    });
  }

  alreadyEnrolled(session: TrainingSession): boolean {
    return this.mine.some(e => e.sessionId === session.id && e.status !== 'CANCELLED');
  }

  isPast(session: TrainingSession): boolean {
    return session.startDate < this.today;
  }

  enrollLabel(session: TrainingSession): string {
    if (this.alreadyEnrolled(session)) {
      return 'Déjà inscrit';
    }
    if (this.isPast(session)) {
      return 'Session terminée';
    }
    if (session.status === 'CLOSED') {
      return 'Session fermée';
    }
    if (session.remainingPlaces <= 0) {
      return 'Session complète';
    }
    return "S'inscrire";
  }

  enrollReason(session: TrainingSession): string {
    if (this.alreadyEnrolled(session)) {
      return '⚠️ Vous êtes déjà inscrit à cette session';
    }
    if (this.isPast(session)) {
      return '⏰ Cette session est terminée';
    }
    if (session.status === 'CLOSED') {
      return '🔒 Les inscriptions sont fermées';
    }
    if (session.remainingPlaces <= 0) {
      return '🔴 Cette session est complète';
    }
    return '';
  }

  canEnroll(session: TrainingSession): boolean {
    return this.enrollLabel(session) === "S'inscrire";
  }

  enroll(session: TrainingSession): void {
    if (!this.canEnroll(session) || this.enrollingId) {
      return;
    }
    this.enrollingId = session.id;
    this.enrollmentsApi.enroll(session.id).subscribe({
      next: () => {
        this.enrollingId = null;
        this.toast.success('✓ Inscription confirmée');
        this.load();
      },
      error: err => {
        this.enrollingId = null;
        this.toast.error(apiErrorMessage(err, 'Inscription impossible'));
      }
    });
  }
}
