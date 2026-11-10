# Referencias de código

Un contexto de ejemplo completo (`orders`) con un fichero por cada tipo de clase del proyecto.
Antes de crear una clase, abre la referencia de su tipo y copia su forma: nombres, visibilidad,
validación, `final`, estructura y comentarios de test.

- Es un ejemplo: el dominio de pedidos es ficticio. Copia la forma, no el negocio.
- Los comentarios `//` explican el patrón para ti; no se copian al código del proyecto, que solo
  lleva comentarios del porqué.
- Estos ficheros no se compilan ni se ejecutan. La fuente de verdad siguen siendo las reglas de
  `CLAUDE.local.md` y `.claude/rules/`; si algo choca, mandan las reglas.
- El SQL está en PostgreSQL. <!-- CAMBIAME: base de datos. Adapta secuencias, tipos y LIMIT/OFFSET a tu motor -->
- Algunas piezas necesitan una dependencia que la plantilla no trae: `spring-boot-starter-restclient`
  para `HttpPaymentGateway` y `org.wiremock:wiremock-standalone` (test) para `WireMockConfig`.
  Inclúyelas en el plan cuando aparezca el primer servicio externo.

Rutas relativas a `.claude/references/`.

## Dominio

| Tipo | Referencia |
|---|---|
| Raíz de agregado | `main/java/com/CAMBIAME/orders/domain/Order.java` |
| Estado del agregado para persistencia (más de 4 datos) | `main/java/com/CAMBIAME/orders/domain/OrderSnapshot.java` |
| Identificador tipado | `main/java/com/CAMBIAME/orders/domain/OrderId.java` |
| ID de un agregado de otro contexto | `main/java/com/CAMBIAME/orders/domain/CustomerId.java` |
| Elemento de colección (value object sin ID) | `main/java/com/CAMBIAME/orders/domain/OrderLine.java` |
| Value object compartido | `main/java/com/CAMBIAME/shared/domain/Money.java` |
| Variantes con interfaz sellada | `main/java/com/CAMBIAME/orders/domain/Discount.java` |
| Enum persistido | `main/java/com/CAMBIAME/orders/domain/OrderStatus.java` |
| Excepción de regla de negocio (409) | `main/java/com/CAMBIAME/orders/domain/OrderNotEditableException.java` |
| Excepción "no existe" (404) | `main/java/com/CAMBIAME/orders/domain/OrderNotFoundException.java` |
| Interfaz de repositorio | `main/java/com/CAMBIAME/orders/domain/OrderRepository.java` |

## Aplicación

| Tipo | Referencia |
|---|---|
| Comando | `main/java/com/CAMBIAME/orders/application/PlaceOrderCommand.java` |
| Caso de uso de creación (ID de secuencia, `Clock`) | `main/java/com/CAMBIAME/orders/application/PlaceOrderService.java` |
| Caso de uso de modificación | `main/java/com/CAMBIAME/orders/application/AddOrderLineService.java` |
| Caso de uso con servicio externo (orden y rollback) | `main/java/com/CAMBIAME/orders/application/PayOrderService.java` |
| Consulta | `main/java/com/CAMBIAME/orders/application/GetOrderQuery.java` |
| Caso de uso de lectura por ID | `main/java/com/CAMBIAME/orders/application/GetOrderService.java` |
| Record de resultado | `main/java/com/CAMBIAME/orders/application/OrderSummary.java` |
| Consulta paginada | `main/java/com/CAMBIAME/orders/application/ListCustomerOrdersQuery.java` |
| Caso de uso de listado | `main/java/com/CAMBIAME/orders/application/ListCustomerOrdersService.java` |
| Puerto de lectura | `main/java/com/CAMBIAME/orders/application/OrderSummaries.java` |
| Resultado paginado | `main/java/com/CAMBIAME/shared/application/PageResult.java` |
| Puerto de salida a un servicio externo | `main/java/com/CAMBIAME/orders/application/PaymentGateway.java` |
| Excepción del contrato de un puerto | `main/java/com/CAMBIAME/orders/application/PaymentDeclinedException.java` |

## Infraestructura

| Tipo | Referencia |
|---|---|
| Especificación de la API | `main/resources/openapi/api.yaml` |
| Controlador | `main/java/com/CAMBIAME/orders/infrastructure/web/OrderController.java` |
| Conversión web | `main/java/com/CAMBIAME/orders/infrastructure/web/OrderApiConversion.java` |
| Migración de Flyway | `main/resources/db/migration/V1__create_orders.sql` |
| Mapper de agregado | `main/java/com/CAMBIAME/orders/infrastructure/persistence/OrderMapper.java` |
| Filas (`*Row`) | `main/java/com/CAMBIAME/orders/infrastructure/persistence/OrderRow.java`, `OrderLineRow.java` |
| Repositorio (versión, colecciones, sellados) | `main/java/com/CAMBIAME/orders/infrastructure/persistence/MyBatisOrderRepository.java` |
| Mapper de lectura | `main/java/com/CAMBIAME/orders/infrastructure/persistence/OrderSummaryMapper.java` |
| Implementación de puerto de lectura | `main/java/com/CAMBIAME/orders/infrastructure/persistence/MyBatisOrderSummaries.java` |
| Propiedades de un adaptador | `main/java/com/CAMBIAME/orders/infrastructure/payment/PaymentProperties.java`, `PaymentConfig.java` |
| Adaptador HTTP con `RestClient` | `main/java/com/CAMBIAME/orders/infrastructure/payment/HttpPaymentGateway.java` |
| Cuerpo de petición externa | `main/java/com/CAMBIAME/orders/infrastructure/payment/ChargeRequest.java` |
| Excepción técnica de integración | `main/java/com/CAMBIAME/orders/infrastructure/payment/PaymentUnavailableException.java` |

## Tests

| Tipo | Referencia |
|---|---|
| Object Mother | `test/java/com/CAMBIAME/orders/domain/OrderMother.java` |
| Test de agregado (incluye parametrizado) | `test/java/com/CAMBIAME/orders/domain/OrderTest.java` |
| Test de value object | `test/java/com/CAMBIAME/shared/domain/MoneyTest.java` |
| Test de caso de uso | `test/java/com/CAMBIAME/orders/application/ConfirmOrderServiceTest.java` |
| Test de caso de uso con servicio externo (WireMock) | `test/java/com/CAMBIAME/orders/application/PayOrderServiceTest.java` |
| Test de puerto de lectura | `test/java/com/CAMBIAME/orders/application/ListCustomerOrdersServiceTest.java` |
| Test de concurrencia del repositorio | `test/java/com/CAMBIAME/orders/infrastructure/persistence/MyBatisOrderRepositoryTest.java` |
| Test de controlador | `test/java/com/CAMBIAME/orders/infrastructure/web/OrderControllerTest.java` |
| Test de flujo completo | `test/java/com/CAMBIAME/orders/OrderFlowTest.java` |
| Servidor WireMock compartido | `test/java/com/CAMBIAME/WireMockConfig.java` |

## Documentación

| Tipo | Referencia |
|---|---|
| Diagrama de un contexto | `docs/diagrams/orders.puml` |
| Mapa de contextos | `docs/diagrams/context-map.puml` |
| ADR | `docs/adr/0002-guardar-el-total-calculado-en-orders.md` |
