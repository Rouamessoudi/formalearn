import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-host',
  standalone: true,
  template: `
    <div class="toasts">
      @for (toast of toasts.toasts(); track toast.id) {
        <div class="toast" [class.error]="toast.type === 'error'">{{ toast.message }}</div>
      }
    </div>
  `,
  styles: [`
    .toasts { position: fixed; right: 1rem; bottom: 1rem; z-index: 50; display: grid; gap: .5rem; width: min(360px, calc(100vw - 2rem)); }
    .toast { background: #0f766e; color: #fff; padding: .85rem 1rem; border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,.15); }
    .toast.error { background: #b91c1c; }
  `]
})
export class ToastHostComponent {
  readonly toasts = inject(ToastService);
}
