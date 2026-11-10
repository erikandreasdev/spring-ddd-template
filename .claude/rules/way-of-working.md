# Way of working

- Always a plan before touching code, and wait for my approval. The plan states which files
  change and whether the API, the model or the schema change.
- Never commit or `push`: changes stay uncommitted so I can review them.
- Never, unless I ask: modify `ArchitectureTest`, `CLAUDE.md`, `.claude/` or `doc/ai/`, edit
  generated code, disable or delete tests.
- If a class does not fit any package in `doc/ai/architecture.md`, ask before creating a new one.
- Do not add dependencies to `pom.xml` without stating which one and why in the plan.
- Destructive migrations (dropping columns or tables, changing types): always explicit in the
  plan, even if the change looks small.
- If the domain model changes, update its diagram in the same task.
- When done: what changed, the result of `mvn verify` and any deviation from the rules.

## Verification

- Work with TDD: test first, then code (details in `.claude/rules/testing.md`).
- `ArchitectureTest` (ArchUnit) enforces dependencies, names and packages. It is not modified or
  disabled to make code pass; the code is fixed. Run it with `mvn -q test -Dtest=ArchitectureTest`
  (no Docker needed); it also runs in `mvn verify`.
- A feature is done when `mvn verify` passes (tests and 80% branch coverage). It needs Docker
  running.
