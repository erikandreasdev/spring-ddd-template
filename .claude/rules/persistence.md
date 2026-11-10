---
paths:
  - "src/main/java/**/infrastructure/persistence/**/*.java"
  - "src/main/resources/db/**"
---

# Persistencia

## Mappers y repositorios
- Mappers con SQL en anotaciones y text blocks. Parámetros siempre con `#{}`, nunca `${}`.
- La conversión entre `*Row` y dominio se hace en la implementación del repositorio, sin
  `TypeHandler` para los IDs tipados.
- `nextId()` lee la secuencia de la tabla. En PostgreSQL: `SELECT nextval('orders_seq')`.
  <!-- CAMBIAME: base de datos. Sintaxis de las secuencias en tu motor -->
- `save(...)` decide por la versión: si es 0, `INSERT` con versión 1; si no,
  `UPDATE ... SET ..., version = version + 1 WHERE id = #{id} AND version = #{version}`.
  Si el `UPDATE` afecta a 0 filas, lanza `ConcurrentUpdateException`.
- Las colecciones del agregado se guardan borrando y reinsertando sus filas en el mismo
  `save(...)`.

## Esquema
- Cambios solo con Flyway: `src/main/resources/db/migration/V<n>__<descripcion>.sql`. Una
  migración ya aplicada no se modifica; se añade otra.
- Tablas en plural y `snake_case`: `orders`, `order_lines`.
- Cada tabla de agregado tiene su secuencia (`orders_seq`), clave primaria `id BIGINT` y
  columna `version BIGINT NOT NULL`.
- Las tablas de colecciones usan clave compuesta: ID del agregado más `position`.
- Restricciones con nombre explícito: `pk_orders`, `fk_order_lines_orders`,
  `uk_customers_email`. Índice en toda clave ajena.
- `NOT NULL` por defecto.
- Dinero: `DECIMAL(19, 4)` más la divisa en `CHAR(3)`.
- Enums guardados por nombre, nunca por posición.
- Fechas en columna con zona horaria, en UTC.
- Borrados físicos. Sin columnas `deleted` salvo que el negocio necesite historial.
