# Module: microservices/product-catalog-service

Purpose:
- Manages product catalog data (definitions, categories, attributes) exposed by the product ecosystem.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:product-catalog-service:bootRun`.
- Tests: `./gradlew :microservices:product-catalog-service:test`.

Notes:
- Often collaborates with the catalog-domain for schema and validation concerns.
