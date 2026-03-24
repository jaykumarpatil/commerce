# E-Commerce Inventory Sub-Skill — Stock Visibility, Tracking, Forecasting & Reservation

This sub-skill defines **4 specialized inventory microservices** that extend the base Inventory Service (see [ecommerce-core.md](../ecommerce-core/ecommerce-core.md)) with SKU-level visibility, audit trails, AI-driven replenishment, and checkout-time stock reservation.

> `${basePackage}` is a project-configurable placeholder. All services share the common `api/`, `util/`, and `Event<K, T>` infrastructure.

---

## Service Overview

| # | Service | Module Path | Persistence | Event Topics |
|---|---------|------------|-------------|-------------|
| 1 | Inventory Visibility Service | `microservices/inventory-visibility-service` | MongoDB (reactive) + Redis (cache) | `inventory-visibility` |
| 2 | Stock Tracking & Audit Service | `microservices/stock-tracking-service` | MongoDB (reactive) | `stock-audits` |
| 3 | Replenishment & Forecasting Service | `microservices/replenishment-service` | MongoDB (reactive) | `replenishment-orders` |
| 4 | Reservation Service | `microservices/reservation-service` | Redis (primary) + MongoDB (durability) | `reservations` |

### `settings.gradle` additions

```groovy
// Inventory domain services:
include ':microservices:inventory-visibility-service'
include ':microservices:stock-tracking-service'
include ':microservices:replenishment-service'
include ':microservices:reservation-service'
```

---

## 1. Inventory Visibility Service

Provides real-time SKU-level stock availability across all warehouses, physical stores, and 3PL partners. Acts as the **single source of truth** for available-to-promise (ATP) quantities.

### DTO — `StockLevel.java`

```java
package ${basePackage}.api.core.inventoryvisibility;

public class StockLevel {
  private String productId;
  private String sku;
  private String locationId;           // warehouse, store, or 3PL partner
  private String locationType;         // WAREHOUSE, STORE, THREE_PL
  private int quantityOnHand;
  private int quantityReserved;
  private int quantityInTransit;
  private int quantityAvailableToPromise;  // ATP = onHand - reserved + inTransit
  private LocalDateTime lastSyncedAt;
  private String serviceAddress;

  public StockLevel() {}
}
```

**Supporting DTOs:**

```java
public class AggregatedStock {
  private String productId;
  private String sku;
  private int totalOnHand;
  private int totalReserved;
  private int totalAvailableToPromise;
  private List<StockLevel> byLocation;
}

public class StockSyncEvent {
  private String locationId;
  private String locationType;
  private List<StockLevel> stockLevels;
  private LocalDateTime syncTimestamp;
  private String source;               // "WMS", "POS", "3PL_API"
}
```

### Interface — `InventoryVisibilityService.java`

```java
package ${basePackage}.api.core.inventoryvisibility;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryVisibilityService {

  @GetMapping(value = "/visibility/{productId}", produces = "application/json")
  Mono<AggregatedStock> getAggregatedStock(@PathVariable String productId);

  @GetMapping(value = "/visibility/{productId}/locations", produces = "application/json")
  Flux<StockLevel> getStockByLocation(@PathVariable String productId);

  @GetMapping(value = "/visibility/location/{locationId}", produces = "application/json")
  Flux<StockLevel> getStockAtLocation(@PathVariable String locationId);

  Mono<Void> syncStockLevels(StockSyncEvent syncEvent);

  Mono<Void> adjustQuantity(StockLevel adjustment);
}
```

**Pattern note:** All read endpoints are synchronous REST (PDP/checkout pages need low-latency ATP). `syncStockLevels` and `adjustQuantity` are event-driven — triggered by warehouse management systems, POS updates, or 3PL partner feeds.

### Entity — `StockLevelEntity.java` (MongoDB + Redis cache)

```java
package ${basePackage}.microservices.core.inventoryvisibility.persistence;

@Document(collection = "stock_levels")
@CompoundIndex(name = "product-location", unique = true,
    def = "{'productId': 1, 'locationId': 1}")
public class StockLevelEntity {
  @Id private String id;
  @Version private Integer version;
  private String productId;
  private String sku;
  private String locationId;
  private String locationType;
  private int quantityOnHand;
  private int quantityReserved;
  private int quantityInTransit;
  private LocalDateTime lastSyncedAt;
}
```

**Redis cache layer:**
```java
// ATP cache for high-frequency reads (product detail page, cart)
@RedisHash(value = "atp", timeToLive = 30)  // 30s TTL, refreshed on stock change
public class AtpCacheEntry {
  @Id private String productId;
  private int availableToPromise;
  private LocalDateTime cachedAt;
}
```

### Additional Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `inventory-visibility` | `productId` | `StockLevel` DTO | ATP recalculated, stock synced |

### Consumes From

| Topic | Source | Purpose |
|-------|--------|---------|
| `inventory` | Inventory Service | Base stock adjustments |
| `reservations` | Reservation Service | Reserved quantity changes |

---

## 2. Stock Tracking & Audit Service

Manages day-to-day stock accuracy through barcode scanning, cycle counts, and stock adjustments. Provides a **complete audit trail** for every inventory movement.

### DTO — `StockAudit.java`

```java
package ${basePackage}.api.core.stocktracking;

public class StockAudit {
  private String auditId;
  private String productId;
  private String sku;
  private String locationId;
  private AuditType type;
  private int quantityBefore;
  private int quantityAfter;
  private int quantityDelta;           // positive = gain, negative = loss
  private String reason;               // "CYCLE_COUNT", "DAMAGE", "THEFT", "CORRECTION"
  private String performedBy;          // userId
  private String barcodeScanned;
  private LocalDateTime performedAt;
  private String serviceAddress;

  public StockAudit() {}
}

public enum AuditType {
  CYCLE_COUNT, STOCK_ADJUSTMENT, RECEIVING, DAMAGE_WRITE_OFF,
  RETURN_RESTOCK, TRANSFER_IN, TRANSFER_OUT
}
```

**Supporting DTOs:**

```java
public class CycleCount {
  private String cycleCountId;
  private String locationId;
  private List<CycleCountItem> items;
  private CycleCountStatus status;     // PLANNED, IN_PROGRESS, COMPLETED, RECONCILED
  private String assignedTo;
  private LocalDateTime scheduledAt;
  private LocalDateTime completedAt;
}

public class CycleCountItem {
  private String productId;
  private String sku;
  private int expectedQuantity;
  private int countedQuantity;
  private int variance;
}
```

### Interface — `StockTrackingService.java`

```java
package ${basePackage}.api.core.stocktracking;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockTrackingService {

  Mono<StockAudit> recordAdjustment(StockAudit body);

  @GetMapping(value = "/audit/{productId}", produces = "application/json")
  Flux<StockAudit> getAuditTrail(
      @PathVariable String productId,
      @RequestParam(value = "locationId", required = false) String locationId);

  @PostMapping(value = "/cycle-count", produces = "application/json")
  Mono<CycleCount> initiateCycleCount(@RequestBody CycleCount body);

  @PutMapping(value = "/cycle-count/{cycleCountId}", produces = "application/json")
  Mono<CycleCount> submitCycleCount(
      @PathVariable String cycleCountId,
      @RequestBody CycleCount body);

  @GetMapping(value = "/cycle-count/{cycleCountId}", produces = "application/json")
  Mono<CycleCount> getCycleCount(@PathVariable String cycleCountId);

  Mono<StockAudit> recordBarcodeScan(StockAudit body);
}
```

**Pattern note:** `initiateCycleCount`, `submitCycleCount`, and `getCycleCount` are **synchronous REST** — used by warehouse staff on handheld scanners. `recordAdjustment` and `recordBarcodeScan` may be event-driven for batch processing from WMS feeds.

### Entity — `StockAuditEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.stocktracking.persistence;

@Document(collection = "stock_audits")
@CompoundIndex(name = "product-location-time",
    def = "{'productId': 1, 'locationId': 1, 'performedAt': -1}")
public class StockAuditEntity {
  @Id private String id;
  @Version private Integer version;
  private String auditId;
  private String productId;
  private String sku;
  private String locationId;
  private String type;
  private int quantityBefore;
  private int quantityAfter;
  private int quantityDelta;
  private String reason;
  private String performedBy;
  private String barcodeScanned;
  private LocalDateTime performedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `stock-audits` | `productId` | `StockAudit` DTO | Adjustment recorded, cycle count reconciled |

**Downstream:** Inventory Visibility Service consumes `stock-audits` to recalculate ATP.

---

## 3. Replenishment & Forecasting Service

Uses historical data and configurable rules (or AI-driven insights) to automate reordering when stock is low and predict future demand.

### DTO — `ReplenishmentOrder.java`

```java
package ${basePackage}.api.core.replenishment;

public class ReplenishmentOrder {
  private String replenishmentId;
  private String productId;
  private String sku;
  private String supplierId;
  private String destinationLocationId;
  private int orderQuantity;
  private int currentStock;
  private int reorderPoint;
  private ReplenishmentStatus status;
  private ReplenishmentTrigger trigger;
  private LocalDateTime createdAt;
  private LocalDateTime expectedDeliveryAt;
  private String serviceAddress;

  public ReplenishmentOrder() {}
}

public enum ReplenishmentStatus {
  SUGGESTED, APPROVED, ORDERED, IN_TRANSIT, RECEIVED, CANCELLED
}

public enum ReplenishmentTrigger {
  THRESHOLD_BREACH, FORECAST_DRIVEN, MANUAL, SEASONAL_ADJUSTMENT
}
```

**Supporting DTOs:**

```java
public class DemandForecast {
  private String productId;
  private String sku;
  private List<ForecastPeriod> periods;
  private double confidenceScore;
  private String modelVersion;
  private LocalDateTime generatedAt;
}

public class ForecastPeriod {
  private LocalDate startDate;
  private LocalDate endDate;
  private int predictedDemand;
  private int safetyStock;
  private int recommendedReorderQuantity;
}

public class ReplenishmentRule {
  private String ruleId;
  private String productId;
  private String sku;
  private int reorderPoint;            // trigger when ATP <= this
  private int reorderQuantity;         // how much to order
  private int safetyStockDays;         // days of safety stock
  private String preferredSupplierId;
  private boolean autoApprove;         // auto-approve below threshold
}
```

### Interface — `ReplenishmentService.java`

```java
package ${basePackage}.api.core.replenishment;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReplenishmentService {

  Mono<ReplenishmentOrder> createReplenishmentOrder(ReplenishmentOrder body);

  @GetMapping(value = "/replenishment/{replenishmentId}", produces = "application/json")
  Mono<ReplenishmentOrder> getReplenishmentOrder(@PathVariable String replenishmentId);

  @GetMapping(value = "/replenishment/product/{productId}", produces = "application/json")
  Flux<ReplenishmentOrder> getReplenishmentsByProduct(@PathVariable String productId);

  @GetMapping(value = "/forecast/{productId}", produces = "application/json")
  Mono<DemandForecast> getDemandForecast(
      @PathVariable String productId,
      @RequestParam(value = "periods", defaultValue = "4") int periods);

  @PutMapping(value = "/replenishment/rules/{productId}", produces = "application/json")
  Mono<ReplenishmentRule> updateReplenishmentRule(
      @PathVariable String productId,
      @RequestBody ReplenishmentRule rule);

  Mono<ReplenishmentOrder> approveReplenishment(ReplenishmentOrder body);
}
```

**Pattern note:** `getDemandForecast` and rule management are synchronous REST for supply chain managers. `createReplenishmentOrder` is event-driven — triggered when the Inventory Visibility Service detects ATP below the reorder point.

### Entity — `ReplenishmentOrderEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.replenishment.persistence;

@Document(collection = "replenishment_orders")
@CompoundIndex(name = "repl-unique", unique = true, def = "{'replenishmentId': 1}")
@CompoundIndex(name = "product-status", def = "{'productId': 1, 'status': 1}")
public class ReplenishmentOrderEntity {
  @Id private String id;
  @Version private Integer version;
  private String replenishmentId;
  private String productId;
  private String sku;
  private String supplierId;
  private String destinationLocationId;
  private int orderQuantity;
  private int currentStock;
  private int reorderPoint;
  private String status;
  private String trigger;
  private LocalDateTime createdAt;
  private LocalDateTime expectedDeliveryAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `replenishment-orders` | `productId` | `ReplenishmentOrder` DTO | Order created, approved, received |

### Consumes From

| Topic | Source | Purpose |
|-------|--------|---------|
| `inventory-visibility` | Inventory Visibility Service | Detect low-stock threshold breaches |

---

## 4. Reservation Service

Temporarily "locks" stock when a customer adds an item to their cart or reaches checkout. Prevents overselling during high-traffic events.

### DTO — `Reservation.java`

```java
package ${basePackage}.api.core.reservation;

public class Reservation {
  private String reservationId;
  private String customerId;
  private String productId;
  private String sku;
  private String locationId;
  private int quantity;
  private ReservationStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;       // TTL — auto-release after expiry
  private String sourceType;             // "CART", "CHECKOUT"
  private String sourceId;               // cartId or orderId
  private String serviceAddress;

  public Reservation() {}
}

public enum ReservationStatus {
  ACTIVE, CONFIRMED, RELEASED, EXPIRED, CANCELLED
}
```

### Interface — `ReservationService.java`

```java
package ${basePackage}.api.core.reservation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReservationService {

  Mono<Reservation> createReservation(Reservation body);

  @GetMapping(value = "/reservation/{reservationId}", produces = "application/json")
  Mono<Reservation> getReservation(@PathVariable String reservationId);

  @GetMapping(value = "/reservations/customer/{customerId}", produces = "application/json")
  Flux<Reservation> getReservationsByCustomer(@PathVariable String customerId);

  @GetMapping(value = "/reservations/product/{productId}", produces = "application/json")
  Flux<Reservation> getReservationsByProduct(@PathVariable String productId);

  Mono<Reservation> confirmReservation(Reservation body);

  Mono<Void> releaseReservation(String reservationId);

  Mono<Void> expireReservations();     // scheduled — releases all expired
}
```

**Pattern note:** `createReservation` is event-driven (triggered from `carts` topic when items are added). `confirmReservation` is triggered from `orders` topic when order is placed. `releaseReservation` is triggered when cart items are removed or cart expires. `expireReservations` is a **scheduled task** (not an event).

### Persistence — Redis (primary) + MongoDB (durability)

```java
// Redis for fast lock/unlock with TTL (prevents overselling under load)
@RedisHash(value = "reservation")
public class ReservationRedisEntity {
  @Id private String reservationId;
  @TimeToLive private Long ttlSeconds;   // auto-expire
  private String customerId;
  private String productId;
  private String sku;
  private String locationId;
  private int quantity;
  private String status;
  private LocalDateTime expiresAt;
}

// MongoDB for audit and recovery
@Document(collection = "reservations")
@CompoundIndex(name = "reservation-unique", unique = true, def = "{'reservationId': 1}")
public class ReservationEntity {
  @Id private String id;
  @Version private Integer version;
  private String reservationId;
  private String customerId;
  private String productId;
  private String sku;
  private String locationId;
  private int quantity;
  private String status;
  private String sourceType;
  private String sourceId;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;
}
```

### Additional Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis-reactive'
implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `reservations` | `productId` | `Reservation` DTO | Created, confirmed, released, expired |

### Consumes From

| Topic | Source | Purpose |
|-------|--------|---------|
| `carts` | Cart Service | Cart item added → create reservation |
| `orders` | Order Service | Order placed → confirm reservation |

**Downstream:** Inventory Visibility Service consumes `reservations` to update reserved quantities in ATP calculation.

---

## Cross-Service Event Flow

```
┌─────────────────┐         ┌─────────────────────────┐
│   Cart Service   │────────►│   Reservation Service   │
│ (item added)     │  carts  │ (lock stock with TTL)   │
└─────────────────┘         └───────────┬─────────────┘
                                        │ reservations
                                        ▼
┌─────────────────┐         ┌─────────────────────────┐
│   Order Service  │────────►│ Inventory Visibility    │
│ (order placed)   │  orders │ (recalculate ATP)       │
└─────────────────┘         └───────────┬─────────────┘
                                        │ inventory-visibility
                                        ▼
┌─────────────────┐         ┌─────────────────────────┐
│   WMS / POS      │────────►│ Stock Tracking & Audit  │
│ (barcode scan)   │  stock  │ (record adjustment)     │
└─────────────────┘ audits  └───────────┬─────────────┘
                                        │ stock-audits
                                        ▼
                            ┌─────────────────────────┐
                            │ Replenishment Service   │
                            │ (auto-reorder if low)   │
                            └─────────────────────────┘
```

---

## Port Assignments

| Service | Default Port | Docker Port |
|---------|-------------|-------------|
| inventory-visibility-service | 7020 | 8080 |
| stock-tracking-service | 7021 | 8080 |
| replenishment-service | 7022 | 8080 |
| reservation-service | 7023 | 8080 |

---

## Checklist

- [ ] Inventory Visibility Service maintains a Redis ATP cache (30s TTL) for PDP reads
- [ ] Stock Tracking Service records an immutable audit trail for every stock movement
- [ ] Replenishment Service consumes `inventory-visibility` topic for threshold-based triggers
- [ ] Reservation Service uses Redis with TTL for automatic expiration of stale locks
- [ ] Reservation Service dual-writes to Redis (speed) and MongoDB (durability/audit)
- [ ] All four services publish to their own event topics
- [ ] Cross-service event flow connects Cart → Reservation → Visibility → Replenishment
- [ ] Cycle count endpoints are synchronous REST for warehouse staff handheld scanners
- [ ] `settings.gradle`, `docker-compose.yml`, `test-em-all.bash` updated for all 4 services
