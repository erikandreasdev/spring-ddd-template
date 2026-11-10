package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.OrderNotFoundException;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.shared.application.UseCase;

// Lectura de un agregado por su ID: repositorio y conversión al record de resultado.
@UseCase
public class GetOrderService {

  private final OrderRepository orders;

  public GetOrderService(final OrderRepository orders) {
    this.orders = orders;
  }

  public OrderSummary get(final GetOrderQuery query) {
    return orders
        .findById(query.orderId())
        .map(OrderSummary::of)
        .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
  }
}
