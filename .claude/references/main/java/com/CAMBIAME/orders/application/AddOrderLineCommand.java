package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.ProductId;
import com.CAMBIAME.shared.domain.Money;
import java.util.Objects;

public record AddOrderLineCommand(
    OrderId orderId, ProductId productId, Money unitPrice, int quantity) {

  public AddOrderLineCommand {
    Objects.requireNonNull(orderId);
    Objects.requireNonNull(productId);
    Objects.requireNonNull(unitPrice);
  }
}
