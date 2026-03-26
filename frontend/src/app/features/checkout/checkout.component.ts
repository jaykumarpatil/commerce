import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CartService } from '@services/cart.service';
import { OrderService } from '@services/order.service';
import { PaymentService } from '@services/payment.service';
import { AuthService } from '@services/auth.service';
import { ToastService } from '@shared/components/toast/toast.component';
import { ShippingAddress } from '@models/order.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
    <div class="checkout-page">
      <div class="container">
        <h1>Checkout</h1>

        <!-- Progress Steps -->
        <div class="step-indicator">
          <div class="step-item" [class.active]="currentStep() >= 1" [class.completed]="currentStep() > 1">
            <span class="step-number">1</span>
            <span class="step-label">Cart</span>
          </div>
          <div class="step-line" [class.completed]="currentStep() > 1"></div>
          <div class="step-item" [class.active]="currentStep() >= 2" [class.completed]="currentStep() > 2">
            <span class="step-number">2</span>
            <span class="step-label">Shipping</span>
          </div>
          <div class="step-line" [class.completed]="currentStep() > 2"></div>
          <div class="step-item" [class.active]="currentStep() >= 3" [class.completed]="currentStep() > 3">
            <span class="step-number">3</span>
            <span class="step-label">Payment</span>
          </div>
          <div class="step-line" [class.completed]="currentStep() > 3"></div>
          <div class="step-item" [class.active]="currentStep() >= 4">
            <span class="step-number">4</span>
            <span class="step-label">Confirm</span>
          </div>
        </div>

        <div class="checkout-layout">
          <!-- Main Content -->
          <div class="checkout-main">
            @switch (currentStep()) {
              @case (1) {
                <!-- Step 1: Review Cart -->
                <div class="checkout-section">
                  <h2>Review Your Cart</h2>
                  <div class="cart-review">
                    @for (item of cartService.items(); track item.productId) {
                      <div class="review-item">
                        <img [src]="item.productImage || '/assets/images/placeholder.png'" [alt]="item.productName">
                        <div class="review-item-details">
                          <span class="item-name">{{ item.productName }}</span>
                          <span class="item-qty">Qty: {{ item.quantity }}</span>
                        </div>
                        <span class="item-price">{{ item.totalPrice | currency }}</span>
                      </div>
                    }
                  </div>
                  <div class="section-actions">
                    <a routerLink="/cart" class="btn btn-secondary">Edit Cart</a>
                    <button class="btn btn-primary" (click)="nextStep()">Continue to Shipping</button>
                  </div>
                </div>
              }

              @case (2) {
                <!-- Step 2: Shipping -->
                <div class="checkout-section">
                  <h2>Shipping Information</h2>
                  <form [formGroup]="shippingForm">
                    <div class="form-grid">
                      <div class="form-group">
                        <label for="fullName" class="form-label">Full Name</label>
                        <input type="text" id="fullName" formControlName="fullName" class="form-input" 
                          [class.error]="shippingForm.get('fullName')?.invalid && shippingForm.get('fullName')?.touched"
                          placeholder="John Doe">
                        @if (shippingForm.get('fullName')?.invalid && shippingForm.get('fullName')?.touched) {
                          <span class="form-error">Full name is required</span>
                        }
                      </div>
                      <div class="form-group">
                        <label for="phone" class="form-label">Phone Number</label>
                        <input type="tel" id="phone" formControlName="phoneNumber" class="form-input"
                          [class.error]="shippingForm.get('phoneNumber')?.invalid && shippingForm.get('phoneNumber')?.touched"
                          placeholder="+1 234 567 8900">
                        @if (shippingForm.get('phoneNumber')?.invalid && shippingForm.get('phoneNumber')?.touched) {
                          <span class="form-error">Phone number is required</span>
                        }
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="street" class="form-label">Street Address</label>
                      <input type="text" id="street" formControlName="streetAddress" class="form-input"
                        [class.error]="shippingForm.get('streetAddress')?.invalid && shippingForm.get('streetAddress')?.touched"
                        placeholder="123 Main St, Apt 4B">
                      @if (shippingForm.get('streetAddress')?.invalid && shippingForm.get('streetAddress')?.touched) {
                        <span class="form-error">Street address is required</span>
                      }
                    </div>
                    <div class="form-grid form-grid-3">
                      <div class="form-group">
                        <label for="city" class="form-label">City</label>
                        <input type="text" id="city" formControlName="city" class="form-input"
                          [class.error]="shippingForm.get('city')?.invalid && shippingForm.get('city')?.touched"
                          placeholder="New York">
                      </div>
                      <div class="form-group">
                        <label for="state" class="form-label">State</label>
                        <input type="text" id="state" formControlName="state" class="form-input"
                          placeholder="NY">
                      </div>
                      <div class="form-group">
                        <label for="zip" class="form-label">ZIP Code</label>
                        <input type="text" id="zip" formControlName="zipCode" class="form-input"
                          [class.error]="shippingForm.get('zipCode')?.invalid && shippingForm.get('zipCode')?.touched"
                          placeholder="10001">
                      </div>
                    </div>
                    <div class="form-group">
                      <label for="country" class="form-label">Country</label>
                      <select id="country" formControlName="country" class="form-input">
                        <option value="US">United States</option>
                        <option value="CA">Canada</option>
                        <option value="UK">United Kingdom</option>
                      </select>
                    </div>
                  </form>
                  <div class="section-actions">
                    <button class="btn btn-secondary" (click)="prevStep()">Back</button>
                    <button class="btn btn-primary" (click)="nextStep()" [disabled]="shippingForm.invalid">
                      Continue to Payment
                    </button>
                  </div>
                </div>
              }

              @case (3) {
                <!-- Step 3: Payment -->
                <div class="checkout-section">
                  <h2>Payment Method</h2>
                  <form [formGroup]="paymentForm">
                    <div class="payment-methods">
                      <label class="payment-method" [class.selected]="paymentMethod() === 'CREDIT_CARD'">
                        <input type="radio" formControlName="paymentMethod" value="CREDIT_CARD">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                          <line x1="1" y1="10" x2="23" y2="10"></line>
                        </svg>
                        <span>Credit Card</span>
                      </label>
                      <label class="payment-method" [class.selected]="paymentMethod() === 'DEBIT_CARD'">
                        <input type="radio" formControlName="paymentMethod" value="DEBIT_CARD">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                          <line x1="1" y1="10" x2="23" y2="10"></line>
                        </svg>
                        <span>Debit Card</span>
                      </label>
                    </div>

                    <div class="card-details">
                      <div class="form-group">
                        <label for="cardNumber" class="form-label">Card Number</label>
                        <input type="text" id="cardNumber" formControlName="cardNumber" class="form-input"
                          placeholder="1234 5678 9012 3456">
                      </div>
                      <div class="form-grid">
                        <div class="form-group">
                          <label for="expiry" class="form-label">Expiry Date</label>
                          <input type="text" id="expiry" formControlName="expiry" class="form-input"
                            placeholder="MM/YY">
                        </div>
                        <div class="form-group">
                          <label for="cvv" class="form-label">CVV</label>
                          <input type="text" id="cvv" formControlName="cvv" class="form-input"
                            placeholder="123">
                        </div>
                      </div>
                    </div>
                  </form>
                  <div class="section-actions">
                    <button class="btn btn-secondary" (click)="prevStep()">Back</button>
                    <button class="btn btn-primary" (click)="placeOrder()" [disabled]="processing()">
                      @if (processing()) {
                        <span class="spinner spinner-sm"></span>
                        Processing...
                      } @else {
                        Place Order
                      }
                    </button>
                  </div>
                </div>
              }

              @case (4) {
                <!-- Step 4: Confirmation -->
                <div class="checkout-section confirmation">
                  <div class="success-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                      <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                  </div>
                  <h2>Order Placed Successfully!</h2>
                  <p>Thank you for your order. Your order ID is: <strong>{{ orderId() }}</strong></p>
                  <p>You will receive a confirmation email shortly.</p>
                  <div class="confirmation-actions">
                    <a [routerLink]="['/checkout/success', orderId()]" class="btn btn-primary">
                      View Order Details
                    </a>
                    <a routerLink="/products" class="btn btn-secondary">
                      Continue Shopping
                    </a>
                  </div>
                </div>
              }
            }
          </div>

          <!-- Order Summary Sidebar -->
          <aside class="order-summary">
            <h3>Order Summary</h3>
            <div class="summary-items">
              @for (item of cartService.items().slice(0, 3); track item.productId) {
                <div class="summary-item">
                  <img [src]="item.productImage || '/assets/images/placeholder.png'" [alt]="item.productName">
                  <span class="item-name">{{ item.productName }} x{{ item.quantity }}</span>
                  <span class="item-price">{{ item.totalPrice | currency }}</span>
                </div>
              }
              @if (cartService.items().length > 3) {
                <p class="more-items">+{{ cartService.items().length - 3 }} more items</p>
              }
            </div>
            <div class="summary-totals">
              <div class="total-row">
                <span>Subtotal</span>
                <span>{{ cartService.subtotal() | currency }}</span>
              </div>
              <div class="total-row">
                <span>Shipping</span>
                <span>{{ cartService.shippingCost() === 0 ? 'FREE' : (cartService.shippingCost() | currency) }}</span>
              </div>
              <div class="total-row">
                <span>Tax</span>
                <span>{{ cartService.taxAmount() | currency }}</span>
              </div>
              <div class="total-row grand-total">
                <span>Total</span>
                <span>{{ cartService.grandTotal() | currency }}</span>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .checkout-page {
      padding: 2rem 0;
    }

    .checkout-page h1 {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 2rem;
    }

    .checkout-layout {
      display: grid;
      grid-template-columns: 1fr 380px;
      gap: 2rem;
      align-items: start;
    }

    /* Step Indicator */
    .step-indicator {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 3rem;
    }

    .step-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .step-number {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-tertiary);
      border-radius: 50%;
      font-weight: 600;
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .step-item.active .step-number {
      background: var(--accent-primary);
      color: white;
    }

    .step-item.completed .step-number {
      background: var(--accent-success);
      color: white;
    }

    .step-label {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .step-item.active .step-label {
      color: var(--text-primary);
      font-weight: 500;
    }

    .step-line {
      width: 60px;
      height: 2px;
      background: var(--border);
      margin: 0 1rem;
    }

    .step-line.completed {
      background: var(--accent-success);
    }

    /* Checkout Section */
    .checkout-section {
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      padding: 2rem;
    }

    .checkout-section h2 {
      font-size: 1.25rem;
      font-weight: 600;
      margin-bottom: 1.5rem;
    }

    .cart-review {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .review-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      background: var(--bg-tertiary);
      border-radius: 0.5rem;
    }

    .review-item img {
      width: 60px;
      height: 60px;
      object-fit: cover;
      border-radius: 0.25rem;
    }

    .review-item-details {
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

    .section-actions {
      display: flex;
      justify-content: space-between;
      margin-top: 2rem;
      padding-top: 1.5rem;
      border-top: 1px solid var(--border);
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }

    .form-grid-3 {
      grid-template-columns: 2fr 1fr 1fr;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      margin-bottom: 1rem;
    }

    .payment-methods {
      display: flex;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .payment-method {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem;
      background: var(--bg-tertiary);
      border: 2px solid var(--border);
      border-radius: 0.5rem;
      cursor: pointer;
      transition: all 0.2s;
    }

    .payment-method:hover {
      border-color: var(--border-hover);
    }

    .payment-method.selected {
      border-color: var(--accent-primary);
      background: rgba(99, 102, 241, 0.05);
    }

    .payment-method input {
      display: none;
    }

    .payment-method svg {
      width: 24px;
      height: 24px;
      color: var(--text-muted);
    }

    .payment-method.selected svg {
      color: var(--accent-primary);
    }

    .card-details {
      padding-top: 1rem;
      border-top: 1px solid var(--border);
    }

    /* Confirmation */
    .confirmation {
      text-align: center;
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

    .confirmation h2 {
      font-size: 1.5rem;
      margin-bottom: 1rem;
    }

    .confirmation p {
      color: var(--text-secondary);
      margin-bottom: 0.5rem;
    }

    .confirmation-actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      margin-top: 2rem;
    }

    /* Order Summary Sidebar */
    .order-summary {
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      padding: 1.5rem;
      position: sticky;
      top: 100px;
    }

    .order-summary h3 {
      font-size: 1rem;
      font-weight: 600;
      margin-bottom: 1rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid var(--border);
    }

    .summary-items {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      margin-bottom: 1rem;
    }

    .summary-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .summary-item img {
      width: 48px;
      height: 48px;
      object-fit: cover;
      border-radius: 0.25rem;
    }

    .summary-item .item-name {
      flex: 1;
      font-size: 0.875rem;
    }

    .summary-item .item-price {
      font-size: 0.875rem;
    }

    .more-items {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .summary-totals {
      padding-top: 1rem;
      border-top: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .total-row {
      display: flex;
      justify-content: space-between;
      font-size: 0.9375rem;
    }

    .total-row span:first-child {
      color: var(--text-secondary);
    }

    .total-row.grand-total {
      padding-top: 0.75rem;
      margin-top: 0.5rem;
      border-top: 1px solid var(--border);
      font-weight: 700;
      font-size: 1.125rem;
    }

    .total-row.grand-total span:last-child {
      color: var(--accent-primary);
    }

    @media (max-width: 1024px) {
      .checkout-layout {
        grid-template-columns: 1fr;
      }

      .order-summary {
        position: static;
        order: -1;
      }
    }
  `]
})
export class CheckoutComponent {
  protected readonly cartService = inject(CartService);
  private readonly orderService = inject(OrderService);
  private readonly paymentService = inject(PaymentService);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  currentStep = signal(1);
  paymentMethod = signal<'CREDIT_CARD' | 'DEBIT_CARD'>('CREDIT_CARD');
  processing = signal(false);
  orderId = signal<string>('');

  shippingForm: FormGroup;
  paymentForm: FormGroup;

  constructor() {
    this.shippingForm = this.fb.group({
      fullName: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      streetAddress: ['', Validators.required],
      city: ['', Validators.required],
      state: [''],
      zipCode: ['', Validators.required],
      country: ['US']
    });

    this.paymentForm = this.fb.group({
      paymentMethod: ['CREDIT_CARD'],
      cardNumber: [''],
      expiry: [''],
      cvv: ['']
    });
  }

  nextStep(): void {
    this.currentStep.update(s => s + 1);
    window.scrollTo(0, 0);
  }

  prevStep(): void {
    this.currentStep.update(s => s - 1);
    window.scrollTo(0, 0);
  }

  placeOrder(): void {
    this.processing.set(true);

    // Simulate order placement
    const orderId = 'ORD-' + Date.now();
    this.orderId.set(orderId);
    
    // Clear cart
    this.cartService.clearCart();
    
    // Move to confirmation
    this.processing.set(false);
    this.nextStep();
  }
}
