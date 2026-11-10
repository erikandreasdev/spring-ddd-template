# Code references

A complete example context (`orders`) with one file for each kind of class in the project.
Before creating a class, open the reference for its kind and copy its shape: names,
visibility, validation, `final`, structure and test comments.

- It is an example: the orders domain is fictional. Copy the shape, not the business.
- The `//` comments explain the pattern for you; they are not copied into project code, which
  only carries comments about the why.
- These files are not compiled or run. The source of truth is still the rules in `CLAUDE.md`
  and `.claude/rules/`; if anything conflicts, the rules win.
- The SQL is PostgreSQL. <!-- CHANGEME: database. Adapt sequences, types and LIMIT/OFFSET to your engine -->
- Some pieces need a dependency the template does not include: `spring-boot-starter-restclient`
  for `HttpPaymentGateway` and `org.wiremock:wiremock-standalone` (test) for `WireMockConfig`.
  Include them in the plan when the first external service appears.

Paths relative to `.claude/references/`.

## Domain

| Kind | Reference |
|---|---|
| Aggregate root | `main/java/com/CHANGEME/orders/domain/Order.java` |
| Aggregate state for persistence (more than 4 values) | `main/java/com/CHANGEME/orders/domain/OrderSnapshot.java` |
| Typed identifier | `main/java/com/CHANGEME/orders/domain/OrderId.java` |
| ID of an aggregate from another context | `main/java/com/CHANGEME/orders/domain/CustomerId.java` |
| Collection element (value object without ID) | `main/java/com/CHANGEME/orders/domain/OrderLine.java` |
| Shared value object | `main/java/com/CHANGEME/shared/domain/Money.java` |
| Variants with a sealed interface | `main/java/com/CHANGEME/orders/domain/Discount.java` |
| Persisted enum | `main/java/com/CHANGEME/orders/domain/OrderStatus.java` |
| Business rule exception (409) | `main/java/com/CHANGEME/orders/domain/OrderNotEditableException.java` |
| "Not found" exception (404) | `main/java/com/CHANGEME/orders/domain/OrderNotFoundException.java` |
| Repository interface | `main/java/com/CHANGEME/orders/domain/OrderRepository.java` |

## Application

| Kind | Reference |
|---|---|
| Command | `main/java/com/CHANGEME/orders/application/PlaceOrderCommand.java` |
| Creation use case (sequence ID, `Clock`) | `main/java/com/CHANGEME/orders/application/PlaceOrderService.java` |
| Modification use case | `main/java/com/CHANGEME/orders/application/AddOrderLineService.java` |
| Use case with an external service (ordering and rollback) | `main/java/com/CHANGEME/orders/application/PayOrderService.java` |
| Query | `main/java/com/CHANGEME/orders/application/GetOrderQuery.java` |
| Read-by-ID use case | `main/java/com/CHANGEME/orders/application/GetOrderService.java` |
| Result record | `main/java/com/CHANGEME/orders/application/OrderSummary.java` |
| Paginated query | `main/java/com/CHANGEME/orders/application/ListCustomerOrdersQuery.java` |
| Listing use case | `main/java/com/CHANGEME/orders/application/ListCustomerOrdersService.java` |
| Read port | `main/java/com/CHANGEME/orders/application/OrderSummaries.java` |
| Paginated result | `main/java/com/CHANGEME/shared/application/PageResult.java` |
| Output port to an external service | `main/java/com/CHANGEME/orders/application/PaymentGateway.java` |
| Exception in a port's contract | `main/java/com/CHANGEME/orders/application/PaymentDeclinedException.java` |

## Infrastructure

| Kind | Reference |
|---|---|
| API specification | `main/resources/openapi/api.yaml` |
| Controller | `main/java/com/CHANGEME/orders/infrastructure/web/OrderController.java` |
| Web conversion | `main/java/com/CHANGEME/orders/infrastructure/web/OrderApiConversion.java` |
| Flyway migration | `main/resources/db/migration/V1__create_orders.sql` |
| Aggregate mapper | `main/java/com/CHANGEME/orders/infrastructure/persistence/OrderMapper.java` |
| Rows (`*Row`) | `main/java/com/CHANGEME/orders/infrastructure/persistence/OrderRow.java`, `OrderLineRow.java` |
| Repository (version, collections, sealed types) | `main/java/com/CHANGEME/orders/infrastructure/persistence/MyBatisOrderRepository.java` |
| Read mapper | `main/java/com/CHANGEME/orders/infrastructure/persistence/OrderSummaryMapper.java` |
| Read port implementation | `main/java/com/CHANGEME/orders/infrastructure/persistence/MyBatisOrderSummaries.java` |
| Adapter properties | `main/java/com/CHANGEME/orders/infrastructure/payment/PaymentProperties.java`, `PaymentConfig.java` |
| HTTP adapter with `RestClient` | `main/java/com/CHANGEME/orders/infrastructure/payment/HttpPaymentGateway.java` |
| External request body | `main/java/com/CHANGEME/orders/infrastructure/payment/ChargeRequest.java` |
| Technical integration exception | `main/java/com/CHANGEME/orders/infrastructure/payment/PaymentUnavailableException.java` |

## Tests

| Kind | Reference |
|---|---|
| Object Mother | `test/java/com/CHANGEME/orders/domain/OrderMother.java` |
| Aggregate test (includes a parameterized one) | `test/java/com/CHANGEME/orders/domain/OrderTest.java` |
| Value object test | `test/java/com/CHANGEME/shared/domain/MoneyTest.java` |
| Use case test | `test/java/com/CHANGEME/orders/application/ConfirmOrderServiceTest.java` |
| Use case test with an external service (WireMock) | `test/java/com/CHANGEME/orders/application/PayOrderServiceTest.java` |
| Read port test | `test/java/com/CHANGEME/orders/application/ListCustomerOrdersServiceTest.java` |
| Repository concurrency test | `test/java/com/CHANGEME/orders/infrastructure/persistence/MyBatisOrderRepositoryTest.java` |
| Controller test | `test/java/com/CHANGEME/orders/infrastructure/web/OrderControllerTest.java` |
| Full flow test | `test/java/com/CHANGEME/orders/OrderFlowTest.java` |
| Shared WireMock server | `test/java/com/CHANGEME/WireMockConfig.java` |

## Documentation

| Kind | Reference |
|---|---|
| Context diagram | `docs/diagrams/orders.puml` |
| Context map | `docs/diagrams/context-map.puml` |
| ADR | `docs/adr/0002-store-computed-total-in-orders.md` |
