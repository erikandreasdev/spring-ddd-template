package com.CHANGEME.orders.infrastructure.persistence;

import com.CHANGEME.orders.application.OrderSummaries;
import com.CHANGEME.orders.application.OrderSummary;
import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderStatus;
import com.CHANGEME.shared.application.PageResult;
import com.CHANGEME.shared.domain.Money;
import java.util.Currency;
import org.springframework.stereotype.Repository;

// Implementation of a read port. It only reads, so it can go straight to SQL without loading
// aggregates.
@Repository
class MyBatisOrderSummaries implements OrderSummaries {

  private final OrderSummaryMapper mapper;

  MyBatisOrderSummaries(final OrderSummaryMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public PageResult<OrderSummary> findByCustomer(
      final CustomerId customerId, final int page, final int size) {
    final long offset = (long) page * size;
    return new PageResult<>(
        mapper.findByCustomer(customerId.value(), size, offset).stream()
            .map(MyBatisOrderSummaries::toSummary)
            .toList(),
        page,
        size,
        mapper.countByCustomer(customerId.value()));
  }

  private static OrderSummary toSummary(final OrderSummaryRow row) {
    return new OrderSummary(
        new OrderId(row.id()),
        new CustomerId(row.customerId()),
        OrderStatus.valueOf(row.status()),
        new Money(row.totalAmount(), Currency.getInstance(row.currency())));
  }
}
