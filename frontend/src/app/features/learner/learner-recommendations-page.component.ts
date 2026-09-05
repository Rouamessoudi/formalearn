import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { LearnerProfile, MlaApi, RecommendedFormation } from '../../core/api/mla.api';
import { ToastService } from '../../core/ui/toast.service';
import { apiErrorMessage } from '../../core/http/api-error';
import { PriceDtPipe } from '../../core/pipes/price-dt.pipe';

@Component({
  selector: 'app-learner-recommendations-page',
  standalone: true,
  imports: [RouterLink, PriceDtPipe],
  templateUrl: './learner-recommendations-page.component.html',
  styleUrl: './learner-recommendations-page.component.scss'
})
export class LearnerRecommendationsPageComponent implements OnInit {
  private readonly mlaApi = inject(MlaApi);
  private readonly toast = inject(ToastService);

  items: RecommendedFormation[] = [];
  profile: LearnerProfile | null = null;
  loading = true;
  error = '';

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    this.error = '';
    forkJoin({
      recs: this.mlaApi.recommendations(),
      profile: this.mlaApi.profile()
    }).subscribe({
      next: data => {
        this.items = data.recs;
        this.profile = data.profile;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.error = apiErrorMessage(err, 'Le moteur de recommandation est indisponible');
        this.toast.error(this.error);
      }
    });
  }

  percent(score: number): number {
    return Math.round(score * 100);
  }

  why(item: RecommendedFormation): string {
    const bits: string[] = [];
    const hay = `${item.title} ${item.category?.name || ''}`.toLowerCase();
    if (this.profile?.interest === 'BACKEND' && /java|spring|web|api|devops|cloud|cyber/.test(hay)) {
      bits.push('correspond à votre centre d’intérêt BACKEND');
    }
    if (this.profile?.interest === 'DATA' && /data|python|sql|intelligence|donn/.test(hay)) {
      bits.push('correspond à votre centre d’intérêt DATA');
    }
    if (this.profile?.interest === 'MANAGEMENT' && /management|projet|agile|business/.test(hay)) {
      bits.push('correspond à votre centre d’intérêt MANAGEMENT');
    }
    if (this.profile?.interest === 'LANGUAGES' && /langue|anglais|communication/.test(hay)) {
      bits.push('correspond à votre centre d’intérêt LANGUAGES');
    }
    for (const skill of this.profile?.skills || []) {
      if (hay.includes(skill.toLowerCase())) {
        bits.push(`compétence ${skill} présente dans le titre ou la catégorie`);
      }
    }
    if (bits.length === 0) {
      return 'Score renvoyé par le modèle Random Forest à partir de votre profil (intérêt, expérience, compétences).';
    }
    return 'Pourquoi : ' + bits.join(' · ') + '.';
  }
}
