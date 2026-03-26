// Cart Models
export interface Cart {
  cartId: string;
  userId: string;
  items: CartItem[];
  subtotal: number;
  discountTotal: number;
  taxAmount: number;
  shippingCost: number;
  grandTotal: number;
  itemTotalCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  cartItemId?: string;
  productId: string;
  productName: string;
  productImage?: string;
  unitPrice: number;
  quantity: number;
  maxOrderQuantity?: number;
  inStock?: boolean;
  options?: CartItemOption[];
  discountAmount: number;
  totalPrice: number;
  createdAt?: string;
}

export interface CartItemOption {
  optionName: string;
  optionValue: string;
}

export interface AddToCartRequest {
  productId: string;
  productName: string;
  productImage?: string;
  unitPrice: number;
  quantity: number;
  maxOrderQuantity?: number;
  options?: CartItemOption[];
}

export interface UpdateCartItemRequest {
  quantity: number;
  options?: CartItemOption[];
}

// Local cart item for guest users
export interface LocalCartItem extends AddToCartRequest {
  cartItemId: string;
  discountAmount: number;
  totalPrice: number;
}
