package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderNotFoundException;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.shared.application.UseCase;

// Caso de uso con un puerto externo. El cobro va al final: si falla, la excepción deshace la
// transacción y el pedido no queda como pagado. Validar antes en el dominio (markPaid) evita
// cobrar un pedido que no se puede pagar.
@UseCase
public class PayOrderService {

  private final OrderRepository orders;
  private final PaymentGateway payments;

  public PayOrderService(final OrderRepository orders, final PaymentGateway payments) {
    this.orders = orders;
    this.payments = payments;
  }

  public void pay(final PayOrderCommand command) {
    final Order order =
        orders
            .findById(command.orderId())
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
    order.markPaid();
    orders.save(order);
    payments.charge(order.id(), order.total());
  }
}
