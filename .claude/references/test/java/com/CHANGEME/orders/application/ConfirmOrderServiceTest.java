package com.CHANGEME.orders.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.CHANGEME.IntegrationTest;
import com.CHANGEME.orders.domain.EmptyOrderException;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderId;
import com.CHANGEME.orders.domain.OrderMother;
import com.CHANGEME.orders.domain.OrderNotFoundException;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// Use case test: real database and real repository, no mocks. Three at most:
// happy path, "not found" and one rule failure. The rules themselves are tested in OrderTest.
// Each test creates its data with new IDs and checks the state by reading from the repository.
@IntegrationTest
class ConfirmOrderServiceTest {

  @Autowired private ConfirmOrderService confirmOrder;
  @Autowired private OrderRepository orders;

  @Test
  void givenDraftOrderWithLines_whenConfirm_thenOrderIsConfirmed() {
    // given
    final Order order = OrderMother.draftWithOneLine(orders.nextId());
    orders.save(order);

    // when
    confirmOrder.confirm(new ConfirmOrderCommand(order.id()));

    // then
    assertThat(orders.findById(order.id()).orElseThrow().status())
        .isEqualTo(OrderStatus.CONFIRMED);
  }

  @Test
  void givenUnknownOrder_whenConfirm_thenOrderNotFoundExceptionIsThrown() {
    // given
    final OrderId unknown = orders.nextId();

    // when / then
    assertThatThrownBy(() -> confirmOrder.confirm(new ConfirmOrderCommand(unknown)))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  void givenOrderWithoutLines_whenConfirm_thenItStaysDraft() {
    // given
    final Order order = OrderMother.draft(orders.nextId(), OrderMother.customerId());
    orders.save(order);

    // when / then
    assertThatThrownBy(() -> confirmOrder.confirm(new ConfirmOrderCommand(order.id())))
        .isInstanceOf(EmptyOrderException.class);
    assertThat(orders.findById(order.id()).orElseThrow().status()).isEqualTo(OrderStatus.DRAFT);
  }
}
