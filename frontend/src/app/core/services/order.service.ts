import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Order, CreateOrderRequest, ShippingAddress, ShippingRate } from '../models';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);
  
  // Signals for state management
  private readonly _orders = signal<Order[]>([]);
  private readonly _currentOrder = signal<Order | null>(null);
  private readonly _loading = signal<boolean>(false);
  
  // Public readonly signals
  readonly orders = this._orders.asReadonly();
  readonly currentOrder = this._currentOrder.asReadonly();
  readonly loading = this._loading.asReadonly();

  // Get all orders for current user
  getOrders(): Observable<Order[]> {
    const userId = this.authService.userId();
    if (!userId) {
      return new Observable(subscriber => {
        subscriber.next([]);
        subscriber.complete();
      });
    }

    this._loading.set(true);
    
    return this.api.get<Order[]>(`/v1/orders/user/${userId}`).pipe(
      tap({
        next: (orders) => {
          this._orders.set(orders);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      })
    );
  }

  // Get single order
  getOrder(orderId: string): Observable<Order> {
    this._loading.set(true);
    
    return this.api.get<Order>(`/v1/orders/${orderId}`).pipe(
      tap({
        next: (order) => {
          this._currentOrder.set(order);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      })
    );
  }

  // Create new order
  createOrder(request: CreateOrderRequest): Observable<Order> {
    this._loading.set(true);
    
    return this.api.post<Order>('/v1/orders', request).pipe(
      tap({
        next: (order) => {
          this._currentOrder.set(order);
          this._orders.update(orders => [order, ...orders]);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      })
    );
  }

  // Update order status
  updateOrderStatus(orderId: string, status: string): Observable<Order> {
    return this.api.patch<Order>(`/v1/orders/${orderId}/status`, null, {
      params: { status }
    }).pipe(
      tap(order => {
        this._currentOrder.set(order);
        this._orders.update(orders => 
          orders.map(o => o.orderId === orderId ? order : o)
        );
      })
    );
  }

  // Cancel order
  cancelOrder(orderId: string): Observable<void> {
    return this.api.delete<void>(`/v1/orders/${orderId}`).pipe(
      tap(() => {
        this._orders.update(orders => 
          orders.map(o => {
            if (o.orderId === orderId) {
              return { ...o, status: 'CANCELLED' as const };
            }
            return o;
          })
        );
      })
    );
  }

  // Get shipping addresses for current user
  getShippingAddresses(): Observable<ShippingAddress[]> {
    const userId = this.authService.userId();
    if (!userId) {
      return new Observable(subscriber => {
        subscriber.next([]);
        subscriber.complete();
      });
    }

    return this.api.get<ShippingAddress[]>(`/v1/shipping/addresses/user/${userId}`);
  }

  // Create shipping address
  createShippingAddress(address: ShippingAddress): Observable<ShippingAddress> {
    return this.api.post<ShippingAddress>('/v1/shipping/addresses', address);
  }

  // Get shipping rates
  getShippingRates(orderId: string): Observable<ShippingRate[]> {
    return this.api.get<ShippingRate[]>('/v1/shipping/rates', {
      params: { orderId }
    });
  }

  // Calculate shipping rate
  calculateShippingRate(params: { weight?: number; distance?: number }): Observable<ShippingRate> {
    return this.api.get<ShippingRate>('/v1/shipping/rates', {
      params: params as Record<string, string>
    });
  }

  // Get shipments for order
  getShipmentByOrder(orderId: string): Observable<any> {
    return this.api.get(`/v1/shipping/shipments/order/${orderId}`);
  }

  // Clear current order
  clearCurrentOrder(): void {
    this._currentOrder.set(null);
  }
}
