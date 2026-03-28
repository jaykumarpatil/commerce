# Module: microservices/payment-service

Purpose:
- Handles payments and settlement flows for orders.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:payment-service:bootRun`.
- Tests: `./gradlew :microservices:payment-service:test`.

Notes:
- Integrates with order and shipping services to finalize purchases.
