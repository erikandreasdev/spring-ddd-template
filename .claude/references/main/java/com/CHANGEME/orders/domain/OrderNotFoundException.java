package com.CHANGEME.orders.domain;

import com.CHANGEME.shared.domain.NotFoundException;

// "Not found": extends NotFoundException and becomes a 404.
public final class OrderNotFoundException extends NotFoundException {

  public OrderNotFoundException(final OrderId id) {
    super("Order " + id.value() + " not found");
  }
}
