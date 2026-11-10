---
name: add-feature
description: Adds or changes a feature (use case, endpoint, aggregate, repository) following the project architecture. Always use it when business code has to be created or changed.
---

# Add or change a feature

## 1. Plan (always, and wait for approval)

Before writing anything, present a plan with:

- Which files are created or changed, per layer, each with the package that `doc/ai/architecture.md`
  assigns to its kind.
- Changes to `api.yaml` (endpoints, schemas, response codes).
- Changes to the domain model and the database schema. Destructive migrations, flagged
  separately.
- New dependencies, with the reason.

## 2. Implementation

In each step, write the test first, check that it fails and then implement. Before creating
each class, read `doc/ai/architecture.md` for its package and open the reference file for its
layer (listed in `.claude/references/INDEX.md`) to follow the shape of its kind.

1. API: change `api.yaml` and run `mvn generate-sources`.
2. Domain: unit test for the rule; then the aggregate, value object or business method (and,
   if needed, a guard, policy or domain service in their packages).
3. Persistence: Flyway migration (table, sequence, constraints); repository interface in
   `domain.repository`; adapter, `*Row`, MyBatis mapper and `*Mapping` in
   `infrastructure.persistence.mybatis` and its `row`, `mapper` and `mapping` packages.
4. Use case: integration test; then the `*Command` or `*Query` (`dto.commands`, `dto.queries`),
   the result record (`dto.results`), the application mapper (`mapper`) and the `@UseCase` class
   (`usecases`).
5. Rest: controller test; then the `*ApiConversion` class (`rest.mapper`) and the controller
   (`rest.controller`) that implements the generated interface.
6. Flow: add or extend the `<Aggregate>FlowTest` if the main flow changes.
7. Documentation: update `docs/diagrams/<context>.puml` if the model changed (and the context
   map if there are several).
8. Format the code and run `mvn verify`.

## 3. Wrap-up

- Do not commit.
- Summarise what changed, the result of `mvn verify` and any deviation from the rules.

## References

`.claude/references/` has a complete example context (`orders`) as one Markdown file per layer
(`domain.md`, `application.md`, `infrastructure.md`, `tests.md`, `diagrams.md`), indexed in
`INDEX.md`: aggregate, value objects, guards, policies, domain services, commands, queries,
results, application mappers, use cases, ports, controller, rest conversion, MyBatis mappers,
rows, persistence mapping, repository, migration, HTTP adapter, tests at every level, diagrams.
