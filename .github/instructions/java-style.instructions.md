---
applyTo: "**/*.java"
---

# Java style

## Types and data
- Never pass or return `null`. `Optional` only as a return type; never in fields, parameters
  or collections. A collection without elements is returned empty.
- Variants of a concept: `sealed interface` with records. Behaviour intrinsic to the variant
  goes as a method on the interface; external logic uses an exhaustive pattern `switch`,
  always without `default`.
- `var` only when the type is visible on the same line (`new`, or a factory of the type itself
  such as `OrderId.newId()`). Otherwise, an explicit type.
- `final` on method and constructor parameters, local variables and `for-each` and `catch`
  variables. Not on lambda parameters.
- Fields `private final` whenever possible.
- Collections are copied on receipt (`List.copyOf`) and a mutable one is never exposed.

## Errors
- Business failures are expressed with exceptions, not result types.
- Every broken business rule has its own exception, extending `DomainException`
  (`OrderNotEditableException`). "Not found" ones extend `NotFoundException`.
- Preconditions use the standard ones: `Objects.requireNonNull` and `IllegalArgumentException`.
- Messages include the identifiers of the data that failed.
- Checked exceptions from libraries are translated in the adapter into an own unchecked
  exception that keeps the cause.
- Never catch `Exception` or `Throwable` generically or leave an empty `catch`. The only
  exception is `ApiExceptionHandler`, which translates the unexpected into a 500.
- Resources always with `try-with-resources`.

## Flow
- Streams to transform, filter and aggregate without side effects. A `for` loop when there are
  side effects, early exit or thrown exceptions.
- Guard clauses with early return. No `else` after a `return` or `throw`.

## Methods and classes
- A method reads top to bottom. Extract a private method only when the fragment has a name in
  the business language or is repeated. No method exceeds about 25 lines.
- At most four parameters; if more are needed, group them in a value object. No builders in
  production code. Exception: injection constructors of Spring classes (a controller receives
  all the use cases of its aggregate). Beyond about six, it is a sign to split the class.
- No comments that repeat what the code says and no Javadoc by default. Only comment the why
  that the code cannot express.
- Record-style accessors in domain classes: `order.id()`, `order.status()`. Never `getX()`.
  Booleans do use `isX()`.
- Minimal visibility: package-private by default; `public` only when another package needs it.
- Domain classes are `final`. Spring-managed classes (use cases, controllers, adapters) are
  not, because Spring may need to extend them.
- Names in English, in the business language, without abbreviations and without `Impl`,
  `Helper`, `Utils` or `DTO` suffixes.
- Dates and times as `Instant` (UTC). The current time comes from an injected `Clock`.
- Logging only in infrastructure (rules in `spring-style.instructions.md`).
- Code generated from `api.yaml` does not follow these rules and is never edited by hand.
