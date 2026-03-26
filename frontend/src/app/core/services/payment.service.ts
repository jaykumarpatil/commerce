import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { Payment, PaymentRequest, RefundRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly api = inject(ApiService);
  
  // Signals for state management
  private readonly _loading = signal<boolean>(false);
  private readonly _currentPayment = signal<Payment | null>(null);
  
  // Public readonly signals
  readonly loading = this._loading.asReadonly();
  readonly currentPayment = this._currentPayment.asReadonly();

  // Process payment
  processPayment(request: PaymentRequest): Observable<Payment> {
    this._loading.set(true);
    
    return this.api.post<Payment>('/v1/payments', request).pipe(
      tap({
        next: (payment) => {
          this._currentPayment.set(payment);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      })
    );
  }

  // Get payment by order ID
  getPaymentByOrder(orderId: string): Observable<Payment> {
    return this.api.get<Payment>(`/v1/payments/order/${orderId}`).pipe(
      tap(payment => this._currentPayment.set(payment))
    );
  }

  // Get payment by ID
  getPayment(paymentId: string): Observable<Payment> {
    return this.api.get<Payment>(`/v1/payments/${paymentId}`).pipe(
      tap(payment => this._currentPayment.set(payment))
    );
  }

  // Refund payment
  refundPayment(request: RefundRequest): Observable<Payment> {
    this._loading.set(true);
    
    return this.api.post<Payment>(`/v1/payments/${request.paymentId}/refund`, request).pipe(
      tap({
        next: (payment) => {
          this._currentPayment.set(payment);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      })
    );
  }

  // Update payment status
  updatePaymentStatus(paymentId: string, status: string): Observable<Payment> {
    return this.api.patch<Payment>(`/v1/payments/${paymentId}/status`, null, {
      params: { status }
    }).pipe(
      tap(payment => this._currentPayment.set(payment))
    );
  }

  // Clear current payment
  clearCurrentPayment(): void {
    this._currentPayment.set(null);
  }
}
