import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '@services/order.service';
import { Order } from '@models/order.model';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent],
  template: `
    <div class="confirmation-page">
      <div class="container">
        @if (loading()) {
          <app-loading-spinner message="Loading order..." />
        } @else if (order()) {
          <div class="confirmation-card">
            <div class="success-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <h1>Thank You for Your Order!</h1>
            <p class="order-id">Order #{{ order()?.orderId }}</p>
            
            <div class="order-details">
              <div class="detail-section">
                <h3>Order Status</h3>
                <span class="status-badge" [class]="order()?.status?.toLowerCase()">
                  {{ order()?.status }}
                </span>
              </div>
              
              <div class="detail-section">
                <h3>Order Date</h3>
                <p>{{ order()?.orderDate | date:'medium' }}</p>
              </div>
              
              <div class="detail-section">
                <h3>Total Amount</h3>
                <p class="total">{{ order()?.grandTotal | currency }}</p>
              </div>
            </div>
            
            <div class="actions">
              <a [routerLink]="['/orders', order()?.orderId]" class="btn btn-primary">
                View Order Details
              </a>
              <a routerLink="/products" class="btn btn-secondary">
                Continue Shopping
              </a>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .confirmation-page {
      padding: 4rem 0;
    }
    
    .confirmation-card {
      max-width: 600px;
      margin: 0 auto;
      text-align: center;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 1rem;
      padding: 3rem;
    }
    
    .success-icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 1.5rem;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(16, 185, 129, 0.1);
      border-radius: 50%;
      color: var(--accent-success);
    }
    
    .success-icon svg {
      width: 40px;
      height: 40px;
    }
    
    h1 {
      font-size: 1.75rem;
      margin-bottom: 0.5rem;
    }
    
    .order-id {
      color: var(--text-muted);
      margin-bottom: 2rem;
    }
    
    .order-details {
      text-align: left;
      padding: 1.5rem;
      background: var(--bg-tertiary);
      border-radius: 0.75rem;
      margin-bottom: 2rem;
    }
    
    .detail-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 0;
      border-bottom: 1px solid var(--border);
    }
    
    .detail-section:last-child {
      border-bottom: none;
    }
    
    .detail-section h3 {
      font-size: 0.875rem;
      color: var(--text-muted);
    }
    
    .status-badge {
      padding: 0.25rem 0.75rem;
      border-radius: 9999px;
      font-size: 0.75rem;
      font-weight: 600;
    }
    
    .status-badge.pending { background: rgba(245, 158, 11, 0.2); color: var(--accent-warning); }
    .status-badge.confirmed { background: rgba(99, 102, 241, 0.2); color: var(--accent-primary); }
    .status-badge.shipped { background: rgba(34, 211, 238, 0.2); color: var(--accent-secondary); }
    .status-badge.delivered { background: rgba(16, 185, 129, 0.2); color: var(--accent-success); }
    
    .total {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--accent-primary);
    }
    
    .actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
    }
  `]
})
export class OrderConfirmationComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly orderService = inject(OrderService);
  
  order = signal<Order | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    const orderId = this.route.snapshot.params['orderId'];
    if (orderId) {
      this.orderService.getOrder(orderId).subscribe({
        next: (order) => {
          this.order.set(order);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    }
  }
}
