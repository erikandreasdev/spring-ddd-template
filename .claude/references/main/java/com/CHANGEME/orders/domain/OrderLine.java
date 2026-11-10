package com.CHANGEME.orders.domain;

import com.CHANGEME.shared.domain.Money;
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
