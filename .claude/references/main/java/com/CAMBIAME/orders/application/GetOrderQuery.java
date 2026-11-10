package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.OrderId;
import java.util.Objects;

// Entrada de un caso de uso que solo lee: record *Query.
public record GetOrderQuery(OrderId orderId) {

  public GetOrderQuery {
    Objects.requireNonNull(orderId);
  }
}
