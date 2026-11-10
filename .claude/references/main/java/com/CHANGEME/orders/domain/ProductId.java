package com.CHANGEME.orders.domain;

public record ProductId(long value) {

  public ProductId {
    if (value < 1) throw new IllegalArgumentException("Product id must be positive: " + value);
  }
}
