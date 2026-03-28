# Commerce Microservices Platform

A Spring-based e-commerce microservices repository with both classic and hexagonal domain implementations, Spring Cloud edge/security services, and local infrastructure orchestration for Kafka/RabbitMQ-backed event flows.

## What this repository contains

### Core shared modules
- `:api` — shared API contracts, DTOs, and exception types used across services.
- `:util` — shared utility classes for HTTP/error handling and common helpers.

### Core microservices
- Product, Review, Recommendation, Product Composite
- User, Product Catalog, Shopping Cart, Order, Inventory
- Payment, Shipping, Notification
- Admin, Analytics

### Hexagonal domain variants (pilot/migration modules)
- Cart domain (`:microservices:cart-domain:shopping-cart-service`)
- Order domain (`:microservices:order-domain:order-service`)
- Auth domain (`:microservices:auth-domain:user-service`)
- Payment domain (`:microservices:payment-domain:payment-service`)
- Catalog domain (`:microservices:catalog-domain:product-catalog-service`)
- Inventory domain (`:microservices:inventory-domain:inventory-service`)
- Notification domain (`:microservices:notification-domain:notification-service`)
- Review domain (`:microservices:review-domain:review-service`)
- Recommendation domain (`:microservices:recommendation-domain:recommendation-service`)
- Analytics domain (`:microservices:analytics-domain:analytics-service`, `:microservices:analytics-domain:admin-service`)
- Composite domain (`:microservices:composite-domain:product-composite-service`)

### Spring Cloud services
- `:spring-cloud:gateway` — edge gateway and TLS entrypoint.
- `:spring-cloud:authorization-server` — OAuth2 authorization server.

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
