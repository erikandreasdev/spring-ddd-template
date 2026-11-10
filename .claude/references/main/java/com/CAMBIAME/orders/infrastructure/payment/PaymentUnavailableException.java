package com.CAMBIAME.orders.infrastructure.payment;

import com.CAMBIAME.orders.domain.OrderId;

// Fallo técnico de la integración (timeout, 5xx). No es una regla de negocio: no extiende
// DomainException, conserva la causa y sale como 500, registrado una vez en ApiExceptionHandler.
final class PaymentUnavailableException extends RuntimeException {

  PaymentUnavailableException(final OrderId orderId, final Throwable cause) {
    super("Payment provider unavailable for order " + orderId.value(), cause);
  }
}
