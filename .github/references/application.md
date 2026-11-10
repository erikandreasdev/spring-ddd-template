# Application references

An example `orders` context. Copy the shape (names, visibility, validation, `final`, structure), not the business: the domain is fictional.
The `//` comments explain the pattern for you; they are not copied into project code, which only carries comments about the why.
The code blocks are not compiled or run. The source of truth is the rules; if anything conflicts, the rules win.

Rules: `doc/ai/architecture.md` (application layer packages), `doc/ai/code-conventions.md` (application rules), `.github/instructions/application.instructions.md` and `.github/instructions/java-coding.instructions.md`.
Commands and queries are validated in their compact constructor; use cases only orchestrate (load, call the aggregate, save) and return an ID or a result record, never the aggregate. Converting an aggregate to a result record is done by an application mapper (`mapper` package: stateless, static methods, only domain and application types). Ports and the exceptions of their contract live directly in `application`.

## Command

`src/main/java/com/CHANGEME/orders/application/dto/commands/PlaceOrderCommand.java`

```java
package com.CHANGEME.orders.application.dto.commands;

import com.CHANGEME.orders.domain.CustomerId;
import java.util.Currency;
import java.util.Objects;

// Input of a use case that modifies: a *Command record validated in the compact constructor,
// with domain types and no Bean Validation annotations.
public record PlaceOrderCommand(CustomerId customerId, Currency currency) {

  public PlaceOrderCommand {
    Objects.requireNonNull(customerId);
    Objects.requireNonNull(currency);
  }
}
```

`src/main/java/com/CHANGEME/orders/application/dto/commands/AddOrderLineCommand.java`

```java
package com.CHANGEME.orders.application.dto.commands;

import com.CHANGEME.common.domain.Money;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.ProductId;
import java.util.Objects;

public record AddOrderLineCommand(
    OrderId orderId, ProductId productId, Money unitPrice, int quantity) {

  public AddOrderLineCommand {
    Objects.requireNonNull(orderId);
    Objects.requireNonNull(productId);
    Objects.requireNonNull(unitPrice);
  }
}
```

`src/main/java/com/CHANGEME/orders/application/dto/commands/ConfirmOrderCommand.java`

```java
package com.CHANGEME.orders.application.dto.commands;

import com.CHANGEME.orders.domain.OrderId;
import java.util.Objects;

public record ConfirmOrderCommand(OrderId orderId) {

  public ConfirmOrderCommand {
    Objects.requireNonNull(orderId);
  }
}
```

`src/main/java/com/CHANGEME/orders/application/dto/commands/PayOrderCommand.java`

```java
package com.CHANGEME.orders.application.dto.commands;

import com.CHANGEME.orders.domain.OrderId;
import java.util.Objects;

public record PayOrderCommand(OrderId orderId) {

  public PayOrderCommand {
    Objects.requireNonNull(orderId);
  }
}
```

## Creation use case (sequence ID, `Clock`)

`src/main/java/com/CHANGEME/orders/application/usecases/PlaceOrderService.java`

```java
package com.CHANGEME.orders.application.usecases;

import com.CHANGEME.common.application.UseCase;
import com.CHANGEME.orders.application.dto.commands.PlaceOrderCommand;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.repository.OrderRepository;
import java.time.Clock;

// Creation use case: requests the ID from the sequence, creates with the business factory and
// returns only the ID. The time comes from the injected Clock. No @Service or @Transactional.
@UseCase
public class PlaceOrderService {

  private final OrderRepository orders;
  private final Clock clock;

  public PlaceOrderService(final OrderRepository orders, final Clock clock) {
    this.orders = orders;
    this.clock = clock;
  }

  public OrderId place(final PlaceOrderCommand command) {
    final Order order =
        Order.place(orders.nextId(), command.customerId(), command.currency(), clock.instant());
    orders.save(order);
    return order.id();
  }
}
```

## Modification use case

`src/main/java/com/CHANGEME/orders/application/usecases/AddOrderLineService.java`

```java
package com.CHANGEME.orders.application.usecases;

import com.CHANGEME.common.application.UseCase;
import com.CHANGEME.orders.application.dto.commands.AddOrderLineCommand;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.exception.OrderNotFoundException;
import com.CHANGEME.orders.domain.repository.OrderRepository;

// Modification use case: load, call the business method, save. Validation of the quantity
// and the currency lives in the domain, not here.
@UseCase
public class AddOrderLineService {

  private final OrderRepository orders;

  public AddOrderLineService(final OrderRepository orders) {
    this.orders = orders;
  }

  public void addLine(final AddOrderLineCommand command) {
    final Order order =
        orders
            .findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
    order.addLine(command.productId(), command.unitPrice(), command.quantity());
    orders.save(order);
  }
}
```

`src/main/java/com/CHANGEME/orders/application/usecases/ConfirmOrderService.java`

```java
package com.CHANGEME.orders.application.usecases;

import com.CHANGEME.common.application.UseCase;
import com.CHANGEME.orders.application.dto.commands.ConfirmOrderCommand;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.exception.OrderNotFoundException;
import com.CHANGEME.orders.domain.repository.OrderRepository;

@UseCase
public class ConfirmOrderService {

  private final OrderRepository orders;

  public ConfirmOrderService(final OrderRepository orders) {
    this.orders = orders;
  }

  public void confirm(final ConfirmOrderCommand command) {
    final Order order =
        orders
            .findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
    order.confirm();
    orders.save(order);
  }
}
```

## Use case with an external service (ordering and rollback)

`src/main/java/com/CHANGEME/orders/application/usecases/PayOrderService.java`

```java
package com.CHANGEME.orders.application.usecases;

import com.CHANGEME.common.application.UseCase;
import com.CHANGEME.orders.application.PaymentGateway;
import com.CHANGEME.orders.application.dto.commands.PayOrderCommand;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.exception.OrderNotFoundException;
import com.CHANGEME.orders.domain.repository.OrderRepository;

// Use case with an external port. The charge goes last: if it fails, the exception rolls back the
// transaction and the order is not left as paid. Validating first in the domain (markPaid) avoids
// charging an order that cannot be paid.
@UseCase
public class PayOrderService {

  private final OrderRepository orders;
  private final PaymentGateway payments;

  public PayOrderService(final OrderRepository orders, final PaymentGateway payments) {
    this.orders = orders;
    this.payments = payments;
  }

  public void pay(final PayOrderCommand command) {
    final Order order =
        orders
            .findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
    order.markPaid();
    orders.save(order);
    payments.charge(order.id(), order.total());
  }
}
```

## Query

`src/main/java/com/CHANGEME/orders/application/dto/queries/GetOrderQuery.java`

```java
package com.CHANGEME.orders.application.dto.queries;

import com.CHANGEME.orders.domain.OrderId;
import java.util.Objects;

// Input of a use case that only reads: a *Query record.
public record GetOrderQuery(OrderId orderId) {

  public GetOrderQuery {
    Objects.requireNonNull(orderId);
  }
}
```

## Result record

`src/main/java/com/CHANGEME/orders/application/dto/results/OrderSummary.java`

```java
package com.CHANGEME.orders.application.dto.results;

import com.CHANGEME.common.domain.Money;
import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderStatus;
import java.util.Objects;

// Result record of a query. The use case never returns the aggregate.
public record OrderSummary(OrderId id, CustomerId customerId, OrderStatus status, Money total) {

  public OrderSummary {
    Objects.requireNonNull(id);
    Objects.requireNonNull(customerId);
    Objects.requireNonNull(status);
    Objects.requireNonNull(total);
  }
}
```

## Application mapper (aggregate to result record)

`src/main/java/com/CHANGEME/orders/application/mapper/OrderResultMapper.java`

```java
package com.CHANGEME.orders.application.mapper;

import com.CHANGEME.orders.application.dto.results.OrderSummary;
import com.CHANGEME.orders.domain.Order;

// Application mapper: converts an aggregate into a result record, so the use case never returns the
// aggregate. Stateless, static methods, only domain and application types.
public final class OrderResultMapper {

  private OrderResultMapper() {}

  public static OrderSummary toSummary(final Order order) {
    return new OrderSummary(order.id(), order.customerId(), order.status(), order.total());
  }
}
```

## Read-by-ID use case

`src/main/java/com/CHANGEME/orders/application/usecases/GetOrderService.java`

```java
package com.CHANGEME.orders.application.usecases;

import com.CHANGEME.common.application.UseCase;
import com.CHANGEME.orders.application.dto.queries.GetOrderQuery;
import com.CHANGEME.orders.application.dto.results.OrderSummary;
import com.CHANGEME.orders.application.mapper.OrderResultMapper;
import com.CHANGEME.orders.domain.exception.OrderNotFoundException;
import com.CHANGEME.orders.domain.repository.OrderRepository;

// Reading an aggregate by its ID: repository plus the application mapper to the result record.
@UseCase
public class GetOrderService {

  private final OrderRepository orders;

  public GetOrderService(final OrderRepository orders) {
    this.orders = orders;
  }

  public OrderSummary get(final GetOrderQuery query) {
    return orders
        .findById(query.orderId())
        .map(OrderResultMapper::toSummary)
        .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
  }
}
```

## Paginated query

`src/main/java/com/CHANGEME/orders/application/dto/queries/ListCustomerOrdersQuery.java`

```java
package com.CHANGEME.orders.application.dto.queries;

import com.CHANGEME.orders.domain.CustomerId;
import java.util.Objects;

public record ListCustomerOrdersQuery(CustomerId customerId, int page, int size) {

  public ListCustomerOrdersQuery {
    Objects.requireNonNull(customerId);
    if (page < 0) throw new IllegalArgumentException("Page must not be negative: " + page);
    if (size < 1 || size > 100) {
      throw new IllegalArgumentException("Size must be between 1 and 100: " + size);
    }
  }
}
```

## Listing use case

`src/main/java/com/CHANGEME/orders/application/usecases/ListCustomerOrdersService.java`

```java
package com.CHANGEME.orders.application.usecases;

import com.CHANGEME.common.application.UseCase;
import com.CHANGEME.common.application.dto.results.PageResult;
import com.CHANGEME.orders.application.OrderSummaries;
import com.CHANGEME.orders.application.dto.queries.ListCustomerOrdersQuery;
import com.CHANGEME.orders.application.dto.results.OrderSummary;

@UseCase
public class ListCustomerOrdersService {

  private final OrderSummaries summaries;

  public ListCustomerOrdersService(final OrderSummaries summaries) {
    this.summaries = summaries;
  }

  public PageResult<OrderSummary> list(final ListCustomerOrdersQuery query) {
    return summaries.findByCustomer(query.customerId(), query.page(), query.size());
  }
}
```

## Read port

`src/main/java/com/CHANGEME/orders/application/OrderSummaries.java`

```java
package com.CHANGEME.orders.application;

import com.CHANGEME.common.application.dto.results.PageResult;
import com.CHANGEME.orders.application.dto.results.OrderSummary;
import com.CHANGEME.orders.domain.CustomerId;

// Read port for listings: returns result records directly from SQL, without loading
// aggregates. It lives in application because it is not a domain concept.
public interface OrderSummaries {

  PageResult<OrderSummary> findByCustomer(CustomerId customerId, int page, int size);
}
```

## Paginated result

`src/main/java/com/CHANGEME/common/application/dto/results/PageResult.java`

```java
package com.CHANGEME.common.application.dto.results;

import java.util.List;
import java.util.Objects;

// Paginated result of a read port. Create it in common.application.dto.results with the first listing.
public record PageResult<T>(List<T> items, int page, int size, long totalItems) {

  public PageResult {
    items = List.copyOf(Objects.requireNonNull(items));
    if (page < 0) throw new IllegalArgumentException("Page must not be negative: " + page);
    if (size < 1) throw new IllegalArgumentException("Size must be positive: " + size);
    if (totalItems < 0) throw new IllegalArgumentException("Total must not be negative");
  }
}
```

## Output port to an external service

`src/main/java/com/CHANGEME/orders/application/PaymentGateway.java`

```java
package com.CHANGEME.orders.application;

import com.CHANGEME.common.domain.Money;
import com.CHANGEME.orders.domain.OrderId;

// Output port to an external service: an interface in application, named after its purpose,
// with domain types. The implementation (HttpPaymentGateway) lives in infrastructure.
public interface PaymentGateway {

  // Throws PaymentDeclinedException if the provider declines the charge.
  void charge(OrderId orderId, Money amount);
}
```

## Exception in a port's contract

`src/main/java/com/CHANGEME/orders/application/PaymentDeclinedException.java`

```java
package com.CHANGEME.orders.application;

import com.CHANGEME.common.domain.exception.DomainException;
import com.CHANGEME.orders.domain.OrderId;

// Part of the PaymentGateway port's contract, which is why it lives next to it. It extends
// DomainException so it becomes a 409 and triggers a rollback.
public final class PaymentDeclinedException extends DomainException {

  public PaymentDeclinedException(final OrderId id) {
    super("Payment for order " + id.value() + " was declined");
  }
}
```
