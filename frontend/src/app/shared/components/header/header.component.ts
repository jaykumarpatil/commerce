import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '@services/auth.service';
import { CartService } from '@services/cart.service';
import { ProductService } from '@services/product.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule],
  template: `
    <header class="header">
      <div class="container">
        <div class="header-content">
          <!-- Logo -->
          <a routerLink="/" class="logo">
            <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <path d="M16 10a4 4 0 0 1-8 0"></path>
            </svg>
            <span class="logo-text">Store</span>
          </a>

          <!-- Navigation -->
          <nav class="nav">
            <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">Home</a>
            <a routerLink="/products" routerLinkActive="active">Products</a>
          </nav>

          <!-- Search -->
          <div class="search-container">
            <input 
              type="text" 
              class="search-input" 
              placeholder="Search products..."
              [(ngModel)]="searchQuery"
              (keyup.enter)="onSearch()"
            >
            <button class="search-btn" (click)="onSearch()">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
            </button>
          </div>

          <!-- Actions -->
          <div class="actions">
            <!-- Cart -->
            <div class="cart-wrapper">
              <a routerLink="/cart" class="cart-btn">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="9" cy="21" r="1"></circle>
                  <circle cx="20" cy="21" r="1"></circle>
                  <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                </svg>
                @if (cartService.itemCount() > 0) {
                  <span class="cart-badge">{{ cartService.itemCount() }}</span>
                }
              </a>
              
              <!-- Mini Cart Dropdown -->
              @if (showMiniCart()) {
                <div class="mini-cart">
                  <div class="mini-cart-header">
                    <h4>Shopping Cart</h4>
                    <span>{{ cartService.itemCount() }} items</span>
                  </div>
                  
                  @if (cartService.isEmpty()) {
                    <div class="mini-cart-empty">
                      <p>Your cart is empty</p>
                      <a routerLink="/products" class="btn btn-primary btn-sm" (click)="closeMiniCart()">
                        Start Shopping
                      </a>
                    </div>
                  } @else {
                    <div class="mini-cart-items">
                      @for (item of cartService.items().slice(0, 3); track item.productId) {
                        <div class="mini-cart-item">
                          <img [src]="item.productImage || '/assets/images/placeholder.png'" [alt]="item.productName">
                          <div class="mini-cart-item-info">
                            <span class="item-name">{{ item.productName }}</span>
                            <span class="item-qty">Qty: {{ item.quantity }}</span>
                          </div>
                          <span class="item-price">{{ item.totalPrice | currency }}</span>
                        </div>
                      }
                    </div>
                    
                    <div class="mini-cart-footer">
                      <div class="mini-cart-total">
                        <span>Total:</span>
                        <span>{{ cartService.grandTotal() | currency }}</span>
                      </div>
                      <a routerLink="/cart" class="btn btn-primary" (click)="closeMiniCart()">
                        View Cart
                      </a>
                    </div>
                  }
                </div>
              }
            </div>

            <!-- User Menu -->
            @if (authService.isAuthenticated()) {
              <div class="user-menu">
                <button class="user-btn" (click)="toggleUserMenu()">
                  <div class="avatar">
                    {{ authService.user()?.firstName?.[0] }}{{ authService.user()?.lastName?.[0] }}
                  </div>
                </button>
                
                @if (showUserMenu()) {
                  <div class="dropdown-menu">
                    <div class="dropdown-header">
                      <span class="user-name">{{ authService.userFullName() }}</span>
                      <span class="user-email">{{ authService.user()?.email }}</span>
                    </div>
                    <div class="dropdown-divider"></div>
                    <a routerLink="/profile" class="dropdown-item" (click)="closeUserMenu()">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                      </svg>
                      Profile
                    </a>
                    <a routerLink="/orders" class="dropdown-item" (click)="closeUserMenu()">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                        <polyline points="14 2 14 8 20 8"></polyline>
                        <line x1="16" y1="13" x2="8" y2="13"></line>
                        <line x1="16" y1="17" x2="8" y2="17"></line>
                      </svg>
                      My Orders
                    </a>
                    @if (authService.isAdmin()) {
                      <a routerLink="/admin" class="dropdown-item" (click)="closeUserMenu()">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="3" y="3" width="7" height="7"></rect>
                          <rect x="14" y="3" width="7" height="7"></rect>
                          <rect x="14" y="14" width="7" height="7"></rect>
                          <rect x="3" y="14" width="7" height="7"></rect>
                        </svg>
                        Admin Dashboard
                      </a>
                    }
                    <div class="dropdown-divider"></div>
                    <button class="dropdown-item danger" (click)="logout()">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                        <polyline points="16 17 21 12 16 7"></polyline>
                        <line x1="21" y1="12" x2="9" y2="12"></line>
                      </svg>
                      Logout
                    </button>
                  </div>
                }
              </div>
            } @else {
              <a routerLink="/login" class="btn btn-primary btn-sm">Login</a>
            }
          </div>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .header {
      position: sticky;
      top: 0;
      z-index: 100;
      background: rgba(13, 13, 15, 0.95);
      backdrop-filter: blur(10px);
      border-bottom: 1px solid var(--border);
    }

    .header-content {
      display: flex;
      align-items: center;
      gap: 2rem;
      height: 72px;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: var(--text-primary);
      text-decoration: none;
      font-weight: 700;
      font-size: 1.25rem;
    }

    .logo-icon {
      width: 32px;
      height: 32px;
      color: var(--accent-primary);
    }

    .nav {
      display: flex;
      gap: 1.5rem;
    }

    .nav a {
      color: var(--text-secondary);
      text-decoration: none;
      font-weight: 500;
      padding: 0.5rem 0;
      transition: color 0.2s;
    }

    .nav a:hover,
    .nav a.active {
      color: var(--text-primary);
    }

    .search-container {
      flex: 1;
      max-width: 480px;
      position: relative;
    }

    .search-input {
      width: 100%;
      padding: 0.625rem 1rem 0.625rem 3rem;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.5rem;
      color: var(--text-primary);
      font-size: 0.875rem;
      transition: all 0.2s;
    }

    .search-input::placeholder {
      color: var(--text-muted);
    }

    .search-input:focus {
      border-color: var(--accent-primary);
      box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
    }

    .search-btn {
      position: absolute;
      left: 0.75rem;
      top: 50%;
      transform: translateY(-50%);
      color: var(--text-muted);
      padding: 0.25rem;
    }

    .search-btn svg {
      width: 18px;
      height: 18px;
    }

    .actions {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .cart-wrapper,
    .user-menu {
      position: relative;
    }

    .cart-btn,
    .user-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0.5rem;
      border-radius: 0.5rem;
      color: var(--text-secondary);
      transition: all 0.2s;
      position: relative;
    }

    .cart-btn:hover,
    .user-btn:hover {
      background: var(--bg-tertiary);
      color: var(--text-primary);
    }

    .cart-btn svg {
      width: 24px;
      height: 24px;
    }

    .cart-badge {
      position: absolute;
      top: 0;
      right: 0;
      min-width: 18px;
      height: 18px;
      padding: 0 4px;
      background: var(--accent-primary);
      color: white;
      font-size: 0.7rem;
      font-weight: 600;
      border-radius: 9999px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .mini-cart {
      position: absolute;
      top: calc(100% + 0.5rem);
      right: 0;
      width: 360px;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.4);
      animation: slideDown 0.2s ease-out;
    }

    .mini-cart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
      border-bottom: 1px solid var(--border);
    }

    .mini-cart-header h4 {
      font-weight: 600;
    }

    .mini-cart-header span {
      color: var(--text-muted);
      font-size: 0.875rem;
    }

    .mini-cart-empty {
      padding: 2rem;
      text-align: center;
      color: var(--text-muted);
    }

    .mini-cart-empty p {
      margin-bottom: 1rem;
    }

    .mini-cart-items {
      max-height: 300px;
      overflow-y: auto;
      padding: 0.5rem;
    }

    .mini-cart-item {
      display: flex;
      gap: 0.75rem;
      padding: 0.75rem;
      border-radius: 0.5rem;
      transition: background 0.2s;
    }

    .mini-cart-item:hover {
      background: var(--bg-tertiary);
    }

    .mini-cart-item img {
      width: 48px;
      height: 48px;
      object-fit: cover;
      border-radius: 0.375rem;
      background: var(--bg-tertiary);
    }

    .mini-cart-item-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .item-name {
      font-size: 0.875rem;
      font-weight: 500;
    }

    .item-qty {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .item-price {
      font-weight: 600;
      font-size: 0.875rem;
    }

    .mini-cart-footer {
      padding: 1rem;
      border-top: 1px solid var(--border);
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .mini-cart-total {
      display: flex;
      justify-content: space-between;
      font-weight: 600;
    }

    .mini-cart-total span:last-child {
      color: var(--accent-primary);
    }

    .dropdown-menu {
      position: absolute;
      top: calc(100% + 0.5rem);
      right: 0;
      width: 240px;
      background: var(--bg-secondary);
      border: 1px solid var(--border);
      border-radius: 0.75rem;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.4);
      padding: 0.5rem;
      animation: slideDown 0.2s ease-out;
    }

    .dropdown-header {
      padding: 0.75rem;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .user-name {
      font-weight: 600;
    }

    .user-email {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .dropdown-divider {
      height: 1px;
      background: var(--border);
      margin: 0.5rem 0;
    }

    .dropdown-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.625rem 0.75rem;
      border-radius: 0.5rem;
      color: var(--text-secondary);
      text-decoration: none;
      font-size: 0.875rem;
      width: 100%;
      transition: all 0.2s;
    }

    .dropdown-item:hover {
      background: var(--bg-tertiary);
      color: var(--text-primary);
    }

    .dropdown-item.danger:hover {
      background: rgba(239, 68, 68, 0.1);
      color: var(--accent-danger);
    }

    .dropdown-item svg {
      width: 18px;
      height: 18px;
    }

    .avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: var(--gradient-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 600;
      font-size: 0.875rem;
    }

    @keyframes slideDown {
      from {
        opacity: 0;
        transform: translateY(-8px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @media (max-width: 768px) {
      .nav {
        display: none;
      }
      
      .search-container {
        display: none;
      }
    }
  `]
})
export class HeaderComponent {
  protected readonly authService = inject(AuthService);
  protected readonly cartService = inject(CartService);
  private readonly productService = inject(ProductService);
  
  searchQuery = '';
  showMiniCart = signal(false);
  showUserMenu = signal(false);

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.productService.searchProducts(this.searchQuery).subscribe();
    }
  }

  toggleMiniCart(): void {
    this.showMiniCart.update(v => !v);
    if (this.showUserMenu()) {
      this.showUserMenu.set(false);
    }
  }

  closeMiniCart(): void {
    this.showMiniCart.set(false);
  }

  toggleUserMenu(): void {
    this.showUserMenu.update(v => !v);
    if (this.showMiniCart()) {
      this.showMiniCart.set(false);
    }
  }

  closeUserMenu(): void {
    this.showUserMenu.set(false);
  }

  logout(): void {
    this.authService.logout();
    this.closeUserMenu();
  }
}
