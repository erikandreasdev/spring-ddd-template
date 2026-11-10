package com.CHANGEME.orders.infrastructure.payment;

import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.shared.domain.Money;

// Body expected by the external provider. It is an adapter detail: it does not leave this package.
record ChargeRequest(String reference, String amount, String currency) {

  static ChargeRequest of(final OrderId orderId, final Money amount) {
    return new ChargeRequest(
        "order-" + orderId.value(),
        amount.amount().toPlainString(),
        amount.currency().getCurrencyCode());
  }
}
