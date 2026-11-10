package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderNotFoundException;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.shared.application.UseCase;

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
