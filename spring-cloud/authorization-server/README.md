# Module: spring-cloud/authorization-server

Purpose:
- OAuth2 / OpenID Connect authorization server for the system. Manages clients, tokens, and user authentication flows.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :spring-cloud:authorization-server:bootRun`.
- Tests: `./gradlew :spring-cloud:authorization-server:test`.

Notes:
- Integrated with the gateway and user services for secure access.
