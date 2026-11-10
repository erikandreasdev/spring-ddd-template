package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.shared.domain.Money;

// Output port to an external service: an interface in application, named after its purpose,
// with domain types. The implementation (HttpPaymentGateway) lives in infrastructure.
public interface PaymentGateway {

  // Throws PaymentDeclinedException if the provider declines the charge.
  void charge(OrderId orderId, Money amount);
}
