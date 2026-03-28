# Module: api

Purpose:
- Shared REST interfaces and DTOs used by multiple microservices in this project. Centralizes common data contracts to reduce duplication and ensure consistency across services.

What to expect:
- Lightweight interfaces, DTOs, and possibly enums used by controllers and clients.
- No business logic; strictly data contracts.

How to work with it:
- Build: `./gradlew build` from repo root.
- Tests: `./gradlew test` (targeted tests can be run per module if needed).
- To run a specific module that consumes these contracts, follow its module's README.

Notes:
- Java 17 / Spring Boot 3.x stack is used across the project.
