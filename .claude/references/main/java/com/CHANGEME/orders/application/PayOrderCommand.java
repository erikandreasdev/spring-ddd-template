package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.OrderId;
import java.util.Objects;

public record PayOrderCommand(OrderId orderId) {

  public PayOrderCommand {
    Objects.requireNonNull(orderId);
  }
}
