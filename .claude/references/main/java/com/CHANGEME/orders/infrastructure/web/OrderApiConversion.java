package com.CHANGEME.orders.infrastructure.web;

import com.CHANGEME.orders.application.AddOrderLineCommand;
import com.CHANGEME.orders.application.ConfirmOrderCommand;
import com.CHANGEME.orders.application.GetOrderQuery;
import com.CHANGEME.orders.application.ListCustomerOrdersQuery;
import com.CHANGEME.orders.application.OrderSummary;
import com.CHANGEME.orders.application.PayOrderCommand;
import com.CHANGEME.orders.application.PlaceOrderCommand;
import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.ProductId;
import com.CHANGEME.shared.application.PageResult;
import com.CHANGEME.shared.domain.Money;
import com.CHANGEME.shared.infrastructure.web.api.AddOrderLineBody;
import com.CHANGEME.shared.infrastructure.web.api.MoneyBody;
import com.CHANGEME.shared.infrastructure.web.api.OrderBody;
import com.CHANGEME.shared.infrastructure.web.api.OrderPageBody;
import com.CHANGEME.shared.infrastructure.web.api.PageInfoBody;
import com.CHANGEME.shared.infrastructure.web.api.PlaceOrderBody;
import java.math.BigDecimal;
import java.util.Currency;

// Conversion between the generated models (*Body) and the application commands, queries and
// results. One class per aggregate, final and package-private, next to the controller.
// Invalid formats (BigDecimal, currency) throw IllegalArgumentException and become a 400.
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
