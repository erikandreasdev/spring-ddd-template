package com.CAMBIAME.orders.domain;

import java.util.Optional;

// Un repositorio por agregado, como interfaz en el dominio. Solo opera con el agregado completo;
// los listados van por un puerto de lectura en application (OrderSummaries).
public interface OrderRepository {

  OrderId nextId();

  Optional<Order> findById(OrderId id);

  // Inserta si version() == 0; si no, actualiza comprobando la versión y lanza
  // ConcurrentUpdateException si otra petición lo modificó antes. El objeto en memoria conserva
  // la versión anterior: un caso de uso guarda una sola vez y, si hay que seguir trabajando con
  // el agregado, se vuelve a cargar.
  void save(Order order);
}
