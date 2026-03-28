# Module: microservices/analytics-service

Purpose:
- Collects and processes analytics data for the system; provides insights to other services.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:analytics-service:bootRun`.
- Tests: `./gradlew :microservices:analytics-service:test`.

Notes:
- This module often consumes events and computes metrics.
