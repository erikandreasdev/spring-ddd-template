package com.CHANGEME.orders.domain;

import com.CHANGEME.shared.domain.DomainException;

public final class OrderNotConfirmedException extends DomainException {

  public OrderNotConfirmedException(final OrderId id) {
    super("Order " + id.value() + " is not confirmed");
  }
}
