# Module: microservices/recommendation-service

Purpose:
- Recommends products to users based on available data. Uses MongoDB (reactive) for storage.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:recommendation-service:bootRun`.
- Tests: `./gradlew :microservices:recommendation-service:test`.

Notes:
- Part of product ecosystem; communicates with product and catalog services.
