# Spring Boot + DDD template for Claude Code

Minimal Spring Boot 4 project (Java 21, Maven, MyBatis, Flyway, contract-first API) with the
Claude Code configuration already included. It has no business code: only the bootstrap, the
shared architecture pieces and the tests that verify them.

## Before the first run

Everything you need to change is marked with the word **CHANGEME**. There are two kinds of
marker:

- `CHANGEME:` (with a colon) flags a decision you must review by hand.
- Plain `CHANGEME` is the project name and is replaced in one go.

### 1. Review the decisions

```
grep -rn "CHANGEME:" --exclude=README.md .
```

There are ten spots, all but one about the database (the format command). The template works with
PostgreSQL without changes; if that is your database, just delete the comments.

| File | What to decide |
|---|---|
| `pom.xml` (two comments) | JDBC driver, Flyway module and Testcontainers module for your database |
| `src/main/resources/application.yml` | JDBC URL prefix |
| `src/test/java/com/CHANGEME/TestcontainersConfig.java` | Container class and image |
| `doc/ai/tech-stack.md` (two comments) | Database name and format command (shared by Claude Code and Copilot) |
| `.claude/rules/persistence.md` | Sequence syntax |
| `.claude/references/infrastructure.md` | SQL syntax of the references |
| `.github/instructions/persistence.instructions.md` | Sequence syntax |
| `.github/references/infrastructure.md` | SQL syntax of the references |

Delete each `CHANGEME:` comment once resolved, so the next step does not alter it.

### 2. Set the project name

Pick a lowercase name without spaces or hyphens (for example `shop`). It is used as the
package (`com.shop`), `artifactId`, application name, API title and database name.

```
NAME=shop
grep -rl "CHANGEME" --exclude=README.md . | xargs sed -i "s/CHANGEME/$NAME/g"
find . -depth -type d -name CHANGEME -execdir mv CHANGEME "$NAME" \;
```

The `find` renames the two package folders: `src/main/java/com/` and `src/test/java/com/`.

On macOS, `sed -i` needs an empty argument: use `sed -i ''` instead of `sed -i`.

If your package does not start with `com`, also change that prefix in the `groupId` and in the
generator packages in `pom.xml`, in the `package` and `import` declarations and in the folders.

Check that no marker is left:

```
grep -rn "CHANGEME" --exclude=README.md .
```

### 3. Keep the Claude Code configuration out of the repository

It is personal configuration. After `git init`, add to `.git/info/exclude`:

```
CLAUDE.md
.claude/
```

`doc/ai/` and `docs/` are part of the project: both tools read the shared instructions in `doc/ai/`.

The GitHub Copilot configuration (`.github/`) is the opposite: the cloud agent reads it from the
repository, so commit it. If you only use Claude Code, delete `.github/copilot-instructions.md`,
`.github/instructions/`, `.github/skills/`, `.github/references/` and
`.github/workflows/copilot-setup-steps.yml`.

### 4. First run

You need Java 21, Maven and Docker running.

```
mvn -q test -Dtest=ArchitectureTest   # architecture rules, no Docker
mvn verify                            # all tests and coverage
mvn spring-boot:test-run              # starts the application with the database in Docker
```

With the application running, `http://localhost:8080/actuator/health` must answer `UP`.

Then open Claude Code (a version that supports `defaultMode` and rules with `paths`) in the
project folder. It starts in plan mode: it edits nothing until you approve the plan.

## What is included

### Shared AI documentation (`doc/ai/`)

Single source for both tools; `CLAUDE.md` and `.github/copilot-instructions.md` only index it.

| File | Purpose |
|---|---|
| `doc/ai/tech-stack.md` | Stack, database, formatting, base package |
| `doc/ai/project-map.md` | Where things are; updated when a package, context or module changes |
| `doc/ai/architecture.md` | Package layout per layer, dependency rule and how the structure is kept |
| `doc/ai/code-conventions.md` | Coding rules per layer (domain, application, infrastructure) |

### Claude Code

| File | Purpose |
|---|---|
| `CLAUDE.md` | Index of the files above and of the rules below; loaded in every session |
| `.claude/rules/way-of-working.md` | Plan first, no commits, verification; loaded in every session |
| `.claude/rules/domain.md`, `application.md`, `infrastructure.md` | Short layer checklists; when reading files of that layer |
| `.claude/rules/java-coding.md` | Java style; loaded when reading `.java` files |
| `.claude/rules/spring.md` | Configuration, logging and observability; when reading infrastructure or resources |
| `.claude/rules/api.md` | API design and controllers; when reading `api.yaml` or the rest layer |
| `.claude/rules/persistence.md` | Repositories, SQL and migrations; when reading persistence or `db/` |
| `.claude/rules/testing.md` | Test strategy and style; when reading `src/test` |
| `.claude/rules/diagram.md` | Diagrams; when reading `docs/` |
| `.claude/skills/add-feature/` | Procedure for adding a feature |
| `.claude/references/` | One example of each kind of class (`orders` context) as a Markdown file per layer, indexed in `INDEX.md`. Not compiled |
| `.claude/settings.local.json` | Plan mode by default and permissions |

Configured permissions: `mvn` and read-only `git` commands without asking; asks before editing
`CLAUDE.md`, `.claude/`, `doc/ai/`, `ArchitectureTest` and `pom.xml`.

### GitHub Copilot

The same configuration for Copilot (cloud agent, agent mode and code review). The instructions,
skill and references are a copy of the Claude Code ones with the paths changed; keep both in sync
when you edit either (the shared `doc/ai/` is not duplicated).

| File | Purpose |
|---|---|
| `.github/copilot-instructions.md` | Index (counterpart of `CLAUDE.md`); applies to every request |
| `.github/instructions/*.instructions.md` | One per rule in `.claude/rules/`; the `applyTo` frontmatter selects the files it applies to (`way-of-working` applies to all) |
| `.github/skills/add-feature/` | Procedure for adding a feature |
| `.github/references/` | Same Markdown references as `.claude/references/` |
| `.github/workflows/copilot-setup-steps.yml` | Java 21, Maven and dependencies for the cloud agent |

Adapted rules: the cloud agent posts its plan in the pull request and commits only to its own
branch (never to `main`, never merges). Copilot also reads `CLAUDE.md` and `AGENTS.md` if present,
so it may load overlapping instructions; delete the ones you do not use.

### Code

| File | Purpose |
|---|---|
| `src/main/resources/openapi/api.yaml` | API specification; generates interfaces and models at build time |
| `common/application/UseCase.java` | Own annotation, without Spring, to mark use cases |
| `infrastructure/config/UseCaseConfig.java` | Registers `@UseCase` classes as transactional beans |
| `infrastructure/config/ClockConfig.java` | UTC `Clock` bean |
| `common/domain/exception/*Exception.java` | Base domain exceptions and version conflict |
| `infrastructure/rest/exception/ApiExceptionHandler.java` | Translates exceptions to `ProblemDetail` with `traceId` |
| `infrastructure/rest/filter/RequestLoggingFilter.java` | One log line per request |
| `ArchitectureTest.java` | ArchUnit rules: dependencies, names and packages (`doc/ai/architecture.md`) |
| `ApiExceptionHandlerTest.java` | Error handling tests |
| `IntegrationTest.java`, `TestcontainersConfig.java` | Integration tests against a real database |
| `TestApplication.java` | Local run with Testcontainers |
| `ApplicationTest.java` | Checks that the context starts |
| `docs/diagrams/` | PlantUML diagrams per context (empty at first) |

## Per-environment configuration

Everything through environment variables, no profiles:

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Real database |
| `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` | JSON logs |
| `MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT` | Sending traces to an OTLP collector |

## Versions

Spring Boot 4.1.2, MyBatis Spring Boot Starter 4.1.0, OpenAPI Generator 7.27.0, ArchUnit
1.5.1, Instancio 6.2.1 and JaCoCo 0.8.16. They are in the `pom.xml` properties.
