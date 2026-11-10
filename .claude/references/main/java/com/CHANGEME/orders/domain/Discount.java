package com.CHANGEME.orders.domain;

import com.CHANGEME.shared.domain.Money;
import java.util.Objects;

// Variants of a concept: a sealed interface with records. Each variant's own behaviour goes as
// a method on the interface. NoDiscount avoids an optional or null field in Order.
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
