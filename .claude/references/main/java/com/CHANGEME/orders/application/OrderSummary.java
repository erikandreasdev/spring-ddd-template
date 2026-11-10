package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderStatus;
import com.CHANGEME.shared.domain.Money;
import java.util.Objects;

// Result record of a query. The use case never returns the aggregate.
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
