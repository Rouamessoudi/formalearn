import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { ToastService } from '../ui/toast.service';

@Component({
  selector: 'app-learner-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './learner-layout.component.html',
  styleUrl: './learner-layout.component.scss'
})
export class LearnerLayoutComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly toast = inject(ToastService);

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      if (params.get('acces') === 'refuse') {
        this.toast.error('Accès refusé : l’espace administrateur est réservé au rôle ADMIN.');
      }
    });
  }
}
