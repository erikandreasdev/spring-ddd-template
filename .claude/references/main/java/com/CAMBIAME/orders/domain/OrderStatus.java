package com.CAMBIAME.orders.domain;

// Se guarda por nombre en la base de datos, así que no se renombran valores sin migración.
public enum OrderStatus {
  DRAFT,
  CONFIRMED,
  PAID
}
