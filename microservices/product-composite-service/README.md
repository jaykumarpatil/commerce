# Module: microservices/product-composite-service

Purpose:
- Aggregator service that composes product data from core services (product, catalog, inventory, reviews, etc.) to present a unified product view.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:product-composite-service:bootRun`.
- Tests: `./gradlew :microservices:product-composite-service:test`.

Notes:
- Demonstrates WebClient-based parallel calls and data aggregation.
