import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '@services/order.service';
import { Order } from '@models/order.model';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent],
  template: `
    <div class="order-detail-page">
      <div class="container">
        <a routerLink="/orders" class="back-link">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
          </svg>
          Back to Orders
        </a>

        @if (loading()) {
          <app-loading-spinner message="Loading order..." />
        } @else if (order()) {
          <div class="order-header">
            <div>
              <h1>Order #{{ order()?.orderId }}</h1>
              <p class="order-date">Placed on {{ order()?.orderDate | date:'full' }}</p>
            </div>
            <span class="status-badge" [class]="order()?.status?.toLowerCase()">
              {{ order()?.status }}
            </span>
          </div>

          <div class="order-grid">
            <!-- Order Items -->
            <div class="order-items-section">
              <h2>Items</h2>
              <div class="items-list">
                @for (item of order()?.items; track item.orderItemId) {
                  <div class="order-item">
                    <img [src]="item.productImage || '/assets/images/placeholder.png'" [alt]="item.productName">
                    <div class="item-details">
                      <span class="item-name">{{ item.productName }}</span>
                      <span class="item-qty">Qty: {{ item.quantity }}</span>
                    </div>
                    <span class="item-price">{{ item.totalPrice | currency }}</span>
                  </div>
                }
              </div>
            </div>

            <!-- Order Summary -->
            <div class="order-summary-section">
              <div class="summary-card">
                <h3>Order Summary</h3>
                <div class="summary-rows">
                  <div class="summary-row">
                    <span>Subtotal</span>
                    <span>{{ order()?.subtotal | currency }}</span>
                  </div>
                  <div class="summary-row">
                    <span>Shipping</span>
                    <span>{{ order()?.shippingCost === 0 ? 'FREE' : (order()?.shippingCost | currency) }}</span>
                  </div>
                  <div class="summary-row">
                    <span>Tax</span>
                    <span>{{ order()?.taxAmount | currency }}</span>
                  </div>
                  @if (order()?.discountTotal && order()!.discountTotal! > 0) {
                    <div class="summary-row discount">
                      <span>Discount</span>
                      <span>-{{ order()?.discountTotal | currency }}</span>
                    </div>
                  }
                  <div class="summary-total">
                    <span>Total</span>
                    <span>{{ order()?.grandTotal | currency }}</span>
                  </div>
                </div>
              </div>

              <!-- Shipping Address -->
              <div class="address-card">
                <h3>Shipping Address</h3>
                <p>{{ order()?.shippingAddress }}</p>
              </div>

              <!-- Payment Info -->
              <div class="payment-card">
                <h3>Payment Method</h3>
                <p>{{ order()?.paymentMethod }}</p>
                <span class="payment-status" [class]="order()?.paymentStatus?.toLowerCase()">
                  {{ order()?.paymentStatus }}
                </span>
              </div>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .order-detail-page {
      padding: 2rem 0;
    }

    .back-link {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      color: var(--text-secondary);
      margin-bottom: 2rem;
      font-size: 0.875rem;
    }

    .back-link:hover {
      color: var(--accent-primary);
    }

    .back-link svg {
      width: 16px;
      height: 16px;
    }

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 2rem;
    }

    .order-header h1 {
      font-size: 1.75rem;
      margin-bottom: 0.25rem;
    }

    .order-date {
      color: var(--text-muted);
      font-size: 0.875rem;
    }

    .status-badge {
      padding: 0.5rem 1rem;
      border-radius: 9999px;
      font-size: 0.875rem;
      font-weight: 600;
    }

    .status-badge.pending { background: rgba(245, 158, 11, 0.2); color: var(--accent-warning); }
    .status-badge.confirmed { background: rgba(99, 102, 241, 0.2); color: var(--accent-primary); }
    .status-badge.shipped { background: rgba(34, 211, 238, 0.2); color: var(--accent-secondary); }
    .status-badge.delivered { background: rgba(16, 185, 129, 0.2); color: var(--accent-success); }

    .order-grid {
      display: grid;
      grid-template-columns: 1fr 380px;
      gap: 2rem;
    }

    .order-items-section,
    .order-summary-section {
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      padding: 1.5rem;
    }

    .order-items-section h2,
    .order-summary-section h3 {
      font-size: 1rem;
      font-weight: 600;
      margin-bottom: 1rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--border);
    }

    .items-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .order-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 0.75rem;
      background: var(--bg-tertiary);
      border-radius: 0.5rem;
    }

    .order-item img {
      width: 60px;
      height: 60px;
      object-fit: cover;
      border-radius: 0.25rem;
    }

    .item-details {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .item-name {
      font-weight: 500;
    }

    .item-qty {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .item-price {
      font-weight: 600;
    }

    .summary-card {
      margin-bottom: 1.5rem;
    }

    .summary-rows {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .summary-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.9375rem;
    }

    .summary-row span:first-child {
      color: var(--text-secondary);
    }

    .summary-row.discount span:last-child {
      color: var(--accent-success);
    }

    .summary-total {
      display: flex;
      justify-content: space-between;
      padding-top: 0.75rem;
      margin-top: 0.5rem;
      border-top: 1px solid var(--border);
      font-weight: 700;
      font-size: 1.125rem;
    }

    .summary-total span:last-child {
      color: var(--accent-primary);
    }

    .address-card,
    .payment-card {
      padding-top: 1.5rem;
      border-top: 1px solid var(--border);
      margin-top: 1.5rem;
    }

    .address-card p,
    .payment-card p {
      color: var(--text-secondary);
      font-size: 0.9375rem;
      line-height: 1.6;
    }

    .payment-status {
      display: inline-block;
      margin-top: 0.5rem;
      padding: 0.25rem 0.5rem;
      border-radius: 0.25rem;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .payment-status.completed { background: rgba(16, 185, 129, 0.2); color: var(--accent-success); }
    .payment-status.pending { background: rgba(245, 158, 11, 0.2); color: var(--accent-warning); }
    .payment-status.failed { background: rgba(239, 68, 68, 0.2); color: var(--accent-danger); }

    @media (max-width: 1024px) {
      .order-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class OrderDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly orderService = inject(OrderService);

  order = this.orderService.currentOrder;
  loading = this.orderService.loading;

  ngOnInit(): void {
    const orderId = this.route.snapshot.params['id'];
    if (orderId) {
      this.orderService.getOrder(orderId).subscribe();
    }
  }
}
