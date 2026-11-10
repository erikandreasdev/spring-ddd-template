package com.CAMBIAME.orders.application;

import static com.CAMBIAME.orders.domain.OrderMother.EUR;
import static org.assertj.core.api.Assertions.assertThat;

import com.CAMBIAME.IntegrationTest;
import com.CAMBIAME.orders.domain.CustomerId;
import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.OrderMother;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.shared.application.PageResult;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// Test de un puerto de lectura: uno por cada filtro, orden o paginación. Cada test usa un cliente
// nuevo, así que los datos de otros tests no interfieren aunque la base de datos se comparta.
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

  // Aquí la fecha es un literal porque el orden depende de ella.
  private OrderId placeAt(final CustomerId customer, final String placedAt) {
    final Order order = Order.place(orders.nextId(), customer, EUR, Instant.parse(placedAt));
    orders.save(order);
    return order.id();
  }
}
