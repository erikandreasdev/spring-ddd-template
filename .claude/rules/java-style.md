---
paths:
  - "**/*.java"
---

# Estilo Java

## Tipos y datos
- Nunca se pasa ni se devuelve `null`. `Optional` solo como tipo de retorno; nunca en campos,
  parámetros ni colecciones. Una colección sin elementos se devuelve vacía.
- Variantes de un concepto: `sealed interface` con records. El comportamiento intrínseco de la
  variante va como método en la interfaz; la lógica externa usa un `switch` de patrones
  exhaustivo, siempre sin `default`.
- `var` solo cuando el tipo se ve en la misma línea (`new`, o una factoría del propio tipo
  como `OrderId.newId()`). En el resto, tipo explícito.
- `final` en parámetros de métodos y constructores, variables locales y variables de
  `for-each` y `catch`. No en parámetros de lambdas.
- Campos `private final` siempre que sea posible.
- Las colecciones se copian al recibirlas (`List.copyOf`) y nunca se expone una mutable.

## Errores
- Los fallos de negocio se expresan con excepciones, no con tipos de resultado.
- Cada regla de negocio incumplida tiene su excepción, que extiende `DomainException`
  (`OrderNotEditableException`). Las de "no existe" extienden `NotFoundException`.
- Las precondiciones usan las estándar: `Objects.requireNonNull` e `IllegalArgumentException`.
- Los mensajes incluyen los identificadores de los datos que fallaron.
- Las excepciones comprobadas de librerías se traducen en el adaptador a una excepción propia
  no comprobada que conserva la causa.
- Nunca se captura `Exception` o `Throwable` de forma genérica ni se deja un `catch` vacío.
  La única excepción es `ApiExceptionHandler`, que traduce lo inesperado a un 500.
- Recursos siempre con `try-with-resources`.

## Flujo
- Streams para transformar, filtrar y agregar sin efectos secundarios. Bucle `for` cuando hay
  efectos, salida anticipada o lanzamiento de excepciones.
- Cláusulas de guarda con retorno anticipado. Sin `else` después de un `return` o `throw`.

## Métodos y clases
- Un método se lee de arriba abajo. Se extrae un método privado solo cuando el fragmento tiene
  nombre en el lenguaje de negocio o se repite. Ningún método supera unas 25 líneas.
- Como mucho cuatro parámetros; si hacen falta más, se agrupan en un value object. Sin
  builders en código de producción. Excepción: los constructores de inyección de clases de
  Spring (un controlador recibe todos los casos de uso de su agregado). Si pasan de unos seis,
  es señal de partir la clase.
- Sin comentarios que repitan lo que dice el código y sin Javadoc por defecto. Solo se comenta
  el porqué que el código no puede expresar.
- Accesores con estilo record en las clases de dominio: `order.id()`, `order.status()`. Nunca
  `getX()`. Los booleanos sí usan `isX()`.
- Visibilidad mínima: package-private por defecto; `public` solo cuando otro paquete lo
  necesita.
- Las clases de dominio son `final`. Las clases gestionadas por Spring (casos de uso,
  controladores, adaptadores) no lo son, porque Spring puede necesitar extenderlas.
- Nombres en inglés, con el lenguaje de negocio, sin abreviaturas y sin sufijos `Impl`,
  `Helper`, `Utils` ni `DTO`.
- Fechas y horas como `Instant` (UTC). La hora actual sale de un `Clock` inyectado.
- Logging solo en infraestructura (reglas en `spring-style.md`).
- El código generado desde `api.yaml` no sigue estas reglas y nunca se edita a mano.
