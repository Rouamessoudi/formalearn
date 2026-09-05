import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EducationLevel, Interest, MlaApi, Skill } from '../../core/api/mla.api';
import { ToastService } from '../../core/ui/toast.service';

@Component({
  selector: 'app-learner-profile-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './learner-profile-page.component.html',
  styleUrl: './learner-profile-page.component.scss'
})
export class LearnerProfilePageComponent implements OnInit {
  private readonly mlaApi = inject(MlaApi);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  readonly skills: Skill[] = ['JAVA', 'SPRING', 'SQL', 'PYTHON', 'MANAGEMENT'];
  loading = true;
  saving = false;

  readonly form = this.fb.nonNullable.group({
    interest: ['BACKEND' as Interest, Validators.required],
    experienceYears: [1, Validators.required],
    educationLevel: ['INGENIEUR' as EducationLevel, Validators.required],
    JAVA: [false],
    SPRING: [false],
    SQL: [false],
    PYTHON: [false],
    MANAGEMENT: [false]
  });

  ngOnInit(): void {
    this.mlaApi.profile().subscribe({
      next: profile => {
        this.form.patchValue({
          interest: profile.interest,
          experienceYears: profile.experienceYears,
          educationLevel: profile.educationLevel,
          JAVA: profile.skills.includes('JAVA'),
          SPRING: profile.skills.includes('SPRING'),
          SQL: profile.skills.includes('SQL'),
          PYTHON: profile.skills.includes('PYTHON'),
          MANAGEMENT: profile.skills.includes('MANAGEMENT')
        });
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.toast.error(err?.error?.message || 'Profil introuvable');
      }
    });
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const value = this.form.getRawValue();
    const selected = this.skills.filter(skill => value[skill]);
    this.mlaApi.saveProfile({
      interest: value.interest,
      experienceYears: Number(value.experienceYears),
      educationLevel: value.educationLevel,
      skills: selected
    }).subscribe({
      next: () => {
        this.saving = false;
        this.toast.success('Profil mis à jour avec succès');
      },
      error: err => {
        this.saving = false;
        this.toast.error(err?.error?.message || 'Enregistrement impossible');
      }
    });
  }
}
