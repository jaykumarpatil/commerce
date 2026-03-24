# E-Commerce Integration Sub-Skill — Marketplace Sync & ERP Connectors

This sub-skill defines **2 integration microservices** that synchronize the e-commerce platform with external ecosystems: marketplaces (Amazon, Flipkart, Myntra, Shopify) and enterprise backend systems (Oracle NetSuite, SAP Business One, QuickBooks).

> `${basePackage}` is a project-configurable placeholder. All services share the common `api/`, `util/`, and `Event<K, T>` infrastructure.

---

## Service Overview

| # | Service | Module Path | Persistence | Event Topics |
|---|---------|------------|-------------|-------------|
| 1 | Marketplace Sync Service | `microservices/marketplace-sync-service` | MongoDB (reactive) | `marketplace-sync` |
| 2 | ERP/Accounting Integration | `microservices/erp-integration-service` | MongoDB (reactive) | `erp-sync` |

### `settings.gradle` additions

```groovy
// Integration services:
include ':microservices:marketplace-sync-service'
include ':microservices:erp-integration-service'
```

---

## 1. Marketplace Sync Service

Keeps inventory levels, pricing, product listings, and order statuses consistent across external platforms like Amazon, Flipkart, Myntra, and Shopify.

### DTO — `MarketplaceListing.java`

```java
package ${basePackage}.api.core.marketplacesync;

public class MarketplaceListing {
  private String listingId;
  private String productId;            // internal product reference
  private String sku;
  private String marketplace;          // AMAZON, FLIPKART, MYNTRA, SHOPIFY
  private String externalListingId;    // marketplace's listing ID
  private String externalSku;          // marketplace SKU (may differ)
  private String title;
  private String description;
  private BigDecimal price;
  private BigDecimal compareAtPrice;   // strikethrough price
  private String currency;
  private int stockQuantity;           // synced from inventory
  private ListingStatus status;
  private LocalDateTime lastSyncedAt;
  private String serviceAddress;

  public MarketplaceListing() {}
}

public enum ListingStatus {
  ACTIVE, INACTIVE, PENDING_APPROVAL, SUPPRESSED, OUT_OF_STOCK, ERROR
}
```

**Supporting DTOs:**

```java
public class SyncJob {
  private String jobId;
  private String marketplace;
  private SyncDirection direction;     // OUTBOUND (push) or INBOUND (pull)
  private SyncType type;               // INVENTORY, PRICING, LISTING, ORDER_STATUS
  private SyncJobStatus status;
  private int totalItems;
  private int successCount;
  private int failureCount;
  private List<SyncError> errors;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
}

public class SyncError {
  private String productId;
  private String externalSku;
  private String errorCode;
  private String errorMessage;
  private boolean retryable;
}

public enum SyncDirection { OUTBOUND, INBOUND }

public enum SyncType { INVENTORY, PRICING, LISTING, ORDER_STATUS, FULL }

public enum SyncJobStatus { QUEUED, IN_PROGRESS, COMPLETED, PARTIAL_FAILURE, FAILED }

public class MarketplaceConfig {
  private String configId;
  private String marketplace;
  private String sellerId;
  private String apiEndpoint;
  private Map<String, String> credentials;  // encrypted API keys/tokens
  private boolean inventorySyncEnabled;
  private boolean pricingSyncEnabled;
  private int syncIntervalMinutes;
  private Map<String, String> skuMappings;  // internal SKU → marketplace SKU
}
```

### Interface — `MarketplaceSyncService.java`

```java
package ${basePackage}.api.core.marketplacesync;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MarketplaceSyncService {

  @GetMapping(value = "/marketplace/listing/{listingId}", produces = "application/json")
  Mono<MarketplaceListing> getListing(@PathVariable String listingId);

  @GetMapping(value = "/marketplace/listings", produces = "application/json")
  Flux<MarketplaceListing> getListings(
      @RequestParam(value = "marketplace", required = false) String marketplace,
      @RequestParam(value = "productId", required = false) String productId);

  @PostMapping(value = "/marketplace/sync", produces = "application/json")
  Mono<SyncJob> triggerSync(@RequestBody SyncJob body);

  @GetMapping(value = "/marketplace/sync/{jobId}", produces = "application/json")
  Mono<SyncJob> getSyncJobStatus(@PathVariable String jobId);

  @GetMapping(value = "/marketplace/sync/history", produces = "application/json")
  Flux<SyncJob> getSyncHistory(
      @RequestParam(value = "marketplace", required = false) String marketplace,
      @RequestParam(value = "type", required = false) String type);

  Mono<Void> syncInventoryToMarketplace(MarketplaceListing listing);

  Mono<Void> syncPricingToMarketplace(MarketplaceListing listing);

  Mono<Void> pullOrdersFromMarketplace(String marketplace);
}
```

**Pattern note:** `triggerSync`, `getListing`, `getSyncJobStatus`, and `getSyncHistory` are **synchronous REST** — operations dashboards and manual sync triggers. `syncInventoryToMarketplace` and `syncPricingToMarketplace` are event-driven — triggered when internal inventory or pricing changes. `pullOrdersFromMarketplace` is scheduled (cron-based) or event-triggered.

### Entity — `MarketplaceListingEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.marketplacesync.persistence;

@Document(collection = "marketplace_listings")
@CompoundIndex(name = "product-marketplace", unique = true,
    def = "{'productId': 1, 'marketplace': 1}")
@CompoundIndex(name = "external-listing", unique = true,
    def = "{'marketplace': 1, 'externalListingId': 1}")
public class MarketplaceListingEntity {
  @Id private String id;
  @Version private Integer version;
  private String listingId;
  private String productId;
  private String sku;
  private String marketplace;
  private String externalListingId;
  private String externalSku;
  private String title;
  private String description;
  private BigDecimal price;
  private BigDecimal compareAtPrice;
  private String currency;
  private int stockQuantity;
  private String status;
  private LocalDateTime lastSyncedAt;
  private LocalDateTime createdAt;
}
```

### Sync Job Entity — `SyncJobEntity.java` (MongoDB)

```java
@Document(collection = "sync_jobs")
@CompoundIndex(name = "job-unique", unique = true, def = "{'jobId': 1}")
public class SyncJobEntity {
  @Id private String id;
  @Version private Integer version;
  private String jobId;
  private String marketplace;
  private String direction;
  private String type;
  private String status;
  private int totalItems;
  private int successCount;
  private int failureCount;
  private List<SyncErrorData> errors;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `marketplace-sync` | `productId` | `MarketplaceListing` DTO | Inventory/pricing synced, listing updated |

### Consumes From

| Topic | Source | Purpose |
|-------|--------|---------|
| `inventory-visibility` | Inventory Visibility Service | Stock changes → push to marketplaces |
| `products` | Product Catalog Service | Price/listing changes → push to marketplaces |

### Marketplace API Integration Pattern

```java
@Component
public class MarketplaceApiClient {

  // Strategy pattern — each marketplace has its own adapter
  private final Map<String, MarketplaceAdapter> adapters;

  public Mono<Void> pushInventory(String marketplace, String externalSku, int quantity) {
    return adapters.get(marketplace).updateInventory(externalSku, quantity);
  }

  public Mono<Void> pushPricing(String marketplace, String externalSku, BigDecimal price) {
    return adapters.get(marketplace).updatePrice(externalSku, price);
  }

  public Flux<CapturedOrder> pullOrders(String marketplace, LocalDateTime since) {
    return adapters.get(marketplace).fetchNewOrders(since);
  }
}

// Each marketplace implements this interface
public interface MarketplaceAdapter {
  Mono<Void> updateInventory(String externalSku, int quantity);
  Mono<Void> updatePrice(String externalSku, BigDecimal price);
  Flux<CapturedOrder> fetchNewOrders(LocalDateTime since);
  String getMarketplaceName();
}
```

### Additional Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-webflux'    // WebClient for APIs
implementation 'org.springframework.boot:spring-boot-starter-data-mongodb-reactive'
// Per-marketplace SDK (optional):
// implementation 'com.amazon:selling-partner-api:...'
// implementation 'com.shopify:shopify-api:...'
```

---

## 2. ERP/Accounting Integration Service

Syncs order data, financial transactions, and inventory updates with backend enterprise systems like Oracle NetSuite, SAP Business One, QuickBooks, or Tally.

### DTO — `ErpSyncRecord.java`

```java
package ${basePackage}.api.core.erpintegration;

public class ErpSyncRecord {
  private String syncRecordId;
  private String entityType;           // ORDER, INVOICE, PAYMENT, INVENTORY, CUSTOMER
  private String entityId;             // orderId, paymentId, etc.
  private String erpSystem;            // NETSUITE, SAP_B1, QUICKBOOKS, TALLY
  private String externalRefId;        // ERP's internal reference
  private SyncDirection direction;     // OUTBOUND (push to ERP), INBOUND (pull from ERP)
  private ErpSyncStatus status;
  private String requestPayload;       // JSON sent to ERP
  private String responsePayload;      // JSON received from ERP
  private String errorMessage;
  private int retryCount;
  private LocalDateTime syncedAt;
  private String serviceAddress;

  public ErpSyncRecord() {}
}

public enum ErpSyncStatus {
  PENDING, IN_PROGRESS, SYNCED, FAILED, RETRY_SCHEDULED
}
```

**Supporting DTOs:**

```java
public class ErpInvoice {
  private String invoiceId;
  private String orderId;
  private String customerId;
  private String erpCustomerId;        // ERP's customer ref
  private List<ErpLineItem> lineItems;
  private BigDecimal subtotal;
  private BigDecimal taxAmount;
  private BigDecimal total;
  private String currency;
  private String paymentTerms;         // "NET30", "DUE_ON_RECEIPT"
  private LocalDateTime invoiceDate;
}

public class ErpLineItem {
  private String productId;
  private String erpItemId;            // ERP's item ref
  private String description;
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal taxRate;
  private BigDecimal lineTotal;
}

public class ErpConfig {
  private String configId;
  private String erpSystem;
  private String apiEndpoint;
  private Map<String, String> credentials;
  private boolean orderSyncEnabled;
  private boolean invoiceSyncEnabled;
  private boolean inventorySyncEnabled;
  private int syncIntervalMinutes;
  private Map<String, String> accountMappings;  // internal account → ERP account
  private Map<String, String> itemMappings;     // internal SKU → ERP item ID
}

public class ErpSyncJob {
  private String jobId;
  private String erpSystem;
  private String entityType;
  private SyncDirection direction;
  private ErpSyncJobStatus status;
  private int totalRecords;
  private int successCount;
  private int failureCount;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
}

public enum ErpSyncJobStatus { QUEUED, IN_PROGRESS, COMPLETED, PARTIAL_FAILURE, FAILED }
```

### Interface — `ErpIntegrationService.java`

```java
package ${basePackage}.api.core.erpintegration;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ErpIntegrationService {

  @PostMapping(value = "/erp/sync", produces = "application/json")
  Mono<ErpSyncJob> triggerErpSync(@RequestBody ErpSyncJob body);

  @GetMapping(value = "/erp/sync/{jobId}", produces = "application/json")
  Mono<ErpSyncJob> getSyncJobStatus(@PathVariable String jobId);

  @GetMapping(value = "/erp/sync/history", produces = "application/json")
  Flux<ErpSyncJob> getSyncHistory(
      @RequestParam(value = "erpSystem", required = false) String erpSystem,
      @RequestParam(value = "entityType", required = false) String entityType);

  @GetMapping(value = "/erp/record/{syncRecordId}", produces = "application/json")
  Mono<ErpSyncRecord> getSyncRecord(@PathVariable String syncRecordId);

  Mono<ErpSyncRecord> syncOrderToErp(ErpSyncRecord body);

  Mono<ErpSyncRecord> syncInvoiceToErp(ErpSyncRecord body);

  Mono<ErpSyncRecord> syncPaymentToErp(ErpSyncRecord body);

  Mono<Void> pullInventoryFromErp(String erpSystem);
}
```

**Pattern note:** `triggerErpSync`, `getSyncJobStatus`, `getSyncHistory`, and `getSyncRecord` are **synchronous REST** — operations dashboard and manual triggers. `syncOrderToErp`, `syncInvoiceToErp`, `syncPaymentToErp` are event-driven — triggered by `orders`, `payments` topic events. `pullInventoryFromErp` is scheduled (cron).

### Entity — `ErpSyncRecordEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.erpintegration.persistence;

@Document(collection = "erp_sync_records")
@CompoundIndex(name = "sync-unique", unique = true, def = "{'syncRecordId': 1}")
@CompoundIndex(name = "entity-erp",
    def = "{'entityType': 1, 'entityId': 1, 'erpSystem': 1}")
public class ErpSyncRecordEntity {
  @Id private String id;
  @Version private Integer version;
  private String syncRecordId;
  private String entityType;
  private String entityId;
  private String erpSystem;
  private String externalRefId;
  private String direction;
  private String status;
  private String requestPayload;
  private String responsePayload;
  private String errorMessage;
  private int retryCount;
  private LocalDateTime syncedAt;
  private LocalDateTime createdAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `erp-sync` | `entityId` | `ErpSyncRecord` DTO | Order/Invoice/Payment synced to ERP |

### Consumes From

| Topic | Source | Purpose |
|-------|--------|---------|
| `orders` | Order Service | New/updated orders → push to ERP |
| `payments` | Payment Service | Payments → push to ERP as receipts |
| `returns` | Returns Service | Refunds → push to ERP as credit memos |

### ERP Adapter Pattern

```java
@Component
public class ErpApiClient {

  private final Map<String, ErpAdapter> adapters;

  public Mono<String> pushOrder(String erpSystem, ErpInvoice invoice) {
    return adapters.get(erpSystem).createSalesOrder(invoice);
  }

  public Mono<String> pushPayment(String erpSystem, String orderId, BigDecimal amount) {
    return adapters.get(erpSystem).recordPayment(orderId, amount);
  }

  public Flux<ErpSyncRecord> pullInventory(String erpSystem) {
    return adapters.get(erpSystem).fetchInventoryUpdates();
  }
}

public interface ErpAdapter {
  Mono<String> createSalesOrder(ErpInvoice invoice);
  Mono<String> recordPayment(String orderId, BigDecimal amount);
  Mono<String> createCreditMemo(String orderId, BigDecimal amount);
  Flux<ErpSyncRecord> fetchInventoryUpdates();
  String getErpSystemName();
}
```

### Retry and Dead Letter Configuration

ERP integrations are prone to transient failures. Configure aggressive retry with DLQ:

```yaml
spring.cloud.stream.bindings.messageProcessor-in-0.consumer:
  maxAttempts: 5
  backOffInitialInterval: 2000
  backOffMaxInterval: 30000
  backOffMultiplier: 3.0

spring.cloud.stream.rabbit.bindings.messageProcessor-in-0.consumer:
  autoBindDlq: true
  republishToDlq: true
  dlqTtl: 300000          # 5 min — retry from DLQ

spring.cloud.stream.kafka.bindings.messageProcessor-in-0.consumer:
  enableDlq: true
```

---

## Integration Architecture

```
┌─────────────────────────────────────────────────────┐
│              Internal E-Commerce Platform            │
│                                                      │
│  ┌──────────┐  ┌──────────┐  ┌───────────┐         │
│  │ Product   │  │ Inventory│  │  Order    │         │
│  │ Catalog   │  │ Visibility│  │  Service  │         │
│  └─────┬─────┘  └─────┬────┘  └─────┬─────┘         │
│        │ products      │ inv-vis     │ orders        │
│        ▼               ▼             ▼               │
│  ┌─────────────────────────────────────────────────┐ │
│  │         Marketplace Sync Service                │ │
│  │  ┌──────────┬──────────┬──────────┬──────────┐  │ │
│  │  │ Amazon   │ Flipkart │  Myntra  │ Shopify  │  │ │
│  │  │ Adapter  │ Adapter  │ Adapter  │ Adapter  │  │ │
│  │  └──────────┴──────────┴──────────┴──────────┘  │ │
│  └─────────────────────────────────────────────────┘ │
│                                                      │
│  ┌─────────────────────────────────────────────────┐ │
│  │          ERP Integration Service                │ │
│  │  ┌──────────┬──────────┬──────────┬──────────┐  │ │
│  │  │ NetSuite │  SAP B1  │QuickBooks│  Tally   │  │ │
│  │  │ Adapter  │ Adapter  │ Adapter  │ Adapter  │  │ │
│  │  └──────────┴──────────┴──────────┴──────────┘  │ │
│  └─────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

---

## Port Assignments

| Service | Default Port | Docker Port |
|---------|-------------|-------------|
| marketplace-sync-service | 7040 | 8080 |
| erp-integration-service | 7041 | 8080 |

---

## Checklist

- [ ] Marketplace Sync Service uses Strategy pattern with per-marketplace `MarketplaceAdapter`
- [ ] ERP Integration Service uses Strategy pattern with per-ERP `ErpAdapter`
- [ ] Both services maintain sync job history with success/failure counts
- [ ] Marketplace deduplication via (`productId`, `marketplace`) compound index
- [ ] ERP deduplication via (`entityType`, `entityId`, `erpSystem`) compound index
- [ ] Aggressive retry config for external API failures (5 attempts, exponential backoff)
- [ ] Dead Letter Queue configured for both services
- [ ] Credentials stored encrypted — never logged or exposed in sync records
- [ ] Adapters are Spring beans — new marketplace/ERP support added by implementing the interface
- [ ] `settings.gradle`, `docker-compose.yml`, `test-em-all.bash` updated for both services
