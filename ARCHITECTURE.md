# Architecture Overview

This document provides a concise purpose map for each module included in the root `settings.gradle`, grouped by domain area.

## Shared Libraries

- `:api` — Shared contracts (DTOs, API interfaces, exception models) to enforce consistent inter-service schemas.
- `:util` — Shared cross-cutting helpers (HTTP/service utilities, error handling scaffolding, common support utilities).

## Core Microservices (OLTP / Business)

### Catalog & Product Experience
- `:microservices:product-service` — Product master data, variants, and pricing ownership.
- `:microservices:review-service` — User review/rating lifecycle and moderation.
- `:microservices:recommendation-service` — Recommendation generation and retrieval.
- `:microservices:product-composite-service` — Product-facing aggregation facade to reduce client round trips.
- `:microservices:product-catalog-service` — Shopper-facing listing/discoverability APIs.

### Shopping, Checkout & Fulfillment
- `:microservices:shopping-cart-service` — Pre-order cart state and quantity management.
- `:microservices:order-service` — Order lifecycle and downstream orchestration entrypoint.
- `:microservices:inventory-service` — Stock tracking, reservation, and availability enforcement.
- `:microservices:payment-service` — Payment auth/capture and provider integration boundary.
- `:microservices:shipping-service` — Shipment lifecycle and delivery tracking.

### Identity, Engagement & Operations
- `:microservices:user-service` — User profile/account metadata for commerce contexts.
- `:microservices:notification-service` — Event-driven user/stakeholder notifications.
- `:microservices:admin-service` — Backoffice operational/admin capabilities.
- `:microservices:analytics-service` — Behavioral/operational data aggregation and reporting APIs.

## Hexagonal Domain Variants (Pilot / Migration)

### Shopping domain
- `:microservices:cart-domain:shopping-cart-service`
- `:microservices:order-domain:order-service`

### Identity / security domain
- `:microservices:auth-domain:user-service`

### Payments domain
- `:microservices:payment-domain:payment-service`

### Catalog domain
- `:microservices:catalog-domain:product-catalog-service`

### Inventory domain
- `:microservices:inventory-domain:inventory-service`

### Notification domain
- `:microservices:notification-domain:notification-service`

### Reviews & recommendations
- `:microservices:review-domain:review-service`
- `:microservices:recommendation-domain:recommendation-service`

### Analytics & admin
- `:microservices:analytics-domain:analytics-service`
- `:microservices:analytics-domain:admin-service`

### Composite domain
- `:microservices:composite-domain:product-composite-service`

## Spring Cloud Infrastructure Services

- `:spring-cloud:gateway` — Edge gateway and inbound routing/TLS/policy point.
- `:spring-cloud:authorization-server` — OAuth2/OIDC token issuance and client/consent management.
