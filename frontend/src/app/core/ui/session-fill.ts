import { TrainingSession } from '../api/session.api';

export type FillKind = 'ok' | 'almost' | 'full';

export function sessionFillKind(session: Pick<TrainingSession, 'capacity' | 'remainingPlaces'>): FillKind {
  if (session.remainingPlaces <= 0) {
    return 'full';
  }
  const ratio = session.capacity === 0 ? 1 : (session.capacity - session.remainingPlaces) / session.capacity;
  if (ratio >= 0.75 || session.remainingPlaces <= 2) {
    return 'almost';
  }
  return 'ok';
}

export function sessionFillLabel(kind: FillKind): string {
  if (kind === 'full') {
    return 'COMPLET';
  }
  if (kind === 'almost') {
    return 'PRESQUE COMPLET';
  }
  return 'PLACES DISPO';
}

export function occupied(session: Pick<TrainingSession, 'capacity' | 'enrolledCount'>): number {
  return session.enrolledCount;
}

export function fillPercent(session: Pick<TrainingSession, 'capacity' | 'enrolledCount'>): number {
  if (!session.capacity) {
    return 0;
  }
  return Math.min(100, Math.round((session.enrolledCount / session.capacity) * 100));
}

export function formatFrDate(iso: string): string {
  if (!iso) {
    return '—';
  }
  const date = new Date(iso.length <= 10 ? `${iso}T00:00:00` : iso);
  return new Intl.DateTimeFormat('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' }).format(date);
}
