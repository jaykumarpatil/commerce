import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProductService } from '@services/product.service';
import { CartService } from '@services/cart.service';
import { Product } from '@models/product.model';
import { ToastService } from '@shared/components/toast/toast.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, LoadingSpinnerComponent],
  template: `
    <div class="product-detail-page">
      @if (loading()) {
        <div class="loading-container">
          <app-loading-spinner message="Loading product..." />
        </div>
      } @else if (error()) {
        <div class="error-container">
          <h2>Product Not Found</h2>
          <p>The product you're looking for doesn't exist.</p>
          <a routerLink="/products" class="btn btn-primary">Browse Products</a>
        </div>
      } @else if (product()) {
        <div class="container">
          <!-- Breadcrumbs -->
          <nav class="breadcrumbs">
            <a routerLink="/">Home</a>
            <span>/</span>
            <a routerLink="/products">Products</a>
            @if (product()?.category) {
              <span>/</span>
              <a [routerLink]="['/products']" [queryParams]="{categoryId: product()?.category?.categoryId}">
                {{ product()?.category?.name }}
              </a>
            }
            <span>/</span>
            <span>{{ product()?.name }}</span>
          </nav>

          <div class="product-content">
            <!-- Product Images -->
            <div class="product-images">
              <div class="main-image">
                <img 
                  [src]="selectedImage() || product()?.imageUrl || product()?.mainImage || '/assets/images/placeholder.png'" 
                  [alt]="product()?.name"
                >
                @if (product()?.discountPercent && product()?.discountPercent > 0) {
                  <span class="discount-badge">-{{ product()?.discountPercent }}%</span>
                }
              </div>
              @if (product()?.images && product()!.images!.length > 1) {
                <div class="thumbnail-grid">
                  @for (image of product()?.images; track image; let i = $index) {
                    <button 
                      class="thumbnail"
                      [class.active]="selectedImage() === image"
                      (click)="selectImage(image)"
                    >
                      <img [src]="image" [alt]="product()?.name">
                    </button>
                  }
                </div>
              }
            </div>

            <!-- Product Info -->
            <div class="product-info">
              <h1 class="product-title">{{ product()?.name }}</h1>
              
              @if (product()?.sku) {
                <p class="product-sku">SKU: {{ product()?.sku }}</p>
              }

              <div class="product-price">
                <span class="price-current">{{ product()?.price | currency }}</span>
                @if (product()?.originalPrice && product()!.originalPrice! > product()!.price!) {
                  <span class="price-original">{{ product()?.originalPrice | currency }}</span>
                  <span class="price-save">
                    Save {{ ((product()!.originalPrice! - product()!.price!) / product()!.originalPrice! * 100).toFixed(0) }}%
                  </span>
                }
              </div>

              @if (product()?.shortDescription) {
                <p class="product-description">{{ product()?.shortDescription }}</p>
              }

              @if (product()?.description) {
                <div class="product-details">
                  <h3>Description</h3>
                  <p>{{ product()?.description }}</p>
                </div>
              }

              <!-- Stock Status -->
              <div class="stock-status">
                @if (isInStock()) {
                  <span class="in-stock">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    In Stock
                  </span>
                  @if (product()?.stockQuantity) {
                    <span class="stock-count">{{ product()?.stockQuantity }} available</span>
                  }
                } @else {
                  <span class="out-of-stock">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="15" y1="9" x2="9" y2="15"></line>
                      <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    Out of Stock
                  </span>
                }
              </div>

              <!-- Add to Cart Form -->
              <form [formGroup]="addToCartForm" (ngSubmit)="onAddToCart()" class="add-to-cart-form">
                <div class="quantity-selector">
                  <label>Quantity:</label>
                  <div class="quantity-input">
                    <button type="button" (click)="decrementQuantity()" [disabled]="quantity() <= 1">-</button>
                    <input type="number" formControlName="quantity" [min]="1" [max]="product()?.maxOrderQuantity || 99">
                    <button type="button" (click)="incrementQuantity()" [disabled]="quantity() >= (product()?.maxOrderQuantity || 99)">+</button>
                  </div>
                </div>

                <button type="submit" class="btn btn-primary btn-lg btn-add-cart" [disabled]="!isInStock()">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="9" cy="21" r="1"></circle>
                    <circle cx="20" cy="21" r="1"></circle>
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                  </svg>
                  Add to Cart
                </button>
              </form>

              <!-- Product Meta -->
              <div class="product-meta">
                <div class="meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="1" y="3" width="15" height="13"></rect>
                    <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"></polygon>
                    <circle cx="5.5" cy="18.5" r="2.5"></circle>
                    <circle cx="18.5" cy="18.5" r="2.5"></circle>
                  </svg>
                  <span>Free shipping on orders over $50</span>
                </div>
                <div class="meta-item">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="23 4 23 10 17 10"></polyline>
                    <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
                  </svg>
                  <span>30-day easy returns</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .product-detail-page {
      padding: 2rem 0;
    }

    .loading-container,
    .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 400px;
      text-align: center;
    }

    .error-container h2 {
      margin-bottom: 0.5rem;
    }

    .error-container p {
      color: var(--text-muted);
      margin-bottom: 1.5rem;
    }

    .breadcrumbs {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 2rem;
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .breadcrumbs a {
      color: var(--text-secondary);
    }

    .breadcrumbs a:hover {
      color: var(--accent-primary);
    }

    .product-content {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 4rem;
    }

    /* Images */
    .product-images {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .main-image {
      position: relative;
      aspect-ratio: 1;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 1rem;
      overflow: hidden;
    }

    .main-image img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .discount-badge {
      position: absolute;
      top: 1rem;
      left: 1rem;
      padding: 0.5rem 1rem;
      background: var(--accent-danger);
      color: white;
      font-weight: 600;
      font-size: 0.875rem;
      border-radius: 0.5rem;
    }

    .thumbnail-grid {
      display: flex;
      gap: 0.75rem;
    }

    .thumbnail {
      width: 80px;
      height: 80px;
      padding: 0.25rem;
      background: var(--bg-secondary);
      border: 2px solid var(--border);
      border-radius: 0.5rem;
      overflow: hidden;
      transition: border-color 0.2s;
    }

    .thumbnail:hover {
      border-color: var(--border-hover);
    }

    .thumbnail.active {
      border-color: var(--accent-primary);
    }

    .thumbnail img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    /* Product Info */
    .product-info {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .product-title {
      font-size: 2rem;
      font-weight: 700;
      line-height: 1.3;
    }

    .product-sku {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .product-price {
      display: flex;
      align-items: baseline;
      gap: 0.75rem;
    }

    .price-current {
      font-size: 2rem;
      font-weight: 700;
      color: var(--accent-primary);
    }

    .price-original {
      font-size: 1.25rem;
      color: var(--text-muted);
      text-decoration: line-through;
    }

    .price-save {
      padding: 0.25rem 0.5rem;
      background: rgba(16, 185, 129, 0.1);
      color: var(--accent-success);
      font-size: 0.75rem;
      font-weight: 600;
      border-radius: 0.25rem;
    }

    .product-description {
      color: var(--text-secondary);
      line-height: 1.7;
    }

    .product-details {
      padding-top: 1.5rem;
      border-top: 1px solid var(--border);
    }

    .product-details h3 {
      font-size: 1rem;
      font-weight: 600;
      margin-bottom: 0.75rem;
    }

    .product-details p {
      color: var(--text-secondary);
      line-height: 1.7;
    }

    .stock-status {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .in-stock,
    .out-of-stock {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-weight: 500;
    }

    .in-stock {
      color: var(--accent-success);
    }

    .in-stock svg,
    .out-of-stock svg {
      width: 18px;
      height: 18px;
    }

    .out-of-stock {
      color: var(--accent-danger);
    }

    .stock-count {
      color: var(--text-muted);
      font-size: 0.875rem;
    }

    /* Add to Cart Form */
    .add-to-cart-form {
      display: flex;
      gap: 1rem;
      padding: 1.5rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
    }

    .quantity-selector {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .quantity-selector label {
      font-size: 0.875rem;
      color: var(--text-secondary);
    }

    .quantity-input {
      display: flex;
      align-items: center;
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      overflow: hidden;
    }

    .quantity-input button {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-tertiary);
      color: var(--text-primary);
      font-size: 1.25rem;
      transition: all 0.2s;
    }

    .quantity-input button:hover:not(:disabled) {
      background: var(--bg-hover);
    }

    .quantity-input button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .quantity-input input {
      width: 60px;
      height: 40px;
      text-align: center;
      border: none;
      background: transparent;
      font-size: 1rem;
      font-weight: 500;
    }

    .btn-add-cart {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.75rem;
    }

    .btn-add-cart svg {
      width: 20px;
      height: 20px;
    }

    /* Product Meta */
    .product-meta {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .meta-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      color: var(--text-secondary);
      font-size: 0.875rem;
    }

    .meta-item svg {
      width: 20px;
      height: 20px;
      color: var(--accent-primary);
    }

    @media (max-width: 1024px) {
      .product-content {
        grid-template-columns: 1fr;
        gap: 2rem;
      }
    }

    @media (max-width: 640px) {
      .add-to-cart-form {
        flex-direction: column;
      }
    }
  `]
})
export class ProductDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  private readonly toastService = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  product = signal<Product | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  selectedImage = signal<string | null>(null);
  quantity = signal(1);

  addToCartForm: FormGroup;

  constructor() {
    this.addToCartForm = this.fb.group({
      quantity: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const productId = params['id'];
      if (productId) {
        this.loadProduct(productId);
      }
    });
  }

  private loadProduct(productId: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.productService.getProduct(productId).subscribe({
      next: (product) => {
        this.product.set(product);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.message || 'Failed to load product');
      }
    });
  }

  isInStock(): boolean {
    const p = this.product();
    if (p?.stockQuantity !== undefined && p.stockQuantity <= 0) {
      return false;
    }
    return p?.inStock ?? true;
  }

  selectImage(image: string): void {
    this.selectedImage.set(image);
  }

  incrementQuantity(): void {
    const max = this.product()?.maxOrderQuantity || 99;
    if (this.quantity() < max) {
      this.quantity.update(q => q + 1);
    }
  }

  decrementQuantity(): void {
    if (this.quantity() > 1) {
      this.quantity.update(q => q - 1);
    }
  }

  onAddToCart(): void {
    const p = this.product();
    if (!p) return;

    this.cartService.addItem({
      productId: p.productId,
      productName: p.name,
      productImage: this.selectedImage() || p.imageUrl || p.mainImage,
      unitPrice: p.price,
      quantity: this.quantity(),
      maxOrderQuantity: p.maxOrderQuantity
    });

    this.toastService.success('Added to Cart', `${p.name} has been added to your cart.`);
  }
}
