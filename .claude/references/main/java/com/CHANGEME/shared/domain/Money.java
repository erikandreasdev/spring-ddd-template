package com.CHANGEME.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

// Value object shared between contexts. Immutable, validated on construction, and it normalises
// the scale to the currency's decimals so that equals() compares by value.
public record Money(BigDecimal amount, Currency currency) {

  public Money {
    Objects.requireNonNull(amount);
    Objects.requireNonNull(currency);
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("Money amount must not be negative: " + amount);
    }
    amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
  }

  public static Money zero(final Currency currency) {
    return new Money(BigDecimal.ZERO, currency);
  }

  public Money plus(final Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money minus(final Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  public Money times(final int quantity) {
    return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
  }

  public Money percentOff(final int percent) {
    final BigDecimal factor = BigDecimal.valueOf(100 - percent).movePointLeft(2);
    return new Money(amount.multiply(factor), currency);
  }

  public boolean isGreaterThan(final Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount) > 0;
  }

  private void requireSameCurrency(final Money other) {
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + currency + " and " + other.currency);
    }
  }
}
