import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { User, ShippingAddress } from '../models';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  // Get current user profile
  getProfile(): Observable<User> {
    const userId = this.authService.userId();
    if (!userId) {
      throw new Error('User not authenticated');
    }
    return this.api.get<User>(`/v1/users/${userId}`);
  }

  // Update user profile
  updateProfile(userId: string, userData: Partial<User>): Observable<User> {
    return this.api.post<User>(`/v1/users/${userId}`, userData);
  }

  // Get user shipping addresses
  getAddresses(): Observable<ShippingAddress[]> {
    const userId = this.authService.userId();
    if (!userId) {
      throw new Error('User not authenticated');
    }
    return this.api.get<ShippingAddress[]>(`/v1/shipping/addresses/user/${userId}`);
  }

  // Create shipping address
  createAddress(address: ShippingAddress): Observable<ShippingAddress> {
    return this.api.post<ShippingAddress>('/v1/shipping/addresses', address);
  }

  // Update shipping address
  updateAddress(addressId: string, address: ShippingAddress): Observable<ShippingAddress> {
    return this.api.put<ShippingAddress>(`/v1/shipping/addresses/${addressId}`, address);
  }

  // Delete shipping address
  deleteAddress(addressId: string): Observable<void> {
    return this.api.delete<void>(`/v1/shipping/addresses/${addressId}`);
  }

  // Set default address
  setDefaultAddress(addressId: string): Observable<ShippingAddress> {
    const userId = this.authService.userId();
    if (!userId) {
      throw new Error('User not authenticated');
    }
    return this.api.patch<ShippingAddress>(`/v1/shipping/addresses/${addressId}/default`, {
      userId
    });
  }
}
