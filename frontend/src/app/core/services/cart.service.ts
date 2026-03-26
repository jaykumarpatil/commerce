import { Injectable, inject, signal, computed } from '@angular/core';
import { Observable, tap, of, BehaviorSubject } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Cart, CartItem, AddToCartRequest, LocalCartItem } from '../models';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);
  
  private readonly storageKey = environment.cartStorageKey;
  
  // Signals for cart state
  private readonly _items = signal<CartItem[]>([]);
  private readonly _loading = signal<boolean>(false);
  private readonly _cart = signal<Cart | null>(null);
  
  // Public readonly signals
  readonly items = this._items.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly cart = this._cart.asReadonly();
  
  // Computed signals
  readonly itemCount = computed(() => 
    this._items().reduce((sum, item) => sum + item.quantity, 0)
  );
  
  readonly subtotal = computed(() =>
    this._items().reduce((sum, item) => sum + item.totalPrice, 0)
  );
  
  readonly taxAmount = computed(() =>
    this.subtotal() * environment.taxRate
  );
  
  readonly shippingCost = computed(() =>
    this.subtotal() >= environment.freeShippingThreshold ? 0 : environment.shippingCost
  );
  
  readonly discountTotal = computed(() =>
    this._items().reduce((sum, item) => sum + item.discountAmount, 0)
  );
  
  readonly grandTotal = computed(() =>
    this.subtotal() + this.taxAmount() + this.shippingCost() - this.discountTotal()
  );
  
  readonly isEmpty = computed(() => this._items().length === 0);
  
  // Initialize cart
  constructor() {
    this.initializeCart();
    
    // Sync cart when user logs in
    if (this.authService.isAuthenticated()) {
      this.loadCartFromServer();
    }
  }

  private initializeCart(): void {
    // Load from localStorage for guest users
    const storedItems = this.loadFromStorage();
    if (storedItems) {
      this._items.set(storedItems);
    }
  }

  private loadFromStorage(): CartItem[] {
    try {
      const stored = localStorage.getItem(this.storageKey);
      return stored ? JSON.parse(stored) : [];
    } catch {
      return [];
    }
  }

  private saveToStorage(items: CartItem[]): void {
    localStorage.setItem(this.storageKey, JSON.stringify(items));
  }

  private loadCartFromServer(): void {
    const userId = this.authService.userId();
    if (!userId) return;

    this._loading.set(true);
    this.api.get<Cart>(`/v1/carts/user/${userId}`).subscribe({
      next: (cart) => {
        this._cart.set(cart);
        this._items.set(cart.items);
        this._loading.set(false);
      },
      error: () => {
        this._loading.set(false);
      }
    });
  }

  addItem(request: AddToCartRequest): void {
    const existingIndex = this._items().findIndex(
      item => item.productId === request.productId
    );

    let newItems: CartItem[];
    
    if (existingIndex >= 0) {
      // Update existing item
      newItems = this._items().map((item, index) => {
        if (index === existingIndex) {
          const newQuantity = item.quantity + request.quantity;
          return {
            ...item,
            quantity: newQuantity,
            totalPrice: newQuantity * item.unitPrice
          };
        }
        return item;
      });
    } else {
      // Add new item
      const newItem: CartItem = {
        productId: request.productId,
        productName: request.productName,
        productImage: request.productImage,
        unitPrice: request.unitPrice,
        quantity: request.quantity,
        maxOrderQuantity: request.maxOrderQuantity,
        options: request.options,
        discountAmount: 0,
        totalPrice: request.unitPrice * request.quantity
      };
      newItems = [...this._items(), newItem];
    }

    this._items.set(newItems);
    this.saveToStorage(newItems);

    // Sync to server if logged in
    if (this.authService.isAuthenticated()) {
      this.syncToServer(newItems);
    }
  }

  updateItemQuantity(productId: string, quantity: number): void {
    if (quantity <= 0) {
      this.removeItem(productId);
      return;
    }

    const newItems = this._items().map(item => {
      if (item.productId === productId) {
        return {
          ...item,
          quantity,
          totalPrice: quantity * item.unitPrice
        };
      }
      return item;
    });

    this._items.set(newItems);
    this.saveToStorage(newItems);

    if (this.authService.isAuthenticated()) {
      this.syncToServer(newItems);
    }
  }

  removeItem(productId: string): void {
    const newItems = this._items().filter(item => item.productId !== productId);
    this._items.set(newItems);
    this.saveToStorage(newItems);

    if (this.authService.isAuthenticated()) {
      this.syncToServer(newItems);
    }
  }

  clearCart(): void {
    this._items.set([]);
    this._cart.set(null);
    localStorage.removeItem(this.storageKey);

    if (this.authService.isAuthenticated()) {
      const userId = this.authService.userId();
      if (userId) {
        this.api.delete(`/v1/carts/user/${userId}`).subscribe();
      }
    }
  }

  private syncToServer(items: CartItem[]): void {
    const userId = this.authService.userId();
    if (!userId) return;

    // Get or create cart first, then add items
    this.api.get<Cart>(`/v1/carts/user/${userId}`).subscribe({
      next: (cart) => {
        // Add items to cart
        items.forEach(item => {
          this.api.post<Cart>(`/v1/carts/${cart.cartId}/items`, item).subscribe();
        });
      },
      error: () => {
        // Cart doesn't exist, create new one
        this.api.post<Cart>('/v1/carts', { userId }).subscribe({
          next: (newCart) => {
            this._cart.set(newCart);
            items.forEach(item => {
              this.api.post<Cart>(`/v1/carts/${newCart.cartId}/items`, item).subscribe();
            });
          }
        });
      }
    });
  }

  // Get cart summary for checkout
  getCartSummary(): { subtotal: number; tax: number; shipping: number; discount: number; total: number } {
    return {
      subtotal: this.subtotal(),
      tax: this.taxAmount(),
      shipping: this.shippingCost(),
      discount: this.discountTotal(),
      total: this.grandTotal()
    };
  }

  // Calculate totals from server response
  updateCartFromServer(cart: Cart): void {
    this._cart.set(cart);
    this._items.set(cart.items);
    this.saveToStorage(cart.items);
  }
}
