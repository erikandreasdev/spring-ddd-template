# Domain references

An example `orders` context. Copy the shape (names, visibility, validation, `final`, structure), not the business: the domain is fictional.
The `//` comments explain the pattern for you; they are not copied into project code, which only carries comments about the why.
The code blocks are not compiled or run. The source of truth is the rules; if anything conflicts, the rules win.

Rules: `doc/ai/architecture.md` (domain layer packages), `doc/ai/code-conventions.md` (domain rules), `.github/instructions/domain.instructions.md` and `.github/instructions/java-coding.instructions.md`.
Guards, policies and domain services are stateless final classes with a private constructor and static methods. The domain has no Spring, so use cases call them directly.

## Aggregate root

`src/main/java/com/CHANGEME/orders/domain/Order.java`

```java
package com.CHANGEME.orders.domain;

import com.CHANGEME.common.domain.Money;
import com.CHANGEME.orders.domain.exception.EmptyOrderException;
import com.CHANGEME.orders.domain.exception.OrderNotConfirmedException;
import com.CHANGEME.orders.domain.exception.OrderNotEditableException;
import com.CHANGEME.orders.domain.guards.OrderSizeGuard;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

// Aggregate root. Private constructor, creation factory with a business name and restore() for
// persistence. No setters: state only changes through business methods that protect the
// invariants. The time arrives as a parameter; the use case takes it from a Clock.
public final class Order {

  private final OrderId id;
  private final CustomerId customerId;
  private final Currency currency;
  private final Instant placedAt;
  private final List<OrderLine> lines;
  private final long version;
  private Discount discount;
  private OrderStatus status;

  private Order(final OrderSnapshot snapshot) {
    this.id = snapshot.id();
    this.customerId = snapshot.customerId();
    this.currency = snapshot.currency();
    this.placedAt = snapshot.placedAt();
    this.lines = new ArrayList<>(snapshot.lines());
    this.discount = snapshot.discount();
    this.status = snapshot.status();
    this.version = snapshot.version();
  }

  public static Order place(
      final OrderId id,
      final CustomerId customerId,
      final Currency currency,
      final Instant placedAt) {
    return new Order(
        new OrderSnapshot(
            id,
            customerId,
            currency,
            placedAt,
            List.of(),
            new Discount.NoDiscount(),
            OrderStatus.DRAFT,
            0));
  }

  public static Order restore(final OrderSnapshot snapshot) {
    return new Order(snapshot);
  }

  public void addLine(final ProductId productId, final Money unitPrice, final int quantity) {
    requireEditable();
    OrderSizeGuard.ensureCanAddLine(id, lines.size());
    if (!unitPrice.currency().equals(currency)) {
      throw new IllegalArgumentException(
          "Order " + id.value() + " uses " + currency + ", not " + unitPrice.currency());
    }
    lines.add(new OrderLine(productId, unitPrice, quantity));
  }

  public void applyDiscount(final Discount newDiscount) {
    requireEditable();
    discount = newDiscount;
  }

  public void confirm() {
    if (lines.isEmpty()) throw new EmptyOrderException(id);
    requireEditable();
    status = OrderStatus.CONFIRMED;
  }

  public void markPaid() {
    if (status != OrderStatus.CONFIRMED) throw new OrderNotConfirmedException(id);
    status = OrderStatus.PAID;
  }

  public Money subtotal() {
    return lines.stream().map(OrderLine::subtotal).reduce(Money.zero(currency), Money::plus);
  }

  public Money total() {
    return discount.applyTo(subtotal());
  }

  public OrderSnapshot snapshot() {
    return new OrderSnapshot(
        id, customerId, currency, placedAt, lines, discount, status, version);
  }

  public OrderId id() {
    return id;
  }

  public CustomerId customerId() {
    return customerId;
  }

  public OrderStatus status() {
    return status;
  }

  public List<OrderLine> lines() {
    return List.copyOf(lines);
  }

  public long version() {
    return version;
  }

  public boolean isEditable() {
    return status == OrderStatus.DRAFT;
  }

  private void requireEditable() {
    if (!isEditable()) throw new OrderNotEditableException(id);
  }
}
```

## Aggregate state for persistence (more than 4 values)

`src/main/java/com/CHANGEME/orders/domain/OrderSnapshot.java`

```java
package com.CHANGEME.orders.domain;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

// Full state of the aggregate as a value object. It groups the data the aggregate needs to be
// built (more than four values) and is what it exchanges with persistence:
// Order.restore(snapshot) when reading and order.snapshot() when saving.
public record OrderSnapshot(
    OrderId id,
    CustomerId customerId,
    Currency currency,
    Instant placedAt,
    List<OrderLine> lines,
    Discount discount,
    OrderStatus status,
    long version) {

  public OrderSnapshot {
    Objects.requireNonNull(id);
    Objects.requireNonNull(customerId);
    Objects.requireNonNull(currency);
    Objects.requireNonNull(placedAt);
    lines = List.copyOf(Objects.requireNonNull(lines));
    Objects.requireNonNull(discount);
    Objects.requireNonNull(status);
    if (version < 0) throw new IllegalArgumentException("Version must not be negative: " + version);
  }
}
```

## Typed identifier

`src/main/java/com/CHANGEME/orders/domain/OrderId.java`

```java
package com.CHANGEME.orders.domain;

// Typed identifier. The value is generated by the orders_seq sequence: it is requested with
// OrderRepository.nextId() before creating the aggregate.
public record OrderId(long value) {

  public OrderId {
    if (value < 1) throw new IllegalArgumentException("Order id must be positive: " + value);
  }
}
```

`src/main/java/com/CHANGEME/orders/domain/ProductId.java`

```java
package com.CHANGEME.orders.domain;

public record ProductId(long value) {

  public ProductId {
    if (value < 1) throw new IllegalArgumentException("Product id must be positive: " + value);
  }
}
```

## ID of an aggregate from another context

`src/main/java/com/CHANGEME/orders/domain/CustomerId.java`

```java
package com.CHANGEME.orders.domain;

// ID of an aggregate from another context (customers). Orders only stores the reference by ID:
// it never imports classes from the customers domain.
public record CustomerId(long value) {

  public CustomerId {
    if (value < 1) throw new IllegalArgumentException("Customer id must be positive: " + value);
  }
}
```

## Collection element (value object without ID)

`src/main/java/com/CHANGEME/orders/domain/OrderLine.java`

```java
package com.CHANGEME.orders.domain;

import com.CHANGEME.common.domain.Money;
import java.util.Objects;

// Element of an aggregate collection: a value object without its own ID. In the database its
// key is the order ID plus the position.
public record OrderLine(ProductId productId, Money unitPrice, int quantity) {

  public OrderLine {
    Objects.requireNonNull(productId);
    Objects.requireNonNull(unitPrice);
    if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive: " + quantity);
  }

  public Money subtotal() {
    return unitPrice.times(quantity);
  }
}
```

## Shared value object

`src/main/java/com/CHANGEME/common/domain/Money.java`

```java
package com.CHANGEME.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

// Value object shared between contexts. Immutable, validated on construction, and it normalises
// the scale to the currency's decimals so that equals() compares by value.
public record Money(BigDecimal amount, Currency currency) {

  public Money {
    Objects.requireNonNull(amount);
    Objects.requireNonNull(currency);
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("Money amount must not be negative: " + amount);
    }
    amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
  }

  public static Money zero(final Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  public Money plus(final Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money minus(final Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  public Money times(final int quantity) {
    return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
  }

  public Money percentOff(final int percent) {
    final BigDecimal factor = BigDecimal.valueOf(100 - percent).movePointLeft(2);
    return new Money(amount.multiply(factor), currency);
  }

  public boolean isGreaterThan(final Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount) > 0;
  }

  private void requireSameCurrency(final Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + currency + " and " + other.currency);
    }
  }
}
```

## Variants with a sealed interface

`src/main/java/com/CHANGEME/orders/domain/Discount.java`

```java
package com.CHANGEME.orders.domain;

import com.CHANGEME.common.domain.Money;
import java.util.Objects;

// Variants of a concept: a sealed interface with records. Each variant's own behaviour goes as
// a method on the interface. NoDiscount avoids an optional or null field in Order.
public sealed interface Discount
    permits Discount.NoDiscount, Discount.PercentageDiscount, Discount.FixedDiscount {

  Money applyTo(Money amount);

  record NoDiscount() implements Discount {
    @Override
    public Money applyTo(final Money amount) {
      return amount;
    }
  }

  record PercentageDiscount(int percent) implements Discount {
    public PercentageDiscount {
      if (percent < 1 || percent > 100) {
        throw new IllegalArgumentException("Percent must be between 1 and 100: " + percent);
      }
    }

    @Override
    public Money applyTo(final Money amount) {
      return amount.percentOff(percent);
    }
  }

  record FixedDiscount(Money value) implements Discount {
    public FixedDiscount {
      Objects.requireNonNull(value);
    }

    @Override
    public Money applyTo(final Money amount) {
      if (value.isGreaterThan(amount)) return Money.zero(amount.currency());
      return amount.minus(value);
    }
  }
}
```

## Persisted enum

`src/main/java/com/CHANGEME/orders/domain/OrderStatus.java`

```java
package com.CHANGEME.orders.domain;

// Stored by name in the database, so values are not renamed without a migration.
public enum OrderStatus {
  DRAFT,
  CONFIRMED,
  PAID
}
```

## Business rule exceptions (409)

`src/main/java/com/CHANGEME/orders/domain/exception/OrderNotEditableException.java`

```java
package com.CHANGEME.orders.domain.exception;

import com.CHANGEME.common.domain.exception.DomainException;
import com.CHANGEME.orders.domain.OrderId;

// One exception per business rule, with the IDs in the message. It becomes a 409.
public final class OrderNotEditableException extends DomainException {

  public OrderNotEditableException(final OrderId id) {
    super("Order " + id.value() + " is not editable");
  }
}
```

`src/main/java/com/CHANGEME/orders/domain/exception/OrderNotConfirmedException.java`

```java
package com.CHANGEME.orders.domain.exception;

import com.CHANGEME.common.domain.exception.DomainException;
import com.CHANGEME.orders.domain.OrderId;

public final class OrderNotConfirmedException extends DomainException {

  public OrderNotConfirmedException(final OrderId id) {
    super("Order " + id.value() + " is not confirmed");
  }
}
```

`src/main/java/com/CHANGEME/orders/domain/exception/EmptyOrderException.java`

```java
package com.CHANGEME.orders.domain.exception;

import com.CHANGEME.common.domain.exception.DomainException;
import com.CHANGEME.orders.domain.OrderId;

public final class EmptyOrderException extends DomainException {

  public EmptyOrderException(final OrderId id) {
    super("Order " + id.value() + " has no lines");
  }
}
```

`src/main/java/com/CHANGEME/orders/domain/exception/OrderTooLargeException.java`

```java
package com.CHANGEME.orders.domain.exception;

import com.CHANGEME.common.domain.exception.DomainException;
import com.CHANGEME.orders.domain.OrderId;

// A guard violation is a business rule failure like any other: its own exception, with the IDs in
// the message.
public final class OrderTooLargeException extends DomainException {

  public OrderTooLargeException(final OrderId id, final int maxLines) {
    super("Order " + id.value() + " cannot have more than " + maxLines + " lines");
  }
}
```

## "Not found" exception (404)

`src/main/java/com/CHANGEME/orders/domain/exception/OrderNotFoundException.java`

```java
package com.CHANGEME.orders.domain.exception;

import com.CHANGEME.common.domain.exception.NotFoundException;
import com.CHANGEME.orders.domain.OrderId;

// "Not found": extends NotFoundException and becomes a 404.
public final class OrderNotFoundException extends NotFoundException {

  public OrderNotFoundException(final OrderId id) {
    super("Order " + id.value() + " not found");
  }
}
```

## Repository interface

`src/main/java/com/CHANGEME/orders/domain/repository/OrderRepository.java`

```java
package com.CHANGEME.orders.domain.repository;

import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderId;
import java.util.Optional;

// One repository per aggregate, as an interface in the domain. It only works with the whole
// aggregate; listings go through a read port in application (OrderSummaries).
public interface OrderRepository {

  OrderId nextId();

  Optional<Order> findById(OrderId id);

  // Inserts if version() == 0; otherwise updates checking the version and throws
  // ConcurrentUpdateException if another request modified it first. The in-memory object keeps
  // the previous version: a use case saves only once and, if it has to keep working with the
  // aggregate, it loads it again.
  void save(Order order);
}
```

## Guard

`src/main/java/com/CHANGEME/orders/domain/guards/OrderSizeGuard.java`

```java
package com.CHANGEME.orders.domain.guards;

import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.exception.OrderTooLargeException;

// Guard: a precondition check that throws a domain exception. Stateless, named after the rule it
// protects, and called by the aggregate before it changes state. Fictional example.
public final class OrderSizeGuard {

  private static final int MAX_LINES = 50;

  private OrderSizeGuard() {}

  public static void ensureCanAddLine(final OrderId id, final int currentLines) {
    if (currentLines >= MAX_LINES) throw new OrderTooLargeException(id, MAX_LINES);
  }
}
```

## Policy

`src/main/java/com/CHANGEME/orders/domain/policies/LoyaltyDiscountPolicy.java`

```java
package com.CHANGEME.orders.domain.policies;

import com.CHANGEME.orders.domain.Discount;

// Policy: a business decision that changes independently of the aggregate (here, which discount a
// customer has earned). Stateless; the use case asks it and passes the result to the aggregate.
// Fictional example.
public final class LoyaltyDiscountPolicy {

  private static final int ORDERS_PER_TIER = 5;
  private static final int PERCENT_PER_TIER = 5;
  private static final int MAX_PERCENT = 15;

  private LoyaltyDiscountPolicy() {}

  public static Discount discountFor(final int previousOrders) {
    final int percent = Math.min(previousOrders / ORDERS_PER_TIER * PERCENT_PER_TIER, MAX_PERCENT);
    if (percent == 0) return new Discount.NoDiscount();
    return new Discount.PercentageDiscount(percent);
  }
}
```

## Domain service

`src/main/java/com/CHANGEME/orders/domain/services/OrderMergeService.java`

```java
package com.CHANGEME.orders.domain.services;

import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderLine;

// Domain service: business logic that involves more than one aggregate and belongs to neither.
// Stateless, no Spring; the use case loads both aggregates, calls it and saves them.
// Fictional example.
public final class OrderMergeService {

  private OrderMergeService() {}

  public static void merge(final Order target, final Order source) {
    for (final OrderLine line : source.lines()) {
      target.addLine(line.productId(), line.unitPrice(), line.quantity());
    }
  }
}
```
