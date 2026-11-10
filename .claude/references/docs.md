# Documentation references

An example `orders` context. Copy the shape (names, visibility, validation, `final`, structure), not the business: the domain is fictional.
The `//` comments explain the pattern for you; they are not copied into project code, which only carries comments about the why.
The code blocks are not compiled or run. The source of truth is the rules; if anything conflicts, the rules win.

Rules: `.claude/rules/docs.md`.

## Context diagram

`docs/diagrams/orders.puml`

```plantuml
@startuml orders
' Class diagram of a context: domain model only, no repositories or use cases.
title Context: orders

hide empty members
skinparam classAttributeIconSize 0

class Order <<Aggregate Root>> {
  id : OrderId
  customerId : CustomerId
  currency : Currency
  placedAt : Instant
  status : OrderStatus
  version : long
  --
  addLine(productId, unitPrice, quantity)
  applyDiscount(discount)
  confirm()
  markPaid()
  subtotal() : Money
  total() : Money
}

class OrderLine <<Value Object>> {
  productId : ProductId
  unitPrice : Money
  quantity : int
  --
  subtotal() : Money
}

interface Discount <<Value Object>> {
  applyTo(amount) : Money
}
class NoDiscount <<Value Object>>
class PercentageDiscount <<Value Object>> {
  percent : int
}
class FixedDiscount <<Value Object>> {
  value : Money
}

enum OrderStatus {
  DRAFT
  CONFIRMED
  PAID
}

class Money <<Value Object>> {
  amount : BigDecimal
  currency : Currency
}

class Customer <<Aggregate Root>> #EEEEEE
note right of Customer : customers context

Order *-- "0..*" OrderLine : lines
Order *-- "1" Discount : discount
Order --> OrderStatus
Discount <|.. NoDiscount
Discount <|.. PercentageDiscount
Discount <|.. FixedDiscount
OrderLine --> Money
Order ..> Customer : customerId
@enduml
```

## Context map

`docs/diagrams/context-map.puml`

```plantuml
@startuml context-map
' Context map: only when there are two or more. One component per context and one arrow per
' relationship, in the direction of the dependency, with what is exchanged.
title Context map

component "orders" as orders
component "customers" as customers
component "Payment provider\n(external)" as payments #EEEEEE

orders --> customers : references CustomerId
orders --> payments : PaymentGateway\n(charge over HTTP)
@enduml
```

## ADR

`docs/adr/0002-store-computed-total-in-orders.md`

````markdown
# 0002. Store the order's computed total in the orders table

## Status

Accepted

## Context

A customer's order list shows the total of each order. The total depends on the lines and the
applied discount, and that rule lives in the domain (`Order.total()`). The `OrderSummaries`
read port goes straight to SQL without loading aggregates, so it would have to repeat the
discount calculation in SQL.

Alternatives considered:

- Compute the total in SQL: duplicates a business rule and drifts out of sync when it changes.
- Load the aggregates in the list: respects the rule, but loads full orders with their lines
  just to show four fields.
- Store the total computed by the domain in a column when the order is saved.

## Decision

We will store the result of `Order.total()` in the `orders.total_amount` column every time
`MyBatisOrderRepository` saves an order. Read queries use that column and never compute
totals.

## Consequences

- The discount rule still exists in a single place: the domain.
- List queries are simple and fast.
- `total_amount` is derived data: it is never written outside `save()`. If the calculation
  rule changes, orders already saved keep the old total until they are saved again; if they
  need to be recalculated, it will be done with an explicit migration.
````
