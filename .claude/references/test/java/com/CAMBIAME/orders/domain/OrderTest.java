package com.CAMBIAME.orders.domain;

import static com.CAMBIAME.orders.domain.OrderMother.EUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.CAMBIAME.shared.domain.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// Test unitario de dominio, sin Spring: uno por regla de negocio. Objetos reales, sin mocks.
// Literales solo donde el test comprueba el valor (totales); el resto sale del Mother.
@ExtendWith(InstancioExtension.class)
class OrderTest {

  @Test
  void givenDraftOrderWithLines_whenConfirm_thenStatusIsConfirmed() {
    // given
    final Order order = OrderMother.draftWithOneLine();

    // when
    order.confirm();

    // then
    assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
    assertThat(order.isEditable()).isFalse();
  }

  @Test
  void givenOrderWithoutLines_whenConfirm_thenEmptyOrderExceptionIsThrown() {
    // given
    final Order order = OrderMother.draft();

    // when / then
    assertThatThrownBy(order::confirm).isInstanceOf(EmptyOrderException.class);
  }

  @Test
  void givenConfirmedOrder_whenAddLine_thenOrderNotEditableExceptionIsThrown() {
    // given
    final Order order = OrderMother.confirmed();

    // when / then
    assertThatThrownBy(() -> order.addLine(new ProductId(1), OrderMother.price(), 1))
        .isInstanceOf(OrderNotEditableException.class);
  }

  @Test
  void givenPriceInOtherCurrency_whenAddLine_thenIllegalArgumentExceptionIsThrown() {
    // given
    final Order order = OrderMother.draft();
    final Money dollars = new Money(BigDecimal.TEN, Currency.getInstance("USD"));

    // when / then
    assertThatThrownBy(() -> order.addLine(new ProductId(1), dollars, 1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenTwoLines_whenTotal_thenSubtotalsAreAdded() {
    // given
    final Order order = OrderMother.draft();
    order.addLine(new ProductId(1), new Money(new BigDecimal("10.00"), EUR), 2);
    order.addLine(new ProductId(2), new Money(new BigDecimal("5.50"), EUR), 1);

    // when
    final Money total = order.total();

    // then
    assertThat(total).isEqualTo(new Money(new BigDecimal("25.50"), EUR));
  }

  @ParameterizedTest(name = "{0}% off 80.00 is {1}")
  @CsvSource({"10, 72.00", "25, 60.00", "100, 0.00"})
  void givenPercentageDiscount_whenTotal_thenDiscountIsApplied(
      final int percent, final String expected) {
    // given
    final Order order = OrderMother.draft();
    order.addLine(new ProductId(1), new Money(new BigDecimal("40.00"), EUR), 2);
    order.applyDiscount(new Discount.PercentageDiscount(percent));

    // when
    final Money total = order.total();

    // then
    assertThat(total).isEqualTo(new Money(new BigDecimal(expected), EUR));
  }

  @Test
  void givenFixedDiscountGreaterThanSubtotal_whenTotal_thenTotalIsZero() {
    // given
    final Order order = OrderMother.draft();
    order.addLine(new ProductId(1), new Money(new BigDecimal("15.00"), EUR), 1);
    order.applyDiscount(new Discount.FixedDiscount(new Money(new BigDecimal("20.00"), EUR)));

    // when
    final Money total = order.total();

    // then
    assertThat(total).isEqualTo(Money.zero(EUR));
  }

  @Test
  void givenDraftOrder_whenMarkPaid_thenOrderNotConfirmedExceptionIsThrown() {
    // given
    final Order order = OrderMother.draftWithOneLine();

    // when / then
    assertThatThrownBy(order::markPaid).isInstanceOf(OrderNotConfirmedException.class);
  }
}
