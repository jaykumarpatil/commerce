// Product Models
export interface Product {
  productId: string;
  name: string;
  description?: string;
  shortDescription?: string;
  slug?: string;
  sku?: string;
  barcode?: string;
  price: number;
  originalPrice?: number;
  discountPercent?: number;
  imageUrl?: string;
  mainImage?: string;
  images?: string[];
  stockQuantity?: number;
  minOrderQuantity?: number;
  maxOrderQuantity?: number;
  inStock?: boolean;
  featured?: boolean;
  active?: boolean;
  categoryId?: string;
  category?: Category;
  variants?: Variant[];
  attributes?: Attribute[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Category {
  categoryId: string;
  name: string;
  description?: string;
  slug?: string;
  imageUrl?: string;
  sortOrder?: number;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Variant {
  variantId: string;
  productId: string;
  name: string;
  sku?: string;
  price: number;
  stockQuantity?: number;
  active?: boolean;
  attributes?: AttributeValue[];
}

export interface Attribute {
  attributeId: string;
  name: string;
  value: string;
}

export interface AttributeValue {
  attributeId: string;
  attributeName: string;
  value: string;
}

export interface ProductSearchParams {
  query?: string;
  categoryId?: string;
  page?: number;
  size?: number;
  sort?: string;
  minPrice?: number;
  maxPrice?: number;
  featured?: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
