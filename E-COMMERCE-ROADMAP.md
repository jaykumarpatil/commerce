# E-Commerce Platform Implementation Roadmap

## Project Status: Foundation Complete, Feature Enhancement Phase

Your existing codebase has 15 microservices with infrastructure already configured. This roadmap maps your 31-user-story backlog to incremental enhancements.

---

## Epic Mapping Summary

| Epic | Existing Services | Enhancement Focus | Timeline |
|------|-----------------|------------------|----------|
| INFRA-001 | Docker Compose, Redis, Postgres | Add Elasticsearch, Redis Cluster, Citus | Week 1 |
| USER-AUTH-001 | user-service | Full JWT flow, BCrypt, email verification | Weeks 1-2 |
| CATALOG-001 | product-service, product-catalog-service | Elasticsearch, JSONB, bulk operations | Weeks 2-3 |
| SEARCH-001 | NEW | Elasticsearch service | Weeks 4-5 |
| CART-001 | shopping-cart-service | Redis Lua, atomic ops, cart merge | Weeks 5-6 |
| CHECKOUT-001 | order-service, payment-service | Saga pattern, Stripe 3DS | Weeks 6-8 |
| ORDER-001 | order-service, inventory-service | Multi-warehouse, carrier API | Weeks 8-9 |
| FRONTEND-001 | frontend/ (Angular) | PWA, SSR, signals | Weeks 3-10 |
| MONITORING-001 | kubernetes/ | Prometheus, Grafana, ELK | Weeks 10-11 |
| DEPLOY-001 | kubernetes/ | GitHub Actions, blue-green | Weeks 11-12 |
| PERF-001 | ALL | Load testing, HPA tuning | Weeks 12-14 |

---

## Phase 0: Architecture Analysis (Current State)

### Service Inventory
```
microservices/
├── user-service (MongoDB reactive)          → USER-AUTH-001
├── product-service (MongoDB reactive)       → CATALOG-001
├── product-catalog-service (PostgreSQL)     → CATALOG-001  
├── recommendation-service (MongoDB)        → SEARCH-001
├── review-service (JPA/MySQL)               → CATALOG-001
├── shopping-cart-service (MongoDB+Redis)    → CART-001
├── order-service (PostgreSQL)               → ORDER-001, CHECKOUT-001
├── inventory-service (PostgreSQL)           → ORDER-001
├── payment-service (PostgreSQL)             → CHECKOUT-001
├── shipping-service (PostgreSQL)            → ORDER-001
├── notification-service (PostgreSQL)        → CHECKOUT-001
├── admin-service (PostgreSQL)               → USER-AUTH-001, CATALOG-001
├── analytics-service (PostgreSQL)           → MONITORING-001
└── product-composite-service               → API Gateway

spring-cloud/
├── gateway (Spring Cloud Gateway)          → Edge Server
└── authorization-server (OAuth2)          → USER-AUTH-001
```

---

## Phase 1: INFRA-001 - Foundation (Week 1)

### Goals
- Add Elasticsearch for SEARCH-001
- Configure Redis Cluster
- Set up PostgreSQL sharding plan

### Tasks
- [ ] Add Elasticsearch 8.x to docker-compose.yml
- [ ] Configure Redis Cluster (3 masters) for CART-001
- [ ] Create config-repo/elasticsearch.yml
- [ ] Add Elasticsearch health checks
- [ ] Update infrastructure ports documentation

### Files to Create/Modify
```
docker-compose.yml                    # + elasticsearch service
config-repo/elasticsearch.yml         # ES configuration
```

### AC Verification
- [ ] Elasticsearch accessible at port 9200
- [ ] Redis Cluster responds to CLUSTER SLOTS
- [ ] Health endpoints return 200

---

## Phase 2: USER-AUTH-001 - Authentication (Weeks 1-2)

### Goals
- Complete JWT implementation (access 24h, refresh 7d)
- Add BCrypt password hashing (12+ chars, strength validation)
- Email verification with SES (24h expiry)
- Rate limiting (5/min per IP)
- Admin CRUD with soft delete

### Existing: user-service
```java
// Current: basic JWT provider
JwtTokenProvider.java (73 lines)
// Missing: email verification, refresh token storage, rate limiting
```

### Tasks

#### US-001: User Registration
- [ ] Add email validation (RFC5322) in UserServiceImpl
- [ ] Add password strength validation (12+ chars, 1 upper, 1 number)
- [ ] Implement unique email check with database constraint
- [ ] Add email verification token generation (UUID, 24h TTL)
- [ ] Integrate SES for verification emails
- [ ] Return JWT immediately after successful registration

#### US-002: User Login
- [ ] Add Redis storage for refresh tokens
- [ ] Configure httpOnly cookies for tokens
- [ ] Implement access token (24h) + refresh token (7d)
- [ ] Add rate limiting (5 attempts/min per IP)
- [ ] Return 401 with generic error message

#### US-003: Admin User Management
- [ ] Add role-based endpoints (USER/ADMIN)
- [ ] Implement soft delete with deleted_at timestamp
- [ ] Add activity logging (who, when, what)
- [ ] Add search/filter by email, role, date

### CQRS Design
```
Commands (writes)          Queries (reads)
├── RegisterUser          ├── GetUserProfile
├── LoginUser             ├── SearchUsers (admin)
├── UpdateUser           ├── GetUserActivity
├── DeleteUser (soft)     └── ListUsers (admin)
└── ChangePassword
```

### Files to Modify
```
microservices/user-service/
├── src/main/java/.../services/
│   ├── UserServiceImpl.java      # + email verification, BCrypt
│   └── JwtTokenProvider.java     # + refresh tokens, Redis storage
├── src/main/java/.../controller/
│   └── UserController.java       # + rate limiting
└── src/main/java/.../persistence/
    └── UserEntity.java           # + verification_token, roles, audit fields
```

### New Files
```
microservices/user-service/src/main/java/.../
├── security/
│   ├── RateLimitFilter.java     # IP-based rate limiting
│   └── EmailVerificationService.java
├── dto/
│   ├── EmailVerificationRequest.java
│   └── RefreshTokenRequest.java
└── config/
    └── SecurityConfig.java       # CORS, CSRF, endpoints
```

---

## Phase 3: CATALOG-001 - Product Management (Weeks 2-3)

### Goals
- Category-based browsing with filtering
- JSONB attributes for variants
- Bulk upload with S3 presigned URLs
- RSQL query support (20/page pagination)

### Existing: product-catalog-service (PostgreSQL)
```java
// Has basic product CRUD
// Missing: JSONB attributes, Elasticsearch sync, variants
```

### Tasks

#### US-004: Browse Products
- [ ] Add category filtering with hierarchical categories
- [ ] Implement price range filtering
- [ ] Add attribute-based filtering (color, size, material)
- [ ] Server-side pagination (20/page default)
- [ ] Sort by price/popularity/rating
- [ ] RSQL query support for advanced filtering

#### US-005: Manage Products (Admin)
- [ ] Add S3 presigned URL generation for image uploads
- [ ] Implement JSONB attributes schema
- [ ] Add variant support (size/color combinations)
- [ ] Bulk CSV/JSON import endpoint
- [ ] Batch update operations

#### US-006: Product Details
- [ ] Add related products algorithm
- [ ] Implement real-time stock status query
- [ ] Add price history tracking
- [ ] Customer reviews/ratings integration

### CQRS Design
```
Commands (writes)          Queries (reads)
├── CreateProduct          ├── GetProductDetails
├── UpdateProduct         ├── BrowseProducts (filter/sort/paginate)
├── DeleteProduct         ├── GetRelatedProducts
├── BulkUpload            ├── GetPriceHistory
└── AddVariant            └── SearchProducts (ES)
```

### Files to Modify
```
microservices/product-catalog-service/
├── src/main/java/.../services/ProductServiceImpl.java
├── src/main/java/.../controller/ProductController.java
└── src/main/java/.../persistence/ProductEntity.java  # + JSONB

config-repo/
├── product-catalog.yml    # + S3 config, ES config
```

### New Files
```
microservices/product-catalog-service/src/main/java/.../
├── dto/
│   ├── ProductFilterRequest.java
│   ├── BulkProductUploadRequest.java
│   └── ProductVariantDto.java
├── service/
│   ├── S3PresignedUrlService.java
│   └── ElasticsearchSyncService.java
└── repository/
    └── ProductRepositoryCustom.java  # RSQL support
```

---

## Phase 4: SEARCH-001 - Search Engine (Weeks 4-5)

### Goals
- <50ms search latency
- Prefix matching on name/description
- Popularity boosting
- Search analytics dashboard

### New Service: search-service

### Tasks

#### US-007: Search Suggestions
- [ ] Index products to Elasticsearch
- [ ] Implement completion suggester for prefix matching
- [ ] Add popularity scoring boost
- [ ] Configure ILM policies for index management
- [ ] Ensure p95 < 50ms response time

#### US-008: Search Analytics
- [ ] Track zero-result queries
- [ ] Monitor popular search terms
- [ ] Implement A/B test framework for relevance
- [ ] Create query performance dashboard

### Architecture
```
Catalog Command → Kafka → Elasticsearch Projector
                                     ↓
Shopper Search → Gateway → search-service → ES
```

---

## Phase 5: CART-001 - Shopping Cart (Weeks 5-6)

### Goals
- Redis Lua scripts for atomic operations
- Guest carts (session_id) and persistent carts (user_id)
- 30-day TTL with Postgres backup
- Cart merging on login

### Existing: shopping-cart-service
```java
// Has basic MongoDB storage
// Missing: Redis Lua scripts, atomic operations, cart merge
```

### Tasks

#### US-009: Add/Remove Items
- [ ] Implement Redis Lua scripts for atomic cart ops
- [ ] Add distributed locking for stock validation
- [ ] Support guest carts (session_id) and user carts (user_id)
- [ ] Real-time stock validation against inventory-service

#### US-010: Cart Persistence
- [ ] Set Redis TTL to 30 days
- [ ] Implement nightly Postgres backup job
- [ ] Add cart merge logic on login (avoid duplicates)

#### US-011: Cart Totals
- [ ] Calculate tax based on shipping address
- [ ] Calculate shipping estimates
- [ ] Apply coupon/promo codes
- [ ] Display subtotal/tax/shipping breakdown

### Redis Data Model
```
cart:{sessionId} → Hash
  items → JSON array of cart items
  createdAt → timestamp
  updatedAt → timestamp
  
inventory:lock:{productId} → String (distributed lock)
  value → lock holder ID
  TTL → 30 seconds
```

---

## Phase 6: CHECKOUT-001 - Transactional Flow (Weeks 6-8)

### Goals
- 3-step checkout wizard
- Stock reservation via Kafka (15 min hold)
- Stripe + PayPal integration
- WebSocket order updates

### Existing: order-service, payment-service
```java
// Has basic order/payment CRUD
// Missing: Saga orchestration, stock reservation, 3DS
```

### Tasks

#### US-012: Checkout Flow
- [ ] Implement 3-step wizard (review → shipping → payment)
- [ ] Add client-side validation
- [ ] Create stock reservation via Kafka events
- [ ] Handle reservation timeout (15 min)

#### US-013: Payment Methods
- [ ] Integrate Stripe (cards, UPI, wallets)
- [ ] Add PayPal integration
- [ ] Implement 3DS verification
- [ ] Ensure PCI-DSS compliance

#### US-014: Order Confirmation
- [ ] WebSocket notifications for order status
- [ ] Transactional email receipts
- [ ] PDF invoice generation
- [ ] Tracking number integration

### Saga Pattern
```
CheckoutSaga:
  1. ReserveInventory (inventory-service)
  2. ProcessPayment (payment-service)  
  3. CreateOrder (order-service)
  4. SendNotification (notification-service)
  
  Compensating actions on failure:
  - ReleaseInventory
  - RefundPayment
  - CancelOrder
```

---

## Phase 7: ORDER-001 - Fulfillment (Weeks 8-9)

### Goals
- Paginated order history
- Multi-warehouse allocation
- Carrier API integration

### Tasks

#### US-015: Order History
- [ ] Paginated history with status filters
- [ ] Downloadable invoices
- [ ] Quick reorder functionality
- [ ] Return request workflow

#### US-016: Inventory Management
- [ ] Multi-warehouse stock allocation
- [ ] Closest warehouse selection by distance
- [ ] Low-stock alerts (<10 units)
- [ ] Stock transfer requests

#### US-017: Shipment Tracking
- [ ] Carrier API integration (FedEx, UPS, USPS)
- [ ] Real-time tracking updates
- [ ] SMS/Email delivery notifications
- [ ] Proof of delivery

---

## Phase 8: FRONTEND-001 - Angular UI (Weeks 3-10)

### Goals
- PWA with offline support
- SSR for SEO
- <2.5s LCP, <0.1 CLS
- WCAG 2.1 AA compliance

### Existing: frontend/ (Angular 17+)
```javascript
// Has basic project setup with Tailwind CSS
// Missing: full feature implementation, PWA, SSR
```

### Tasks

#### US-018: Responsive UI
- [ ] Implement PWA with offline cart capability
- [ ] Add SSR with Angular Universal
- [ ] Target Lighthouse >90 score

#### US-019: Fast Navigation
- [ ] Route-level code splitting
- [ ] Infinite scroll for product lists
- [ ] Component lazy loading
- [ ] Core Web Vitals optimization

#### US-020: Form Validation
- [ ] Reactive forms implementation
- [ ] Real-time validation feedback
- [ ] WCAG 2.1 AA compliance
- [ ] Screen reader support

### Angular Structure
```
frontend/src/app/
├── core/                    # Singleton services, guards
│   ├── auth/
│   ├── cart/
│   └── api/
├── shared/                  # Reusable components, pipes
│   ├── components/
│   └── pipes/
├── features/                # Feature modules (lazy-loaded)
│   ├── auth/
│   ├── catalog/
│   ├── cart/
│   ├── checkout/
│   └── orders/
└── app.config.ts           # Standalone component config
```

---

## Phase 9: MONITORING-001 - Observability (Weeks 10-11)

### Goals
- SLO dashboards
- Structured logging with correlation IDs
- Circuit breakers

### Tasks

#### US-024: Metrics Dashboard
- [ ] Prometheus scrape configs for all services
- [ ] JVM metrics (heap, GC, threads)
- [ ] Database connection pool metrics
- [ ] Custom business metrics
- [ ] Grafana dashboards (SLO-based)

#### US-025: Centralized Logging
- [ ] ELK stack integration
- [ ] Structured JSON logging
- [ ] Correlation ID propagation
- [ ] 90-day log retention

#### US-026: Circuit Breakers
- [ ] Resilience4j configuration
- [ ] Timeout policies
- [ ] Fallback responses
- [ ] Failure rate alerts

---

## Phase 10: DEPLOY-001 - CI/CD (Weeks 11-12)

### Goals
- GitHub Actions pipeline
- Blue-green deployments
- Feature flags

### Tasks

#### US-027: Automated Deployments
- [ ] GitHub Actions workflow
- [ ] Blue-green deployment strategy
- [ ] Automated rollback (<5 min)
- [ ] Canary releases

#### US-028: Security
- [ ] OWASP ZAP scanning
- [ ] WAF rules configuration
- [ ] Secrets rotation
- [ ] JWT validation in Gateway

#### US-029: Testing
- [ ] 80% code coverage target
- [ ] Testcontainers for integration tests
- [ ] Pact contract testing
- [ ] Cypress E2E tests

---

## Phase 11: PERF-001 - Performance (Weeks 12-14)

### Goals
- p95 <200ms response time
- 10k concurrent users
- Zero-downtime scaling

### Tasks

#### US-030: Page Load Performance
- [ ] CDN configuration for static assets
- [ ] Image lazy loading
- [ ] Connection pooling optimization
- [ ] Cache headers tuning

#### US-031: Auto-Scaling
- [ ] K8s HPA configuration (CPU >70%)
- [ ] Queue-based request decoupling
- [ ] Load testing with k6
- [ ] Black Friday capacity planning

---

## Implementation Order

```
Week 1:   INFRA-001 → USER-AUTH-001 (start)
Week 2:   USER-AUTH-001 → CATALOG-001 (start)
Week 3:   CATALOG-001 → FRONTEND-001 (start)
Week 4:   SEARCH-001 (design)
Week 5:   SEARCH-001 → CART-001 (start)
Week 6:   CART-001 → CHECKOUT-001 (start)
Week 7:   CHECKOUT-001
Week 8:   CHECKOUT-001 → ORDER-001
Week 9:   ORDER-001 → FRONTEND-001 (integrate)
Week 10:  MONITORING-001
Week 11:  DEPLOY-001
Week 12:  PERF-001 (load testing)
Week 13:  PERF-001 (scaling)
Week 14:  Polish, documentation
```

---

## Next Steps

1. **Start Phase 1**: `./gradlew build` to verify current state
2. **Add Elasticsearch** to docker-compose.yml
3. **Enhance user-service** with complete JWT implementation

Ready to begin? Say "start phase 1" or "implement USER-AUTH-001".
