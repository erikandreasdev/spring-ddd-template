# Documentation references

An example `orders` context. Copy the shape (names, visibility, validation, `final`, structure), not the business: the domain is fictional.
The `//` comments explain the pattern for you; they are not copied into project code, which only carries comments about the why.
The code blocks are not compiled or run. The source of truth is the rules; if anything conflicts, the rules win.

Rules: `.github/instructions/docs.instructions.md`.

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
