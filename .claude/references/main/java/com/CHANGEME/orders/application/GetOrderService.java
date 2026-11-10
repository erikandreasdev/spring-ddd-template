package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.OrderNotFoundException;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.shared.application.UseCase;

// Reading an aggregate by its ID: repository plus conversion to the result record.
@UseCase
public class GetOrderService {

  private final OrderRepository orders;

  public GetOrderService(final OrderRepository orders) {
    this.orders = orders;
  }

  public OrderSummary get(final GetOrderQuery query) {
    return orders
        .findById(query.orderId())
        .map(OrderSummary::of)
        .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
  }
}
