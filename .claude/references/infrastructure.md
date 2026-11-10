# Infrastructure references

An example `orders` context. Copy the shape (names, visibility, validation, `final`, structure), not the business: the domain is fictional.
The `//` comments explain the pattern for you; they are not copied into project code, which only carries comments about the why.
The code blocks are not compiled or run. The source of truth is the rules; if anything conflicts, the rules win.

Rules: the `infrastructure` section of `CLAUDE.md`, `.claude/rules/spring-style.md`, `.claude/rules/api.md` and `.claude/rules/persistence.md`.
The SQL is PostgreSQL. <!-- CHANGEME: database. Adapt sequences, types and LIMIT/OFFSET to your engine -->
The HTTP adapter needs a dependency the template does not include: `spring-boot-starter-restclient`. Include it in the plan when the first external service appears.

## Controller

`src/main/java/com/CHANGEME/orders/infrastructure/web/OrderController.java`

```java
package com.CHANGEME.orders.infrastructure.web;

import com.CHANGEME.orders.application.AddOrderLineService;
import com.CHANGEME.orders.application.ConfirmOrderService;
import com.CHANGEME.orders.application.GetOrderService;
import com.CHANGEME.orders.application.ListCustomerOrdersService;
import com.CHANGEME.orders.application.PayOrderService;
import com.CHANGEME.orders.application.PlaceOrderService;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.shared.infrastructure.web.api.AddOrderLineBody;
import com.CHANGEME.shared.infrastructure.web.api.OrderBody;
import com.CHANGEME.shared.infrastructure.web.api.OrderPageBody;
import com.CHANGEME.shared.infrastructure.web.api.OrdersApi;
import com.CHANGEME.shared.infrastructure.web.api.PlaceOrderBody;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

// One controller per aggregate that implements the interface generated from api.yaml. Paths,
// validation and status codes are defined by the specification; here it only converts, calls the
// use case and builds the response. No logic, no try/catch and no repositories.
// If it grows beyond six or seven endpoints, it is a sign to split it.
@RestController
class OrderController implements OrdersApi {

  private final PlaceOrderService placeOrder;
  private final AddOrderLineService addOrderLine;
  private final ConfirmOrderService confirmOrder;
  private final PayOrderService payOrder;
  private final GetOrderService getOrder;
  private final ListCustomerOrdersService listCustomerOrders;

  OrderController(
      final PlaceOrderService placeOrder,
      final AddOrderLineService addOrderLine,
      final ConfirmOrderService confirmOrder,
      final PayOrderService payOrder,
      final GetOrderService getOrder,
      final ListCustomerOrdersService listCustomerOrders) {
    this.placeOrder = placeOrder;
    this.addOrderLine = addOrderLine;
    this.confirmOrder = confirmOrder;
    this.payOrder = payOrder;
    this.getOrder = getOrder;
    this.listCustomerOrders = listCustomerOrders;
  }

  @Override
  public ResponseEntity<Void> placeOrder(final PlaceOrderBody body) {
    final OrderId id = placeOrder.place(OrderApiConversion.toCommand(body));
    return ResponseEntity.created(URI.create("/v1/orders/" + id.value())).build();
  }

  @Override
  public ResponseEntity<Void> addOrderLine(final Long id, final AddOrderLineBody body) {
    addOrderLine.addLine(OrderApiConversion.toCommand(id, body));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> confirmOrder(final Long id) {
    confirmOrder.confirm(OrderApiConversion.toConfirmCommand(id));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> payOrder(final Long id) {
    payOrder.pay(OrderApiConversion.toPayCommand(id));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<OrderBody> getOrder(final Long id) {
    return ResponseEntity.ok(OrderApiConversion.toBody(getOrder.get(OrderApiConversion.toQuery(id))));
  }

  @Override
  public ResponseEntity<OrderPageBody> listCustomerOrders(
      final Long customerId, final Integer page, final Integer size) {
    return ResponseEntity.ok(
        OrderApiConversion.toBody(
            listCustomerOrders.list(OrderApiConversion.toQuery(customerId, page, size))));
  }
}
```

## Web conversion

`src/main/java/com/CHANGEME/orders/infrastructure/web/OrderApiConversion.java`

```java
package com.CHANGEME.orders.infrastructure.web;

import com.CHANGEME.orders.application.AddOrderLineCommand;
import com.CHANGEME.orders.application.ConfirmOrderCommand;
import com.CHANGEME.orders.application.GetOrderQuery;
import com.CHANGEME.orders.application.ListCustomerOrdersQuery;
import com.CHANGEME.orders.application.OrderSummary;
import com.CHANGEME.orders.application.PayOrderCommand;
import com.CHANGEME.orders.application.PlaceOrderCommand;
import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.ProductId;
import com.CHANGEME.shared.application.PageResult;
import com.CHANGEME.shared.domain.Money;
import com.CHANGEME.shared.infrastructure.web.api.AddOrderLineBody;
import com.CHANGEME.shared.infrastructure.web.api.MoneyBody;
import com.CHANGEME.shared.infrastructure.web.api.OrderBody;
import com.CHANGEME.shared.infrastructure.web.api.OrderPageBody;
import com.CHANGEME.shared.infrastructure.web.api.PageInfoBody;
import com.CHANGEME.shared.infrastructure.web.api.PlaceOrderBody;
import java.math.BigDecimal;
import java.util.Currency;

// Conversion between the generated models (*Body) and the application commands, queries and
// results. One class per aggregate, final and package-private, next to the controller.
// Invalid formats (BigDecimal, currency) throw IllegalArgumentException and become a 400.
final class OrderApiConversion {

  private OrderApiConversion() {}

  static PlaceOrderCommand toCommand(final PlaceOrderBody body) {
    return new PlaceOrderCommand(
        new CustomerId(body.getCustomerId()), Currency.getInstance(body.getCurrency()));
  }

  static AddOrderLineCommand toCommand(final long orderId, final AddOrderLineBody body) {
    return new AddOrderLineCommand(
        new OrderId(orderId),
        new ProductId(body.getProductId()),
        toMoney(body.getUnitPrice()),
        body.getQuantity());
  }

  static ConfirmOrderCommand toConfirmCommand(final long orderId) {
    return new ConfirmOrderCommand(new OrderId(orderId));
  }

  static PayOrderCommand toPayCommand(final long orderId) {
    return new PayOrderCommand(new OrderId(orderId));
  }

  static GetOrderQuery toQuery(final long orderId) {
    return new GetOrderQuery(new OrderId(orderId));
  }

  static ListCustomerOrdersQuery toQuery(final long customerId, final int page, final int size) {
    return new ListCustomerOrdersQuery(new CustomerId(customerId), page, size);
  }

  static OrderBody toBody(final OrderSummary summary) {
    return new OrderBody(
        summary.id().value(),
        summary.customerId().value(),
        OrderBody.StatusEnum.fromValue(summary.status().name()),
        toBody(summary.total()));
  }

  static OrderPageBody toBody(final PageResult<OrderSummary> result) {
    return new OrderPageBody(
        result.items().stream().map(OrderApiConversion::toBody).toList(),
        new PageInfoBody(result.page(), result.size(), result.totalItems()));
  }

  private static Money toMoney(final MoneyBody body) {
    return new Money(new BigDecimal(body.getAmount()), Currency.getInstance(body.getCurrency()));
  }

  private static MoneyBody toBody(final Money money) {
    return new MoneyBody(money.amount().toPlainString(), money.currency().getCurrencyCode());
  }
}
```

## API specification

`src/main/resources/openapi/api.yaml`

```yaml
paths:
  /orders:
    post:
      tags: [orders]
      operationId: placeOrder
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PlaceOrder'
      responses:
        '201':
          description: Order placed
          headers:
            Location:
              schema:
                type: string
        '400':
          $ref: '#/components/responses/BadRequest'

  /orders/{id}:
    get:
      tags: [orders]
      operationId: getOrder
      parameters:
        - $ref: '#/components/parameters/OrderId'
      responses:
        '200':
          description: The order
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Order'
        '404':
          $ref: '#/components/responses/NotFound'

  /orders/{id}/lines:
    post:
      tags: [orders]
      operationId: addOrderLine
      parameters:
        - $ref: '#/components/parameters/OrderId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AddOrderLine'
      responses:
        '204':
          description: Line added
        '400':
          $ref: '#/components/responses/BadRequest'
        '404':
          $ref: '#/components/responses/NotFound'
        '409':
          $ref: '#/components/responses/Conflict'

  /orders/{id}/confirmation:
    post:
      tags: [orders]
      operationId: confirmOrder
      parameters:
        - $ref: '#/components/parameters/OrderId'
      responses:
        '204':
          description: Order confirmed
        '404':
          $ref: '#/components/responses/NotFound'
        '409':
          $ref: '#/components/responses/Conflict'

  /orders/{id}/payment:
    post:
      tags: [orders]
      operationId: payOrder
      parameters:
        - $ref: '#/components/parameters/OrderId'
      responses:
        '204':
          description: Order paid
        '404':
          $ref: '#/components/responses/NotFound'
        '409':
          $ref: '#/components/responses/Conflict'

  /customers/{customerId}/orders:
    get:
      tags: [orders]
      operationId: listCustomerOrders
      parameters:
        - name: customerId
          in: path
          required: true
          schema:
            type: integer
            format: int64
            minimum: 1
        - $ref: '#/components/parameters/Page'
        - $ref: '#/components/parameters/Size'
      responses:
        '200':
          description: Orders of the customer, newest first
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrderPage'
        '400':
          $ref: '#/components/responses/BadRequest'
```

`src/main/resources/openapi/api.yaml`

```yaml
    OrderId:
      name: id
      in: path
      required: true
      schema:
        type: integer
        format: int64
        minimum: 1
```

`src/main/resources/openapi/api.yaml`

```yaml
    PlaceOrder:
      type: object
      required: [customerId, currency]
      properties:
        customerId:
          type: integer
          format: int64
          minimum: 1
        currency:
          type: string
          pattern: '^[A-Z]{3}$'
          example: EUR

    AddOrderLine:
      type: object
      required: [productId, unitPrice, quantity]
      properties:
        productId:
          type: integer
          format: int64
          minimum: 1
        unitPrice:
          $ref: '#/components/schemas/Money'
        quantity:
          type: integer
          minimum: 1
          maximum: 999

    Order:
      type: object
      required: [id, customerId, status, total]
      properties:
        id:
          type: integer
          format: int64
        customerId:
          type: integer
          format: int64
        status:
          type: string
          enum: [DRAFT, CONFIRMED, PAID]
        total:
          $ref: '#/components/schemas/Money'

    OrderPage:
      type: object
      required: [items, page]
      properties:
        items:
          type: array
          items:
            $ref: '#/components/schemas/Order'
        page:
          $ref: '#/components/schemas/PageInfo'
```

## Flyway migration

`src/main/resources/db/migration/V1__create_orders.sql`

```sql
-- One sequence per aggregate table, named constraints, NOT NULL by default,
-- money as DECIMAL(19, 4) plus the currency, enums by name and time-zone-aware dates.

CREATE SEQUENCE orders_seq;

CREATE TABLE orders (
  id             BIGINT         NOT NULL,
  customer_id    BIGINT         NOT NULL,
  currency       CHAR(3)        NOT NULL,
  placed_at      TIMESTAMPTZ    NOT NULL,
  discount_type  VARCHAR(20)    NOT NULL,
  discount_value DECIMAL(19, 4) NOT NULL,
  status         VARCHAR(20)    NOT NULL,
  total_amount   DECIMAL(19, 4) NOT NULL,
  version        BIGINT         NOT NULL,
  CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE INDEX ix_orders_customer_id ON orders (customer_id, placed_at DESC, id DESC);

-- Aggregate collection: no ID of its own, composite key of order and position.
CREATE TABLE order_lines (
  order_id   BIGINT         NOT NULL,
  position   INT            NOT NULL,
  product_id BIGINT         NOT NULL,
  unit_price DECIMAL(19, 4) NOT NULL,
  quantity   INT            NOT NULL,
  CONSTRAINT pk_order_lines PRIMARY KEY (order_id, position),
  CONSTRAINT fk_order_lines_orders FOREIGN KEY (order_id) REFERENCES orders (id)
);
```

## Aggregate mapper

`src/main/java/com/CHANGEME/orders/infrastructure/persistence/OrderMapper.java`

```java
package com.CHANGEME.orders.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

// SQL in annotations with text blocks and parameters always with #{}. Works with *Row records;
// MyBatisOrderRepository does the conversion to the domain. Rows are built by column name
// (arg-name-based-constructor-auto-mapping in application.yml).
@Mapper
interface OrderMapper {

  @Select("SELECT nextval('orders_seq')")
  long nextId();

  @Select("""
      SELECT id, customer_id, currency, placed_at, discount_type, discount_value,
             status, total_amount, version
      FROM orders
      WHERE id = #{id}
      """)
  OrderRow findById(@Param("id") long id);

  @Select("""
      SELECT order_id, position, product_id, unit_price, quantity
      FROM order_lines
      WHERE order_id = #{orderId}
      ORDER BY position
      """)
  List<OrderLineRow> findLines(@Param("orderId") long orderId);

  @Insert("""
      INSERT INTO orders (id, customer_id, currency, placed_at, discount_type, discount_value,
                          status, total_amount, version)
      VALUES (#{id}, #{customerId}, #{currency}, #{placedAt}, #{discountType}, #{discountValue},
              #{status}, #{totalAmount}, 1)
      """)
  void insert(OrderRow row);

  @Update("""
      UPDATE orders
      SET discount_type = #{discountType},
          discount_value = #{discountValue},
          status = #{status},
          total_amount = #{totalAmount},
          version = version + 1
      WHERE id = #{id} AND version = #{version}
      """)
  int update(OrderRow row);

  @Delete("DELETE FROM order_lines WHERE order_id = #{orderId}")
  void deleteLines(@Param("orderId") long orderId);

  @Insert("""
      INSERT INTO order_lines (order_id, position, product_id, unit_price, quantity)
      VALUES (#{orderId}, #{position}, #{productId}, #{unitPrice}, #{quantity})
      """)
  void insertLine(OrderLineRow row);
}
```

## Rows (`*Row`)

`src/main/java/com/CHANGEME/orders/infrastructure/persistence/OrderRow.java`

```java
package com.CHANGEME.orders.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;

// Row of the orders table, as is. Database types (long, String, BigDecimal), never domain
// types. total_amount is a copy of the total computed by the domain, so that read queries do not
// repeat the discount rules in SQL.
record OrderRow(
    long id,
    long customerId,
    String currency,
    Instant placedAt,
    String discountType,
    BigDecimal discountValue,
    String status,
    BigDecimal totalAmount,
    long version) {}
```

`src/main/java/com/CHANGEME/orders/infrastructure/persistence/OrderLineRow.java`

```java
package com.CHANGEME.orders.infrastructure.persistence;

import java.math.BigDecimal;

record OrderLineRow(
    long orderId, int position, long productId, BigDecimal unitPrice, int quantity) {}
```

## Repository (version, collections, sealed types)

`src/main/java/com/CHANGEME/orders/infrastructure/persistence/MyBatisOrderRepository.java`

```java
package com.CHANGEME.orders.infrastructure.persistence;

import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.orders.domain.Discount;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderLine;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.orders.domain.OrderSnapshot;
import com.CHANGEME.orders.domain.OrderStatus;
import com.CHANGEME.orders.domain.ProductId;
import com.CHANGEME.shared.domain.ConcurrentUpdateException;
import com.CHANGEME.shared.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

// Repository implementation: all conversion between rows and domain lives here.
// Saves by version (optimistic locking) and the lines are deleted and reinserted on every save().
@Repository
class MyBatisOrderRepository implements OrderRepository {

  private final OrderMapper mapper;

  MyBatisOrderRepository(final OrderMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public OrderId nextId() {
    return new OrderId(mapper.nextId());
  }

  @Override
  public Optional<Order> findById(final OrderId id) {
    return Optional.ofNullable(mapper.findById(id.value()))
        .map(row -> Order.restore(toSnapshot(row, mapper.findLines(row.id()))));
  }

  @Override
  public void save(final Order order) {
    final OrderRow row = toRow(order);
    if (order.version() == 0) {
      mapper.insert(row);
    } else if (mapper.update(row) == 0) {
      throw new ConcurrentUpdateException("Order", order.id().value());
    }
    replaceLines(order);
  }

  private void replaceLines(final Order order) {
    final long orderId = order.id().value();
    mapper.deleteLines(orderId);
    final List<OrderLine> lines = order.lines();
    for (int position = 0; position < lines.size(); position++) {
      final OrderLine line = lines.get(position);
      mapper.insertLine(
          new OrderLineRow(
              orderId,
              position,
              line.productId().value(),
              line.unitPrice().amount(),
              line.quantity()));
    }
  }

  private static OrderRow toRow(final Order order) {
    final OrderSnapshot snapshot = order.snapshot();
    final DiscountColumns discount = toColumns(snapshot.discount());
    return new OrderRow(
        snapshot.id().value(),
        snapshot.customerId().value(),
        snapshot.currency().getCurrencyCode(),
        snapshot.placedAt(),
        discount.type(),
        discount.value(),
        snapshot.status().name(),
        order.total().amount(),
        snapshot.version());
  }

  private static OrderSnapshot toSnapshot(final OrderRow row, final List<OrderLineRow> lines) {
    final Currency currency = Currency.getInstance(row.currency());
    return new OrderSnapshot(
        new OrderId(row.id()),
        new CustomerId(row.customerId()),
        currency,
        row.placedAt(),
        lines.stream().map(line -> toLine(line, currency)).toList(),
        toDiscount(row.discountType(), row.discountValue(), currency),
        OrderStatus.valueOf(row.status()),
        row.version());
  }

  private static OrderLine toLine(final OrderLineRow row, final Currency currency) {
    return new OrderLine(
        new ProductId(row.productId()), new Money(row.unitPrice(), currency), row.quantity());
  }

  // Exhaustive switch over the sealed interface, without default: if a variant is added, it stops
  // compiling until it is handled here.
  private static DiscountColumns toColumns(final Discount discount) {
    return switch (discount) {
      case Discount.NoDiscount none -> new DiscountColumns("NONE", BigDecimal.ZERO);
      case Discount.PercentageDiscount percentage ->
          new DiscountColumns("PERCENTAGE", BigDecimal.valueOf(percentage.percent()));
      case Discount.FixedDiscount fixed -> new DiscountColumns("FIXED", fixed.value().amount());
    };
  }

  // Here the switch is over a String from the database, so it needs a default.
  private static Discount toDiscount(
      final String type, final BigDecimal value, final Currency currency) {
    return switch (type) {
      case "NONE" -> new Discount.NoDiscount();
      case "PERCENTAGE" -> new Discount.PercentageDiscount(value.intValueExact());
      case "FIXED" -> new Discount.FixedDiscount(new Money(value, currency));
      default -> throw new IllegalStateException("Unknown discount type: " + type);
    };
  }

  private record DiscountColumns(String type, BigDecimal value) {}
}
```

## Read mapper

`src/main/java/com/CHANGEME/orders/infrastructure/persistence/OrderSummaryMapper.java`

```java
package com.CHANGEME.orders.infrastructure.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

// Read mapper: flat queries for listings, without going through the aggregate.
// Ordering always ends with the ID so that pagination is deterministic.
@Mapper
interface OrderSummaryMapper {

  @Select("""
      SELECT id, customer_id, status, total_amount, currency
      FROM orders
      WHERE customer_id = #{customerId}
      ORDER BY placed_at DESC, id DESC
      LIMIT #{size} OFFSET #{offset}
      """)
  List<OrderSummaryRow> findByCustomer(
      @Param("customerId") long customerId, @Param("size") int size, @Param("offset") long offset);

  @Select("SELECT COUNT(*) FROM orders WHERE customer_id = #{customerId}")
  long countByCustomer(@Param("customerId") long customerId);
}
```

`src/main/java/com/CHANGEME/orders/infrastructure/persistence/OrderSummaryRow.java`

```java
package com.CHANGEME.orders.infrastructure.persistence;

import java.math.BigDecimal;

record OrderSummaryRow(
    long id, long customerId, String status, BigDecimal totalAmount, String currency) {}
```

## Read port implementation

`src/main/java/com/CHANGEME/orders/infrastructure/persistence/MyBatisOrderSummaries.java`

```java
package com.CHANGEME.orders.infrastructure.persistence;

import com.CHANGEME.orders.application.OrderSummaries;
import com.CHANGEME.orders.application.OrderSummary;
import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderStatus;
import com.CHANGEME.shared.application.PageResult;
import com.CHANGEME.shared.domain.Money;
import java.util.Currency;
import org.springframework.stereotype.Repository;

// Implementation of a read port. It only reads, so it can go straight to SQL without loading
// aggregates.
@Repository
class MyBatisOrderSummaries implements OrderSummaries {

  private final OrderSummaryMapper mapper;

  MyBatisOrderSummaries(final OrderSummaryMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public PageResult<OrderSummary> findByCustomer(
      final CustomerId customerId, final int page, final int size) {
    final long offset = (long) page * size;
    return new PageResult<>(
        mapper.findByCustomer(customerId.value(), size, offset).stream()
            .map(MyBatisOrderSummaries::toSummary)
            .toList(),
        page,
        size,
        mapper.countByCustomer(customerId.value()));
  }

  private static OrderSummary toSummary(final OrderSummaryRow row) {
    return new OrderSummary(
        new OrderId(row.id()),
        new CustomerId(row.customerId()),
        OrderStatus.valueOf(row.status()),
        new Money(row.totalAmount(), Currency.getInstance(row.currency())));
  }
}
```

## Adapter properties

`src/main/java/com/CHANGEME/orders/infrastructure/payment/PaymentProperties.java`

```java
package com.CHANGEME.orders.infrastructure.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Adapter properties: a validated record, never @Value. If a value is missing, the application
// does not start. In application.yml:
//
//   payments:
//     base-url: ${PAYMENTS_BASE_URL}
//     api-key: ${PAYMENTS_API_KEY}
//     timeout: 5s
@Validated
@ConfigurationProperties("payments")
record PaymentProperties(@NotNull URI baseUrl, @NotBlank String apiKey, @NotNull Duration timeout) {}
```

`src/main/java/com/CHANGEME/orders/infrastructure/payment/PaymentConfig.java`

```java
package com.CHANGEME.orders.infrastructure.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Registers the adapter's properties record.
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PaymentProperties.class)
class PaymentConfig {}
```

## HTTP adapter with `RestClient`

`src/main/java/com/CHANGEME/orders/infrastructure/payment/HttpPaymentGateway.java`

```java
package com.CHANGEME.orders.infrastructure.payment;

import com.CHANGEME.orders.application.PaymentDeclinedException;
import com.CHANGEME.orders.application.PaymentGateway;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.shared.domain.Money;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// Adapter of an output port to an external HTTP service, with RestClient.
// Needs spring-boot-starter-restclient: Spring Boot's RestClient.Builder propagates the
// traceId and records metrics. Base URL and timeouts come from PaymentProperties.
// Translates responses to the port's semantics: 4xx is a business rejection, anything else a
// technical failure. It does not log: the error is logged only once in ApiExceptionHandler.
@Component
class HttpPaymentGateway implements PaymentGateway {

  private final RestClient client;

  HttpPaymentGateway(final RestClient.Builder builder, final PaymentProperties properties) {
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(properties.timeout());
    requestFactory.setReadTimeout(properties.timeout());
    this.client =
        builder
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
            .build();
  }

  @Override
  public void charge(final OrderId orderId, final Money amount) {
    try {
      client
          .post()
          .uri("/charges")
          .body(ChargeRequest.of(orderId, amount))
          .retrieve()
          .onStatus(
              HttpStatusCode::is4xxClientError,
              (request, response) -> {
                throw new PaymentDeclinedException(orderId);
              })
          .toBodilessEntity();
    } catch (final RestClientException exception) {
      throw new PaymentUnavailableException(orderId, exception);
    }
  }
}
```

## External request body

`src/main/java/com/CHANGEME/orders/infrastructure/payment/ChargeRequest.java`

```java
package com.CHANGEME.orders.infrastructure.payment;

import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.shared.domain.Money;

// Body expected by the external provider. It is an adapter detail: it does not leave this package.
record ChargeRequest(String reference, String amount, String currency) {

  static ChargeRequest of(final OrderId orderId, final Money amount) {
    return new ChargeRequest(
        "order-" + orderId.value(),
        amount.amount().toPlainString(),
        amount.currency().getCurrencyCode());
  }
}
```

## Technical integration exception

`src/main/java/com/CHANGEME/orders/infrastructure/payment/PaymentUnavailableException.java`

```java
package com.CHANGEME.orders.infrastructure.payment;

import com.CHANGEME.orders.domain.OrderId;

// Technical integration failure (timeout, 5xx). It is not a business rule: it does not extend
// DomainException, keeps the cause and becomes a 500, logged once in ApiExceptionHandler.
final class PaymentUnavailableException extends RuntimeException {

  PaymentUnavailableException(final OrderId orderId, final Throwable cause) {
    super("Payment provider unavailable for order " + orderId.value(), cause);
  }
}
```
