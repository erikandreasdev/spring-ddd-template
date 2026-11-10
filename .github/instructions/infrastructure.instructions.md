---
applyTo: "src/main/java/**/infrastructure/**/*.java,src/test/java/**/infrastructure/**/*.java"
---

# Infrastructure layer

Packages: "infrastructure layer" in `doc/ai/architecture.md`. Rules: "infrastructure" in
`doc/ai/code-conventions.md`. Examples: `.github/references/infrastructure.md`.

- The only place where Spring, MyBatis, Jakarta and logging are used.
- Persistence: adapters in `persistence.mybatis`, `*Row` in `.row`, MyBatis mappers in `.mapper`,
  row/domain conversion in `.mapping`. The aggregate is never a MyBatis type.
- Rest: controllers in `rest.controller`, `*ApiConversion` in `rest.mapper`, exception handlers in
  `rest.exception`. Controllers only translate between HTTP and use cases.
- Other adapters in `infrastructure.<purpose>` and implement an output port from `application`.
- Public only what another package needs; everything else stays package-private.
- `ArchitectureTest` enforces these packages and that rest never touches repositories.
