package com.CAMBIAME.orders.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.CAMBIAME.IntegrationTest;
import com.CAMBIAME.orders.domain.Order;
import com.CAMBIAME.orders.domain.OrderMother;
import com.CAMBIAME.orders.domain.OrderRepository;
import com.CAMBIAME.orders.domain.ProductId;
import com.CAMBIAME.shared.domain.ConcurrentUpdateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// La única excepción a "los repositorios no tienen tests propios": un test por agregado que
// demuestra el bloqueo optimista. Simula dos peticiones que leen la misma versión.
@IntegrationTest
class MyBatisOrderRepositoryTest {

  @Autowired private OrderRepository orders;

  @Test
  void givenTwoCopiesOfTheSameVersion_whenBothAreSaved_thenSecondSaveFails() {
    // given
    final Order order = OrderMother.draftWithOneLine(orders.nextId());
    orders.save(order);
    final Order first = orders.findById(order.id()).orElseThrow();
    final Order second = orders.findById(order.id()).orElseThrow();
    first.addLine(new ProductId(1), OrderMother.price(), 1);
    orders.save(first);
    second.confirm();

    // when / then
    assertThatThrownBy(() -> orders.save(second))
        .isInstanceOf(ConcurrentUpdateException.class);
  }
}
