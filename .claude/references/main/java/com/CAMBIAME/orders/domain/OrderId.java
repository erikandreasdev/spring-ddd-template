package com.CAMBIAME.orders.domain;

// Identificador tipado. El valor lo genera la secuencia orders_seq: se pide con
// OrderRepository.nextId() antes de crear el agregado.
public record OrderId(long value) {

  public OrderId {
    if (value < 1) throw new IllegalArgumentException("Order id must be positive: " + value);
  }
}
