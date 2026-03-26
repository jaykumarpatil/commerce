import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '@services/product.service';
import { Product, Category } from '@models/product.model';
import { ProductCardComponent } from '@shared/components/product-card/product-card.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, ProductCardComponent, LoadingSpinnerComponent],
  template: `
    <div class="home">
      <!-- Hero Section -->
      <section class="hero">
        <div class="container">
          <div class="hero-content">
            <h1 class="hero-title">
              Discover <span class="text-gradient">Premium</span> Products
            </h1>
            <p class="hero-description">
              Shop the latest collection of high-quality products at unbeatable prices. 
              Free shipping on orders over $50.
            </p>
            <div class="hero-actions">
              <a routerLink="/products" class="btn btn-primary btn-lg">
                Shop Now
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                  <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
              </a>
              <a routerLink="/products?featured=true" class="btn btn-secondary btn-lg">
                View Featured
              </a>
            </div>
          </div>
          <div class="hero-image">
            <div class="hero-image-bg"></div>
            <div class="hero-stats">
              <div class="stat">
                <span class="stat-value">10k+</span>
                <span class="stat-label">Products</span>
              </div>
              <div class="stat">
                <span class="stat-value">50k+</span>
                <span class="stat-label">Customers</span>
              </div>
              <div class="stat">
                <span class="stat-value">4.9</span>
                <span class="stat-label">Rating</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Categories Section -->
      <section class="categories">
        <div class="container">
          <div class="section-header">
            <h2>Shop by Category</h2>
            <a routerLink="/products" class="view-all">View All</a>
          </div>
          <div class="category-grid">
            @for (category of categories(); track category.categoryId) {
              <a [routerLink]="['/products']" [queryParams]="{categoryId: category.categoryId}" class="category-card">
                <div class="category-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="7" height="7"></rect>
                    <rect x="14" y="3" width="7" height="7"></rect>
                    <rect x="14" y="14" width="7" height="7"></rect>
                    <rect x="3" y="14" width="7" height="7"></rect>
                  </svg>
                </div>
                <span class="category-name">{{ category.name }}</span>
              </a>
            } @empty {
              @for (i of [1, 2, 3, 4, 5, 6] ; track i) {
                <div class="category-card skeleton-card">
                  <div class="skeleton" style="width: 48px; height: 48px;"></div>
                  <div class="skeleton" style="width: 80px; height: 20px; margin-top: 12px;"></div>
                </div>
              }
            }
          </div>
        </div>
      </section>

      <!-- Featured Products Section -->
      <section class="featured-products">
        <div class="container">
          <div class="section-header">
            <h2>Featured Products</h2>
            <a routerLink="/products?featured=true" class="view-all">View All</a>
          </div>
          
          @if (loading()) {
            <app-loading-spinner [overlay]="false" message="Loading products..." />
          } @else {
            <div class="product-grid">
              @for (product of featuredProducts(); track product.productId) {
                <app-product-card [product]="product" />
              } @empty {
                @for (i of [1, 2, 3, 4] ; track i) {
                  <div class="product-skeleton">
                    <div class="skeleton" style="aspect-ratio: 1;"></div>
                    <div style="padding: 1rem;">
                      <div class="skeleton" style="width: 100%; height: 20px; margin-bottom: 8px;"></div>
                      <div class="skeleton" style="width: 60%; height: 20px;"></div>
                    </div>
                  </div>
                }
              }
            </div>
          }
        </div>
      </section>

      <!-- Benefits Section -->
      <section class="benefits">
        <div class="container">
          <div class="benefits-grid">
            <div class="benefit-card">
              <div class="benefit-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="1" y="3" width="15" height="13"></rect>
                  <polygon points="16 8 20 8 23 11 23 16 16 16 16 8"></polygon>
                  <circle cx="5.5" cy="18.5" r="2.5"></circle>
                  <circle cx="18.5" cy="18.5" r="2.5"></circle>
                </svg>
              </div>
              <h3>Free Shipping</h3>
              <p>On orders over $50</p>
            </div>
            <div class="benefit-card">
              <div class="benefit-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
              </div>
              <h3>Secure Payment</h3>
              <p>100% secure checkout</p>
            </div>
            <div class="benefit-card">
              <div class="benefit-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10"></polyline>
                  <polyline points="1 20 1 14 7 14"></polyline>
                  <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"></path>
                </svg>
              </div>
              <h3>Easy Returns</h3>
              <p>30-day return policy</p>
            </div>
            <div class="benefit-card">
              <div class="benefit-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                </svg>
              </div>
              <h3>24/7 Support</h3>
              <p>Dedicated support team</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  `,
  styles: [`
    .home {
      min-height: 100%;
    }

    /* Hero Section */
    .hero {
      padding: 4rem 0;
      background: linear-gradient(180deg, var(--bg-secondary) 0%, var(--bg-primary) 100%);
    }

    .hero .container {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 4rem;
      align-items: center;
    }

    .hero-title {
      font-size: 3.5rem;
      font-weight: 800;
      line-height: 1.1;
      margin-bottom: 1.5rem;
    }

    .text-gradient {
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .hero-description {
      font-size: 1.125rem;
      color: var(--text-secondary);
      line-height: 1.7;
      margin-bottom: 2rem;
    }

    .hero-actions {
      display: flex;
      gap: 1rem;
    }

    .hero-actions .btn svg {
      width: 20px;
      height: 20px;
    }

    .hero-image {
      position: relative;
    }

    .hero-image-bg {
      aspect-ratio: 1;
      background: var(--gradient-primary);
      border-radius: 2rem;
      opacity: 0.1;
      position: absolute;
      inset: 0;
      transform: rotate(6deg);
    }

    .hero-stats {
      position: relative;
      display: flex;
      justify-content: center;
      gap: 3rem;
      padding: 2rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 1rem;
    }

    .stat {
      text-align: center;
    }

    .stat-value {
      display: block;
      font-size: 2rem;
      font-weight: 700;
      color: var(--text-primary);
    }

    .stat-label {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    /* Categories Section */
    .categories {
      padding: 4rem 0;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .section-header h2 {
      font-size: 1.75rem;
      font-weight: 700;
    }

    .view-all {
      color: var(--accent-primary);
      font-weight: 500;
    }

    .category-grid {
      display: grid;
      grid-template-columns: repeat(6, 1fr);
      gap: 1rem;
    }

    .category-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 1.5rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      text-decoration: none;
      transition: all 0.3s ease;
    }

    .category-card:hover {
      background: var(--bg-tertiary);
      border-color: var(--accent-primary);
      transform: translateY(-2px);
    }

    .category-icon {
      width: 48px;
      height: 48px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-tertiary);
      border-radius: 0.5rem;
      color: var(--accent-primary);
    }

    .category-icon svg {
      width: 24px;
      height: 24px;
    }

    .category-name {
      margin-top: 0.75rem;
      font-weight: 500;
      color: var(--text-primary);
      font-size: 0.875rem;
    }

    .skeleton-card {
      pointer-events: none;
    }

    /* Featured Products Section */
    .featured-products {
      padding: 4rem 0;
      background: var(--bg-secondary);
    }

    .product-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1.5rem;
    }

    .product-skeleton {
      background: var(--bg-tertiary);
      border-radius: 0.75rem;
      overflow: hidden;
    }

    .product-skeleton .skeleton {
      border-radius: 0;
    }

    /* Benefits Section */
    .benefits {
      padding: 4rem 0;
    }

    .benefits-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 2rem;
    }

    .benefit-card {
      text-align: center;
      padding: 2rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
    }

    .benefit-icon {
      width: 56px;
      height: 56px;
      margin: 0 auto 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(99, 102, 241, 0.1);
      border-radius: 0.75rem;
      color: var(--accent-primary);
    }

    .benefit-icon svg {
      width: 28px;
      height: 28px;
    }

    .benefit-card h3 {
      font-weight: 600;
      margin-bottom: 0.25rem;
    }

    .benefit-card p {
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    @media (max-width: 1024px) {
      .hero .container {
        grid-template-columns: 1fr;
        gap: 2rem;
      }

      .hero-title {
        font-size: 2.5rem;
      }

      .category-grid {
        grid-template-columns: repeat(3, 1fr);
      }

      .product-grid {
        grid-template-columns: repeat(3, 1fr);
      }

      .benefits-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    @media (max-width: 640px) {
      .hero-title {
        font-size: 2rem;
      }

      .hero-actions {
        flex-direction: column;
      }

      .hero-stats {
        flex-direction: column;
        gap: 1rem;
      }

      .category-grid {
        grid-template-columns: repeat(2, 1fr);
      }

      .product-grid {
        grid-template-columns: repeat(2, 1fr);
      }

      .benefits-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class HomeComponent implements OnInit {
  private readonly productService = inject(ProductService);

  featuredProducts = signal<Product[]>([]);
  categories = signal<Category[]>([]);
  loading = signal(true);

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.productService.getFeaturedProducts(8).subscribe({
      next: (products) => {
        this.featuredProducts.set(products);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });

    this.productService.getCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
      }
    });
  }
}
