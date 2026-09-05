import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Chapter, ChapterApi } from '../../core/api/chapter.api';
import { Formation, FormationApi } from '../../core/api/formation.api';
import { ToastService } from '../../core/ui/toast.service';

@Component({
  selector: 'app-admin-chapters-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-chapters-page.component.html',
  styleUrl: './admin-chapters-page.component.scss'
})
export class AdminChaptersPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly chaptersApi = inject(ChapterApi);
  private readonly formationsApi = inject(FormationApi);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  formationId = Number(this.route.snapshot.paramMap.get('id'));
  formation: Formation | null = null;
  chapters: Chapter[] = [];
  loading = true;
  saving = false;
  modalOpen = false;
  editing: Chapter | null = null;

  readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    content: [''],
    position: [1, Validators.required]
  });

  ngOnInit(): void {
    this.formationsApi.get(this.formationId).subscribe(f => this.formation = f);
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.chaptersApi.list(this.formationId).subscribe({
      next: data => {
        this.chapters = data;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.toast.error(err?.error?.message || 'Chargement impossible');
      }
    });
  }

  openCreate(): void {
    this.editing = null;
    this.form.reset({ title: '', content: '', position: (this.chapters.at(-1)?.position || 0) + 1 });
    this.modalOpen = true;
  }

  openEdit(chapter: Chapter): void {
    this.editing = chapter;
    this.form.reset({ title: chapter.title, content: chapter.content || '', position: chapter.position });
    this.modalOpen = true;
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      return;
    }
    this.saving = true;
    const body = this.form.getRawValue();
    const req = this.editing
      ? this.chaptersApi.update(this.formationId, this.editing.id, body)
      : this.chaptersApi.create(this.formationId, body);
    req.subscribe({
      next: () => {
        this.saving = false;
        this.modalOpen = false;
        this.toast.success('Chapitre enregistré');
        this.reload();
      },
      error: err => {
        this.saving = false;
        this.toast.error(err?.error?.message || 'Enregistrement impossible');
      }
    });
  }

  remove(chapter: Chapter): void {
    if (!confirm(`Supprimer le chapitre « ${chapter.title} » ?`)) {
      return;
    }
    this.chaptersApi.delete(this.formationId, chapter.id).subscribe({
      next: () => {
        this.toast.success('Chapitre supprimé');
        this.reload();
      },
      error: err => this.toast.error(err?.error?.message || 'Suppression impossible')
    });
  }

  move(chapter: Chapter, direction: 'up' | 'down'): void {
    this.chaptersApi.move(this.formationId, chapter.id, direction).subscribe({
      next: data => {
        this.chapters = data;
        this.toast.success('Ordre mis à jour');
      },
      error: err => this.toast.error(err?.error?.message || 'Déplacement impossible')
    });
  }
}
