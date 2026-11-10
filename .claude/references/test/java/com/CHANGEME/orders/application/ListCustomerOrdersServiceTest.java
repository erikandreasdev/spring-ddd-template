package com.CHANGEME.orders.application;

import static com.CHANGEME.orders.domain.OrderMother.EUR;
import static org.assertj.core.api.Assertions.assertThat;

import com.CHANGEME.IntegrationTest;
import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderMother;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.shared.application.PageResult;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// Read port test: one per filter, ordering or pagination. Each test uses a new customer, so
// data from other tests does not interfere even though the database is shared.
@IntegrationTest
class ListCustomerOrdersServiceTest {

  @Autowired private ListCustomerOrdersService listCustomerOrders;
  @Autowired private OrderRepository orders;

  @Test
  void givenOrdersOfSeveralCustomers_whenList_thenOnlyThatCustomerOrdersAreCounted() {
    // given
    final CustomerId customer = OrderMother.customerId();
    orders.save(OrderMother.draftWithOneLine(orders.nextId(), customer));
    orders.save(OrderMother.draftWithOneLine(orders.nextId(), customer));
    orders.save(OrderMother.draftWithOneLine(orders.nextId(), OrderMother.customerId()));

    // when
    final PageResult<OrderSummary> result =
        listCustomerOrders.list(new ListCustomerOrdersQuery(customer, 0, 20));

    // then
    assertThat(result.totalItems()).isEqualTo(2);
    assertThat(result.items()).extracting(OrderSummary::customerId).containsOnly(customer);
  }

  @Test
  void givenThreeOrders_whenListSecondPageOfTwo_thenOldestOrderIsReturned() {
    // given
    final CustomerId customer = OrderMother.customerId();
    final OrderId oldest = placeAt(customer, "2026-01-01T10:00:00Z");
    placeAt(customer, "2026-02-01T10:00:00Z");
    placeAt(customer, "2026-03-01T10:00:00Z");

    // when
    final PageResult<OrderSummary> result =
        listCustomerOrders.list(new ListCustomerOrdersQuery(customer, 1, 2));

    // then
    assertThat(result.items()).extracting(OrderSummary::id).containsExactly(oldest);
    assertThat(result.totalItems()).isEqualTo(3);
  }

  // Here the date is a literal because the ordering depends on it.
  private OrderId placeAt(final CustomerId customer, final String placedAt) {
    final Order order = Order.place(orders.nextId(), customer, EUR, Instant.parse(placedAt));
    orders.save(order);
    return order.id();
  }
}
