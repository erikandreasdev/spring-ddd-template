package com.CAMBIAME.orders.infrastructure.persistence;

import com.CAMBIAME.orders.application.OrderSummaries;
import com.CAMBIAME.orders.application.OrderSummary;
import com.CAMBIAME.orders.domain.CustomerId;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.OrderStatus;
import com.CAMBIAME.shared.application.PageResult;
import com.CAMBIAME.shared.domain.Money;
import java.util.Currency;
import org.springframework.stereotype.Repository;

// Implementación de un puerto de lectura. Solo lee, así que puede ir directa a SQL sin cargar
// agregados.
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
