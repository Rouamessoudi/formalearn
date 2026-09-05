import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { Category, CategoryApi } from '../../core/api/category.api';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';

@Component({
  selector: 'app-admin-categories-page',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './admin-categories-page.component.html',
  styleUrl: './admin-categories-page.component.scss'
})
export class AdminCategoriesPageComponent implements OnInit {
  private readonly api = inject(CategoryApi);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  categories: Category[] = [];
  displayed: Category[] = [];
  q = '';
  loading = true;
  saving = false;
  modalOpen = false;
  editing: Category | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: ['']
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.api.list().subscribe({
      next: data => {
        this.categories = data;
        this.apply();
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.toast.error(apiErrorMessage(err, 'Impossible de charger les catégories'));
      }
    });
  }

  apply(): void {
    const q = this.q.trim().toLowerCase();
    this.displayed = q
      ? this.categories.filter(c => c.name.toLowerCase().includes(q) || (c.description || '').toLowerCase().includes(q))
      : this.categories;
  }

  openCreate(): void {
    this.editing = null;
    this.form.reset({ name: '', description: '' });
    this.modalOpen = true;
  }

  openEdit(category: Category): void {
    this.editing = category;
    this.form.reset({ name: category.name, description: category.description || '' });
    this.modalOpen = true;
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const body = this.form.getRawValue();
    const req = this.editing ? this.api.update(this.editing.id, body) : this.api.create(body);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.modalOpen = false;
        this.toast.success(this.editing ? 'Catégorie mise à jour' : 'Catégorie créée');
        this.reload();
      },
      error: err => {
        this.saving = false;
        this.toast.error(apiErrorMessage(err, 'Enregistrement impossible'));
      }
    });
  }

  remove(category: Category): void {
    const used = category.formationCount ?? 0;
    const warning = used > 0
      ? `Cette catégorie est utilisée par ${used} formation(s). Le backend refusera la suppression si elle est encore liée.\n\nContinuer ?`
      : `Supprimer la catégorie « ${category.name} » ?`;
    if (!confirm(warning)) {
      return;
    }
    this.api.delete(category.id).subscribe({
      next: () => {
        this.toast.success('Catégorie supprimée');
        this.reload();
      },
      error: err => this.toast.error(apiErrorMessage(err, 'Suppression impossible'))
    });
  }
}
