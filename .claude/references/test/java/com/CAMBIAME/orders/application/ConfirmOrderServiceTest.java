package com.CAMBIAME.orders.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.CAMBIAME.IntegrationTest;
import com.CAMBIAME.orders.domain.EmptyOrderException;
import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderId;
import com.CAMBIAME.orders.domain.OrderMother;
import com.CAMBIAME.orders.domain.OrderNotFoundException;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.orders.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// Test de caso de uso: base de datos real y repositorio real, sin mocks. Tres como máximo:
// camino feliz, "no existe" y un fallo de regla. Las reglas en sí se prueban en OrderTest.
// Cada test crea sus datos con IDs nuevos y comprueba el estado leyendo del repositorio.
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
