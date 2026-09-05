import { Component, inject, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Enrollment, EnrollmentApi, EnrollmentStatus } from '../../core/api/enrollment.api';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';
import { formatFrDate } from '../../core/ui/session-fill';

@Component({
  selector: 'app-learner-enrollments-page',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './learner-enrollments-page.component.html',
  styleUrl: './learner-enrollments-page.component.scss'
})
export class LearnerEnrollmentsPageComponent implements OnInit {
  private readonly enrollmentsApi = inject(EnrollmentApi);
  private readonly toast = inject(ToastService);

  enrollments: Enrollment[] = [];
  displayed: Enrollment[] = [];
  loading = true;
  filter: 'ALL' | EnrollmentStatus = 'ALL';
  cancellingId: number | null = null;
  readonly formatDate = formatFrDate;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.enrollmentsApi.mine().subscribe({
      next: data => {
        this.enrollments = data;
        this.applyFilter();
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.toast.error(apiErrorMessage(err, 'Chargement impossible'));
      }
    });
  }

  applyFilter(): void {
    this.displayed = this.filter === 'ALL'
      ? this.enrollments
      : this.enrollments.filter(e => e.status === this.filter);
  }

  setFilter(value: 'ALL' | EnrollmentStatus): void {
    this.filter = value;
    this.applyFilter();
  }

  canCancel(item: Enrollment): boolean {
    return item.status === 'PENDING' || item.status === 'CONFIRMED';
  }

  cancel(item: Enrollment): void {
    if (!this.canCancel(item) || this.cancellingId) {
      return;
    }
    if (!confirm(`Annuler votre inscription à « ${item.formationTitle} » ? La place sera libérée.`)) {
      return;
    }
    this.cancellingId = item.id;
    this.enrollmentsApi.cancelMine(item.id).subscribe({
      next: () => {
        this.cancellingId = null;
        this.toast.success('Inscription annulée — place libérée');
        this.reload();
      },
      error: err => {
        this.cancellingId = null;
        this.toast.error(apiErrorMessage(err, 'Annulation impossible'));
      }
    });
  }
}
