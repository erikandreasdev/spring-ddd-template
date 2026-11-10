---
paths:
  - "src/main/resources/openapi/**"
  - "src/main/java/**/infrastructure/web/**/*.java"
---

# API (contract-first)

## La especificación manda
- Todo cambio de la API empieza en `src/main/resources/openapi/api.yaml`. Después se regenera
  (`mvn generate-sources`) y se implementa.
- El código generado (`shared.infrastructure.web.api`, en `target/generated-sources`) nunca se
  edita a mano.
- OpenAPI 3.0. Una etiqueta (`tags`) por agregado, que genera la interfaz `<Etiqueta>Api`
  (`orders` → `OrdersApi`).
- Los esquemas se nombran sin sufijo (`PlaceOrder`, `Order`); el generador añade `Body`
  (`PlaceOrderBody`) para que nunca choquen con clases del dominio.
- `operationId` en `camelCase` con verbo y sustantivo: `placeOrder`, `confirmOrder`.
- La validación de formato va en la especificación (`required`, `minimum`, `maxLength`,
  `pattern`...) y se convierte en Bean Validation en el código generado.

## Diseño
- Versión en la URL: `servers: [{url: /v1}]`. Solo se crea `/v2` ante un cambio incompatible.
- Recursos en plural y acciones como subrecurso con sustantivo:
  `POST /orders/{id}/confirmation`.
- Códigos: 201 con cabecera `Location` al crear, 204 en comandos sin cuerpo, 200 en
  consultas; 400 validación, 404 no existe, 409 regla de negocio o conflicto de versión.
  Los errores reutilizan `components/responses` (`BadRequest`, `NotFound`, `Conflict`).
- Identificadores como `integer` `int64` con `minimum: 1`.
- Dinero con el esquema `Money` (importe como cadena decimal y divisa ISO 4217).
- Fechas como `string` `date-time` en UTC.
- Enums por nombre en mayúsculas.
- Listados paginados por página con los parámetros `Page` y `Size` (20 por defecto, 100 como
  máximo) y una respuesta `{ items: [...], page: PageInfo }`. Orden siempre determinista,
  terminando en el ID.
- Nunca un array en la raíz de una respuesta; siempre dentro de un objeto.
- Las propiedades sin valor se omiten (no se envía `null`).

## Controladores
- Un controlador por agregado (`OrderController`) que implementa la interfaz generada
  (`OrdersApi`). Solo llama al caso de uso y construye la respuesta.
- La conversión entre modelos generados y comandos, consultas o resultados vive en una clase
  final package-private por agregado, junto al controlador: `OrderApiConversion`, con métodos
  estáticos `toCommand(...)`, `toQuery(...)` y `toBody(...)`.
- El importe de `Money` se convierte con `new BigDecimal(...)`; un formato inválido lanza
  `NumberFormatException`, que ya sale como 400.
- Los controladores no capturan excepciones.
