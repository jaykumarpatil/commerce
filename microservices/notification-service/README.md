# Module: microservices/notification-service

Purpose:
- Manages user notifications (email, SMS, push) and related delivery workflows.

How to run:
- Build: `./gradlew build` from repo root.
- Run: `./gradlew :microservices:notification-service:bootRun`.
- Tests: `./gradlew :microservices:notification-service:test`.

Notes:
- Consumes events from other services to trigger notifications.
