package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.shared.application.UseCase;
import java.time.Clock;

// Creation use case: requests the ID from the sequence, creates with the business factory and
// returns only the ID. The time comes from the injected Clock. No @Service or @Transactional.
@UseCase
public class PlaceOrderService {

  private final OrderRepository orders;
  private final Clock clock;

  public PlaceOrderService(final OrderRepository orders, final Clock clock) {
    this.orders = orders;
    this.clock = clock;
  }

  public OrderId place(final PlaceOrderCommand command) {
    final Order order =
        Order.place(orders.nextId(), command.customerId(), command.currency(), clock.instant());
    orders.save(order);
    return order.id();
  }
}
