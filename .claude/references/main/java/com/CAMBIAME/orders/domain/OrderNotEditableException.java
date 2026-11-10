package com.CAMBIAME.orders.domain;

import com.CAMBIAME.shared.domain.DomainException;

// Una excepción por regla de negocio, con los IDs en el mensaje. Sale como 409.
public final class OrderNotEditableException extends DomainException {

  public OrderNotEditableException(final OrderId id) {
    super("Order " + id.value() + " is not editable");
  }
}
