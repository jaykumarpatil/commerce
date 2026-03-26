import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ProductService } from '@services/product.service';
import { Product, Category } from '@models/product.model';
import { ProductCardComponent } from '@shared/components/product-card/product-card.component';
import { LoadingSpinnerComponent } from '@shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, FormsModule, ProductCardComponent, LoadingSpinnerComponent],
  template: `
    <div class="product-list-page">
      <div class="container">
        <div class="page-header">
          <h1>Products</h1>
          <p class="results-count">
            @if (!loading()) {
              {{ totalElements() }} products found
            }
          </p>
        </div>

        <div class="content-grid">
          <!-- Filters Sidebar -->
          <aside class="filters-sidebar">
            <div class="filter-section">
              <h3>Search</h3>
              <form [formGroup]="searchForm" (ngSubmit)="onSearch()">
                <div class="search-input-wrapper">
                  <input 
                    type="text" 
                    formControlName="query"
                    placeholder="Search products..."
                    class="form-input"
                  >
                  <button type="submit" class="search-btn">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="11" cy="11" r="8"></circle>
                      <path d="m21 21-4.35-4.35"></path>
                    </svg>
                  </button>
                </div>
              </form>
            </div>

            <div class="filter-section">
              <h3>Categories</h3>
              <div class="category-list">
                <button 
                  class="category-item"
                  [class.active]="!selectedCategory()"
                  (click)="selectCategory(null)"
                >
                  All Products
                </button>
                @for (category of categories(); track category.categoryId) {
                  <button 
                    class="category-item"
                    [class.active]="selectedCategory() === category.categoryId"
                    (click)="selectCategory(category.categoryId)"
                  >
                    {{ category.name }}
                  </button>
                }
              </div>
            </div>

            <div class="filter-section">
              <h3>Price Range</h3>
              <div class="price-inputs">
                <input 
                  type="number" 
                  [(ngModel)]="minPrice" 
                  placeholder="Min"
                  class="form-input"
                  (change)="onPriceChange()"
                >
                <span>to</span>
                <input 
                  type="number" 
                  [(ngModel)]="maxPrice" 
                  placeholder="Max"
                  class="form-input"
                  (change)="onPriceChange()"
                >
              </div>
            </div>

            <button class="btn btn-secondary btn-block" (click)="clearFilters()">
              Clear Filters
            </button>
          </aside>

          <!-- Products Grid -->
          <main class="products-main">
            <!-- Sort & View Options -->
            <div class="toolbar">
              <div class="sort-select">
                <label>Sort by:</label>
                <select [(ngModel)]="sortBy" (change)="onSortChange()" class="form-input">
                  <option value="">Featured</option>
                  <option value="price_asc">Price: Low to High</option>
                  <option value="price_desc">Price: High to Low</option>
                  <option value="name_asc">Name: A-Z</option>
                  <option value="newest">Newest First</option>
                </select>
              </div>
            </div>

            <!-- Products -->
            @if (loading()) {
              <div class="loading-container">
                <app-loading-spinner [overlay]="false" message="Loading products..." />
              </div>
            } @else if (products().length === 0) {
              <div class="empty-state">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
                <h3>No Products Found</h3>
                <p>Try adjusting your filters or search query</p>
                <button class="btn btn-primary" (click)="clearFilters()">Clear Filters</button>
              </div>
            } @else {
              <div class="product-grid">
                @for (product of products(); track product.productId) {
                  <app-product-card [product]="product" />
                }
              </div>

              <!-- Pagination -->
              @if (totalPages() > 1) {
                <div class="pagination">
                  <button 
                    class="pagination-btn"
                    [disabled]="currentPage() === 0"
                    (click)="previousPage()"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="15 18 9 12 15 6"></polyline>
                    </svg>
                  </button>

                  @for (page of visiblePages(); track page) {
                    @if (page === -1) {
                      <span class="pagination-ellipsis">...</span>
                    } @else {
                      <button 
                        class="pagination-btn"
                        [class.active]="currentPage() === page"
                        (click)="goToPage(page)"
                      >
                        {{ page + 1 }}
                      </button>
                    }
                  }

                  <button 
                    class="pagination-btn"
                    [disabled]="currentPage() >= totalPages() - 1"
                    (click)="nextPage()"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="9 18 15 12 9 6"></polyline>
                    </svg>
                  </button>
                </div>
              }
            }
          </main>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .product-list-page {
      padding: 2rem 0;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .page-header h1 {
      font-size: 2rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
    }

    .results-count {
      color: var(--text-muted);
      font-size: 0.875rem;
    }

    .content-grid {
      display: grid;
      grid-template-columns: 280px 1fr;
      gap: 2rem;
    }

    /* Filters Sidebar */
    .filters-sidebar {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .filter-section {
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      padding: 1.25rem;
    }

    .filter-section h3 {
      font-size: 0.875rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--text-secondary);
      margin-bottom: 1rem;
    }

    .search-input-wrapper {
      position: relative;
    }

    .search-input-wrapper .form-input {
      padding-right: 2.5rem;
    }

    .search-btn {
      position: absolute;
      right: 0.75rem;
      top: 50%;
      transform: translateY(-50%);
      color: var(--text-muted);
    }

    .search-btn:hover {
      color: var(--text-primary);
    }

    .search-btn svg {
      width: 18px;
      height: 18px;
    }

    .category-list {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .category-item {
      text-align: left;
      padding: 0.625rem 0.75rem;
      border-radius: 0.5rem;
      color: var(--text-secondary);
      font-size: 0.875rem;
      transition: all 0.2s;
    }

    .category-item:hover {
      background: var(--bg-tertiary);
      color: var(--text-primary);
    }

    .category-item.active {
      background: rgba(99, 102, 241, 0.1);
      color: var(--accent-primary);
      font-weight: 500;
    }

    .price-inputs {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .price-inputs input {
      flex: 1;
      padding: 0.5rem 0.75rem;
    }

    .price-inputs span {
      color: var(--text-muted);
      font-size: 0.875rem;
    }

    /* Products Main */
    .products-main {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    .toolbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 1.25rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
    }

    .sort-select {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .sort-select label {
      font-size: 0.875rem;
      color: var(--text-secondary);
    }

    .sort-select select {
      min-width: 180px;
      padding: 0.5rem 0.75rem;
    }

    .loading-container {
      display: flex;
      justify-content: center;
      padding: 4rem 0;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem 2rem;
      text-align: center;
    }

    .empty-state svg {
      width: 64px;
      height: 64px;
      color: var(--text-muted);
      margin-bottom: 1.5rem;
    }

    .empty-state h3 {
      font-size: 1.25rem;
      margin-bottom: 0.5rem;
    }

    .empty-state p {
      color: var(--text-muted);
      margin-bottom: 1.5rem;
    }

    .product-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 1.5rem;
    }

    /* Pagination */
    .pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 0.5rem;
      margin-top: 2rem;
    }

    .pagination-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 40px;
      height: 40px;
      padding: 0 0.75rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      color: var(--text-secondary);
      font-weight: 500;
      transition: all 0.2s;
    }

    .pagination-btn:hover:not(:disabled) {
      background: var(--bg-tertiary);
      border-color: var(--border-hover);
      color: var(--text-primary);
    }

    .pagination-btn.active {
      background: var(--accent-primary);
      border-color: var(--accent-primary);
      color: white;
    }

    .pagination-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .pagination-btn svg {
      width: 18px;
      height: 18px;
    }

    .pagination-ellipsis {
      color: var(--text-muted);
      padding: 0 0.5rem;
    }

    @media (max-width: 1024px) {
      .content-grid {
        grid-template-columns: 1fr;
      }

      .filters-sidebar {
        flex-direction: row;
        flex-wrap: wrap;
      }

      .filter-section {
        flex: 1;
        min-width: 200px;
      }

      .product-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }

    @media (max-width: 640px) {
      .product-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ProductListComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  products = this.productService.products;
  categories = this.productService.categories;
  loading = this.productService.loading;
  totalElements = this.productService.totalElements;
  totalPages = this.productService.totalPages;
  currentPage = this.productService.currentPage;
  selectedCategory = this.productService.selectedCategory;

  searchForm: FormGroup;
  sortBy = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;

  visiblePages = computed(() => {
    const total = this.totalPages();
    const current = this.currentPage();
    const pages: number[] = [];
    
    if (total <= 7) {
      for (let i = 0; i < total; i++) pages.push(i);
    } else {
      if (current < 4) {
        for (let i = 0; i < 5; i++) pages.push(i);
        pages.push(-1);
        pages.push(total - 1);
      } else if (current > total - 4) {
        pages.push(0);
        pages.push(-1);
        for (let i = total - 5; i < total; i++) pages.push(i);
      } else {
        pages.push(0);
        pages.push(-1);
        for (let i = current - 1; i <= current + 1; i++) pages.push(i);
        pages.push(-1);
        pages.push(total - 1);
      }
    }
    
    return pages;
  });

  constructor() {
    this.searchForm = this.fb.group({
      query: ['']
    });
  }

  ngOnInit(): void {
    // Load categories
    this.productService.getCategories().subscribe();

    // Check for query params
    this.route.queryParams.subscribe(params => {
      if (params['query']) {
        this.searchForm.patchValue({ query: params['query'] });
        this.productService.searchProducts(params['query']);
      } else if (params['categoryId']) {
        this.productService.getProductsByCategory(params['categoryId']);
      } else if (params['featured']) {
        this.productService.getFeaturedProducts();
      } else {
        this.productService.getProducts();
      }
    });
  }

  onSearch(): void {
    const query = this.searchForm.get('query')?.value;
    if (query) {
      this.productService.searchProducts(query);
      this.router.navigate([], { queryParams: { query } });
    }
  }

  selectCategory(categoryId: string | null): void {
    if (categoryId) {
      this.productService.getProductsByCategory(categoryId);
      this.router.navigate([], { queryParams: { categoryId } });
    } else {
      this.productService.getProducts();
      this.router.navigate([], { queryParams: {} });
    }
  }

  onSortChange(): void {
    // Implement sorting
  }

  onPriceChange(): void {
    // Implement price filter
  }

  clearFilters(): void {
    this.searchForm.reset();
    this.sortBy = '';
    this.minPrice = null;
    this.maxPrice = null;
    this.productService.clearFilters();
    this.productService.getProducts();
    this.router.navigate([], { queryParams: {} });
  }

  previousPage(): void {
    this.productService.previousPage();
  }

  nextPage(): void {
    this.productService.nextPage();
  }

  goToPage(page: number): void {
    this.productService.goToPage(page);
  }
}
