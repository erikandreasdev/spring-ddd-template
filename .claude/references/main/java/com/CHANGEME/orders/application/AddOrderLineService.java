package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderNotFoundException;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.shared.application.UseCase;

// Modification use case: load, call the business method, save. Validation of the quantity
// and the currency lives in the domain, not here.
@UseCase
public class AddOrderLineService {

  private final OrderRepository orders;

  public AddOrderLineService(final OrderRepository orders) {
    this.orders = orders;
  }

  public void addLine(final AddOrderLineCommand command) {
    final Order order =
        orders
            .findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
    order.addLine(command.productId(), command.unitPrice(), command.quantity());
    orders.save(order);
  }
}
