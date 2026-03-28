# Module: spring-cloud/gateway

Purpose:
- API gateway configuration using Spring Cloud Gateway. Routes external requests to underlying microservices and provides cross-cutting concerns (auth, rate limiting, logging).

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :spring-cloud:gateway:bootRun`.
- Tests: `./gradlew :spring-cloud:gateway:test`.

Notes:
- Integrates with the authorization server for secure routing.
