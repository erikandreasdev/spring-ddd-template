package com.CAMBIAME.orders.domain;

// ID de un agregado de otro contexto (customers). Pedidos solo guarda la referencia por ID:
// nunca importa clases del dominio de clientes.
public record CustomerId(long value) {

  public CustomerId {
    if (value < 1) throw new IllegalArgumentException("Customer id must be positive: " + value);
  }
}
