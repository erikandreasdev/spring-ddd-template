package com.CAMBIAME.orders.domain;

import com.CAMBIAME.shared.domain.DomainException;

public final class EmptyOrderException extends DomainException {

  public EmptyOrderException(final OrderId id) {
    super("Order " + id.value() + " has no lines");
  }
}
