package com.CAMBIAME.orders.infrastructure.web;

import com.CAMBIAME.orders.application.AddOrderLineService;
import com.CAMBIAME.orders.application.ConfirmOrderService;
import com.CAMBIAME.orders.application.GetOrderService;
import com.CAMBIAME.orders.application.ListCustomerOrdersService;
import com.CAMBIAME.orders.application.PayOrderService;
import com.CAMBIAME.orders.application.PlaceOrderService;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.shared.infrastructure.web.api.AddOrderLineBody;
import com.CAMBIAME.shared.infrastructure.web.api.OrderBody;
import com.CAMBIAME.shared.infrastructure.web.api.OrderPageBody;
import com.CAMBIAME.shared.infrastructure.web.api.OrdersApi;
import com.CAMBIAME.shared.infrastructure.web.api.PlaceOrderBody;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

// Un controlador por agregado que implementa la interfaz generada desde api.yaml. Las rutas,
// la validación y los códigos los define la especificación; aquí solo se convierte, se llama al
// caso de uso y se construye la respuesta. Sin lógica, sin try/catch y sin repositorios.
// Si crece por encima de seis o siete endpoints, es señal de partirlo.
@RestController
class OrderController implements OrdersApi {

  private final PlaceOrderService placeOrder;
  private final AddOrderLineService addOrderLine;
  private final ConfirmOrderService confirmOrder;
  private final PayOrderService payOrder;
  private final GetOrderService getOrder;
  private final ListCustomerOrdersService listCustomerOrders;

  OrderController(
      final PlaceOrderService placeOrder,
      final AddOrderLineService addOrderLine,
      final ConfirmOrderService confirmOrder,
      final PayOrderService payOrder,
      final GetOrderService getOrder,
      final ListCustomerOrdersService listCustomerOrders) {
    this.placeOrder = placeOrder;
    this.addOrderLine = addOrderLine;
    this.confirmOrder = confirmOrder;
    this.payOrder = payOrder;
    this.getOrder = getOrder;
    this.listCustomerOrders = listCustomerOrders;
  }

  @Override
  public ResponseEntity<Void> placeOrder(final PlaceOrderBody body) {
    final OrderId id = placeOrder.place(OrderApiConversion.toCommand(body));
    return ResponseEntity.created(URI.create("/v1/orders/" + id.value())).build();
  }

  @Override
  public ResponseEntity<Void> addOrderLine(final Long id, final AddOrderLineBody body) {
    addOrderLine.addLine(OrderApiConversion.toCommand(id, body));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> confirmOrder(final Long id) {
    confirmOrder.confirm(OrderApiConversion.toConfirmCommand(id));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> payOrder(final Long id) {
    payOrder.pay(OrderApiConversion.toPayCommand(id));
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<OrderBody> getOrder(final Long id) {
    return ResponseEntity.ok(OrderApiConversion.toBody(getOrder.get(OrderApiConversion.toQuery(id))));
  }

  @Override
  public ResponseEntity<OrderPageBody> listCustomerOrders(
      final Long customerId, final Integer page, final Integer size) {
    return ResponseEntity.ok(
        OrderApiConversion.toBody(
            listCustomerOrders.list(OrderApiConversion.toQuery(customerId, page, size))));
  }
}
