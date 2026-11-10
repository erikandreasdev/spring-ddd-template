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
