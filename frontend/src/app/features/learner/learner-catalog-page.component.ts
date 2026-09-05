import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subject, catchError, debounceTime, distinctUntilChanged, of, switchMap, tap } from 'rxjs';
import { Category, CategoryApi } from '../../core/api/category.api';
import { Formation, FormationApi, FormationQuery } from '../../core/api/formation.api';
import { PriceDtPipe } from '../../core/pipes/price-dt.pipe';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';

type SortKey = 'date' | 'price' | 'duration';

@Component({
  selector: 'app-learner-catalog-page',
  standalone: true,
  imports: [FormsModule, RouterLink, PriceDtPipe],
  templateUrl: './learner-catalog-page.component.html',
  styleUrl: './learner-catalog-page.component.scss'
})
export class LearnerCatalogPageComponent implements OnInit {
  private readonly formationsApi = inject(FormationApi);
  private readonly categoriesApi = inject(CategoryApi);
  private readonly toast = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);
  private readonly filters = new Subject<FormationQuery>();

  formations: Formation[] = [];
  displayed: Formation[] = [];
  categories: Category[] = [];
  loading = true;
  error = '';
  query: FormationQuery = { q: '', categoryId: '', minPrice: '', maxPrice: '' };
  minDuration: number | '' = '';
  maxDuration: number | '' = '';
  sort: SortKey = 'date';

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.get('acces') === 'refuse') {
      this.toast.error('Accès refusé : l’espace administrateur est réservé au rôle ADMIN.');
    }
    this.categoriesApi.list().subscribe({
      next: data => this.categories = data,
      error: err => this.toast.error(apiErrorMessage(err, 'Catégories indisponibles'))
    });
    this.filters.pipe(
      debounceTime(300),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      tap(() => {
        this.loading = true;
        this.error = '';
      }),
      switchMap(query => this.formationsApi.list(query).pipe(
        catchError(err => {
          this.error = apiErrorMessage(err, 'Catalogue indisponible');
          this.toast.error(this.error);
          return of([] as Formation[]);
        })
      )),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(data => {
      this.formations = data;
      this.applyLocal();
      this.loading = false;
    });
    this.search();
  }

  onFilterChange(): void {
    this.filters.next({ ...this.query });
  }

  search(): void {
    this.onFilterChange();
  }

  applyLocal(): void {
    let list = [...this.formations];
    if (this.minDuration !== '' && this.minDuration != null) {
      list = list.filter(f => f.durationHours >= Number(this.minDuration));
    }
    if (this.maxDuration !== '' && this.maxDuration != null) {
      list = list.filter(f => f.durationHours <= Number(this.maxDuration));
    }
    list.sort((a, b) => {
      if (this.sort === 'price') {
        return a.price - b.price;
      }
      if (this.sort === 'duration') {
        return a.durationHours - b.durationHours;
      }
      return (b.createdAt || '').localeCompare(a.createdAt || '');
    });
    this.displayed = list;
  }

  reset(): void {
    this.query = { q: '', categoryId: '', minPrice: '', maxPrice: '' };
    this.minDuration = '';
    this.maxDuration = '';
    this.sort = 'date';
    this.search();
  }

  snippet(text: string): string {
    if (!text) {
      return '';
    }
    return text.length > 140 ? text.slice(0, 140) + '…' : text;
  }
}
