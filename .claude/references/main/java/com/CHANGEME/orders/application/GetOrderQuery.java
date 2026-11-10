package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.OrderId;
import java.util.Objects;

// Input of a use case that only reads: a *Query record.
public record GetOrderQuery(OrderId orderId) {

  public GetOrderQuery {
    Objects.requireNonNull(orderId);
  }
}
