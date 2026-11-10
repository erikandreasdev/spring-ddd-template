package com.CAMBIAME.orders.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;

// Fila de la tabla orders, tal cual. Tipos de base de datos (long, String, BigDecimal), nunca
// tipos del dominio. total_amount es una copia del total calculado por el dominio, para que las
// consultas de lectura no repitan las reglas de descuento en SQL.
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
