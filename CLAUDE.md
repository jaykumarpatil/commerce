# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-module Gradle project implementing a microservice landscape using Spring Boot 3 and Spring Cloud. Based on "Microservices with Spring Boot and Spring Cloud, Third Edition" by Magnus Larsson.

## Build & Run Commands

```bash
# Requires Java 17
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home

# Build all modules
./gradlew build

# Build without tests
./gradlew build -x test

# Run all tests
./gradlew test

# Run a specific module's tests
./gradlew :microservices:product-service:test

# Start all services with Docker Compose
docker compose up -d

# Run integration test script
./test-em-all.bash start
./test-em-all.bash stop
```

## Architecture

### Module Structure

```
Root Project
├── api/              # Shared REST interfaces and DTOs
├── util/             # Shared utilities (exception handlers, ServiceUtil)
├── microservices/
│   ├── product-service           # Core service - MongoDB reactive
│   ├── recommendation-service   # Core service - MongoDB reactive
│   ├── review-service            # Core service - JPA/MySQL
│   └── product-composite-service # Aggregator service
└── spring-cloud/
    ├── gateway/                  # Spring Cloud Gateway (edge server)
    └── authorization-server/     # OAuth 2.1 Authorization Server
```

### Package Naming

| Module | Base Package |
|--------|--------------|
| API library | `com.projects.api` |
| Core microservices | `com.projects.microservices.core.<service>` |
| Composite services | `com.projects.microservices.composite.<domain>` |

### Core Patterns

- **Composite Service**: Calls core services in parallel via WebClient, aggregates responses
- **Core Services**: Single-entity services with repository and service layer
- **Event-Driven**: Spring Cloud Stream for async messaging (RabbitMQ/Kafka)
- **Edge Server**: Gateway routes external traffic; core services hidden

### Infrastructure Services

| Service | Port | Docker Image |
|---------|------|--------------|
| product-composite-service | 7000 | |
| product-service | 7001 | |
| recommendation-service | 7002 | |
| review-service | 7003 | |
| gateway | 8443 | |
| authorization-server | 9999 | |
| MongoDB | 27017 | mongo:7.0 |
| MySQL | 3306 | mysql:8.4 |
| RabbitMQ | 5672/15672 | rabbitmq:3.13-management |

## Key Files

- `settings.gradle` - Declares all modules
- `docker-compose.yml` - Full local infrastructure
- `docker-compose-kafka.yml` - Uses Kafka instead of RabbitMQ
- `test-em-all.bash` - Automated integration tests
- `kubernetes/` - Helm charts for K8s deployment
- `config-repo/` - Externalized configuration

## Development Notes

- All services run on port 8080 inside Docker containers
- Spring profiles: `docker` (container networking), `kafka` (Kafka binder)
- Use `RANDOM_PORT` for integration tests with `@SpringBootTest`
- Global exception handling via `GlobalControllerExceptionHandler` in util module