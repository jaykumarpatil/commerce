import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, tap, of, map } from 'rxjs';
import { ApiService } from './api.service';
import { 
  Product, 
  Category, 
  ProductSearchParams,
  PaginatedResponse 
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly api = inject(ApiService);
  
  // Signals for state management
  private readonly _products = signal<Product[]>([]);
  private readonly _categories = signal<Category[]>([]);
  private readonly _currentProduct = signal<Product | null>(null);
  private readonly _loading = signal<boolean>(false);
  private readonly _totalElements = signal<number>(0);
  private readonly _totalPages = signal<number>(0);
  private readonly _currentPage = signal<number>(0);
  private readonly _pageSize = signal<number>(12);
  private readonly _searchQuery = signal<string>('');
  private readonly _selectedCategory = signal<string | null>(null);
  
  // Public readonly signals
  readonly products = this._products.asReadonly();
  readonly categories = this._categories.asReadonly();
  readonly currentProduct = this._currentProduct.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly totalElements = this._totalElements.asReadonly();
  readonly totalPages = this._totalPages.asReadonly();
  readonly currentPage = this._currentPage.asReadonly();
  readonly pageSize = this._pageSize.asReadonly();
  readonly searchQuery = this._searchQuery.asReadonly();
  readonly selectedCategory = this._selectedCategory.asReadonly();
  
  // Computed signals
  readonly hasProducts = computed(() => this._products().length > 0);
  readonly hasNextPage = computed(() => this._currentPage() < this._totalPages() - 1);
  readonly hasPreviousPage = computed(() => this._currentPage() > 0);
  
  // Load all products with pagination
  getProducts(page = 0, size = 12): Observable<Product[]> {
    this._loading.set(true);
    this._currentPage.set(page);
    this._pageSize.set(size);
    
    return this.api.get<PaginatedResponse<Product>>('/v1/products', {
      params: { page, size }
    }).pipe(
      tap({
        next: (response) => {
          this._products.set(response.content);
          this._totalElements.set(response.totalElements);
          this._totalPages.set(response.totalPages);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      }),
      map(response => response.content)
    );
  }

  // Get single product by ID
  getProduct(productId: string): Observable<Product> {
    this._loading.set(true);
    
    return this.api.get<Product>(`/v1/products/${productId}`).pipe(
      tap({
        next: (product) => {
          this._currentProduct.set(product);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      })
    );
  }

  // Search products
  searchProducts(query: string, page = 0, size = 12): Observable<Product[]> {
    this._loading.set(true);
    this._searchQuery.set(query);
    this._currentPage.set(page);
    
    return this.api.get<PaginatedResponse<Product>>('/v1/products/search', {
      params: { query, page, size }
    }).pipe(
      tap({
        next: (response) => {
          this._products.set(response.content);
          this._totalElements.set(response.totalElements);
          this._totalPages.set(response.totalPages);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      }),
      map(response => response.content)
    );
  }

  // Get products by category
  getProductsByCategory(categoryId: string, page = 0, size = 12): Observable<Product[]> {
    this._loading.set(true);
    this._selectedCategory.set(categoryId);
    this._currentPage.set(page);
    
    return this.api.get<PaginatedResponse<Product>>(`/v1/products/category/${categoryId}`, {
      params: { page, size }
    }).pipe(
      tap({
        next: (response) => {
          this._products.set(response.content);
          this._totalElements.set(response.totalElements);
          this._totalPages.set(response.totalPages);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      }),
      map(response => response.content)
    );
  }

  // Get all categories
  getCategories(): Observable<Category[]> {
    return this.api.get<Category[]>('/v1/categories').pipe(
      tap(categories => this._categories.set(categories))
    );
  }

  // Get single category
  getCategory(categoryId: string): Observable<Category> {
    return this.api.get<Category>(`/v1/categories/${categoryId}`);
  }

  // Get featured products
  getFeaturedProducts(limit = 8): Observable<Product[]> {
    this._loading.set(true);
    
    return this.api.get<PaginatedResponse<Product>>('/v1/products', {
      params: { featured: true, size: limit }
    }).pipe(
      tap({
        next: (response) => {
          this._products.set(response.content);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      }),
      map(response => response.content)
    );
  }

  // Advanced search with filters
  searchWithFilters(params: ProductSearchParams): Observable<Product[]> {
    this._loading.set(true);
    
    const queryParams: Record<string, string | number | boolean> = {};
    if (params.query) queryParams['query'] = params.query;
    if (params.categoryId) queryParams['categoryId'] = params.categoryId;
    if (params.page !== undefined) queryParams['page'] = params.page;
    if (params.size !== undefined) queryParams['size'] = params.size;
    if (params.sort) queryParams['sort'] = params.sort;
    if (params.minPrice !== undefined) queryParams['minPrice'] = params.minPrice;
    if (params.maxPrice !== undefined) queryParams['maxPrice'] = params.maxPrice;
    if (params.featured !== undefined) queryParams['featured'] = params.featured;
    
    return this.api.get<PaginatedResponse<Product>>('/v1/products/search', {
      params: queryParams
    }).pipe(
      tap({
        next: (response) => {
          this._products.set(response.content);
          this._totalElements.set(response.totalElements);
          this._totalPages.set(response.totalPages);
          if (params.page !== undefined) this._currentPage.set(params.page);
          this._loading.set(false);
        },
        error: () => {
          this._loading.set(false);
        }
      }),
      map(response => response.content)
    );
  }

  // Clear filters and reset
  clearFilters(): void {
    this._searchQuery.set('');
    this._selectedCategory.set(null);
    this._currentPage.set(0);
  }

  // Go to next page
  nextPage(): void {
    if (this.hasNextPage()) {
      const nextPage = this._currentPage() + 1;
      this.loadCurrentSearch(nextPage);
    }
  }

  // Go to previous page
  previousPage(): void {
    if (this.hasPreviousPage()) {
      const prevPage = this._currentPage() - 1;
      this.loadCurrentSearch(prevPage);
    }
  }

  // Go to specific page
  goToPage(page: number): void {
    if (page >= 0 && page < this._totalPages()) {
      this.loadCurrentSearch(page);
    }
  }

  // Load current search based on current filters
  private loadCurrentSearch(page: number): void {
    const query = this._searchQuery();
    const categoryId = this._selectedCategory();
    const size = this._pageSize();

    if (query) {
      this.searchProducts(query, page, size).subscribe();
    } else if (categoryId) {
      this.getProductsByCategory(categoryId, page, size).subscribe();
    } else {
      this.getProducts(page, size).subscribe();
    }
  }
}
