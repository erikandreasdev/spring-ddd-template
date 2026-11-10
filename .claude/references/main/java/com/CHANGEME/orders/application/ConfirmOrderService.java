package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderNotFoundException;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.shared.application.UseCase;

@UseCase
public class ConfirmOrderService {

  private final OrderRepository orders;

  public ConfirmOrderService(final OrderRepository orders) {
    this.orders = orders;
  }

  public void confirm(final ConfirmOrderCommand command) {
    final Order order =
        orders
            .findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
    order.confirm();
    orders.save(order);
  }
}
