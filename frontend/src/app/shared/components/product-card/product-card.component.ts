import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Product } from '@models/product.model';
import { CartService } from '@services/cart.service';
import { ToastService } from '../toast/toast.component';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <article class="product-card" [class.featured]="product().featured">
      <a [routerLink]="['/products', product().productId]" class="card-link">
        <div class="card-image">
          <img 
            [src]="product().imageUrl || product().mainImage || '/assets/images/placeholder.png'" 
            [alt]="product().name"
            loading="lazy"
          >
          @if (product().discountPercent && product().discountPercent > 0) {
            <span class="badge badge-danger discount-badge">
              -{{ product().discountPercent }}%
            </span>
          }
          @if (product().featured) {
            <span class="badge badge-primary featured-badge">Featured</span>
          }
          @if (!isInStock()) {
            <div class="out-of-stock-overlay">
              <span>Out of Stock</span>
            </div>
          }
        </div>
        <div class="card-body">
          <h3 class="product-name">{{ product().name }}</h3>
          @if (product().shortDescription) {
            <p class="product-description">{{ product().shortDescription }}</p>
          }
          <div class="product-price">
            <span class="price-current">{{ product().price | currency }}</span>
            @if (product().originalPrice && product().originalPrice > product().price) {
              <span class="price-original">{{ product().originalPrice | currency }}</span>
            }
          </div>
        </div>
      </a>
      <div class="card-footer">
        <button 
          class="btn btn-primary btn-add-cart"
          [disabled]="!isInStock()"
          (click)="onAddToCart($event)"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="21" r="1"></circle>
            <circle cx="20" cy="21" r="1"></circle>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
          </svg>
          Add to Cart
        </button>
      </div>
    </article>
  `,
  styles: [`
    .product-card {
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      overflow: hidden;
      transition: all 0.3s ease;
      display: flex;
      flex-direction: column;
    }

    .product-card:hover {
      background: var(--bg-tertiary);
      border-color: var(--accent-primary);
      transform: translateY(-4px);
      box-shadow: 0 12px 40px rgba(99, 102, 241, 0.15);
    }

    .product-card.featured {
      border-color: var(--accent-primary);
    }

    .card-link {
      display: block;
      text-decoration: none;
      color: inherit;
    }

    .card-image {
      position: relative;
      aspect-ratio: 1;
      background: var(--bg-tertiary);
      overflow: hidden;
    }

    .card-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .product-card:hover .card-image img {
      transform: scale(1.05);
    }

    .badge {
      position: absolute;
      padding: 0.25rem 0.5rem;
      font-size: 0.7rem;
      font-weight: 600;
      border-radius: 9999px;
    }

    .badge-danger {
      top: 0.75rem;
      right: 0.75rem;
      background: var(--accent-danger);
      color: white;
    }

    .badge-primary {
      top: 0.75rem;
      left: 0.75rem;
      background: var(--accent-primary);
      color: white;
    }

    .out-of-stock-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.6);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .out-of-stock-overlay span {
      background: var(--bg-secondary);
      padding: 0.5rem 1rem;
      border-radius: 0.5rem;
      font-weight: 600;
      font-size: 0.875rem;
    }

    .card-body {
      padding: 1rem;
      flex: 1;
    }

    .product-name {
      font-weight: 600;
      font-size: 1rem;
      margin-bottom: 0.5rem;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .product-description {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 0.75rem;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .product-price {
      display: flex;
      align-items: baseline;
      gap: 0.5rem;
    }

    .price-current {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--accent-primary);
    }

    .price-original {
      font-size: 0.875rem;
      color: var(--text-muted);
      text-decoration: line-through;
    }

    .card-footer {
      padding: 1rem;
      padding-top: 0;
    }

    .btn-add-cart {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.5rem;
    }

    .btn-add-cart svg {
      width: 18px;
      height: 18px;
    }

    .btn-add-cart:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `]
})
export class ProductCardComponent {
  product = input.required<Product>();
  addToCart = output<Product>();

  constructor(
    private readonly cartService: CartService,
    private readonly toastService: ToastService
  ) {}

  isInStock(): boolean {
    const p = this.product();
    if (p.stockQuantity !== undefined && p.stockQuantity <= 0) {
      return false;
    }
    return p.inStock ?? true;
  }

  onAddToCart(event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    const p = this.product();
    
    this.cartService.addItem({
      productId: p.productId,
      productName: p.name,
      productImage: p.imageUrl || p.mainImage,
      unitPrice: p.price,
      quantity: 1,
      maxOrderQuantity: p.maxOrderQuantity
    });

    this.toastService.success('Added to Cart', `${p.name} has been added to your cart.`);
    this.addToCart.emit(p);
  }
}
