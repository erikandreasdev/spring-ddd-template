---
paths:
  - "src/main/java/**/application/**/*.java"
  - "src/test/java/**/application/**/*.java"
---

# Application layer

Packages: "application layer" in `doc/ai/architecture.md`. Rules: "application" in
`doc/ai/code-conventions.md`. Examples: `.claude/references/application.md`.

- Imports only `java.*`, `domain` and `application`. No Spring, Jakarta or logging.
- Use cases in `usecases` (public, not final, `@UseCase`, `<Verb><Noun>Service`, a single public
  method); commands, queries and results in `dto.commands`, `dto.queries` and `dto.results`;
  mappers in `mapper`; ports and the exceptions of their contract directly in `application`.
- Commands and queries are records validated in their compact constructor.
- Use cases orchestrate (load, call the aggregate, save) and return an ID, `void` or a result
  record, never the aggregate.
- No `@Service` or `@Transactional`: `UseCaseConfig` provides both.
- `ArchitectureTest` enforces the dependency rule, the names and these packages.
