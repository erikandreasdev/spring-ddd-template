package com.CHANGEME.orders.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;

// Row of the orders table, as is. Database types (long, String, BigDecimal), never domain
// types. total_amount is a copy of the total computed by the domain, so that read queries do not
// repeat the discount rules in SQL.
record OrderRow(
    long id,
    long customerId,
    String currency,
    Instant placedAt,
    String discountType,
    BigDecimal discountValue,
    String status,
    BigDecimal totalAmount,
    long version) {}
