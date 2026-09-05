import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Formation, FormationApi } from '../../core/api/formation.api';
import { SessionApi, SessionPayload, TrainingSession } from '../../core/api/session.api';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';
import { fillPercent, formatFrDate, sessionFillKind, sessionFillLabel } from '../../core/ui/session-fill';

@Component({
  selector: 'app-admin-sessions-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-sessions-page.component.html',
  styleUrl: './admin-sessions-page.component.scss'
})
export class AdminSessionsPageComponent implements OnInit {
  private readonly sessionsApi = inject(SessionApi);
  private readonly formationsApi = inject(FormationApi);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  private readonly route = inject(ActivatedRoute);

  sessions: TrainingSession[] = [];
  formations: Formation[] = [];
  loading = true;
  saving = false;
  modalOpen = false;
  editing: TrainingSession | null = null;

  readonly form = this.fb.nonNullable.group({
    formationId: [0, Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    capacity: [12, Validators.required],
    status: ['OPEN' as 'OPEN' | 'CLOSED', Validators.required]
  });

  readonly formatDate = formatFrDate;
  readonly fillKind = sessionFillKind;
  readonly fillLabel = (s: TrainingSession) => sessionFillLabel(sessionFillKind(s));
  readonly percent = fillPercent;

  ngOnInit(): void {
    this.formationsApi.list().subscribe(data => this.formations = data);
    this.reload();
    if (this.route.snapshot.queryParamMap.get('nouvelle') === '1') {
      this.openCreate();
    }
  }

  reload(): void {
    this.loading = true;
    this.sessionsApi.list().subscribe({
      next: data => {
        this.sessions = data;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.toast.error(apiErrorMessage(err, 'Chargement impossible'));
      }
    });
  }

  openCreate(): void {
    this.editing = null;
    const start = this.isoOffset(7);
    this.form.reset({
      formationId: this.formations.find(f => f.status === 'PUBLISHED')?.id || this.formations[0]?.id || 0,
      startDate: start,
      endDate: this.isoOffset(14),
      capacity: 12,
      status: 'OPEN'
    });
    this.modalOpen = true;
  }

  openEdit(session: TrainingSession): void {
    this.editing = session;
    this.form.reset({
      formationId: session.formationId,
      startDate: session.startDate,
      endDate: session.endDate,
      capacity: session.capacity,
      status: session.status
    });
    this.modalOpen = true;
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    if (value.endDate < value.startDate) {
      this.toast.error('La date de fin doit être après la date de début');
      return;
    }
    this.saving = true;
    const body: SessionPayload = {
      formationId: Number(value.formationId),
      startDate: value.startDate,
      endDate: value.endDate,
      capacity: Number(value.capacity),
      status: value.status
    };
    const req = this.editing ? this.sessionsApi.update(this.editing.id, body) : this.sessionsApi.create(body);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.modalOpen = false;
        this.toast.success('Session enregistrée');
        this.reload();
      },
      error: err => {
        this.saving = false;
        this.toast.error(apiErrorMessage(err, 'Enregistrement impossible'));
      }
    });
  }

  remove(session: TrainingSession): void {
    if (!confirm(`Supprimer la session « ${session.formationTitle} » du ${session.startDate} ?`)) {
      return;
    }
    this.sessionsApi.delete(session.id).subscribe({
      next: () => {
        this.toast.success('Session supprimée');
        this.reload();
      },
      error: err => this.toast.error(apiErrorMessage(err, 'Suppression impossible'))
    });
  }

  private isoOffset(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() + days);
    return date.toISOString().slice(0, 10);
  }
}
