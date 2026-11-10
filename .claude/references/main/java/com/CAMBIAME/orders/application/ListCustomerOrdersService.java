package com.CAMBIAME.orders.application;

import com.CAMBIAME.shared.application.PageResult;
import com.CAMBIAME.shared.application.UseCase;

@UseCase
public class ListCustomerOrdersService {

  private final OrderSummaries summaries;

  public ListCustomerOrdersService(final OrderSummaries summaries) {
    this.summaries = summaries;
  }

  public PageResult<OrderSummary> list(final ListCustomerOrdersQuery query) {
    return summaries.findByCustomer(query.customerId(), query.page(), query.size());
  }
}
