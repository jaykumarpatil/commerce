# Commerce Microservices Platform

A Spring-based e-commerce microservices repository with both classic and hexagonal domain implementations, Spring Cloud edge/security services, and local infrastructure orchestration for Kafka/RabbitMQ-backed event flows.

## Service Map (Grouped by Domain Area)

### Shared libraries
- `:api` — defines shared API contracts, DTO transfer objects, and common exception types so all services speak the same contract language.
- `:util` — hosts shared utility classes (HTTP helpers, error handling scaffolding, logging/formatting, retry/backoff helpers) to avoid cross-service duplication.

### Core microservices (OLTP / business)

#### Catalog & product experience
- `:microservices:product-service` — product master data, pricing, variants, and product metadata used by catalog/cart/checkout.
- `:microservices:review-service` — product reviews, ratings, and moderation workflows.
- `:microservices:recommendation-service` — recommendation APIs such as similar items and frequently-bought-together.
- `:microservices:product-composite-service` — facade that aggregates product, review, recommendation, and related catalog-facing data.
- `:microservices:product-catalog-service` — shopper-facing product listings, category navigation, and discovery/search-facing views.

#### Shopping, checkout & fulfillment
- `:microservices:shopping-cart-service` — user cart state, quantity management, and checkout-readiness.
- `:microservices:order-service` — order lifecycle, status transitions, cancellations, and refund orchestration.
- `:microservices:inventory-service` — stock levels, reservations, and allocations to prevent overselling.
- `:microservices:payment-service` — payment authorization/capture/reconciliation with external providers.
- `:microservices:shipping-service` — shipment creation, carrier coordination, and tracking lifecycle.

#### Identity, engagement & operations
- `:microservices:user-service` — user identities, profiles, and account metadata (business-facing user context).
- `:microservices:notification-service` — event-driven outbound notifications (email/SMS/push).
- `:microservices:admin-service` — internal administration and operational support APIs.
- `:microservices:analytics-service` — behavior and operational aggregation for dashboards/reports.

### Hexagonal domain variants (pilot / migration)

#### Shopping domain
- `:microservices:cart-domain:shopping-cart-service` — cart bounded context with explicit ports/adapters.
- `:microservices:order-domain:order-service` — order bounded context and lifecycle/state machine ownership.

#### Identity domain
- `:microservices:auth-domain:user-service` — auth/identity concerns (auth, roles, sessions), separated from business profile concerns.

#### Catalog domain
- `:microservices:catalog-domain:product-catalog-service` — product discoverability/search/category logic as a bounded context.

#### Inventory domain
- `:microservices:inventory-domain:inventory-service` — inventory reservations, allocations, and replenishment event logic.

#### Payments domain
- `:microservices:payment-domain:payment-service` — isolated payment domain integrations and secret-bearing flows.

#### Notification domain
- `:microservices:notification-domain:notification-service` — reusable channel/template/delivery domain.

#### Reviews & recommendations
- `:microservices:review-domain:review-service` — review moderation/rating/spam-defense rules.
- `:microservices:recommendation-domain:recommendation-service` — recommendation model and tuning lifecycle.

#### Analytics & admin
- `:microservices:analytics-domain:analytics-service` — domain event aggregation into analytics-ready datasets.
- `:microservices:analytics-domain:admin-service` — admin APIs for operations/configuration built from analytics + core data.

#### Composite domain
- `:microservices:composite-domain:product-composite-service` — domain facade coordinating product/review/recommendation/catalog dependencies.

### Spring Cloud infrastructure
- `:spring-cloud:gateway` — edge gateway and TLS entrypoint; routing, auth passthrough, and cross-cutting policies.
- `:spring-cloud:authorization-server` — OAuth2/OIDC authorization server for token issuance and client/consent flows.

## Message brokers and data stores

This repo supports two integration styles for local development:

### Kafka stack (`docker-compose-kafka.yml`)
- Kafka + Zookeeper
- MongoDB (e.g., product/recommendation)
- MySQL (review)
- Zipkin (tracing)

### Native/RabbitMQ stack (`docker-compose-native.yml`)
- RabbitMQ
- MongoDB
- MySQL
- Zipkin

Additional service-level storage also exists in some services (for example Postgres/Redis in specific modules via their own application configs).

## Spring profiles

Common profile patterns used in this repo:
- `docker` — containerized runtime settings.
- `kafka` — Kafka binder/runtime profile for stream messaging.
- `streaming_partitioned` — enables partitioned stream processing.
- `streaming_instance_0`, `streaming_instance_1` — instance identity for partitioned consumers.

Examples from compose files:
- `docker,streaming_partitioned,streaming_instance_0,kafka`
- `docker,streaming_partitioned,kafka`
- `docker`

## Running locally

### Prerequisites
- Java 26+
- Docker + Docker Compose

### Build
```bash
./gradlew clean build
```

### Start Kafka-based stack
```bash
docker compose -f docker-compose-kafka.yml up -d
```

### Start native/RabbitMQ stack
```bash
docker compose -f docker-compose-native.yml up -d
```

### Run a single service (example)
```bash
./gradlew :microservices:inventory-service:bootRun
```

### Run tests (example)
```bash
./gradlew :util:test
```

## Smoke/integration checks

Use the provided script to run end-to-end API checks:

```bash
HOST=localhost PORT=8443 HEALTH_URL=https://localhost:8443 USE_K8S=false ./test-em-all.bash
```

## Notes
- Some modules represent production-style services, while others are architecture migration pilots.
- Prefer module-specific `README.md` files for service-specific configuration and endpoints.
