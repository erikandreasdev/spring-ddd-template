package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderNotFoundException;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.shared.application.UseCase;

// Use case with an external port. The charge goes last: if it fails, the exception rolls back the
// transaction and the order is not left as paid. Validating first in the domain (markPaid) avoids
// charging an order that cannot be paid.
@UseCase
public class PayOrderService {

  private final OrderRepository orders;
  private final PaymentGateway payments;

  public PayOrderService(final OrderRepository orders, final PaymentGateway payments) {
    this.orders = orders;
    this.payments = payments;
  }

  public void pay(final PayOrderCommand command) {
    final Order order =
        orders
            .findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
    order.markPaid();
    orders.save(order);
    payments.charge(order.id(), order.total());
  }
}
