---
applyTo: "src/main/resources/openapi/**,src/main/java/**/infrastructure/web/**/*.java"
---

# API (contract-first)

## The specification rules
- Every API change starts in `src/main/resources/openapi/api.yaml`. Then regenerate
  (`mvn generate-sources`) and implement.
- Generated code (`shared.infrastructure.web.api`, in `target/generated-sources`) is never
  edited by hand.
- OpenAPI 3.0. One tag (`tags`) per aggregate, which generates the `<Tag>Api` interface
  (`orders` → `OrdersApi`).
- Schemas are named without a suffix (`PlaceOrder`, `Order`); the generator adds `Body`
  (`PlaceOrderBody`) so they never clash with domain classes.
- `operationId` in `camelCase` with verb and noun: `placeOrder`, `confirmOrder`.
- Format validation goes in the specification (`required`, `minimum`, `maxLength`,
  `pattern`...) and becomes Bean Validation in the generated code.

## Design
- Version in the URL: `servers: [{url: /v1}]`. Only create `/v2` for an incompatible change.
- Plural resources and actions as a noun sub-resource:
  `POST /orders/{id}/confirmation`.
- Status codes: 201 with a `Location` header on creation, 204 for commands without a body,
  200 for queries; 400 validation, 404 not found, 409 business rule or version conflict.
  Errors reuse `components/responses` (`BadRequest`, `NotFound`, `Conflict`).
- Identifiers as `integer` `int64` with `minimum: 1`.
- Money with the `Money` schema (amount as a decimal string and ISO 4217 currency).
- Dates as `string` `date-time` in UTC.
- Enums by name in upper case.
- Lists paginated by page with the `Page` and `Size` parameters (20 by default, 100 at most)
  and a `{ items: [...], page: PageInfo }` response. Ordering is always deterministic, ending
  with the ID.
- Never an array at the root of a response; always inside an object.
- Properties without a value are omitted (`null` is not sent).

## Controllers
- One controller per aggregate (`OrderController`) that implements the generated interface
  (`OrdersApi`). It only calls the use case and builds the response.
- Conversion between generated models and commands, queries or results lives in a final
  package-private class per aggregate, next to the controller: `OrderApiConversion`, with
  static methods `toCommand(...)`, `toQuery(...)` and `toBody(...)`.
- The `Money` amount is converted with `new BigDecimal(...)`; an invalid format throws
  `NumberFormatException`, which already becomes a 400.
- Controllers do not catch exceptions.
