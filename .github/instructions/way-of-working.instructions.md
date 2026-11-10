---
applyTo: "**"
---
# Way of working

- Always a plan before touching code. As the cloud agent, post the plan in the pull request
  description (or first comment) before the first commit; in the IDE, wait for my approval. The
  plan states which files change and whether the API, the model or the schema change.
- Commit only on the branch Copilot creates for the task; never push to `main` and never merge:
  I review and merge the pull request.
- Never, unless I ask: modify `ArchitectureTest`, `.github/copilot-instructions.md`, `.github/` or `doc/ai/`, edit
  generated code, disable or delete tests.
- If a class does not fit any package in `doc/ai/architecture.md`, ask (in the pull request) before creating a new one.
- Do not add dependencies to `pom.xml` without stating which one and why in the plan.
- Destructive migrations (dropping columns or tables, changing types): always explicit in the
  plan, even if the change looks small.
- If the domain model changes, update its diagram in the same task.
- When done: what changed, the result of `mvn verify` and any deviation from the rules.

## Verification

- Work with TDD: test first, then code (details in `.github/instructions/testing.instructions.md`).
- `ArchitectureTest` (ArchUnit) enforces dependencies, names and packages. It is not modified or
  disabled to make code pass; the code is fixed. Run it with `mvn -q test -Dtest=ArchitectureTest`
  (no Docker needed); it also runs in `mvn verify`.
- A feature is done when `mvn verify` passes (tests and 80% branch coverage). It needs Docker
  running.
