import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Enrollment, EnrollmentApi, EnrollmentStatus } from '../../core/api/enrollment.api';
import { SessionApi, TrainingSession } from '../../core/api/session.api';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';
import { fillPercent, formatFrDate, sessionFillKind } from '../../core/ui/session-fill';

@Component({
  selector: 'app-admin-session-enrollments-page',
  standalone: true,
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './admin-session-enrollments-page.component.html',
  styleUrl: './admin-session-enrollments-page.component.scss'
})
export class AdminSessionEnrollmentsPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly sessionsApi = inject(SessionApi);
  private readonly enrollmentsApi = inject(EnrollmentApi);
  private readonly toast = inject(ToastService);

  session: TrainingSession | null = null;
  enrollments: Enrollment[] = [];
  displayed: Enrollment[] = [];
  q = '';
  loading = true;
  readonly formatDate = formatFrDate;
  readonly fillKind = sessionFillKind;
  readonly percent = fillPercent;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    this.sessionsApi.get(id).subscribe({
      next: session => {
        this.session = session;
        this.enrollmentsApi.listBySession(id).subscribe({
          next: data => {
            this.enrollments = data;
            this.apply();
            this.loading = false;
          },
          error: err => {
            this.loading = false;
            this.toast.error(apiErrorMessage(err, 'Chargement des inscrits impossible'));
          }
        });
      },
      error: err => {
        this.loading = false;
        this.toast.error(apiErrorMessage(err, 'Session introuvable'));
      }
    });
  }

  apply(): void {
    const q = this.q.trim().toLowerCase();
    this.displayed = q
      ? this.enrollments.filter(e =>
          e.learnerName.toLowerCase().includes(q) || e.learnerEmail.toLowerCase().includes(q))
      : this.enrollments;
  }

  confirm(item: Enrollment): void {
    this.changeStatus(item, 'CONFIRMED');
  }

  cancel(item: Enrollment): void {
    if (!confirm(`Annuler l'inscription de ${item.learnerName} ? La place sera libérée.`)) {
      return;
    }
    this.changeStatus(item, 'CANCELLED');
  }

  private changeStatus(item: Enrollment, status: EnrollmentStatus): void {
    this.enrollmentsApi.updateStatus(item.id, status).subscribe({
      next: () => {
        this.toast.success(status === 'CANCELLED' ? 'Inscription annulée, place libérée' : 'Inscription confirmée');
        this.reload();
      },
      error: err => this.toast.error(apiErrorMessage(err, 'Mise à jour impossible'))
    });
  }
}
