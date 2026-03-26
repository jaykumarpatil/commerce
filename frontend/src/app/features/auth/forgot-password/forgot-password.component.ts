import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '@services/auth.service';
import { ToastService } from '@shared/components/toast/toast.component';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <a routerLink="/" class="logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <path d="M16 10a4 4 0 0 1-8 0"></path>
            </svg>
          </a>
          <h1>Reset Password</h1>
          <p>Enter your email to receive a reset link</p>
        </div>

        @if (!emailSent()) {
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="auth-form">
            @if (error()) {
              <div class="error-alert">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="15" y1="9" x2="9" y2="15"></line>
                  <line x1="9" y1="9" x2="15" y2="15"></line>
                </svg>
                <span>{{ error() }}</span>
              </div>
            }

            <div class="form-group">
              <label for="email" class="form-label">Email Address</label>
              <input 
                type="email" 
                id="email" 
                formControlName="email"
                class="form-input"
                [class.error]="isFieldInvalid('email')"
                placeholder="john@example.com"
              >
              @if (isFieldInvalid('email')) {
                <span class="form-error">Please enter a valid email</span>
              }
            </div>

            <button type="submit" class="btn btn-primary btn-lg btn-block" [disabled]="loading()">
              @if (loading()) {
                <span class="spinner spinner-sm"></span>
                Sending...
              } @else {
                Send Reset Link
              }
            </button>
          </form>
        } @else {
          <div class="success-message">
            <div class="success-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <h3>Check Your Email</h3>
            <p>We've sent a password reset link to <strong>{{ form.get('email')?.value }}</strong></p>
            <p class="hint">Didn't receive the email? Check your spam folder or <button (click)="resendEmail()">try again</button></p>
          </div>
        }

        <div class="auth-footer">
          <a routerLink="/login">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="19" y1="12" x2="5" y2="12"></line>
              <polyline points="12 19 5 12 12 5"></polyline>
            </svg>
            Back to Login
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container {
      min-height: calc(100vh - 200px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem;
    }

    .auth-card {
      width: 100%;
      max-width: 420px;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 1rem;
      padding: 2.5rem;
    }

    .auth-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .logo {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      background: var(--gradient-primary);
      border-radius: 0.75rem;
      margin-bottom: 1.5rem;
    }

    .logo svg {
      width: 28px;
      height: 28px;
      color: white;
    }

    .auth-header h1 {
      font-size: 1.5rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
    }

    .auth-header p {
      color: var(--text-muted);
      font-size: 0.875rem;
    }

    .auth-form {
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }

    .error-alert {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 1rem;
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid rgba(239, 68, 68, 0.3);
      border-radius: 0.5rem;
      color: var(--accent-danger);
      font-size: 0.875rem;
    }

    .error-alert svg {
      width: 18px;
      height: 18px;
      flex-shrink: 0;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .btn-block {
      width: 100%;
      margin-top: 0.5rem;
    }

    .spinner-sm {
      width: 16px;
      height: 16px;
      border-width: 2px;
    }

    .success-message {
      text-align: center;
      padding: 1rem 0;
    }

    .success-icon {
      width: 64px;
      height: 64px;
      margin: 0 auto 1.5rem;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(16, 185, 129, 0.1);
      border-radius: 50%;
      color: var(--accent-success);
    }

    .success-icon svg {
      width: 32px;
      height: 32px;
    }

    .success-message h3 {
      font-size: 1.25rem;
      margin-bottom: 0.75rem;
    }

    .success-message p {
      color: var(--text-secondary);
      font-size: 0.875rem;
      margin-bottom: 0.5rem;
    }

    .success-message .hint {
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--border);
    }

    .success-message button {
      color: var(--accent-primary);
      font-weight: 500;
    }

    .auth-footer {
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--border);
      text-align: center;
    }

    .auth-footer a {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      color: var(--text-secondary);
      font-size: 0.875rem;
    }

    .auth-footer a:hover {
      color: var(--text-primary);
    }

    .auth-footer svg {
      width: 16px;
      height: 16px;
    }
  `]
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  form: FormGroup;
  loading = signal(false);
  error = signal<string | null>(null);
  emailSent = signal(false);

  constructor() {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  isFieldInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!(control && control.invalid && control.touched);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const { email } = this.form.value;

    this.authService.requestPasswordReset({ email }).subscribe({
      next: () => {
        this.emailSent.set(true);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.message || 'Failed to send reset email. Please try again.');
      }
    });
  }

  resendEmail(): void {
    this.emailSent.set(false);
  }
}
