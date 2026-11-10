package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.shared.domain.Money;

// Puerto de salida hacia un servicio externo: interfaz en application, nombrada por su
// propósito, con tipos del dominio. La implementación (HttpPaymentGateway) vive en
// infrastructure.
public interface PaymentGateway {

  // Lanza PaymentDeclinedException si el proveedor rechaza el cobro.
  void charge(OrderId orderId, Money amount);
}
