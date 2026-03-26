import { Component, input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  template: `
    <div class="spinner-wrapper" [class.overlay]="overlay()">
      <div class="spinner" [class]="'spinner-' + size()"></div>
      @if (message()) {
        <p class="spinner-message">{{ message() }}</p>
      }
    </div>
  `,
  styles: [`
    .spinner-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 2rem;
    }

    .spinner-wrapper.overlay {
      position: absolute;
      inset: 0;
      background: rgba(13, 13, 15, 0.8);
      z-index: 10;
    }

    .spinner {
      border: 2px solid var(--border);
      border-top-color: var(--accent-primary);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    .spinner-sm {
      width: 16px;
      height: 16px;
      border-width: 2px;
    }

    .spinner-md {
      width: 24px;
      height: 24px;
      border-width: 2px;
    }

    .spinner-lg {
      width: 40px;
      height: 40px;
      border-width: 3px;
    }

    .spinner-xl {
      width: 64px;
      height: 64px;
      border-width: 4px;
    }

    .spinner-message {
      margin-top: 1rem;
      color: var(--text-secondary);
      font-size: 0.875rem;
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
  `]
})
export class LoadingSpinnerComponent {
  size = input<'sm' | 'md' | 'lg' | 'xl'>('md');
  message = input<string>();
  overlay = input<boolean>(false);
}
