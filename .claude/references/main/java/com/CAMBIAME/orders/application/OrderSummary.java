package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.CustomerId;
import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.OrderStatus;
import com.CAMBIAME.shared.domain.Money;
import java.util.Objects;

// Record de resultado de una consulta. El caso de uso nunca devuelve el agregado.
public record OrderSummary(OrderId id, CustomerId customerId, OrderStatus status, Money total) {

  public OrderSummary {
    Objects.requireNonNull(id);
    Objects.requireNonNull(customerId);
    Objects.requireNonNull(status);
    Objects.requireNonNull(total);
  }

  static OrderSummary of(final Order order) {
    return new OrderSummary(order.id(), order.customerId(), order.status(), order.total());
  }
}
