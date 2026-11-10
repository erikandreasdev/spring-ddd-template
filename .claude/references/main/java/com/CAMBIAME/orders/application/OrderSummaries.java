package com.CAMBIAME.orders.application;

import com.CAMBIAME.orders.domain.CustomerId;
import com.CAMBIAME.shared.application.PageResult;

// Puerto de lectura para listados: devuelve records de resultado directamente desde SQL, sin
// cargar agregados. Vive en application porque no es un concepto del dominio.
public interface OrderSummaries {

  PageResult<OrderSummary> findByCustomer(CustomerId customerId, int page, int size);
}
