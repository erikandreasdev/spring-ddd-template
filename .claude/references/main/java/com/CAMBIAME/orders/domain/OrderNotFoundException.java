package com.CAMBIAME.orders.domain;

import com.CAMBIAME.shared.domain.NotFoundException;

// "No existe": extiende NotFoundException y sale como 404.
public final class OrderNotFoundException extends NotFoundException {

  public OrderNotFoundException(final OrderId id) {
    super("Order " + id.value() + " not found");
  }
}
