package com.CHANGEME.orders.infrastructure.persistence;

import java.math.BigDecimal;

record OrderSummaryRow(
    long id, long customerId, String status, BigDecimal totalAmount, String currency) {}
