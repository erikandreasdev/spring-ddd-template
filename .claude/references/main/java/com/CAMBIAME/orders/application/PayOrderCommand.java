package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.OrderId;
import java.util.Objects;

public record PayOrderCommand(OrderId orderId) {

  public PayOrderCommand {
    Objects.requireNonNull(orderId);
  }
}
