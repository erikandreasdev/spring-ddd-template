package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.OrderId;
import java.util.Objects;

public record ConfirmOrderCommand(OrderId orderId) {

  public ConfirmOrderCommand {
    Objects.requireNonNull(orderId);
  }
}
