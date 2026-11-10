package com.CHANGEME.orders.domain;

import java.util.Optional;

// One repository per aggregate, as an interface in the domain. It only works with the whole
// aggregate; listings go through a read port in application (OrderSummaries).
public interface OrderRepository {

  OrderId nextId();

  Optional<Order> findById(OrderId id);

  // Inserts if version() == 0; otherwise updates checking the version and throws
  // ConcurrentUpdateException if another request modified it first. The in-memory object keeps
  // the previous version: a use case saves only once and, if it has to keep working with the
  // aggregate, it loads it again.
  void save(Order order);
}
