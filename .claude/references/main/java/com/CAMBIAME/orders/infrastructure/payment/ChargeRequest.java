package com.CAMBIAME.orders.infrastructure.payment;

import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.shared.domain.Money;

// Cuerpo que espera el proveedor externo. Es un detalle del adaptador: no sale de este paquete.
record ChargeRequest(String reference, String amount, String currency) {

  static ChargeRequest of(final OrderId orderId, final Money amount) {
    return new ChargeRequest(
        "order-" + orderId.value(),
        amount.amount().toPlainString(),
        amount.currency().getCurrencyCode());
  }
}
