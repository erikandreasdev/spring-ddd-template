package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.CustomerId;
import java.util.Currency;
import java.util.Objects;

// Entrada de un caso de uso que modifica: record *Command validado en el constructor compacto,
// con tipos del dominio y sin anotaciones de Bean Validation.
public record PlaceOrderCommand(CustomerId customerId, Currency currency) {

  public PlaceOrderCommand {
    Objects.requireNonNull(customerId);
    Objects.requireNonNull(currency);
  }
}
