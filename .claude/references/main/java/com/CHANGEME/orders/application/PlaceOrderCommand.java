package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.CustomerId;
import java.util.Currency;
import java.util.Objects;

// Input of a use case that modifies: a *Command record validated in the compact constructor,
// with domain types and no Bean Validation annotations.
public record PlaceOrderCommand(CustomerId customerId, Currency currency) {

  public PlaceOrderCommand {
    Objects.requireNonNull(customerId);
    Objects.requireNonNull(currency);
  }
}
