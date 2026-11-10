package com.CHANGEME.orders.infrastructure.payment;

import com.CHANGEME.orders.domain.OrderId;

// Technical integration failure (timeout, 5xx). It is not a business rule: it does not extend
// DomainException, keeps the cause and becomes a 500, logged once in ApiExceptionHandler.
final class PaymentUnavailableException extends RuntimeException {

  PaymentUnavailableException(final OrderId orderId, final Throwable cause) {
    super("Payment provider unavailable for order " + orderId.value(), cause);
  }
}
