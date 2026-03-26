import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '@services/auth.service';
import { UserService } from '@services/user.service';
import { User, ShippingAddress } from '@models/user.model';
import { ToastService } from '@shared/components/toast/toast.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LoadingSpinnerComponent],
  template: `
    <div class="profile-page">
      <div class="container">
        <h1>My Profile</h1>

        <!-- Tabs -->
        <div class="tabs">
          <button class="tab" [class.active]="activeTab() === 'profile'" (click)="activeTab.set('profile')">
            Profile
          </button>
          <button class="tab" [class.active]="activeTab() === 'addresses'" (click)="activeTab.set('addresses')">
            Addresses
          </button>
        </div>

        <!-- Profile Tab -->
        @if (activeTab() === 'profile') {
          <div class="tab-content">
            <form [formGroup]="profileForm" (ngSubmit)="onUpdateProfile()">
              <div class="form-section">
                <h3>Personal Information</h3>
                <div class="form-grid">
                  <div class="form-group">
                    <label for="firstName" class="form-label">First Name</label>
                    <input type="text" id="firstName" formControlName="firstName" class="form-input">
                  </div>
                  <div class="form-group">
                    <label for="lastName" class="form-label">Last Name</label>
                    <input type="text" id="lastName" formControlName="lastName" class="form-input">
                  </div>
                </div>
                <div class="form-group">
                  <label for="email" class="form-label">Email</label>
                  <input type="email" id="email" formControlName="email" class="form-input">
                </div>
                <div class="form-group">
                  <label for="username" class="form-label">Username</label>
                  <input type="text" id="username" formControlName="username" class="form-input" disabled>
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn btn-primary" [disabled]="saving()">
                  @if (saving()) {
                    <span class="spinner spinner-sm"></span>
                    Saving...
                  } @else {
                    Save Changes
                  }
                </button>
              </div>
            </form>

            <form class="password-form" (ngSubmit)="onChangePassword()">
              <div class="form-section">
                <h3>Change Password</h3>
                <div class="form-group">
                  <label for="currentPassword" class="form-label">Current Password</label>
                  <input type="password" id="currentPassword" class="form-input">
                </div>
                <div class="form-group">
                  <label for="newPassword" class="form-label">New Password</label>
                  <input type="password" id="newPassword" class="form-input">
                </div>
                <div class="form-group">
                  <label for="confirmPassword" class="form-label">Confirm New Password</label>
                  <input type="password" id="confirmPassword" class="form-input">
                </div>
              </div>
              <div class="form-actions">
                <button type="submit" class="btn btn-secondary">Change Password</button>
              </div>
            </form>
          </div>
        }

        <!-- Addresses Tab -->
        @if (activeTab() === 'addresses') {
          <div class="tab-content">
            <div class="addresses-header">
              <h3>Shipping Addresses</h3>
              <button class="btn btn-primary btn-sm">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                Add New Address
              </button>
            </div>

            @if (addresses().length === 0) {
              <div class="empty-addresses">
                <p>No shipping addresses saved yet.</p>
              </div>
            } @else {
              <div class="addresses-grid">
                @for (address of addresses(); track address.addressId) {
                  <div class="address-card">
                    @if (address.isDefault) {
                      <span class="default-badge">Default</span>
                    }
                    <p class="address-name">{{ address.fullName }}</p>
                    <p class="address-line">{{ address.streetAddress }}</p>
                    <p class="address-line">{{ address.city }}, {{ address.state }} {{ address.zipCode }}</p>
                    <p class="address-line">{{ address.country }}</p>
                    <p class="address-phone">{{ address.phoneNumber }}</p>
                    <div class="address-actions">
                      <button class="btn btn-ghost btn-sm">Edit</button>
                      <button class="btn btn-ghost btn-sm text-danger">Delete</button>
                    </div>
                  </div>
                }
              </div>
            }
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .profile-page {
      padding: 2rem 0;
    }

    .profile-page h1 {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 2rem;
    }

    .tabs {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 2rem;
      border-bottom: 1px solid var(--border);
    }

    .tab {
      padding: 0.75rem 1.5rem;
      color: var(--text-secondary);
      font-weight: 500;
      border-bottom: 2px solid transparent;
      margin-bottom: -1px;
      transition: all 0.2s;
    }

    .tab:hover {
      color: var(--text-primary);
    }

    .tab.active {
      color: var(--accent-primary);
      border-bottom-color: var(--accent-primary);
    }

    .tab-content {
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      padding: 2rem;
    }

    .form-section {
      margin-bottom: 2rem;
    }

    .form-section h3 {
      font-size: 1rem;
      font-weight: 600;
      margin-bottom: 1.25rem;
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }

    .form-group input:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }

    .form-actions {
      padding-top: 1.5rem;
      border-top: 1px solid var(--border);
    }

    .password-form {
      margin-top: 2rem;
      padding-top: 2rem;
      border-top: 1px solid var(--border);
    }

    .addresses-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }

    .addresses-header h3 {
      font-size: 1rem;
      font-weight: 600;
    }

    .addresses-header .btn svg {
      width: 16px;
      height: 16px;
    }

    .empty-addresses {
      text-align: center;
      padding: 2rem;
      color: var(--text-muted);
    }

    .addresses-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }

    .address-card {
      position: relative;
      padding: 1.5rem;
      background: var(--bg-tertiary);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
    }

    .default-badge {
      position: absolute;
      top: 1rem;
      right: 1rem;
      padding: 0.25rem 0.5rem;
      background: var(--accent-primary);
      color: white;
      font-size: 0.7rem;
      font-weight: 600;
      border-radius: 0.25rem;
    }

    .address-name {
      font-weight: 600;
      margin-bottom: 0.5rem;
    }

    .address-line {
      font-size: 0.875rem;
      color: var(--text-secondary);
      margin-bottom: 0.25rem;
    }

    .address-phone {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-top: 0.5rem;
    }

    .address-actions {
      display: flex;
      gap: 0.5rem;
      margin-top: 1rem;
      padding-top: 1rem;
      border-top: 1px solid var(--border);
    }

    .btn-ghost {
      color: var(--text-secondary);
    }

    .btn-ghost:hover {
      background: var(--bg-hover);
      color: var(--text-primary);
    }

    .text-danger {
      color: var(--accent-danger);
    }

    .text-danger:hover {
      color: var(--accent-danger);
    }

    @media (max-width: 768px) {
      .form-grid {
        grid-template-columns: 1fr;
      }

      .addresses-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ProfileComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  activeTab = signal<'profile' | 'addresses'>('profile');
  saving = signal(false);
  addresses = signal<ShippingAddress[]>([]);

  profileForm: FormGroup;

  constructor() {
    this.profileForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      username: ['']
    });
  }

  ngOnInit(): void {
    const user = this.authService.user();
    if (user) {
      this.profileForm.patchValue({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        username: user.username
      });
    }

    this.loadAddresses();
  }

  private loadAddresses(): void {
    this.userService.getAddresses().subscribe({
      next: (addresses) => this.addresses.set(addresses),
      error: () => {}
    });
  }

  onUpdateProfile(): void {
    if (this.profileForm.invalid) return;

    this.saving.set(true);
    const userId = this.authService.userId();
    
    if (userId) {
      this.authService.updateUser(userId, this.profileForm.value).subscribe({
        next: () => {
          this.saving.set(false);
          this.toastService.success('Profile Updated', 'Your profile has been updated successfully.');
        },
        error: () => {
          this.saving.set(false);
          this.toastService.error('Update Failed', 'Could not update profile. Please try again.');
        }
      });
    }
  }

  onChangePassword(): void {
    this.toastService.info('Coming Soon', 'Password change feature will be available soon.');
  }
}
