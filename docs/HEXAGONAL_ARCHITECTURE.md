# Hexagonal Architecture Domain Structure

## Overview

This document describes the proposed hexagonal (ports and adapters) architecture for the e-commerce microservices platform. This is a **future-state architecture** to be implemented incrementally.

## Domain Structure

```
microservices/
├── auth-domain/                    # USER-AUTH-001, AUTH-001
│   └── user-service/
│       └── src/main/java/se/magnus/microservices/auth/
│           ├── domain/
│           │   ├── model/          # UserEntity, UserRole
│           │   ├── service/        # Domain services (JwtTokenProvider, PasswordValidation)
│           │   └── event/         # Domain events
│           ├── application/
│           │   ├── port/
│           │   │   ├── inbound/   # Use case interfaces
│           │   │   └── outbound/  # Repository interfaces
│           │   └── usecase/       # Use case implementations
│           └── adapter/
│               ├── inbound/
│               │   ├── rest/      # UserController
│               │   └── security/  # SecurityConfig, RateLimitFilter
│               └── outbound/
│                   └── persistence/ # UserRepository implementation
│
├── catalog-domain/                 # CATALOG-001, SEARCH-001
│   ├── product-service/
│   ├── product-catalog-service/
│   └── search-service/
│
├── cart-domain/                    # CART-001
│   └── shopping-cart-service/
│       └── src/main/java/se/magnus/microservices/cart/
│           ├── domain/
│           │   ├── model/         # Cart, CartItem entities
│           │   ├── service/      # Cart domain logic
│           │   └── event/        # CartEvent
│           ├── application/
│           │   ├── port/
│           │   │   ├── inbound/  # CartServicePort
│           │   │   └── outbound/ # CartRepositoryPort
│           │   └── usecase/     # AddItemUseCase, RemoveItemUseCase
│           └── adapter/
│               ├── inbound/rest/ # CartController
│               └── outbound/redis/ # RedisCartRepository
│
├── order-domain/                   # ORDER-001, SHIPPING-001
│   ├── order-service/
│   └── shipping-service/
│
├── payment-domain/                 # CHECKOUT-001
│   └── payment-service/
│
├── inventory-domain/               # ORDER-001
│   └── inventory-service/
│
├── notification-domain/           # NOTIFICATION-001
│   └── notification-service/
│
├── review-domain/                 # REVIEW-001
│   └── review-service/
│
├── recommendation-domain/         # RECOMMENDATION-001
│   └── recommendation-service/
│
├── analytics-domain/             # ANALYTICS-001, ADMIN-001
│   ├── analytics-service/
│   └── admin-service/
│
└── composite-domain/             # API-001
    └── product-composite-service/
```

## Hexagonal Architecture Principles

### 1. Domain Layer (Core)
- **Model**: Entities, value objects, aggregates
- **Service**: Domain logic that doesn't fit in entities
- **Event**: Domain events for inter-aggregate communication

### 2. Application Layer
- **Port (Inbound)**: Interfaces for use cases (driven by)
- **Port (Outbound)**: Interfaces for external dependencies (driving)
- **UseCase**: Orchestrates domain objects and ports

### 3. Adapter Layer
- **Inbound**: Implements inbound ports (controllers, message handlers)
- **Outbound**: Implements outbound ports (repositories, external clients)

## Team Alignment

| Domain | Squad | Epic | Services |
|--------|-------|------|----------|
| Auth | Auth Team | USER-AUTH-001 | user-service |
| Catalog | Catalog Team | CATALOG-001, SEARCH-001 | product-service, product-catalog-service, search-service |
| Cart | Cart Team | CART-001 | shopping-cart-service |
| Order | Order Team | ORDER-001 | order-service, inventory-service |
| Payment | Payment Team | CHECKOUT-001 | payment-service |
| Shipping | Logistics Team | SHIPPING-001 | shipping-service |
| Notification | Platform Team | NOTIFICATION-001 | notification-service |
| Review | Catalog Team | REVIEW-001 | review-service |
| Recommendation | AI/ML Team | RECOMMENDATION-001 | recommendation-service |
| Analytics | Data Team | ANALYTICS-001, ADMIN-001 | analytics-service, admin-service |

## Migration Strategy

### Phase 1: Parallel Run
1. Create new domain structure alongside existing
2. Run both in parallel with feature flags
3. Gradually migrate traffic

### Phase 2: Domain by Domain
1. Start with least coupled domain (Cart)
2. Validate pattern
3. Iterate

### Phase 3: Composite Services
1. Migrate composite services last
2. Ensure API contracts maintained

## Shared Libraries

### Current (via api module)
- `se.magnus.api.core.*` - DTOs and API interfaces
- `se.magnus.api.exceptions` - Common exceptions

### Future (Domain-specific)
- `auth-domain-model` - Auth domain types
- `cart-domain-model` - Cart domain types
- etc.

## Key Principles

1. **Dependency Rule**: Outer layers depend on inner layers, never the reverse
2. **Ports over Frameworks**: Define interfaces independent of implementation
3. **Single Responsibility**: Each adapter has one job
4. **Testability**: Core business logic testable without infrastructure
