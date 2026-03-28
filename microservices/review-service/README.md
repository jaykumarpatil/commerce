# Module: microservices/review-service

Purpose:
- Manages product reviews and ratings. Uses JPA/MySQL for persistence as described in the architecture notes.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:review-service:bootRun`.
- Tests: `./gradlew :microservices:review-service:test`.

Notes:
- Part of the review domain; integrates with product and user services via REST.
