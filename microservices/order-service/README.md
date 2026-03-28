# Module: microservices/order-service

Purpose:
- Handles order lifecycle, orchestration with payment and shipping domains.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:order-service:bootRun`.
- Tests: `./gradlew :microservices:order-service:test`.

Notes:
- Core to order-domain interactions and external service calls.
