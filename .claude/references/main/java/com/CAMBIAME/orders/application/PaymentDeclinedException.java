package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.shared.domain.DomainException;

// Forma parte del contrato del puerto PaymentGateway, por eso vive junto a él. Extiende
// DomainException para salir como 409 y provocar rollback.
public final class PaymentDeclinedException extends DomainException {

  public PaymentDeclinedException(final OrderId id) {
    super("Payment for order " + id.value() + " was declined");
  }
}
