package com.CAMBIAME.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// Test de value object: un test por caso límite. @ParameterizedTest solo cuando cambia el dato y
// no el comportamiento, siempre con name legible.
class MoneyTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  @ParameterizedTest(name = "amount {0}")
  @ValueSource(strings = {"-0.01", "-1", "-1000"})
  void givenNegativeAmount_whenCreate_thenIllegalArgumentExceptionIsThrown(final String amount) {
    // when / then
    assertThatThrownBy(() -> new Money(new BigDecimal(amount), EUR))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void givenSameValueWithDifferentScale_whenCompare_thenTheyAreEqual() {
    // given
    final Money ten = new Money(new BigDecimal("10"), EUR);

    // when / then
    assertThat(ten).isEqualTo(new Money(new BigDecimal("10.00"), EUR));
  }

  @Test
  void givenAmountWithMoreDecimalsThanCurrency_whenCreate_thenItIsRoundedHalfEven() {
    // when
    final Money money = new Money(new BigDecimal("2.345"), EUR);

    // then
    assertThat(money.amount()).isEqualByComparingTo("2.34");
  }

  @Test
  void givenDifferentCurrencies_whenPlus_thenIllegalArgumentExceptionIsThrown() {
    // given
    final Money euros = new Money(BigDecimal.ONE, EUR);
    final Money dollars = new Money(BigDecimal.ONE, Currency.getInstance("USD"));

    // when / then
    assertThatThrownBy(() -> euros.plus(dollars)).isInstanceOf(IllegalArgumentException.class);
  }
}
