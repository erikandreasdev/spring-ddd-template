---
applyTo: "docs/**"
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
