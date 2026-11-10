---
name: add-feature
description: Adds or changes a feature (use case, endpoint, aggregate, repository) following the project architecture. Always use it when business code has to be created or changed.
---

# Add or change a feature

## 1. Plan (always, and wait for approval)

Before writing anything, present a plan with:

- Which files are created or changed, per layer.
- Changes to `api.yaml` (endpoints, schemas, response codes).
- Changes to the domain model and the database schema. Destructive migrations, flagged
  separately.
- New dependencies, with the reason.
- If any part departs from the rules or chooses between alternatives: propose the ADR.

Review the ADRs in `docs/adr/` first so you do not contradict decisions already made.

## 2. Implementation

In each step, write the test first, check that it fails and then implement. Before creating
each class, open the reference file for its layer (listed in `.claude/references/INDEX.md`)
and follow the shape of its kind.

1. API: change `api.yaml` and run `mvn generate-sources`.
2. Domain: unit test for the rule; then the aggregate, value object or business method.
3. Persistence: Flyway migration (table, sequence, constraints); repository interface in
   `domain`; implementation and mapper in `infrastructure.persistence`.
4. Use case: integration test; then the `*Command` or `*Query` and the `@UseCase` class.
5. Web: controller test; then the `*ApiConversion` class and the controller that implements
   the generated interface.
6. Flow: add or extend the `<Aggregate>FlowTest` if the main flow changes.
7. Documentation: update `docs/diagrams/<context>.puml` if the model changed (and the context
   map if there are several). Write the ADR if it was approved in the plan.
8. Format the code and run `mvn verify`.

## 3. Wrap-up

- Do not commit.
- Summarise what changed, the result of `mvn verify` and any deviation from the rules.

## References

`.claude/references/` has a complete example context (`orders`) as one Markdown file per layer
(`domain.md`, `application.md`, `infrastructure.md`, `tests.md`, `docs.md`), indexed in
`INDEX.md`: aggregate, value objects, commands, queries, results, use cases, ports, controller,
web conversion, mappers, repository, migration, HTTP adapter, tests at every level, diagrams
and ADR.
