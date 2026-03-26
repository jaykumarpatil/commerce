export * from './user.model';
export * from './product.model';
export * from './cart.model';
export * from './order.model';

// Common API response wrapper
export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface ApiError {
  message: string;
  code?: string;
  status?: number;
  timestamp: string;
}

// Pagination params
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

// Filter params
export interface FilterParams {
  [key: string]: string | number | boolean | undefined;
}
