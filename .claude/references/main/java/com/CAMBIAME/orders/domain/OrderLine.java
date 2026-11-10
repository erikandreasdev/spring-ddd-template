package com.CAMBIAME.orders.domain;

import com.CAMBIAME.shared.domain.Money;
import java.util.Objects;

// Elemento de una colección del agregado: value object sin ID propio. En base de datos su clave
// es el ID del pedido más la posición.
public record OrderLine(ProductId productId, Money unitPrice, int quantity) {

  public OrderLine {
    Objects.requireNonNull(productId);
    Objects.requireNonNull(unitPrice);
    if (quantity < 1) throw new IllegalArgumentException("Quantity must be positive: " + quantity);
  }

  public Money subtotal() {
    return unitPrice.times(quantity);
  }
}
