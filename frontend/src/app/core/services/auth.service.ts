import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, tap, BehaviorSubject, of } from 'rxjs';
import { ApiService } from './api.service';
import {
  User,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  PasswordResetRequest,
  PasswordResetConfirmRequest
} from '../models';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly api = inject(ApiService);
  
  // Token keys
  private readonly tokenKey = environment.tokenKey;
  private readonly refreshTokenKey = environment.refreshTokenKey;
  
  // Signals for reactive state
  private readonly _user = signal<User | null>(null);
  private readonly _isAuthenticated = signal<boolean>(false);
  private readonly _isLoading = signal<boolean>(false);
  
  // Public readonly signals
  readonly user = this._user.asReadonly();
  readonly isAuthenticated = this._isAuthenticated.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();
  
  // Computed signals
  readonly isAdmin = computed(() => this._user()?.role === 'ADMIN');
  readonly userId = computed(() => this._user()?.userId);
  readonly userName = computed(() => this._user()?.username);
  readonly userFullName = computed(() => {
    const user = this._user();
    return user ? `${user.firstName} ${user.lastName}` : '';
  });

  constructor() {
    this.initializeAuth();
  }

  private initializeAuth(): void {
    const token = this.getToken();
    const storedUser = this.getStoredUser();
    
    if (token && storedUser) {
      this._user.set(storedUser);
      this._isAuthenticated.set(true);
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    this._isLoading.set(true);
    
    return this.api.post<LoginResponse>('/v1/users/login', credentials).pipe(
      tap({
        next: (response) => {
          this.handleLoginSuccess(response);
          this._isLoading.set(false);
        },
        error: () => {
          this._isLoading.set(false);
        }
      })
    );
  }

  register(userData: RegisterRequest): Observable<User> {
    this._isLoading.set(true);
    
    return this.api.post<User>('/v1/users/register', userData).pipe(
      tap({
        next: () => {
          this._isLoading.set(false);
        },
        error: () => {
          this._isLoading.set(false);
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem('user');
    
    this._user.set(null);
    this._isAuthenticated.set(false);
  }

  requestPasswordReset(request: PasswordResetRequest): Observable<void> {
    return this.api.post<void>('/v1/users/password-reset', request);
  }

  confirmPasswordReset(request: PasswordResetConfirmRequest): Observable<void> {
    return this.api.post<void>('/v1/users/password-reset/confirm', request);
  }

  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }
    
    return this.api.post<LoginResponse>('/v1/users/refresh', { refreshToken }).pipe(
      tap((response) => this.handleLoginSuccess(response))
    );
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
  }

  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000; // Convert to milliseconds
      return Date.now() >= expirationTime;
    } catch {
      return true;
    }
  }

  private handleLoginSuccess(response: LoginResponse): void {
    localStorage.setItem(this.tokenKey, response.accessToken);
    localStorage.setItem(this.refreshTokenKey, response.refreshToken);
    localStorage.setItem('user', JSON.stringify(response.user));
    
    this._user.set(response.user);
    this._isAuthenticated.set(true);
  }

  private getStoredUser(): User | null {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }

  updateUser(userId: string, userData: Partial<User>): Observable<User> {
    return this.api.post<User>(`/v1/users/${userId}`, userData).pipe(
      tap((updatedUser) => {
        this._user.set(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
      })
    );
  }

  enableUser(userId: string): Observable<User> {
    return this.api.post<User>(`/v1/users/${userId}/enable`, {}).pipe(
      tap((updatedUser) => {
        this._user.set(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
      })
    );
  }

  disableUser(userId: string): Observable<User> {
    return this.api.post<User>(`/v1/users/${userId}/disable`, {}).pipe(
      tap((updatedUser) => {
        this._user.set(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser));
      })
    );
  }
}
