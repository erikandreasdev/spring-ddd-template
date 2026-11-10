package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.CustomerId;
import java.util.Objects;

public record ListCustomerOrdersQuery(CustomerId customerId, int page, int size) {

  public ListCustomerOrdersQuery {
    Objects.requireNonNull(customerId);
    if (page < 0) throw new IllegalArgumentException("Page must not be negative: " + page);
    if (size < 1 || size > 100) {
      throw new IllegalArgumentException("Size must be between 1 and 100: " + size);
    }
  }
}
