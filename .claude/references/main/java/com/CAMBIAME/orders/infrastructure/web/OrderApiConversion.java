package com.CAMBIAME.orders.infrastructure.web;

import com.CAMBIAME.orders.application.AddOrderLineCommand;
import com.CAMBIAME.orders.application.ConfirmOrderCommand;
import com.CAMBIAME.orders.application.GetOrderQuery;
import com.CAMBIAME.orders.application.ListCustomerOrdersQuery;
import com.CAMBIAME.orders.application.OrderSummary;
import com.CAMBIAME.orders.application.PayOrderCommand;
import com.CAMBIAME.orders.application.PlaceOrderCommand;
import com.CAMBIAME.orders.domain.CustomerId;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.ProductId;
import com.CAMBIAME.shared.application.PageResult;
import com.CAMBIAME.shared.domain.Money;
import com.CAMBIAME.shared.infrastructure.web.api.AddOrderLineBody;
import com.CAMBIAME.shared.infrastructure.web.api.MoneyBody;
import com.CAMBIAME.shared.infrastructure.web.api.OrderBody;
import com.CAMBIAME.shared.infrastructure.web.api.OrderPageBody;
import com.CAMBIAME.shared.infrastructure.web.api.PageInfoBody;
import com.CAMBIAME.shared.infrastructure.web.api.PlaceOrderBody;
import java.math.BigDecimal;
import java.util.Currency;

// Conversión entre los modelos generados (*Body) y los comandos, consultas y resultados de
// aplicación. Una clase por agregado, final y package-private, junto al controlador.
// Formatos inválidos (BigDecimal, divisa) lanzan IllegalArgumentException y salen como 400.
final class OrderApiConversion {

  private OrderApiConversion() {}

  static PlaceOrderCommand toCommand(final PlaceOrderBody body) {
    return new PlaceOrderCommand(
        new CustomerId(body.getCustomerId()), Currency.getInstance(body.getCurrency()));
  }

  static AddOrderLineCommand toCommand(final long orderId, final AddOrderLineBody body) {
    return new AddOrderLineCommand(
        new OrderId(orderId),
        new ProductId(body.getProductId()),
        toMoney(body.getUnitPrice()),
        body.getQuantity());
  }

  static ConfirmOrderCommand toConfirmCommand(final long orderId) {
    return new ConfirmOrderCommand(new OrderId(orderId));
  }

  static PayOrderCommand toPayCommand(final long orderId) {
    return new PayOrderCommand(new OrderId(orderId));
  }

  static GetOrderQuery toQuery(final long orderId) {
    return new GetOrderQuery(new OrderId(orderId));
  }

  static ListCustomerOrdersQuery toQuery(final long customerId, final int page, final int size) {
    return new ListCustomerOrdersQuery(new CustomerId(customerId), page, size);
  }

  static OrderBody toBody(final OrderSummary summary) {
    return new OrderBody(
        summary.id().value(),
        summary.customerId().value(),
        OrderBody.StatusEnum.fromValue(summary.status().name()),
        toBody(summary.total()));
  }

  static OrderPageBody toBody(final PageResult<OrderSummary> result) {
    return new OrderPageBody(
        result.items().stream().map(OrderApiConversion::toBody).toList(),
        new PageInfoBody(result.page(), result.size(), result.totalItems()));
  }

  private static Money toMoney(final MoneyBody body) {
    return new Money(new BigDecimal(body.getAmount()), Currency.getInstance(body.getCurrency()));
  }

  private static MoneyBody toBody(final Money money) {
    return new MoneyBody(money.amount().toPlainString(), money.currency().getCurrencyCode());
  }
}
