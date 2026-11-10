package com.CHANGEME.orders.infrastructure.web;

import com.CHANGEME.orders.application.AddOrderLineService;
import com.CHANGEME.orders.application.ConfirmOrderService;
import com.CHANGEME.orders.application.GetOrderService;
import com.CHANGEME.orders.application.ListCustomerOrdersService;
import com.CHANGEME.orders.application.PayOrderService;
import com.CHANGEME.orders.application.PlaceOrderService;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.shared.infrastructure.web.api.AddOrderLineBody;
import com.CHANGEME.shared.infrastructure.web.api.OrderBody;
import com.CHANGEME.shared.infrastructure.web.api.OrderPageBody;
import com.CHANGEME.shared.infrastructure.web.api.OrdersApi;
import com.CHANGEME.shared.infrastructure.web.api.PlaceOrderBody;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

// One controller per aggregate that implements the interface generated from api.yaml. Paths,
// validation and status codes are defined by the specification; here it only converts, calls the
// use case and builds the response. No logic, no try/catch and no repositories.
// If it grows beyond six or seven endpoints, it is a sign to split it.
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
