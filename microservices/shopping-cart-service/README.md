# Module: microservices/shopping-cart-service

Purpose:
- Manages the shopping cart lifecycle, including add/remove items and preparing for checkout.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:shopping-cart-service:bootRun`.
- Tests: `./gradlew :microservices:shopping-cart-service:test`.

Notes:
- Interacts with product, catalog, and order services during checkout.
