package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.shared.domain.DomainException;

// Part of the PaymentGateway port's contract, which is why it lives next to it. It extends
// DomainException so it becomes a 409 and triggers a rollback.
public final class PaymentDeclinedException extends DomainException {

  public PaymentDeclinedException(final OrderId id) {
    super("Payment for order " + id.value() + " was declined");
  }
}
