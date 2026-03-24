# E-Commerce OMS Sub-Skill — Order Lifecycle Microservices

This sub-skill defines **5 Order Management System (OMS) microservices** that handle the complete order lifecycle from multi-channel capture through fulfillment, shipping, and returns.

> `${basePackage}` is a project-configurable placeholder. These services extend the base Order Service defined in [ecommerce-core.md](../ecommerce-core/ecommerce-core.md) with specialized capabilities.

---

## Service Overview

| # | Service | Module Path | Persistence | Event Topics |
|---|---------|------------|-------------|-------------|
| 1 | Order Capture Service | `microservices/order-capture-service` | MongoDB (reactive) | `captured-orders` |
| 2 | Order Orchestration Service | `microservices/order-orchestration-service` | MongoDB (reactive) | `order-routing` |
| 3 | Fulfillment Service | `microservices/fulfillment-service` | MongoDB (reactive) | `fulfillments` |
| 4 | Shipping & Logistics Service | `microservices/shipping-logistics-service` | MongoDB (reactive) | `shipment-events` |
| 5 | Returns & Refunds Service | `microservices/returns-service` | MongoDB (reactive) | `returns` |

### `settings.gradle` additions

```groovy
// OMS domain services:
include ':microservices:order-capture-service'
include ':microservices:order-orchestration-service'
include ':microservices:fulfillment-service'
include ':microservices:shipping-logistics-service'
include ':microservices:returns-service'
```

---

## 1. Order Capture Service

Aggregates orders from multiple sales channels (web, mobile, marketplaces like Amazon, Flipkart, Shopify) into a single normalized processing queue.

### DTO — `CapturedOrder.java`

```java
package ${basePackage}.api.core.ordercapture;

public class CapturedOrder {
  private String captureId;
  private String externalOrderId;      // marketplace order ref
  private String channel;              // WEB, MOBILE_APP, AMAZON, FLIPKART, SHOPIFY
  private String customerId;
  private List<CapturedOrderItem> items;
  private Address shippingAddress;
  private Address billingAddress;
  private BigDecimal total;
  private String currency;
  private CaptureStatus status;
  private Map<String, String> channelMetadata;  // marketplace-specific fields
  private LocalDateTime capturedAt;
  private String serviceAddress;

  public CapturedOrder() {}
}

public class CapturedOrderItem {
  private String productId;
  private String sku;
  private String externalSku;          // marketplace SKU mapping
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal lineTotal;
}

public enum CaptureStatus {
  RECEIVED, VALIDATED, NORMALIZED, ENRICHED, QUEUED, REJECTED
}
```

**Supporting DTOs:**

```java
public class ChannelConfig {
  private String channelId;
  private String channelName;          // "Amazon India", "Shopify US"
  private String channelType;          // MARKETPLACE, DIRECT, WHOLESALE
  private String apiEndpoint;
  private boolean isActive;
  private Map<String, String> credentials;  // encrypted
  private Map<String, String> skuMappings;  // external SKU → internal SKU
}
```

### Interface — `OrderCaptureService.java`

```java
package ${basePackage}.api.core.ordercapture;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderCaptureService {

  Mono<CapturedOrder> captureOrder(CapturedOrder body);

  @GetMapping(value = "/capture/{captureId}", produces = "application/json")
  Mono<CapturedOrder> getCapturedOrder(@PathVariable String captureId);

  @GetMapping(value = "/captures", produces = "application/json")
  Flux<CapturedOrder> getCapturedOrders(
      @RequestParam(value = "channel", required = false) String channel,
      @RequestParam(value = "status", required = false) String status);

  @PostMapping(value = "/capture/webhook/{channel}", produces = "application/json")
  Mono<CapturedOrder> receiveChannelWebhook(
      @PathVariable String channel,
      @RequestBody String rawPayload);

  Mono<CapturedOrder> validateAndNormalize(CapturedOrder body);
}
```

**Pattern note:** `receiveChannelWebhook` is **synchronous REST** — marketplaces POST order webhooks. `captureOrder` and `validateAndNormalize` are event-driven for internal processing. The service normalizes diverse marketplace order formats into a single `CapturedOrder` structure.

### Entity — `CapturedOrderEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.ordercapture.persistence;

@Document(collection = "captured_orders")
@CompoundIndex(name = "capture-unique", unique = true, def = "{'captureId': 1}")
@CompoundIndex(name = "external-order", unique = true,
    def = "{'channel': 1, 'externalOrderId': 1}")
public class CapturedOrderEntity {
  @Id private String id;
  @Version private Integer version;
  private String captureId;
  private String externalOrderId;
  private String channel;
  private String customerId;
  private List<CapturedOrderItemData> items;
  private Address shippingAddress;
  private Address billingAddress;
  private BigDecimal total;
  private String currency;
  private String status;
  private Map<String, String> channelMetadata;
  private LocalDateTime capturedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `captured-orders` | `captureId` | `CapturedOrder` DTO | Order received, validated, queued |

**Downstream:** Order Orchestration Service consumes `captured-orders` to route and assign fulfillment.

---

## 2. Order Orchestration Service

Uses intelligent rules to assign orders to the optimal fulfillment center based on stock availability, customer proximity, and cost optimization.

### DTO — `RoutingDecision.java`

```java
package ${basePackage}.api.core.orderorchestration;

public class RoutingDecision {
  private String routingId;
  private String orderId;              // captureId or internal orderId
  private String customerId;
  private List<RoutingAssignment> assignments;   // may split across locations
  private RoutingStrategy strategy;
  private RoutingStatus status;
  private BigDecimal estimatedShippingCost;
  private int estimatedDeliveryDays;
  private LocalDateTime decidedAt;
  private String serviceAddress;

  public RoutingDecision() {}
}

public class RoutingAssignment {
  private String assignmentId;
  private String fulfillmentLocationId;  // warehouse/store that fulfills
  private String fulfillmentLocationType;
  private List<CapturedOrderItem> items;  // items assigned to this location
  private BigDecimal shippingCost;
  private int estimatedDays;
  private String carrier;
}

public enum RoutingStrategy {
  NEAREST_WAREHOUSE, LOWEST_COST, FASTEST_DELIVERY,
  SINGLE_SHIPMENT, SPLIT_OPTIMAL
}

public enum RoutingStatus {
  PENDING, EVALUATED, ASSIGNED, FAILED
}
```

### Interface — `OrderOrchestrationService.java`

```java
package ${basePackage}.api.core.orderorchestration;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderOrchestrationService {

  Mono<RoutingDecision> routeOrder(RoutingDecision body);

  @GetMapping(value = "/routing/{routingId}", produces = "application/json")
  Mono<RoutingDecision> getRoutingDecision(@PathVariable String routingId);

  @GetMapping(value = "/routing/order/{orderId}", produces = "application/json")
  Mono<RoutingDecision> getRoutingByOrder(@PathVariable String orderId);

  @GetMapping(value = "/routing/simulate", produces = "application/json")
  Flux<RoutingDecision> simulateRouting(
      @RequestParam String orderId,
      @RequestParam(value = "strategies", required = false) List<RoutingStrategy> strategies);

  Mono<RoutingDecision> reassignOrder(RoutingDecision body);
}
```

**Pattern note:** `routeOrder` is event-driven (triggered by `captured-orders` events). `simulateRouting` is synchronous REST — allows ops teams to preview routing options without committing. `reassignOrder` is event-driven for handling fulfillment failures.

### Routing Logic (Service Impl)

The orchestration service calls Inventory Visibility and Shipping services to make routing decisions:

```java
@Override
public Mono<RoutingDecision> routeOrder(RoutingDecision body) {
  return inventoryVisibilityService.getStockByLocation(body.getItems().get(0).getProductId())
      .collectList()
      .flatMap(stockLevels -> {
        // 1. Filter locations with sufficient stock
        // 2. For each candidate, call shippingService.calculateRates()
        // 3. Apply routing strategy (nearest, cheapest, fastest)
        // 4. Generate assignment(s) — may split order across locations
        return Mono.just(buildRoutingDecision(body, stockLevels));
      });
}
```

### Entity — `RoutingDecisionEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.orderorchestration.persistence;

@Document(collection = "routing_decisions")
@CompoundIndex(name = "routing-unique", unique = true, def = "{'routingId': 1}")
@CompoundIndex(name = "order-routing", def = "{'orderId': 1}")
public class RoutingDecisionEntity {
  @Id private String id;
  @Version private Integer version;
  private String routingId;
  private String orderId;
  private String customerId;
  private List<RoutingAssignmentData> assignments;
  private String strategy;
  private String status;
  private BigDecimal estimatedShippingCost;
  private int estimatedDeliveryDays;
  private LocalDateTime decidedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `order-routing` | `orderId` | `RoutingDecision` DTO | Order routed, reassigned |

### Consumes From

| Topic | Source | Purpose |
|-------|--------|---------|
| `captured-orders` | Order Capture Service | New orders to route |
| `fulfillments` | Fulfillment Service | Fulfillment failures → re-route |

---

## 3. Fulfillment Service

Manages the physical preparation of orders, including pick list generation, packing, quality control (QC), and handoff to shipping.

### DTO — `Fulfillment.java`

```java
package ${basePackage}.api.core.fulfillment;

public class Fulfillment {
  private String fulfillmentId;
  private String orderId;
  private String assignmentId;         // from routing decision
  private String locationId;
  private List<FulfillmentItem> items;
  private FulfillmentStatus status;
  private String assignedPicker;       // warehouse staff userId
  private String assignedPacker;
  private String qcResult;             // PASS, FAIL
  private String qcNotes;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
  private String serviceAddress;

  public Fulfillment() {}
}

public class FulfillmentItem {
  private String productId;
  private String sku;
  private int quantityOrdered;
  private int quantityPicked;
  private int quantityPacked;
  private String binLocation;          // warehouse bin/shelf location
  private FulfillmentItemStatus status;
}

public enum FulfillmentStatus {
  PENDING, PICK_LIST_GENERATED, PICKING, PICKED,
  PACKING, QC_CHECK, QC_PASSED, READY_TO_SHIP, SHIPPED, FAILED
}

public enum FulfillmentItemStatus {
  PENDING, PICKED, SHORT, DAMAGED, PACKED
}
```

### Interface — `FulfillmentService.java`

```java
package ${basePackage}.api.core.fulfillment;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FulfillmentService {

  Mono<Fulfillment> createFulfillment(Fulfillment body);

  @GetMapping(value = "/fulfillment/{fulfillmentId}", produces = "application/json")
  Mono<Fulfillment> getFulfillment(@PathVariable String fulfillmentId);

  @GetMapping(value = "/fulfillment/order/{orderId}", produces = "application/json")
  Flux<Fulfillment> getFulfillmentsByOrder(@PathVariable String orderId);

  @GetMapping(value = "/fulfillment/location/{locationId}", produces = "application/json")
  Flux<Fulfillment> getFulfillmentsByLocation(
      @PathVariable String locationId,
      @RequestParam(value = "status", required = false) String status);

  @PutMapping(value = "/fulfillment/{fulfillmentId}/pick", produces = "application/json")
  Mono<Fulfillment> recordPick(
      @PathVariable String fulfillmentId,
      @RequestBody List<FulfillmentItem> pickedItems);

  @PutMapping(value = "/fulfillment/{fulfillmentId}/pack", produces = "application/json")
  Mono<Fulfillment> recordPack(
      @PathVariable String fulfillmentId,
      @RequestBody List<FulfillmentItem> packedItems);

  @PutMapping(value = "/fulfillment/{fulfillmentId}/qc", produces = "application/json")
  Mono<Fulfillment> recordQcResult(
      @PathVariable String fulfillmentId,
      @RequestParam String result,
      @RequestParam(required = false) String notes);

  Mono<Fulfillment> updateFulfillmentStatus(Fulfillment body);
}
```

**Pattern note:** `recordPick`, `recordPack`, and `recordQcResult` are **synchronous REST** — used by warehouse staff on handheld scanners during physical operations. `createFulfillment` is event-driven (triggered from `order-routing`). `updateFulfillmentStatus` is event-driven for system-generated transitions.

### Entity — `FulfillmentEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.fulfillment.persistence;

@Document(collection = "fulfillments")
@CompoundIndex(name = "fulfillment-unique", unique = true, def = "{'fulfillmentId': 1}")
@CompoundIndex(name = "order-fulfillment", def = "{'orderId': 1}")
@CompoundIndex(name = "location-status", def = "{'locationId': 1, 'status': 1}")
public class FulfillmentEntity {
  @Id private String id;
  @Version private Integer version;
  private String fulfillmentId;
  private String orderId;
  private String assignmentId;
  private String locationId;
  private List<FulfillmentItemData> items;
  private String status;
  private String assignedPicker;
  private String assignedPacker;
  private String qcResult;
  private String qcNotes;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `fulfillments` | `orderId` | `Fulfillment` DTO | Pick complete, pack complete, QC pass/fail, ready to ship |

### Consumes From

| Topic | Source | Purpose |
|-------|--------|---------|
| `order-routing` | Order Orchestration Service | Routing assignment → create fulfillment |

---

## 4. Shipping & Logistics Service

Integrates with carrier APIs to generate shipping labels, compare rates, and provide real-time tracking. Extends the base Shipping Service in [ecommerce-core.md](../ecommerce-core/ecommerce-core.md) with multi-carrier orchestration.

### DTO — `ShipmentLabel.java`

```java
package ${basePackage}.api.core.shippinglogistics;

public class ShipmentLabel {
  private String labelId;
  private String shipmentId;
  private String orderId;
  private String carrier;              // "fedex", "delhivery", "shiprocket", "dhl"
  private String serviceLevel;         // "standard", "express", "overnight"
  private String trackingNumber;
  private String labelUrl;             // PDF/ZPL label download URL
  private BigDecimal declaredValue;
  private BigDecimal shippingCost;
  private String currency;
  private Address origin;
  private Address destination;
  private List<PackageDetail> packages;
  private ShipmentLabelStatus status;
  private LocalDateTime createdAt;
  private String serviceAddress;

  public ShipmentLabel() {}
}

public class PackageDetail {
  private String packageId;
  private BigDecimal weightKg;
  private BigDecimal lengthCm;
  private BigDecimal widthCm;
  private BigDecimal heightCm;
  private int itemCount;
}

public class CarrierRate {
  private String carrier;
  private String serviceLevel;
  private BigDecimal cost;
  private String currency;
  private int estimatedDays;
  private boolean insured;
  private String rateId;               // carrier-specific rate reference
}

public class TrackingEvent {
  private String trackingNumber;
  private String status;
  private String location;
  private String description;
  private LocalDateTime timestamp;
}

public enum ShipmentLabelStatus {
  DRAFT, LABEL_CREATED, MANIFESTED, PICKED_UP,
  IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION
}
```

### Interface — `ShippingLogisticsService.java`

```java
package ${basePackage}.api.core.shippinglogistics;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShippingLogisticsService {

  @PostMapping(value = "/shipping/rates", produces = "application/json")
  Flux<CarrierRate> compareRates(@RequestBody ShipmentLabel body);

  Mono<ShipmentLabel> createLabel(ShipmentLabel body);

  @GetMapping(value = "/shipping/label/{labelId}", produces = "application/json")
  Mono<ShipmentLabel> getLabel(@PathVariable String labelId);

  @GetMapping(value = "/shipping/tracking/{trackingNumber}", produces = "application/json")
  Flux<TrackingEvent> getTrackingHistory(@PathVariable String trackingNumber);

  @PostMapping(value = "/shipping/webhook/{carrier}", produces = "application/json")
  Mono<Void> handleCarrierWebhook(
      @PathVariable String carrier,
      @RequestBody String rawPayload);

  Mono<ShipmentLabel> updateShipmentStatus(ShipmentLabel body);
}
```

**Pattern note:** `compareRates` and `getTrackingHistory` are **synchronous REST** (checkout rate display, customer tracking page). `createLabel` is event-driven (triggered from `fulfillments` when status = READY_TO_SHIP). `handleCarrierWebhook` is synchronous REST — carriers POST status updates.

### Entity — `ShipmentLabelEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.shippinglogistics.persistence;

@Document(collection = "shipment_labels")
@CompoundIndex(name = "label-unique", unique = true, def = "{'labelId': 1}")
@CompoundIndex(name = "tracking-idx", unique = true, def = "{'trackingNumber': 1}")
@CompoundIndex(name = "order-shipment", def = "{'orderId': 1}")
public class ShipmentLabelEntity {
  @Id private String id;
  @Version private Integer version;
  private String labelId;
  private String shipmentId;
  private String orderId;
  private String carrier;
  private String serviceLevel;
  private String trackingNumber;
  private String labelUrl;
  private BigDecimal declaredValue;
  private BigDecimal shippingCost;
  private String currency;
  private Address origin;
  private Address destination;
  private List<PackageDetailData> packages;
  private String status;
  private List<TrackingEventData> trackingHistory;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `shipment-events` | `orderId` | `ShipmentLabel` DTO | Label created, picked up, in transit, delivered |

### Consumes From

| Topic | Source | Purpose |
|-------|--------|---------|
| `fulfillments` | Fulfillment Service | Ready-to-ship → generate label |

---

## 5. Returns & Refunds Service

Automates reverse logistics, manages refund processing, and updates inventory once returned items are restocked.

### DTO — `ReturnRequest.java`

```java
package ${basePackage}.api.core.returns;

public class ReturnRequest {
  private String returnId;
  private String orderId;
  private String customerId;
  private List<ReturnItem> items;
  private ReturnReason reason;
  private String reasonDescription;
  private ReturnMethod method;
  private ReturnStatus status;
  private String returnLabel;          // shipping label URL
  private String trackingNumber;
  private RefundDetail refund;
  private LocalDateTime requestedAt;
  private LocalDateTime receivedAt;
  private LocalDateTime resolvedAt;
  private String serviceAddress;

  public ReturnRequest() {}
}

public class ReturnItem {
  private String productId;
  private String sku;
  private int quantity;
  private BigDecimal unitPrice;
  private String condition;            // UNOPENED, USED, DAMAGED
  private boolean restockable;
}

public class RefundDetail {
  private String refundId;
  private BigDecimal amount;
  private String currency;
  private RefundMethod refundMethod;   // ORIGINAL_PAYMENT, STORE_CREDIT
  private RefundStatus refundStatus;
  private LocalDateTime processedAt;
}

public enum ReturnReason {
  DEFECTIVE, WRONG_ITEM, NOT_AS_DESCRIBED, CHANGED_MIND,
  ARRIVED_LATE, DAMAGED_IN_TRANSIT
}

public enum ReturnMethod { PICKUP, DROP_OFF, MAIL }

public enum ReturnStatus {
  REQUESTED, APPROVED, LABEL_SENT, IN_TRANSIT,
  RECEIVED, INSPECTED, RESTOCKED, REFUNDED, REJECTED
}

public enum RefundMethod { ORIGINAL_PAYMENT, STORE_CREDIT, GIFT_CARD }

public enum RefundStatus { PENDING, PROCESSING, COMPLETED, FAILED }
```

### Interface — `ReturnsService.java`

```java
package ${basePackage}.api.core.returns;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReturnsService {

  @PostMapping(value = "/return", produces = "application/json")
  Mono<ReturnRequest> initiateReturn(@RequestBody ReturnRequest body);

  @GetMapping(value = "/return/{returnId}", produces = "application/json")
  Mono<ReturnRequest> getReturn(@PathVariable String returnId);

  @GetMapping(value = "/returns/customer/{customerId}", produces = "application/json")
  Flux<ReturnRequest> getReturnsByCustomer(@PathVariable String customerId);

  @GetMapping(value = "/returns/order/{orderId}", produces = "application/json")
  Flux<ReturnRequest> getReturnsByOrder(@PathVariable String orderId);

  @PutMapping(value = "/return/{returnId}/approve", produces = "application/json")
  Mono<ReturnRequest> approveReturn(@PathVariable String returnId);

  @PutMapping(value = "/return/{returnId}/receive", produces = "application/json")
  Mono<ReturnRequest> receiveReturn(
      @PathVariable String returnId,
      @RequestBody List<ReturnItem> inspectedItems);

  Mono<ReturnRequest> processRefund(ReturnRequest body);

  Mono<Void> restockItems(ReturnRequest body);
}
```

**Pattern note:** `initiateReturn`, `approveReturn`, and `receiveReturn` are **synchronous REST** — customer and warehouse staff interactions. `processRefund` publishes to the `payments` topic to trigger refund processing. `restockItems` publishes to the `inventory` topic to update stock levels.

### Entity — `ReturnRequestEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.returns.persistence;

@Document(collection = "returns")
@CompoundIndex(name = "return-unique", unique = true, def = "{'returnId': 1}")
@CompoundIndex(name = "order-returns", def = "{'orderId': 1}")
@CompoundIndex(name = "customer-returns", def = "{'customerId': 1, 'requestedAt': -1}")
public class ReturnRequestEntity {
  @Id private String id;
  @Version private Integer version;
  private String returnId;
  private String orderId;
  private String customerId;
  private List<ReturnItemData> items;
  private String reason;
  private String reasonDescription;
  private String method;
  private String status;
  private String returnLabel;
  private String trackingNumber;
  private RefundDetailData refund;
  private LocalDateTime requestedAt;
  private LocalDateTime receivedAt;
  private LocalDateTime resolvedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `returns` | `orderId` | `ReturnRequest` DTO | Return approved, received, refunded, restocked |

### Publishes To

| Topic | Target | Purpose |
|-------|--------|---------|
| `payments` | Payment Service | Refund processing |
| `inventory` | Inventory Service | Restock returned items |

---

## OMS Order Lifecycle Flow

```
┌──────────────┐   webhook    ┌────────────────────┐
│ Amazon/       ├─────────────►│ Order Capture      │
│ Flipkart/     │              │ Service             │
│ Shopify/Web   │              │ (normalize, enrich) │
└──────────────┘              └──────────┬───────────┘
                                         │ captured-orders
                                         ▼
                              ┌────────────────────┐
                              │ Order Orchestration│
                              │ Service             │
                              │ (route to optimal   │
                              │  fulfillment center) │
                              └──────────┬───────────┘
                                         │ order-routing
                                         ▼
                              ┌────────────────────┐
                              │ Fulfillment Service│
                              │ (pick → pack → QC) │
                              └──────────┬───────────┘
                                         │ fulfillments (READY_TO_SHIP)
                                         ▼
                              ┌────────────────────┐
                              │ Shipping & Logistics│
                              │ Service              │
                              │ (label, track, deliver)│
                              └──────────┬───────────┘
                                         │ shipment-events (DELIVERED)
                                         ▼
                              ┌────────────────────┐
                              │ Notification Service│
                              │ (email/SMS updates) │
                              └────────────────────┘

                    ── REVERSE FLOW ──

                              ┌────────────────────┐
                              │ Returns & Refunds  │
                              │ Service             │
                              │ (inspect, restock,  │
                              │  process refund)     │
                              └─────┬──────┬────────┘
                                    │      │
                            returns │      │ returns
                                    ▼      ▼
                              inventory  payments
                              (restock)  (refund)
```

---

## Port Assignments

| Service | Default Port | Docker Port |
|---------|-------------|-------------|
| order-capture-service | 7030 | 8080 |
| order-orchestration-service | 7031 | 8080 |
| fulfillment-service | 7032 | 8080 |
| shipping-logistics-service | 7033 | 8080 |
| returns-service | 7034 | 8080 |

---

## Checklist

- [ ] Order Capture Service deduplicates by (`channel`, `externalOrderId`) compound index
- [ ] Order Orchestration Service calls Inventory Visibility and Shipping for routing logic
- [ ] Fulfillment Service exposes warehouse-staff-facing REST endpoints for pick/pack/QC
- [ ] Shipping Service handles carrier webhooks with per-carrier signature validation
- [ ] Returns Service publishes to `payments` (refund) and `inventory` (restock) topics
- [ ] All services use `@Version` for optimistic concurrency
- [ ] Event flow follows the linear pipeline: capture → route → fulfill → ship → notify
- [ ] Reverse flow (returns) reconnects to inventory and payments
- [ ] `settings.gradle`, `docker-compose.yml`, `test-em-all.bash` updated for all 5 services
