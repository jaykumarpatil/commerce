# Spring Boot 4 Claims Review (as of 2026-03-28)

This note validates the claims against official Spring docs/blog pages.

## Verdict Summary

- **Mostly correct overall**, with a few points that need nuance.
- **Strongly supported:** API versioning support, interface HTTP clients, OpenTelemetry starter, modularization, JSpecify null-safety, Spring Security 7 authorization server integration, SSL certificate health checks.
- **Needs correction/qualification:**
  - Virtual threads are **supported and easy to enable**, but not universally a "full embrace" replacement for reactive programming in every workload.
  - Claims like "<50ms startup" and "30–60% less memory" are **not universal guarantees** and depend heavily on app/profile/deployment.

## Claim-by-Claim

## 1) Performance & Cloud-Native

### Modularization into many focused modules

**Status: Supported (with nuance).**
Spring Boot 4 split the previously monolithic auto-configuration packaging into smaller focused modules. The official Spring blog explicitly describes this shift and motivations (leaner footprint, maintainability, reduced classpath overhead). It does not market this as a universal startup-time guarantee for all apps.

### Native-first (GraalVM 25 / Leyden) and dramatic startup-memory numbers

**Status: Directionally supported; absolute performance numbers are situational.**
Spring release highlights confirm GraalVM 25 alignment in Spring Framework 7. But fixed claims such as "<50ms startup" or "30–60% less memory" should be presented as **possible in specific optimized native scenarios**, not as blanket outcomes.

### Virtual threads in Java 21+

**Status: Supported, but wording should be careful.**
Spring Boot supports virtual threads via configuration (e.g., `spring.threads.virtual.enabled=true`) with caveats (daemon behavior and keep-alive considerations). This is real and production-relevant, but it does not make reactive programming obsolete in every I/O pattern.

## 2) Built-in Microservice Patterns

### API versioning in request mappings + global config

**Status: Supported.**
Spring Framework 7 introduces first-class API versioning with mapping support and resolver strategies (header/query/path/media-type), and Boot 4 auto-configuration properties for MVC/WebFlux versioning.

### Declarative HTTP clients (@HttpExchange style)

**Status: Supported.**
Spring Boot 4 highlights confirm HTTP service client auto-configuration for annotated Java interfaces backed by RestClient/WebClient.

### Retry + concurrency limits in core/context

**Status: Supported.**
Release highlights indicate built-in resilience annotations (`@Retryable`, `@ConcurrencyLimit`) in Spring Core/Context with `@EnableResilientMethods`.

## 3) Observability & Monitoring

### `spring-boot-starter-opentelemetry`

**Status: Supported.**
Spring Boot 4 adds a first-party OpenTelemetry starter and OTLP-oriented setup.

### SSL expiration health checks in Actuator

**Status: Supported.**
Actuator health docs describe the SSL health indicator and warning threshold properties that surface upcoming certificate expiration details.

### `@Observed`

**Status: True feature, but not brand-new to Boot 4.**
`@Observed` is part of Micrometer observation-based instrumentation and has existed before Boot 4; Boot 4 improves and extends the broader observability experience.

## 4) Code Quality & Security

### JSpecify null-safety

**Status: Supported.**
Spring release highlights explicitly call out portfolio-wide JSpecify adoption.

### OAuth 2.1 + built-in auth server with Security 7

**Status: Supported (with wording precision).**
Spring Security 7 documents OAuth 2.1 authorization server capabilities; Spring’s release highlights state authorization server integration into Spring Security 7 without separate project wiring.

## Safer phrasing you can use publicly

"Spring Boot 4 and Spring Framework 7 introduce major platform upgrades for microservices: modularized auto-configuration, first-class API versioning, interface-based HTTP clients, and improved observability with a first-party OpenTelemetry starter. Virtual threads are straightforward to enable on Java 21+, but they complement rather than universally replace reactive designs. Native-image improvements are substantial in many deployments, though startup and memory gains vary by workload."

## Sources checked

- Spring Framework reference: API versioning.
- Spring Boot reference: observability and SSL health indicator.
- Spring blog: "OpenTelemetry with Spring Boot".
- Spring blog: "Modularizing Spring Boot".
- Spring project release highlights page (Boot 4 release train).
- Spring Security reference: OAuth 2.1 Authorization Server.

## Suggested Improvements for Spring Boot 4 + Java 25 Adoption

1. **Add a measured benchmark appendix** (JVM vs AOT/native, Java 21 vs 25, virtual threads on/off) with p95/p99 latency, startup time, RSS memory, and CPU to prevent overgeneralized performance claims.
2. **Define a virtual-thread adoption playbook** that lists safe workloads (request-per-thread blocking I/O) and anti-patterns (pinning risks, long synchronized blocks, blocking inside event loops).
3. **Document structured-concurrency guidance** for Java 25 (`StructuredTaskScope`) in service orchestration paths, with timeout/cancellation propagation patterns.
4. **Create an observability baseline profile** for OTel (traces/metrics/log correlation) including sampling defaults, baggage conventions, and cardinality guardrails.
5. **Add resilience policy examples** combining `@Retryable` + `@ConcurrencyLimit` with per-endpoint budgets and failure classification to avoid retry storms.
6. **Provide API versioning governance rules** (deprecation headers, sunset windows, compatibility matrix, and contract test requirements).
7. **Introduce SSL health SLO runbooks** tying Actuator SSL warnings to alerting thresholds and automated certificate-rotation pipelines.
8. **Publish Java 25 migration guardrails** (GC defaults, container memory tuning, CDS/AppCDS strategy, and JSpecify enforcement in CI).

## Deep Dive: Modularizing Spring Boot (What to Do in Real Projects)

For migration teams, "modularizing Spring Boot" should be treated as a dependency-and-autoconfiguration optimization exercise, not only a packaging change.

### Practical approach

1. **Start with dependency minimization**
   - Replace broad starters where possible with focused starters that match runtime needs.
   - Remove transitive libraries not required by production traffic paths.

2. **Audit auto-configuration usage**
   - Use condition evaluation reports to identify auto-configurations that are loaded but unused.
   - Exclude unneeded auto-configurations explicitly and verify behavior through smoke tests.

3. **Split service roles cleanly**
   - Separate API edge services, internal orchestration services, and batch/worker services into distinct deployables with tailored dependency sets.
   - Avoid shipping the same "kitchen sink" dependency graph to every service type.

4. **Align with AOT/native goals**
   - Keep reflection/proxy-heavy libraries out of hot-path services where native image startup is a priority.
   - Validate reachability metadata and runtime hints early in CI.

5. **Measure impact per module decision**
   - Track startup time, RSS memory, class count, and image size before/after each dependency reduction.
   - Keep a changelog of dependency removals and observed effects.

### Expected outcomes (when done well)

- Smaller container images and faster pull/deploy times.
- Lower cold-start overhead and reduced memory pressure.
- Clearer ownership boundaries per microservice role.
- Fewer accidental runtime capabilities enabled by default.
