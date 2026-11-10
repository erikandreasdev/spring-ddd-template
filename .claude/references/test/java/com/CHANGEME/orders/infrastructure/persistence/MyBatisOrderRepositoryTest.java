package com.CHANGEME.orders.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.CHANGEME.IntegrationTest;
import com.CHANGEME.orders.domain.Order;
import com.CHANGEME.orders.domain.OrderMother;
import com.CHANGEME.orders.domain.OrderRepository;
import com.CHANGEME.orders.domain.ProductId;
import com.CHANGEME.shared.domain.ConcurrentUpdateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// The only exception to "repositories have no tests of their own": one test per aggregate that
// proves optimistic locking. It simulates two requests that read the same version.
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
