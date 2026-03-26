import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '@services/order.service';
import { Order } from '@models/order.model';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink, LoadingSpinnerComponent],
  template: `
    <div class="orders-page">
      <div class="container">
        <h1>My Orders</h1>

        @if (loading()) {
          <app-loading-spinner message="Loading orders..." />
        } @else if (orders().length === 0) {
          <div class="empty-orders">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
            </svg>
            <h2>No Orders Yet</h2>
            <p>You haven't placed any orders yet.</p>
            <a routerLink="/products" class="btn btn-primary">Start Shopping</a>
          </div>
        } @else {
          <div class="orders-list">
            @for (order of orders(); track order.orderId) {
              <a [routerLink]="['/orders', order.orderId]" class="order-card">
                <div class="order-header">
                  <div class="order-info">
                    <span class="order-id">#{{ order.orderId }}</span>
                    <span class="order-date">{{ order.orderDate | date:'medium' }}</span>
                  </div>
                  <span class="status-badge" [class]="order.status.toLowerCase()">
                    {{ order.status }}
                  </span>
                </div>
                <div class="order-items">
                  @for (item of order.items.slice(0, 3); track item.orderItemId) {
                    <img [src]="item.productImage || '/assets/images/placeholder.png'" [alt]="item.productName">
                  }
                  @if (order.items.length > 3) {
                    <span class="more-items">+{{ order.items.length - 3 }}</span>
                  }
                </div>
                <div class="order-footer">
                  <span class="item-count">{{ order.items.length }} items</span>
                  <span class="order-total">{{ order.grandTotal | currency }}</span>
                </div>
              </a>
            }
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .orders-page {
      padding: 2rem 0;
    }

    .orders-page h1 {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 2rem;
    }

    .empty-orders {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem 2rem;
      text-align: center;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
    }

    .empty-orders svg {
      width: 64px;
      height: 64px;
      color: var(--text-muted);
      margin-bottom: 1.5rem;
    }

    .empty-orders h2 {
      font-size: 1.25rem;
      margin-bottom: 0.5rem;
    }

    .empty-orders p {
      color: var(--text-muted);
      margin-bottom: 1.5rem;
    }

    .orders-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .order-card {
      display: block;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      padding: 1.5rem;
      text-decoration: none;
      color: inherit;
      transition: all 0.2s;
    }

    .order-card:hover {
      border-color: var(--accent-primary);
      transform: translateX(4px);
    }

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1rem;
    }

    .order-info {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .order-id {
      font-weight: 600;
    }

    .order-date {
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
    .status-badge.cancelled { background: rgba(239, 68, 68, 0.2); color: var(--accent-danger); }

    .order-items {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }

    .order-items img {
      width: 48px;
      height: 48px;
      object-fit: cover;
      border-radius: 0.25rem;
      background: var(--bg-tertiary);
    }

    .more-items {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-tertiary);
      border-radius: 0.25rem;
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 1rem;
      border-top: 1px solid var(--border);
    }

    .item-count {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .order-total {
      font-weight: 700;
      color: var(--accent-primary);
    }
  `]
})
export class OrderListComponent implements OnInit {
  private readonly orderService = inject(OrderService);

  orders = this.orderService.orders;
  loading = this.orderService.loading;

  ngOnInit(): void {
    this.orderService.getOrders().subscribe();
  }
}
