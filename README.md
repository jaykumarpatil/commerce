# ECOM Modular Monolith (Spring Boot 4 + Java 26)

Executive summary: Pragmatic, modular monolith with strict module boundaries.

What this repo provides now:
- A multi-module Gradle project with modules: app, shared, catalog, cart, user, order, payment.
- A single Spring Boot 4 application inside the app module that wires in all domain modules.
- Basic skeletons for core domains: catalog (read path), users (auth skeleton), cart (redis), order/payments stubs.
- Java 26 toolchain configured across modules; RestClient and Declarative HTTP stubs prepared for future external integrations.
- Basic observability and Flyway migrations roots prepared.

How to run:
- Ensure Java 26 is installed and JAVA_HOME points to it.
- Build: ./gradlew :app:bootJar
- Run: java -jar app/build/libs/ecom-modular-monolith.jar

Next steps you can take from here:
- Fill in the catalog data model, implement a few read queries and index attributes as JSONB.
- Implement real Cart, User, Order services in their modules and wire them to the app.
- Add ArchUnit tests to enforce module boundaries and regression checks.
- Integrate a real database, Redis, and migrations with Flyway in CI.
