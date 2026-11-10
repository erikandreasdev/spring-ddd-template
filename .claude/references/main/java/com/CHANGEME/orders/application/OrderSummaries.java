package com.CHANGEME.orders.application;

import com.CHANGEME.orders.domain.CustomerId;
import com.CHANGEME.shared.application.PageResult;

// Read port for listings: returns result records directly from SQL, without loading
// aggregates. It lives in application because it is not a domain concept.
public interface OrderSummaries {

  PageResult<OrderSummary> findByCustomer(CustomerId customerId, int page, int size);
}
