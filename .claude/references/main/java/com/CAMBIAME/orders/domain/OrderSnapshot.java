package com.CAMBIAME.orders.domain;

import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

// Estado completo del agregado como value object. Agrupa los datos que el agregado necesita para
// construirse (más de cuatro) y es lo que intercambia con la persistencia:
// Order.restore(snapshot) al leer y order.snapshot() al guardar.
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
