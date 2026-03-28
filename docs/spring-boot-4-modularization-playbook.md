# Spring Boot 4 Modularization Playbook

This playbook is for **iterative dependency reduction** in the commerce platform while preserving production behavior.

## 1) Minimize Dependencies and Broad Starters

### Starter minimization policy

Prefer narrow dependencies over broad starters in internal modules:

- Keep `spring-boot-starter-web` only in edge/API modules.
- Use `org.springframework:spring-context` for pure service/component modules.
- Remove persistence starters (`data-jpa`, `data-r2dbc`, `data-redis`, etc.) from modules that only expose APIs or marker types.
- Keep database drivers only in modules that really access that datastore.

### Role-classpath contract

Use these classpath rules when splitting services:

- **Edge** (north-south traffic): web starter, security starter, actuator, API docs.
- **Orchestrator** (workflow/composition): web starter if sync APIs are needed, otherwise messaging + context + actuator.
- **Worker** (async/background): context + messaging + actuator, avoid web starter and servlet/reactive server dependencies.

## 2) Audit and Exclude Unused Auto-Configurations

Run each service with:

- `--debug` to inspect auto-configuration matches.
- `management.endpoints.web.exposure.include=conditions,beans,startup,health,info,metrics`

Prune in this order:

1. Remove the dependency that triggers the unwanted auto-config.
2. If dependency must remain, use `spring.autoconfigure.exclude` to disable specific auto-config classes.
3. Re-run smoke tests and startup probes.

## 3) Align Module Choices with AOT/Native Goals

For modules that target GraalVM native image:

- Prefer explicit bean wiring over dynamic classpath scanning.
- Keep reflection-heavy libraries out of worker modules.
- Keep optional integrations in dedicated modules so they can be excluded from native builds.
- Validate each role independently with AOT processing before building native images.

## 4) Measure Impact After Every Reduction

Track these four metrics after each dependency change:

1. Startup time (ms)
2. RSS memory (KB/MB)
3. Container image size (bytes/MB)
4. Class count in runtime classpath/JAR

Use `tools/measure-modularization-impact.sh` to standardize measurements and compare before/after snapshots.

## 5) Suggested Iteration Loop

1. Pick one service role (edge/orchestrator/worker).
2. Remove one broad starter or transitive dependency cluster.
3. Run tests + smoke checks.
4. Capture metrics.
5. Commit only if metrics are neutral or improved and behavior remains correct.

This keeps changes safe while steadily reducing startup cost, memory footprint, and native-image complexity.
