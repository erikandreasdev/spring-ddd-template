package com.CHANGEME.orders.infrastructure.persistence;

import java.math.BigDecimal;

record OrderLineRow(
    long orderId, int position, long productId, BigDecimal unitPrice, int quantity) {}
