package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.ProductId;
import com.CHANGEME.shared.domain.Money;
import java.util.Objects;

public record AddOrderLineCommand(
    OrderId orderId, ProductId productId, Money unitPrice, int quantity) {

  public AddOrderLineCommand {
    Objects.requireNonNull(orderId);
    Objects.requireNonNull(productId);
    Objects.requireNonNull(unitPrice);
  }
}
