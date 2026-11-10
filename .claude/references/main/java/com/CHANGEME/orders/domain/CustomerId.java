package com.CHANGEME.orders.domain;

// ID of an aggregate from another context (customers). Orders only stores the reference by ID:
// it never imports classes from the customers domain.
public record CustomerId(long value) {

  public CustomerId {
    if (value < 1) throw new IllegalArgumentException("Customer id must be positive: " + value);
  }
}
