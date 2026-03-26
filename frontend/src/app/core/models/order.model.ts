// Order Models
export interface Order {
  orderId: string;
  userId: string;
  cartId: string;
  items: OrderItem[];
  subtotal: number;
  discountTotal: number;
  taxAmount: number;
  shippingCost: number;
  grandTotal: number;
  status: OrderStatus;
  shippingAddress: string;
  billingAddress: string;
  paymentMethod: string;
  paymentStatus: PaymentStatus;
  trackingNumber?: string;
  carrier?: string;
  orderDate: string;
  confirmedDate?: string;
  shippedDate?: string;
  deliveredDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  orderItemId: string;
  productId: string;
  productName: string;
  productImage?: string;
  unitPrice: number;
  quantity: number;
  discountAmount: number;
  totalPrice: number;
  status: string;
}

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export interface CreateOrderRequest {
  cartId: string;
  shippingAddress: string;
  billingAddress: string;
  paymentMethod: string;
}

// Shipping Models
export interface ShippingAddress {
  addressId?: string;
  userId: string;
  fullName: string;
  phoneNumber: string;
  streetAddress: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
  isDefault?: boolean;
  createdAt?: string;
}

export interface ShippingRate {
  rateId?: string;
  orderId?: string;
  basePrice: number;
  weightPrice: number;
  distancePrice: number;
  totalAmount: number;
  carrier: string;
  serviceLevel: 'STANDARD' | 'EXPEDITED' | 'OVERNIGHT';
  estimatedDeliveryDays: number;
  estimatedDeliveryDate: string;
  isExpress: boolean;
}

export interface Shipment {
  shipmentId?: string;
  orderId: string;
  trackingNumber?: string;
  carrier?: string;
  status: 'PENDING' | 'PICKED_UP' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED';
  shippingAddress: string;
  weight?: number;
  dimensions?: number;
  estimatedDeliveryDate?: string;
  actualDeliveryDate?: string;
  deliveryConfirmation?: string;
  createdAt?: string;
}

// Payment Models
export interface Payment {
  paymentId?: string;
  orderId: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  transactionId?: string;
  cardLastFour?: string;
  cardBrand?: string;
  paymentDate?: string;
  failureReason?: string;
  is3DSecure?: boolean;
  createdAt?: string;
}

export type PaymentMethod = 'CREDIT_CARD' | 'DEBIT_CARD' | 'WALLET' | 'BANK_TRANSFER';

export interface PaymentRequest {
  orderId: string;
  amount: number;
  currency: string;
  paymentMethod: PaymentMethod;
  cardToken?: string;
  is3DSecure?: boolean;
  billingAddress?: string;
}

export interface RefundRequest {
  paymentId: string;
  amount?: number;
  reason?: string;
}

// Checkout Session
export interface CheckoutSession {
  cart: Cart;
  shippingAddress?: ShippingAddress;
  shippingRate?: ShippingRate;
  payment?: Payment;
  currentStep: number;
}
