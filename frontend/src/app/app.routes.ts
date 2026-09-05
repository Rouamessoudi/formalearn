import { Routes } from '@angular/router';
import { LoginPageComponent } from './features/auth/login-page.component';
import { AdminLayoutComponent } from './core/layout/admin-layout.component';
import { LearnerLayoutComponent } from './core/layout/learner-layout.component';
import { adminGuard, guestGuard, learnerGuard } from './core/auth/auth.guards';
import { formationResolver } from './core/routing/formation.resolver';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginPageComponent, canActivate: [guestGuard] },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [adminGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/admin/admin-dashboard-page.component').then(m => m.AdminDashboardPageComponent)
      },
      {
        path: 'atelier',
        loadComponent: () => import('./features/admin/admin-atelier-page.component').then(m => m.AdminAtelierPageComponent)
      },
      {
        path: 'categories',
        loadComponent: () => import('./features/admin/admin-categories-page.component').then(m => m.AdminCategoriesPageComponent)
      },
      {
        path: 'formations/:id/chapitres',
        loadComponent: () => import('./features/admin/admin-chapters-page.component').then(m => m.AdminChaptersPageComponent)
      },
      {
        path: 'formations',
        loadComponent: () => import('./features/admin/admin-formations-page.component').then(m => m.AdminFormationsPageComponent)
      },
      {
        path: 'sessions/:id/inscriptions',
        loadComponent: () => import('./features/admin/admin-session-enrollments-page.component').then(m => m.AdminSessionEnrollmentsPageComponent)
      },
      {
        path: 'sessions',
        loadComponent: () => import('./features/admin/admin-sessions-page.component').then(m => m.AdminSessionsPageComponent)
      },
      {
        path: 'inscriptions',
        loadComponent: () => import('./features/admin/admin-sessions-page.component').then(m => m.AdminSessionsPageComponent)
      },
    ]
  },
  {
    path: 'app',
    component: LearnerLayoutComponent,
    canActivate: [learnerGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'catalogue' },
      {
        path: 'catalogue/:id',
        loadComponent: () => import('./features/learner/learner-detail-page.component').then(m => m.LearnerDetailPageComponent),
        resolve: { formation: formationResolver }
      },
      {
        path: 'catalogue',
        loadComponent: () => import('./features/learner/learner-catalog-page.component').then(m => m.LearnerCatalogPageComponent)
      },
      {
        path: 'inscriptions',
        loadComponent: () => import('./features/learner/learner-enrollments-page.component').then(m => m.LearnerEnrollmentsPageComponent)
      },
      {
        path: 'recommandations',
        loadComponent: () => import('./features/learner/learner-recommendations-page.component').then(m => m.LearnerRecommendationsPageComponent)
      },
      {
        path: 'profil',
        loadComponent: () => import('./features/learner/learner-profile-page.component').then(m => m.LearnerProfilePageComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
