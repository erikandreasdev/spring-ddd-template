package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.OrderId;
import java.util.Objects;

public record ConfirmOrderCommand(OrderId orderId) {

  public ConfirmOrderCommand {
    Objects.requireNonNull(orderId);
  }
}
