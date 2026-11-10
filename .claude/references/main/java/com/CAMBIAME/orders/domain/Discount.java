package com.CAMBIAME.orders.domain;

import com.CAMBIAME.shared.domain.Money;
import java.util.Objects;

// Variantes de un concepto: interfaz sellada con records. El comportamiento propio de cada
// variante va como método de la interfaz. NoDiscount evita un campo opcional o nulo en Order.
public sealed interface Discount
    permits Discount.NoDiscount, Discount.PercentageDiscount, Discount.FixedDiscount {

  Money applyTo(Money amount);

  record NoDiscount() implements Discount {
    @Override
    public Money applyTo(final Money amount) {
      return amount;
    }
  }

  record PercentageDiscount(int percent) implements Discount {
    public PercentageDiscount {
      if (percent < 1 || percent > 100) {
        throw new IllegalArgumentException("Percent must be between 1 and 100: " + percent);
      }
    }

    @Override
    public Money applyTo(final Money amount) {
      return amount.percentOff(percent);
    }
  }

  record FixedDiscount(Money value) implements Discount {
    public FixedDiscount {
      Objects.requireNonNull(value);
    }

    @Override
    public Money applyTo(final Money amount) {
      if (value.isGreaterThan(amount)) return Money.zero(amount.currency());
      return amount.minus(value);
    }
  }
}
