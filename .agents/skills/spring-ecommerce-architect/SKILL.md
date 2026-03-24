---
name: spring-ecommerce-architect
description: "E-commerce domain microservices built on the spring-microservices-architect platform. Provides 21 production-grade commerce services: 10 core (customer, auth, catalog, search, inventory, cart, order, payment, shipping, notification), 4 inventory-domain (ATP visibility, stock tracking, replenishment, reservation), 5 OMS-pipeline (order capture, orchestration, fulfillment, shipping logistics, returns), 2 integration (marketplace sync, ERP connectors). USE FOR: e-commerce, inventory, order management, OMS, payment, shipping, notification, marketplace sync, ERP integration, returns, fulfillment, reservation, cart, checkout, customer, catalog, search, storefront, ATP, stock tracking, replenishment. DO NOT USE FOR: non-e-commerce domains, generic microservice scaffolding (use spring-microservices-architect instead)."
license: MIT
metadata:
  author: Jay Patil
  version: "3.0.0"
---

# Spring E-Commerce Architect Skill

Domain-specific **e-commerce microservice templates** built on the [spring-microservices-architect](../spring-microservices-architect/SKILL.md) platform skill. Provides 21 production-grade commerce services organized into 4 domains.

> **Prerequisite:** This skill extends the platform skill. All patterns (scaffold, api-patterns, dockerize, governance, spring-cloud, kubernetes, etc.) come from [spring-microservices-architect](../spring-microservices-architect/SKILL.md). Load that skill first.

---

## Sub-Skills

> **MANDATORY: Before executing ANY workflow, you MUST read the corresponding sub-skill document.** The sub-skill document contains required service definitions, API patterns, event topics, and persistence strategies.

### Core Commerce (10 services + composite)

| Sub-Skill | Services | Reference |
|-----------|----------|-----------|
| **ecommerce-core** | Customer, Auth, Product Catalog, Search, Inventory, Cart, Order, Payment, Shipping, Notification + Storefront Composite | [ecommerce-core.md](sub-skills/ecommerce-core/ecommerce-core.md) |

### Inventory Domain (4 services)

| Sub-Skill | Services | Reference |
|-----------|----------|-----------|
| **ecommerce-inventory** | Inventory Visibility, Stock Tracking, Replenishment, Reservation | [ecommerce-inventory.md](sub-skills/ecommerce-inventory/ecommerce-inventory.md) |

### Order Management Pipeline (5 services)

| Sub-Skill | Services | Reference |
|-----------|----------|-----------|
| **ecommerce-oms** | Order Capture, Order Orchestration, Fulfillment, Shipping Logistics, Returns | [ecommerce-oms.md](sub-skills/ecommerce-oms/ecommerce-oms.md) |

### External Integration (2 services)

| Sub-Skill | Services | Reference |
|-----------|----------|-----------|
| **ecommerce-integration** | Marketplace Sync, ERP/Accounting Integration | [ecommerce-integration.md](sub-skills/ecommerce-integration/ecommerce-integration.md) |

---

## Workflows

Each workflow chains this skill's domain sub-skills with the platform skill's infrastructure sub-skills.

| User Intent | Workflow Chain |
|-------------|----------------|
| Build e-commerce core services | ecommerce-core → api-patterns* → scaffold* → dockerize* → governance* |
| Add inventory domain | ecommerce-inventory → api-patterns* → scaffold* → dockerize* → governance* |
| Add order management pipeline | ecommerce-oms → api-patterns* → scaffold* → dockerize* → governance* |
| Integrate with marketplaces / ERP | ecommerce-integration → api-patterns* → scaffold* → dockerize* → governance* |
| Full e-commerce platform | ecommerce-core → ecommerce-inventory → ecommerce-oms → ecommerce-integration → dockerize* → governance* |

> Items marked with `*` are sub-skills from the [spring-microservices-architect](../spring-microservices-architect/SKILL.md) platform skill.

---

## Domain Layer Map

These layers are additive — apply after the platform's `reactive` layer or higher.

| Layer | Services Added | Detection Signal |
|-------|---------------|-----------------|
| **ecommerce-core** | 10 core + storefront composite | `settings.gradle` includes `:microservices:customer-service` |
| **ecommerce-inventory** | 4 inventory-domain services | `settings.gradle` includes `:microservices:inventory-visibility-service` |
| **ecommerce-oms** | 5 OMS pipeline services | `settings.gradle` includes `:microservices:order-capture-service` |
| **ecommerce-integration** | 2 integration connectors | `settings.gradle` includes `:microservices:marketplace-sync-service` |

---

## Port Assignments (Default)

Ports are project-specific — define in `.agents/project.yml` (see platform skill's [project-config.md](../spring-microservices-architect/references/project-config.md)).

| Range | Domain |
|-------|--------|
| `7009` | Storefront Composite |
| `7010–7019` | Core Commerce Services |
| `7020–7029` | Inventory Domain |
| `7030–7039` | OMS Pipeline |
| `7040–7049` | Integration Connectors |

---

## References

| Reference | Purpose | Location |
|-----------|---------|----------|
| Platform Skill | Infrastructure patterns, conventions, governance | [spring-microservices-architect](../spring-microservices-architect/SKILL.md) |
| Project Config | Service registry, port assignments, adoption guide | [project-config.md](../spring-microservices-architect/references/project-config.md) |
| Conventions | Coding standards, Dockerfile patterns, test patterns | [conventions.md](../spring-microservices-architect/references/conventions.md) |
| Tech Stack | Version-pinned dependencies | [tech-stack.md](../spring-microservices-architect/references/tech-stack.md) |
| Layer Map | Full layer progression including e-commerce layers | [layer-map.md](../spring-microservices-architect/references/layer-map.md) |
