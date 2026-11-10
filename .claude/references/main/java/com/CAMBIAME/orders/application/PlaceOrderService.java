package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.shared.application.UseCase;
import java.time.Clock;

// Caso de uso de creación: pide el ID a la secuencia, crea con la factoría de negocio y devuelve
// solo el ID. La hora sale del Clock inyectado. Sin @Service ni @Transactional.
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
