# E-Commerce Core Sub-Skill — Foundational Commerce Services

This sub-skill defines the **10 foundational e-commerce microservices** using the project's interface-driven pattern (see [api-patterns.md](../../../spring-microservices-architect/sub-skills/api-patterns/api-patterns.md)). Each service follows the same DTO → Interface → Impl → Entity → Mapper → MessageProcessor → Composite architecture.

> `${basePackage}` is a project-configurable placeholder. All services share the common `api/`, `util/`, and `Event<K, T>` infrastructure.

---

## Service Overview

| # | Service | Module Path | Persistence | Event Topics |
|---|---------|------------|-------------|-------------|
| 1 | Customer Service | `microservices/customer-service` | MongoDB (reactive) | `customers` |
| 2 | Auth & Security Service | `microservices/auth-service` | MongoDB (reactive) | `auth-events` |
| 3 | Product Catalog Service | `microservices/product-catalog-service` | MongoDB (reactive) | `products` |
| 4 | Search Service | `microservices/search-service` | Elasticsearch | `search-index` |
| 5 | Inventory Service | `microservices/inventory-service` | MongoDB (reactive) | `inventory` |
| 6 | Shopping Cart Service | `microservices/cart-service` | Redis + MongoDB | `carts` |
| 7 | Order Management Service | `microservices/order-service` | MongoDB (reactive) | `orders` |
| 8 | Payment Service | `microservices/payment-service` | MongoDB (reactive) | `payments` |
| 9 | Shipping Service | `microservices/shipping-service` | MongoDB (reactive) | `shipments` |
| 10 | Notification Service | `microservices/notification-service` | MongoDB (reactive) | `notifications` |

### `settings.gradle` additions

```groovy
// E-Commerce core services:
include ':microservices:customer-service'
include ':microservices:auth-service'
include ':microservices:product-catalog-service'
include ':microservices:search-service'
include ':microservices:inventory-service'
include ':microservices:cart-service'
include ':microservices:order-service'
include ':microservices:payment-service'
include ':microservices:shipping-service'
include ':microservices:notification-service'

// Composite:
include ':microservices:storefront-composite-service'
```

---

## 1. Customer Service

Manages customer profiles, registration, account details, and preferences.

### DTO — `Customer.java`

```java
package ${basePackage}.api.core.customer;

public class Customer {
  private String customerId;       // UUID
  private String email;
  private String firstName;
  private String lastName;
  private String phone;
  private Address defaultAddress;  // embedded value object
  private CustomerStatus status;   // ACTIVE, SUSPENDED, DEACTIVATED
  private Map<String, String> preferences;
  private String serviceAddress;

  public Customer() {}
  // All-args constructor, getters, setters...
}
```

**Supporting value objects** (same package):

```java
public class Address {
  private String street;
  private String city;
  private String state;
  private String zipCode;
  private String country;
}

public enum CustomerStatus { ACTIVE, SUSPENDED, DEACTIVATED }
```

### Interface — `CustomerService.java`

```java
package ${basePackage}.api.core.customer;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface CustomerService {

  Mono<Customer> createCustomer(Customer body);

  @GetMapping(value = "/customer/{customerId}", produces = "application/json")
  Mono<Customer> getCustomer(@PathVariable String customerId);

  @GetMapping(value = "/customer", produces = "application/json")
  Mono<Customer> getCustomerByEmail(
      @RequestParam(value = "email") String email);

  Mono<Customer> updateCustomer(Customer body);

  Mono<Void> deactivateCustomer(String customerId);
}
```

**Pattern note:** `createCustomer`, `updateCustomer`, `deactivateCustomer` have no HTTP mapping — invoked via messaging. Reads remain REST.

### Entity — `CustomerEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.customer.persistence;

@Document(collection = "customers")
@CompoundIndex(name = "email-unique", unique = true, def = "{'email': 1}")
public class CustomerEntity {
  @Id private String id;
  @Version private Integer version;
  private String customerId;
  private String email;
  private String firstName;
  private String lastName;
  private String phone;
  private Address defaultAddress;
  private String status;
  private Map<String, String> preferences;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `customers` | `customerId` | `Customer` DTO | Registration, profile update, deactivation |

### Cloud Stream Bindings

```yaml
# customer-service (consumer)
spring.cloud.stream.bindings:
  messageProcessor-in-0:
    destination: customers
    group: customersGroup

# storefront-composite-service (producer)
spring.cloud.stream.bindings:
  customers-out-0:
    destination: customers
    producer:
      required-groups: customersGroup
```

---

## 2. Auth & Security Service

Handles user login, session management, JWT issuance, and authorization.

### DTO — `AuthToken.java`

```java
package ${basePackage}.api.core.auth;

public class AuthToken {
  private String accessToken;
  private String refreshToken;
  private String tokenType;       // "Bearer"
  private long expiresIn;         // seconds
  private List<String> scopes;
  private String serviceAddress;

  public AuthToken() {}
}
```

**Supporting DTOs:**

```java
public class LoginRequest {
  private String email;
  private String password;
}

public class RegisterRequest {
  private String email;
  private String password;
  private String firstName;
  private String lastName;
}

public class UserCredential {
  private String userId;
  private String email;
  private String passwordHash;
  private List<String> roles;     // ROLE_USER, ROLE_ADMIN
  private boolean enabled;
  private boolean accountLocked;
}
```

### Interface — `AuthService.java`

```java
package ${basePackage}.api.core.auth;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface AuthService {

  @PostMapping(value = "/auth/login", produces = "application/json")
  Mono<AuthToken> login(@RequestBody LoginRequest request);

  @PostMapping(value = "/auth/register", produces = "application/json")
  Mono<AuthToken> register(@RequestBody RegisterRequest request);

  @PostMapping(value = "/auth/refresh", produces = "application/json")
  Mono<AuthToken> refreshToken(@RequestHeader("Authorization") String refreshToken);

  @GetMapping(value = "/auth/validate", produces = "application/json")
  Mono<UserCredential> validateToken(@RequestHeader("Authorization") String token);

  Mono<Void> revokeToken(String userId);
}
```

**Pattern deviation:** `login`, `register`, `refreshToken`, and `validateToken` are **synchronous REST** (not event-driven) because auth requires request-response semantics. Only `revokeToken` may be event-driven for cross-service session invalidation.

### Entity — `UserCredentialEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.auth.persistence;

@Document(collection = "user_credentials")
@CompoundIndex(name = "email-unique", unique = true, def = "{'email': 1}")
public class UserCredentialEntity {
  @Id private String id;
  @Version private Integer version;
  private String userId;
  private String email;
  private String passwordHash;
  private List<String> roles;
  private boolean enabled;
  private boolean accountLocked;
  private int failedLoginAttempts;
  private LocalDateTime lastLoginAt;
  private LocalDateTime createdAt;
}
```

### Additional Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.security:spring-security-oauth2-resource-server'
implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'
implementation 'org.springframework.security:spring-security-crypto'
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `auth-events` | `userId` | `null` (key only) | Token revocation, account lock |

---

## 3. Product Catalog Service

Manages product listings, descriptions, categories, attributes, and digital assets.

### DTO — `Product.java`

```java
package ${basePackage}.api.core.product;

public class Product {
  private String productId;        // UUID or SKU
  private String name;
  private String description;
  private String category;
  private List<String> tags;
  private BigDecimal price;
  private String currency;         // ISO 4217 (USD, INR, EUR)
  private List<ProductAttribute> attributes;
  private List<String> imageUrls;
  private ProductStatus status;    // DRAFT, ACTIVE, ARCHIVED
  private String serviceAddress;

  public Product() {}
}
```

**Supporting value objects:**

```java
public class ProductAttribute {
  private String key;              // e.g., "color", "size", "material"
  private String value;
}

public enum ProductStatus { DRAFT, ACTIVE, ARCHIVED }
```

### Interface — `ProductCatalogService.java`

```java
package ${basePackage}.api.core.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductCatalogService {

  Mono<Product> createProduct(Product body);

  @GetMapping(value = "/product/{productId}", produces = "application/json")
  Mono<Product> getProduct(@PathVariable String productId);

  @GetMapping(value = "/products", produces = "application/json")
  Flux<Product> getProductsByCategory(
      @RequestParam(value = "category") String category);

  Mono<Product> updateProduct(Product body);

  Mono<Void> archiveProduct(String productId);
}
```

### Entity — `ProductEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.product.persistence;

@Document(collection = "products")
@CompoundIndex(name = "sku-unique", unique = true, def = "{'productId': 1}")
public class ProductEntity {
  @Id private String id;
  @Version private Integer version;
  private String productId;
  private String name;
  private String description;
  private String category;
  private List<String> tags;
  private BigDecimal price;
  private String currency;
  private List<ProductAttribute> attributes;
  private List<String> imageUrls;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `products` | `productId` | `Product` DTO | Create, update, archive |

**Cross-service consumers:** The Search Service and Inventory Service both listen on the `products` topic to sync their indexes/stock records when catalog changes occur.

---

## 4. Search Service

Powers fast product discovery through indexing and filtering.

### DTO — `SearchResult.java`

```java
package ${basePackage}.api.core.search;

public class SearchResult {
  private String productId;
  private String name;
  private String category;
  private BigDecimal price;
  private String currency;
  private double relevanceScore;
  private String highlightedName;     // search term highlighted
  private String highlightedDesc;
  private String serviceAddress;

  public SearchResult() {}
}
```

**Supporting DTOs:**

```java
public class SearchRequest {
  private String query;
  private String category;            // optional filter
  private BigDecimal minPrice;        // optional filter
  private BigDecimal maxPrice;        // optional filter
  private List<String> tags;          // optional filter
  private String sortBy;              // "relevance", "price_asc", "price_desc"
  private int page;
  private int pageSize;
}

public class SearchResponse {
  private List<SearchResult> results;
  private long totalHits;
  private int page;
  private int pageSize;
  private Map<String, List<FacetEntry>> facets;  // aggregation results
}

public class FacetEntry {
  private String value;
  private long count;
}
```

### Interface — `SearchService.java`

```java
package ${basePackage}.api.core.search;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface SearchService {

  @PostMapping(value = "/search", produces = "application/json")
  Mono<SearchResponse> search(@RequestBody SearchRequest request);

  @GetMapping(value = "/search/suggest", produces = "application/json")
  Mono<List<String>> suggest(
      @RequestParam(value = "q") String prefix,
      @RequestParam(value = "limit", defaultValue = "10") int limit);

  Mono<Void> indexProduct(Product product);

  Mono<Void> removeProductIndex(String productId);
}
```

**Pattern deviation:** `search` and `suggest` are **synchronous REST** for low-latency user interaction. `indexProduct` and `removeProductIndex` are invoked via messaging (consumes from the `products` topic to keep the index in sync).

### Persistence — Elasticsearch (not MongoDB/JPA)

```java
package ${basePackage}.microservices.core.search.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products")
public class ProductSearchDocument {
  @Id private String productId;
  @Field(type = FieldType.Text, analyzer = "standard") private String name;
  @Field(type = FieldType.Text, analyzer = "standard") private String description;
  @Field(type = FieldType.Keyword) private String category;
  @Field(type = FieldType.Keyword) private List<String> tags;
  @Field(type = FieldType.Double) private BigDecimal price;
  @Field(type = FieldType.Keyword) private String currency;
}
```

### Additional Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-elasticsearch'
// Does NOT use spring-boot-starter-data-mongodb
```

### Event Consumption

The Search Service **consumes** from the `products` topic (published by Product Catalog Service):

```yaml
spring.cloud.stream.bindings:
  messageProcessor-in-0:
    destination: products
    group: searchIndexGroup
```

```java
@Bean
public Consumer<Event<String, Product>> messageProcessor() {
  return event -> {
    switch (event.getEventType()) {
      case CREATE -> searchService.indexProduct(event.getData()).block();
      case DELETE -> searchService.removeProductIndex(event.getKey()).block();
    }
  };
}
```

---

## 5. Inventory Service

Tracks real-time stock levels across warehouses and manages availability updates.

### DTO — `InventoryItem.java`

```java
package ${basePackage}.api.core.inventory;

public class InventoryItem {
  private String inventoryId;
  private String productId;
  private String sku;
  private String warehouseId;
  private int quantityOnHand;
  private int quantityReserved;
  private int quantityAvailable;      // onHand - reserved
  private int reorderThreshold;
  private InventoryStatus status;     // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
  private String serviceAddress;

  public InventoryItem() {}
}

public enum InventoryStatus { IN_STOCK, LOW_STOCK, OUT_OF_STOCK }
```

### Interface — `InventoryService.java`

```java
package ${basePackage}.api.core.inventory;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryService {

  Mono<InventoryItem> createInventoryItem(InventoryItem body);

  @GetMapping(value = "/inventory/{productId}", produces = "application/json")
  Flux<InventoryItem> getInventory(@PathVariable String productId);

  @GetMapping(value = "/inventory/{productId}/availability", produces = "application/json")
  Mono<Integer> getAvailableQuantity(
      @PathVariable String productId,
      @RequestParam(value = "warehouseId", required = false) String warehouseId);

  Mono<InventoryItem> adjustStock(InventoryItem body);

  Mono<Void> deleteInventory(String productId);
}
```

### Entity — `InventoryEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.inventory.persistence;

@Document(collection = "inventory")
@CompoundIndex(name = "product-warehouse", unique = true,
    def = "{'productId': 1, 'warehouseId': 1}")
public class InventoryEntity {
  @Id private String id;
  @Version private Integer version;
  private String inventoryId;
  private String productId;
  private String sku;
  private String warehouseId;
  private int quantityOnHand;
  private int quantityReserved;
  private int reorderThreshold;
  private String status;
  private LocalDateTime lastUpdatedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `inventory` | `productId` | `InventoryItem` DTO | Stock adjustment, reservation, replenishment |

---

## 6. Shopping Cart Service

Maintains the state of a user's selected items and handles temporary calculations.

### DTO — `Cart.java`

```java
package ${basePackage}.api.core.cart;

public class Cart {
  private String cartId;
  private String customerId;
  private List<CartItem> items;
  private BigDecimal subtotal;
  private BigDecimal tax;
  private BigDecimal total;
  private String currency;
  private CartStatus status;          // ACTIVE, MERGED, CONVERTED, EXPIRED
  private LocalDateTime expiresAt;
  private String serviceAddress;

  public Cart() {}
}

public class CartItem {
  private String productId;
  private String sku;
  private String productName;
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal lineTotal;
}

public enum CartStatus { ACTIVE, MERGED, CONVERTED, EXPIRED }
```

### Interface — `CartService.java`

```java
package ${basePackage}.api.core.cart;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface CartService {

  @GetMapping(value = "/cart/{customerId}", produces = "application/json")
  Mono<Cart> getCart(@PathVariable String customerId);

  @PostMapping(value = "/cart/{customerId}/items", produces = "application/json")
  Mono<Cart> addItem(
      @PathVariable String customerId,
      @RequestBody CartItem item);

  @PutMapping(value = "/cart/{customerId}/items/{productId}", produces = "application/json")
  Mono<Cart> updateItemQuantity(
      @PathVariable String customerId,
      @PathVariable String productId,
      @RequestParam int quantity);

  @DeleteMapping(value = "/cart/{customerId}/items/{productId}", produces = "application/json")
  Mono<Cart> removeItem(
      @PathVariable String customerId,
      @PathVariable String productId);

  @DeleteMapping(value = "/cart/{customerId}")
  Mono<Void> clearCart(@PathVariable String customerId);
}
```

**Pattern deviation:** Cart operations are **all synchronous REST** — the cart is a user-facing, interactive, low-latency resource. Cart state changes are published as events for downstream consumers (reservation service, analytics).

### Persistence — Redis (primary) + MongoDB (durability)

```java
package ${basePackage}.microservices.core.cart.persistence;

// Redis for fast reads/writes (session-like data)
@RedisHash(value = "cart", timeToLive = 86400)  // 24h TTL
public class CartRedisEntity {
  @Id private String customerId;
  private List<CartItemData> items;
  private LocalDateTime expiresAt;
}

// MongoDB for durable cart history (abandoned cart recovery)
@Document(collection = "carts")
public class CartEntity {
  @Id private String id;
  @Version private Integer version;
  private String cartId;
  private String customerId;
  private List<CartItemData> items;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
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
| `carts` | `customerId` | `Cart` DTO | Item added/removed, cart converted/expired |

---

## 7. Order Management Service

Oversees the entire order lifecycle from placement through fulfillment and status tracking.

### DTO — `Order.java`

```java
package ${basePackage}.api.core.order;

public class Order {
  private String orderId;
  private String customerId;
  private List<OrderItem> items;
  private Address shippingAddress;
  private Address billingAddress;
  private BigDecimal subtotal;
  private BigDecimal shippingCost;
  private BigDecimal tax;
  private BigDecimal total;
  private String currency;
  private OrderStatus status;
  private String paymentId;           // reference to payment
  private String shipmentId;          // reference to shipment
  private LocalDateTime placedAt;
  private LocalDateTime updatedAt;
  private String serviceAddress;

  public Order() {}
}

public class OrderItem {
  private String productId;
  private String sku;
  private String productName;
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal lineTotal;
}

public enum OrderStatus {
  PENDING, CONFIRMED, PAYMENT_RECEIVED, PROCESSING,
  SHIPPED, DELIVERED, CANCELLED, REFUNDED
}
```

### Interface — `OrderService.java`

```java
package ${basePackage}.api.core.order;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

  Mono<Order> placeOrder(Order body);

  @GetMapping(value = "/order/{orderId}", produces = "application/json")
  Mono<Order> getOrder(@PathVariable String orderId);

  @GetMapping(value = "/orders", produces = "application/json")
  Flux<Order> getOrdersByCustomer(
      @RequestParam(value = "customerId") String customerId);

  Mono<Order> updateOrderStatus(Order body);

  Mono<Void> cancelOrder(String orderId);
}
```

### Entity — `OrderEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.order.persistence;

@Document(collection = "orders")
@CompoundIndex(name = "order-unique", unique = true, def = "{'orderId': 1}")
@CompoundIndex(name = "customer-orders", def = "{'customerId': 1, 'placedAt': -1}")
public class OrderEntity {
  @Id private String id;
  @Version private Integer version;
  private String orderId;
  private String customerId;
  private List<OrderItemData> items;
  private Address shippingAddress;
  private Address billingAddress;
  private BigDecimal subtotal;
  private BigDecimal shippingCost;
  private BigDecimal tax;
  private BigDecimal total;
  private String currency;
  private String status;
  private String paymentId;
  private String shipmentId;
  private LocalDateTime placedAt;
  private LocalDateTime updatedAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `orders` | `orderId` | `Order` DTO | Order placed, status change, cancellation |

**Cross-service choreography:** When an order is placed, it triggers a cascade:
1. `orders` topic → Payment Service (initiate payment)
2. `payments` topic → Order Service (update status to PAYMENT_RECEIVED)
3. `orders` topic → Inventory Service (deduct stock)
4. `orders` topic → Shipping Service (create shipment)
5. `orders` topic → Notification Service (send confirmation)

---

## 8. Payment Service

Securely processes transactions by integrating with external payment gateways.

### DTO — `Payment.java`

```java
package ${basePackage}.api.core.payment;

public class Payment {
  private String paymentId;
  private String orderId;
  private String customerId;
  private BigDecimal amount;
  private String currency;
  private PaymentMethod method;
  private PaymentStatus status;
  private String gatewayTransactionId;   // Stripe/PayPal ref
  private String gatewayProvider;        // "stripe", "paypal", "razorpay"
  private String failureReason;
  private LocalDateTime processedAt;
  private String serviceAddress;

  public Payment() {}
}

public enum PaymentMethod { CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET, COD }

public enum PaymentStatus {
  PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, PARTIALLY_REFUNDED
}
```

### Interface — `PaymentService.java`

```java
package ${basePackage}.api.core.payment;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface PaymentService {

  Mono<Payment> initiatePayment(Payment body);

  @GetMapping(value = "/payment/{paymentId}", produces = "application/json")
  Mono<Payment> getPayment(@PathVariable String paymentId);

  @GetMapping(value = "/payment/order/{orderId}", produces = "application/json")
  Mono<Payment> getPaymentByOrder(@PathVariable String orderId);

  Mono<Payment> processRefund(Payment body);

  @PostMapping(value = "/payment/webhook/{provider}", produces = "application/json")
  Mono<Void> handleWebhook(
      @PathVariable String provider,
      @RequestBody String rawPayload,
      @RequestHeader("Stripe-Signature") String signature);
}
```

**Pattern deviation:** `handleWebhook` is a **synchronous REST endpoint** — payment gateways POST webhook callbacks. `initiatePayment` and `processRefund` are event-driven (triggered from orders topic). The webhook endpoint must validate gateway signatures for security.

### Entity — `PaymentEntity.java` (MongoDB)

```java
package ${basePackage}.microservices.core.payment.persistence;

@Document(collection = "payments")
@CompoundIndex(name = "payment-unique", unique = true, def = "{'paymentId': 1}")
@CompoundIndex(name = "order-payment", def = "{'orderId': 1}")
public class PaymentEntity {
  @Id private String id;
  @Version private Integer version;
  private String paymentId;
  private String orderId;
  private String customerId;
  private BigDecimal amount;
  private String currency;
  private String method;
  private String status;
  private String gatewayTransactionId;
  private String gatewayProvider;
  private String failureReason;
  private LocalDateTime processedAt;
  private LocalDateTime createdAt;
}
```

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `payments` | `orderId` | `Payment` DTO | Payment completed, failed, refunded |

---

## 9. Shipping Service

Calculates shipping costs, generates labels, and provides delivery tracking.

### DTO — `Shipment.java`

```java
package ${basePackage}.api.core.shipping;

public class Shipment {
  private String shipmentId;
  private String orderId;
  private String customerId;
  private Address origin;
  private Address destination;
  private String carrier;              // "fedex", "delhivery", "shiprocket"
  private String trackingNumber;
  private ShipmentStatus status;
  private BigDecimal shippingCost;
  private String currency;
  private BigDecimal weight;           // kg
  private LocalDateTime estimatedDelivery;
  private LocalDateTime shippedAt;
  private LocalDateTime deliveredAt;
  private String serviceAddress;

  public Shipment() {}
}

public enum ShipmentStatus {
  PENDING, LABEL_CREATED, PICKED_UP, IN_TRANSIT,
  OUT_FOR_DELIVERY, DELIVERED, RETURNED
}
```

**Supporting DTO:**

```java
public class ShippingRate {
  private String carrier;
  private String serviceLevel;         // "standard", "express", "overnight"
  private BigDecimal cost;
  private String currency;
  private int estimatedDays;
}
```

### Interface — `ShippingService.java`

```java
package ${basePackage}.api.core.shipping;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShippingService {

  Mono<Shipment> createShipment(Shipment body);

  @GetMapping(value = "/shipment/{shipmentId}", produces = "application/json")
  Mono<Shipment> getShipment(@PathVariable String shipmentId);

  @GetMapping(value = "/shipment/order/{orderId}", produces = "application/json")
  Mono<Shipment> getShipmentByOrder(@PathVariable String orderId);

  @GetMapping(value = "/shipping/rates", produces = "application/json")
  Flux<ShippingRate> calculateRates(
      @RequestParam String originZip,
      @RequestParam String destinationZip,
      @RequestParam BigDecimal weightKg);

  @GetMapping(value = "/shipment/{shipmentId}/tracking", produces = "application/json")
  Mono<Shipment> trackShipment(@PathVariable String shipmentId);

  Mono<Shipment> updateShipmentStatus(Shipment body);
}
```

**Pattern note:** `calculateRates` and `trackShipment` are synchronous REST (user-facing). `createShipment` and `updateShipmentStatus` are event-driven (triggered from orders/carrier webhooks).

### Event Topics

| Topic | Event Key | Event Data | Trigger |
|-------|-----------|-----------|---------|
| `shipments` | `orderId` | `Shipment` DTO | Label created, picked up, delivered |

---

## 10. Notification Service

Dispatches transactional updates via email, SMS, or push notifications.

### DTO — `Notification.java`

```java
package ${basePackage}.api.core.notification;

public class Notification {
  private String notificationId;
  private String recipientId;          // customerId
  private String recipientEmail;
  private String recipientPhone;
  private NotificationChannel channel; // EMAIL, SMS, PUSH
  private NotificationType type;
  private String templateId;
  private Map<String, String> templateVars;  // dynamic content
  private NotificationStatus status;
  private LocalDateTime sentAt;
  private String serviceAddress;

  public Notification() {}
}

public enum NotificationChannel { EMAIL, SMS, PUSH }

public enum NotificationType {
  ORDER_CONFIRMATION, SHIPPING_UPDATE, PAYMENT_RECEIPT,
  PASSWORD_RESET, WELCOME, PROMOTION, CART_ABANDONED
}

public enum NotificationStatus { PENDING, SENT, DELIVERED, FAILED, BOUNCED }
```

### Interface — `NotificationService.java`

```java
package ${basePackage}.api.core.notification;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {

  Mono<Notification> sendNotification(Notification body);

  @GetMapping(value = "/notification/{notificationId}", produces = "application/json")
  Mono<Notification> getNotification(@PathVariable String notificationId);

  @GetMapping(value = "/notifications", produces = "application/json")
  Flux<Notification> getNotificationsByRecipient(
      @RequestParam(value = "recipientId") String recipientId);
}
```

**Pattern note:** The Notification Service is an **event consumer only** — it listens on multiple topics (`orders`, `payments`, `shipments`, `auth-events`) and dispatches notifications. It does NOT publish events of its own (fire-and-forget). `sendNotification` is invoked exclusively via messaging.

### Multi-Topic Consumer

```java
@Configuration
public class MessageProcessorConfig {

  @Bean
  public Consumer<Event<String, Order>> orderNotificationProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE -> notificationService.sendNotification(
            buildOrderConfirmation(event.getData())).block();
      }
    };
  }

  @Bean
  public Consumer<Event<String, Shipment>> shipmentNotificationProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE -> notificationService.sendNotification(
            buildShippingUpdate(event.getData())).block();
      }
    };
  }

  @Bean
  public Consumer<Event<String, Payment>> paymentNotificationProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE -> notificationService.sendNotification(
            buildPaymentReceipt(event.getData())).block();
      }
    };
  }
}
```

```yaml
spring.cloud.stream.bindings:
  orderNotificationProcessor-in-0:
    destination: orders
    group: notificationGroup
  shipmentNotificationProcessor-in-0:
    destination: shipments
    group: notificationGroup
  paymentNotificationProcessor-in-0:
    destination: payments
    group: notificationGroup
```

### Additional Dependencies

```groovy
implementation 'org.springframework.boot:spring-boot-starter-mail'          // email
implementation 'com.twilio.sdk:twilio:10.1.0'                               // SMS
implementation 'com.google.firebase:firebase-admin:9.2.0'                   // push
implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'     // email templates
```

---

## Storefront Composite Service

The composite aggregates reads from multiple core services into a single API for the frontend.

### DTO — `StorefrontAggregate.java`

```java
package ${basePackage}.api.composite.storefront;

public class StorefrontAggregate {
  private Product product;
  private List<InventoryItem> inventory;
  private List<SearchResult> relatedProducts;
  private ServiceAddresses serviceAddresses;
}

public class StorefrontOrderAggregate {
  private Order order;
  private Payment payment;
  private Shipment shipment;
  private Customer customer;
  private ServiceAddresses serviceAddresses;
}
```

### Interface — `StorefrontCompositeService.java`

```java
package ${basePackage}.api.composite.storefront;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface StorefrontCompositeService {

  @GetMapping(value = "/storefront/product/{productId}", produces = "application/json")
  Mono<StorefrontAggregate> getProductPage(@PathVariable String productId);

  @PostMapping(value = "/storefront/order", produces = "application/json")
  Mono<StorefrontOrderAggregate> placeOrder(@RequestBody Order order);

  @GetMapping(value = "/storefront/order/{orderId}", produces = "application/json")
  Mono<StorefrontOrderAggregate> getOrderDetails(@PathVariable String orderId);
}
```

### Integration Pattern

```java
@Component
public class StorefrontIntegration
    implements ProductCatalogService, InventoryService, SearchService,
               OrderService, PaymentService, ShippingService, CustomerService {
  // READ via WebClient (REST) for all services
  // WRITE via StreamBridge (Events) for orders, payments, etc.
}
```

---

## Port Assignments

| Service | Default Port | Docker Port |
|---------|-------------|-------------|
| customer-service | 7010 | 8080 |
| auth-service | 7011 | 8080 |
| product-catalog-service | 7012 | 8080 |
| search-service | 7013 | 8080 |
| inventory-service | 7014 | 8080 |
| cart-service | 7015 | 8080 |
| order-service | 7016 | 8080 |
| payment-service | 7017 | 8080 |
| shipping-service | 7018 | 8080 |
| notification-service | 7019 | 8080 |
| storefront-composite-service | 7009 | 8080 |

---

## Event Choreography Map

```
                    ┌──────────────┐
    ┌───────────────│  Cart Service│
    │ cart converted│              │
    ▼               └──────────────┘
┌──────────┐     places      ┌────────────────┐    initiates    ┌─────────────────┐
│ Customer ├────────────────►│ Order Service  ├───────────────►│ Payment Service │
│ Service  │                 │                │                 │                 │
└──────────┘                 └───────┬────────┘                 └────────┬────────┘
                                     │                                   │
                           order placed                        payment completed
                                     │                                   │
                    ┌────────────────▼──────────┐               ┌───────▼────────┐
                    │ Inventory Service         │               │ Order Service  │
                    │ (deduct stock)            │               │ (update status)│
                    └───────────────────────────┘               └────────────────┘
                                     │
                           stock deducted                 order status: SHIPPED
                                     │                            │
                    ┌────────────────▼──────────┐    ┌───────────▼────────────┐
                    │ Shipping Service          │    │ Notification Service   │
                    │ (create shipment)         │    │ (email/SMS/push)       │
                    └───────────────────────────┘    └────────────────────────┘
```

---

## Checklist

- [ ] All DTOs in `api/` are pure POJOs — no framework annotations
- [ ] Each service interface has `@GetMapping` on reads only
- [ ] Event-driven methods have no HTTP mapping annotations
- [ ] Each service has its own event topic (no shared topics between producers)
- [ ] Notification Service consumes from multiple topics using separate functional beans
- [ ] Search Service uses Elasticsearch (not MongoDB) — different persistence pattern
- [ ] Cart Service uses Redis + MongoDB dual persistence
- [ ] Auth Service endpoints are synchronous REST (not event-driven)
- [ ] Payment Service webhook endpoint validates gateway signatures
- [ ] All entities have `@Version` for optimistic concurrency
- [ ] Cross-service choreography uses event keys consistently (orderId, customerId, productId)
- [ ] `settings.gradle`, `docker-compose.yml`, `test-em-all.bash` updated for all 11 services
