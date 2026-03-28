# Module: microservices/product-service

Purpose:
- Core product management service. Exposes APIs to query and manage product data, and integrates with downstream services for catalog, pricing, and inventory as part of the e-commerce system.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:product-service:bootRun` (requires dependencies resolved).
- Tests: `./gradlew :microservices:product-service:test`.

Notes:
- Part of the hexagonal architecture; interacts with other services over REST.
