package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderNotFoundException;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.shared.application.UseCase;

// Caso de uso de modificación: cargar, llamar al método de negocio, guardar. La validación de la
// cantidad y de la divisa vive en el dominio, no aquí.
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
