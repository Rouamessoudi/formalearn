import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Category, CategoryApi } from '../../core/api/category.api';
import { Formation, FormationApi, FormationPayload } from '../../core/api/formation.api';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';

@Component({
  selector: 'app-admin-formations-page',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: './admin-formations-page.component.html',
  styleUrl: './admin-formations-page.component.scss'
})
export class AdminFormationsPageComponent implements OnInit {
  private readonly formationsApi = inject(FormationApi);
  private readonly categoriesApi = inject(CategoryApi);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);

  formations: Formation[] = [];
  displayed: Formation[] = [];
  categories: Category[] = [];
  loading = true;
  saving = false;
  modalOpen = false;
  editing: Formation | null = null;
  q = '';
  status: '' | 'DRAFT' | 'PUBLISHED' = '';
  sort: 'title' | 'price' | 'date' = 'date';
  page = 1;
  readonly pageSize = 8;

  readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: ['', Validators.required],
    price: [0, Validators.required],
    durationHours: [8, Validators.required],
    categoryId: [0, Validators.required],
    status: ['DRAFT' as 'DRAFT' | 'PUBLISHED', Validators.required]
  });

  ngOnInit(): void {
    this.categoriesApi.list().subscribe(data => this.categories = data);
    this.reload();
    this.route.queryParamMap.subscribe(params => {
      if (params.get('nouvelle') === '1') {
        this.openCreate();
      }
    });
  }

  reload(): void {
    this.loading = true;
    this.formationsApi.list().subscribe({
      next: data => {
        this.formations = data;
        this.apply();
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.toast.error(apiErrorMessage(err, 'Chargement impossible'));
      }
    });
  }

  apply(): void {
    let list = [...this.formations];
    const q = this.q.trim().toLowerCase();
    if (q) {
      list = list.filter(f => f.title.toLowerCase().includes(q) || f.description.toLowerCase().includes(q));
    }
    if (this.status) {
      list = list.filter(f => f.status === this.status);
    }
    list.sort((a, b) => {
      if (this.sort === 'title') {
        return a.title.localeCompare(b.title);
      }
      if (this.sort === 'price') {
        return a.price - b.price;
      }
      return (b.createdAt || '').localeCompare(a.createdAt || '');
    });
    this.displayed = list;
    this.page = 1;
  }

  paged(): Formation[] {
    const start = (this.page - 1) * this.pageSize;
    return this.displayed.slice(start, start + this.pageSize);
  }

  pageCount(): number {
    return Math.max(1, Math.ceil(this.displayed.length / this.pageSize));
  }

  openCreate(): void {
    this.editing = null;
    this.form.reset({
      title: '',
      description: '',
      price: 0,
      durationHours: 8,
      categoryId: this.categories[0]?.id || 0,
      status: 'DRAFT'
    });
    this.modalOpen = true;
  }

  openEdit(formation: Formation): void {
    this.editing = formation;
    this.form.reset({
      title: formation.title,
      description: formation.description,
      price: formation.price,
      durationHours: formation.durationHours,
      categoryId: formation.category.id,
      status: formation.status
    });
    this.modalOpen = true;
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const value = this.form.getRawValue();
    const body: FormationPayload = { ...value, categoryId: Number(value.categoryId) };
    const req = this.editing ? this.formationsApi.update(this.editing.id, body) : this.formationsApi.create(body);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.modalOpen = false;
        this.toast.success('Formation enregistrée avec succès');
        this.reload();
      },
      error: err => {
        this.saving = false;
        this.toast.error(apiErrorMessage(err, 'Enregistrement impossible'));
      }
    });
  }

  togglePublish(formation: Formation): void {
    const next = formation.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED';
    const label = next === 'PUBLISHED' ? 'publier' : 'repasser en brouillon';
    if (!confirm(`${label.charAt(0).toUpperCase() + label.slice(1)} « ${formation.title} » ?`)) {
      return;
    }
    const body: FormationPayload = {
      title: formation.title,
      description: formation.description,
      price: formation.price,
      durationHours: formation.durationHours,
      categoryId: formation.category.id,
      status: next
    };
    this.formationsApi.update(formation.id, body).subscribe({
      next: () => {
        this.toast.success(next === 'PUBLISHED' ? 'Formation publiée avec succès' : 'Formation repassée en brouillon');
        this.reload();
      },
      error: err => this.toast.error(apiErrorMessage(err, 'Mise à jour du statut impossible'))
    });
  }

  remove(formation: Formation): void {
    if (!confirm(`Supprimer définitivement « ${formation.title} » ?`)) {
      return;
    }
    this.formationsApi.delete(formation.id).subscribe({
      next: () => {
        this.toast.success('Formation supprimée avec succès');
        this.reload();
      },
      error: err => this.toast.error(apiErrorMessage(err, 'Suppression impossible'))
    });
  }
}
