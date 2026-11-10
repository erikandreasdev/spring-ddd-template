---
paths:
  - "docs/**"
---

# Documentation

Written in English. Class, table and endpoint names are kept as they are.

## Diagrams (PlantUML)
- One class diagram per context: `docs/diagrams/<context>.puml`.
- It contains aggregates (`<<Aggregate Root>>`), entities (`<<Entity>>`) and value objects
  (`<<Value Object>>`), with their business attributes and public business methods. No
  repositories, use cases or technical details.
- References by ID between aggregates are drawn as a dashed dependency labelled with the ID
  name (`Order ..> Customer : customerId`). Composition, with a filled diamond.
- With two or more contexts, also `docs/diagrams/context-map.puml`: one component per context
  and one arrow per relationship, showing the direction of the dependency and what is
  exchanged.
- They are updated in the same task that changes the model.

## ADRs (Michael Nygard format)
- One per decision: `docs/adr/NNNN-lowercase-title-with-hyphens.md`, numbered in order.
- Created when an exception to the project rules is accepted, when choosing between real
  alternatives (a library, a way to integrate a service) or when a rule changes. Not for every
  task.
- Fixed structure: title, status, context, decision and consequences. Template in
  `docs/adr/template.md`.
- An accepted ADR is never rewritten: if the decision changes, create a new one and mark the
  old one as `Superseded by NNNN`.
