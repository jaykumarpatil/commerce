import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '@services/cart.service';
import { ToastService } from '@shared/components/toast/toast.component';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="cart-page">
      <div class="container">
        <h1>Shopping Cart</h1>

        @if (cartService.isEmpty()) {
          <div class="empty-cart">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="9" cy="21" r="1"></circle>
              <circle cx="20" cy="21" r="1"></circle>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
            </svg>
            <h2>Your cart is empty</h2>
            <p>Looks like you haven't added any items to your cart yet.</p>
            <a routerLink="/products" class="btn btn-primary btn-lg">
              Start Shopping
            </a>
          </div>
        } @else {
          <div class="cart-layout">
            <!-- Cart Items -->
            <div class="cart-items">
              @for (item of cartService.items(); track item.productId) {
                <div class="cart-item">
                  <div class="item-image">
                    <img [src]="item.productImage || '/assets/images/placeholder.png'" [alt]="item.productName">
                  </div>
                  <div class="item-details">
                    <a [routerLink]="['/products', item.productId]" class="item-name">
                      {{ item.productName }}
                    </a>
                    @if (item.options && item.options.length > 0) {
                      <div class="item-options">
                        @for (option of item.options; track option.optionName) {
                          <span>{{ option.optionName }}: {{ option.optionValue }}</span>
                        }
                      </div>
                    }
                    <p class="item-price">{{ item.unitPrice | currency }} each</p>
                  </div>
                  <div class="item-quantity">
                    <button (click)="decrementQuantity(item)" [disabled]="item.quantity <= 1">-</button>
                    <span>{{ item.quantity }}</span>
                    <button (click)="incrementQuantity(item)" [disabled]="item.quantity >= (item.maxOrderQuantity || 99)">+</button>
                  </div>
                  <div class="item-total">
                    {{ item.totalPrice | currency }}
                  </div>
                  <button class="item-remove" (click)="removeItem(item)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"></polyline>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    </svg>
                  </button>
                </div>
              }
            </div>

            <!-- Cart Summary -->
            <div class="cart-summary">
              <h2>Order Summary</h2>
              
              <div class="summary-rows">
                <div class="summary-row">
                  <span>Subtotal</span>
                  <span>{{ cartService.subtotal() | currency }}</span>
                </div>
                @if (cartService.discountTotal() > 0) {
                  <div class="summary-row discount">
                    <span>Discount</span>
                    <span>-{{ cartService.discountTotal() | currency }}</span>
                  </div>
                }
                <div class="summary-row">
                  <span>Estimated Tax</span>
                  <span>{{ cartService.taxAmount() | currency }}</span>
                </div>
                <div class="summary-row">
                  <span>Shipping</span>
                  @if (cartService.shippingCost() === 0) {
                    <span class="free-shipping">FREE</span>
                  } @else {
                    <span>{{ cartService.shippingCost() | currency }}</span>
                  }
                </div>
              </div>

              <div class="summary-total">
                <span>Total</span>
                <span>{{ cartService.grandTotal() | currency }}</span>
              </div>

              @if (cartService.subtotal() < 50 && cartService.subtotal() > 0) {
                <div class="free-shipping-notice">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="16" x2="12" y2="12"></line>
                    <line x1="12" y1="8" x2="12.01" y2="8"></line>
                  </svg>
                  <span>Add {{ (50 - cartService.subtotal()) | currency }} more for free shipping!</span>
                </div>
              }

              <a routerLink="/checkout" class="btn btn-primary btn-lg btn-block">
                Proceed to Checkout
              </a>

              <div class="continue-shopping">
                <a routerLink="/products">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="19" y1="12" x2="5" y2="12"></line>
                    <polyline points="12 19 5 12 12 5"></polyline>
                  </svg>
                  Continue Shopping
                </a>
              </div>
            </div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .cart-page {
      padding: 2rem 0;
    }

    .cart-page h1 {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 2rem;
    }

    .empty-cart {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem 2rem;
      text-align: center;
    }

    .empty-cart svg {
      width: 80px;
      height: 80px;
      color: var(--text-muted);
      margin-bottom: 1.5rem;
    }

    .empty-cart h2 {
      font-size: 1.5rem;
      margin-bottom: 0.5rem;
    }

    .empty-cart p {
      color: var(--text-muted);
      margin-bottom: 2rem;
    }

    .cart-layout {
      display: grid;
      grid-template-columns: 1fr 400px;
      gap: 2rem;
      align-items: start;
    }

    /* Cart Items */
    .cart-items {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .cart-item {
      display: grid;
      grid-template-columns: 100px 1fr auto auto auto;
      gap: 1.5rem;
      align-items: center;
      padding: 1.5rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
    }

    .item-image {
      width: 100px;
      height: 100px;
      background: var(--bg-tertiary);
      border-radius: 0.5rem;
      overflow: hidden;
    }

    .item-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .item-details {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .item-name {
      font-weight: 600;
      color: var(--text-primary);
      text-decoration: none;
    }

    .item-name:hover {
      color: var(--accent-primary);
    }

    .item-options {
      display: flex;
      gap: 1rem;
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .item-price {
      font-size: 0.875rem;
      color: var(--text-secondary);
    }

    .item-quantity {
      display: flex;
      align-items: center;
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      overflow: hidden;
    }

    .item-quantity button {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-tertiary);
      color: var(--text-primary);
      font-size: 1rem;
      transition: all 0.2s;
    }

    .item-quantity button:hover:not(:disabled) {
      background: var(--bg-hover);
    }

    .item-quantity button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .item-quantity span {
      width: 40px;
      text-align: center;
      font-weight: 500;
    }

    .item-total {
      font-weight: 600;
      font-size: 1.125rem;
      min-width: 80px;
      text-align: right;
    }

    .item-remove {
      padding: 0.5rem;
      color: var(--text-muted);
      border-radius: 0.5rem;
      transition: all 0.2s;
    }

    .item-remove:hover {
      background: rgba(239, 68, 68, 0.1);
      color: var(--accent-danger);
    }

    .item-remove svg {
      width: 20px;
      height: 20px;
    }

    /* Cart Summary */
    .cart-summary {
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      padding: 1.5rem;
      position: sticky;
      top: 100px;
    }

    .cart-summary h2 {
      font-size: 1.25rem;
      font-weight: 600;
      margin-bottom: 1.5rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid var(--border);
    }

    .summary-rows {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-bottom: 1rem;
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

    .free-shipping {
      color: var(--accent-success);
      font-weight: 500;
    }

    .summary-total {
      display: flex;
      justify-content: space-between;
      padding-top: 1rem;
      margin-top: 0.5rem;
      border-top: 1px solid var(--border);
      font-size: 1.25rem;
      font-weight: 700;
    }

    .summary-total span:last-child {
      color: var(--accent-primary);
    }

    .free-shipping-notice {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem;
      margin: 1rem 0;
      background: rgba(99, 102, 241, 0.1);
      border-radius: 0.5rem;
      font-size: 0.875rem;
      color: var(--accent-primary);
    }

    .free-shipping-notice svg {
      width: 18px;
      height: 18px;
      flex-shrink: 0;
    }

    .btn-block {
      width: 100%;
      margin-top: 1rem;
    }

    .continue-shopping {
      margin-top: 1rem;
      padding-top: 1rem;
      border-top: 1px solid var(--border);
      text-align: center;
    }

    .continue-shopping a {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      color: var(--text-secondary);
      font-size: 0.875rem;
    }

    .continue-shopping a:hover {
      color: var(--text-primary);
    }

    .continue-shopping svg {
      width: 16px;
      height: 16px;
    }

    @media (max-width: 1024px) {
      .cart-layout {
        grid-template-columns: 1fr;
      }

      .cart-summary {
        position: static;
      }
    }

    @media (max-width: 768px) {
      .cart-item {
        grid-template-columns: 80px 1fr;
        grid-template-rows: auto auto;
      }

      .item-details {
        grid-column: 2;
      }

      .item-quantity {
        grid-column: 1 / -1;
        justify-self: start;
      }

      .item-total {
        text-align: left;
      }

      .item-remove {
        position: absolute;
        right: 1rem;
        top: 1rem;
      }

      .cart-item {
        position: relative;
      }
    }
  `]
})
export class CartComponent {
  protected readonly cartService = inject(CartService);
  private readonly toastService = inject(ToastService);

  incrementQuantity(item: any): void {
    this.cartService.updateItemQuantity(item.productId, item.quantity + 1);
  }

  decrementQuantity(item: any): void {
    if (item.quantity > 1) {
      this.cartService.updateItemQuantity(item.productId, item.quantity - 1);
    }
  }

  removeItem(item: any): void {
    this.cartService.removeItem(item.productId);
    this.toastService.info('Item Removed', `${item.productName} has been removed from your cart.`);
  }
}
