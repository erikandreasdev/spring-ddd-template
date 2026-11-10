package com.CHANGEME.orders.domain;

import com.CHANGEME.shared.domain.DomainException;

// One exception per business rule, with the IDs in the message. It becomes a 409.
public final class OrderNotEditableException extends DomainException {

  public OrderNotEditableException(final OrderId id) {
    super("Order " + id.value() + " is not editable");
  }
}
