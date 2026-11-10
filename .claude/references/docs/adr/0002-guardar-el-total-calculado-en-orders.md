# 0002. Guardar el total calculado del pedido en la tabla orders

## Estado

Aceptado

## Contexto

El listado de pedidos de un cliente muestra el total de cada pedido. El total depende de las
líneas y del descuento aplicado, y esa regla vive en el dominio (`Order.total()`). El puerto de
lectura `OrderSummaries` va directo a SQL, sin cargar agregados, así que tendría que repetir el
cálculo del descuento en SQL.

Alternativas valoradas:

- Calcular el total en SQL: duplica una regla de negocio y se desincroniza al cambiarla.
- Cargar los agregados en el listado: respeta la regla, pero carga pedidos completos con sus
  líneas solo para mostrar cuatro campos.
- Guardar el total calculado por el dominio en una columna al guardar el pedido.

## Decisión

Guardaremos el resultado de `Order.total()` en la columna `orders.total_amount` cada vez que
`MyBatisOrderRepository` guarda un pedido. Las consultas de lectura usan esa columna y nunca
calculan totales.

## Consecuencias

- La regla del descuento sigue existiendo en un único sitio: el dominio.
- Las consultas de listados son simples y rápidas.
- `total_amount` es un dato derivado: no se escribe nunca fuera de `save()`. Si cambia la regla
  de cálculo, los pedidos ya guardados conservan el total antiguo hasta que se vuelvan a
  guardar; si hace falta recalcularlos, se hará con una migración explícita.
