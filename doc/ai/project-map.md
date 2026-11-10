# Project map

Where things are. Update this file in the same task that adds, moves or removes a package,
context or module.

## Repository

| Path | What it holds |
|---|---|
| `pom.xml` | Build, dependencies, OpenAPI generator, JaCoCo (80% branch coverage) |
| `src/main/java/com/CHANGEME/` | Production code (see below) |
| `src/main/resources/openapi/api.yaml` | API contract; generates interfaces and models into `infrastructure.rest.api` |
| `src/main/resources/db/migration/` | Flyway migrations (`V<n>__<description>.sql`) |
| `src/main/resources/application.yml` | Configuration; environment-specific values come from environment variables |
| `src/test/java/com/CHANGEME/` | Tests; they mirror the package of the class under test |
| `docs/diagrams/` | PlantUML class diagram per context and the context map |
| `doc/ai/` | Shared instructions for AI agents (this folder) |
| `.claude/`, `CLAUDE.md` | Claude Code configuration (index, rules, skill, references) |
| `.github/` | GitHub Copilot configuration (index, instructions, skill, references, setup workflow) |

## Production code (`com.CHANGEME`)

```
Application.java                 Spring Boot entry point
common
├── domain
│   └── exception                DomainException, NotFoundException, ConcurrentUpdateException
└── application                  UseCase (own annotation, no Spring)
infrastructure                   technical code not owned by any context
├── config                       UseCaseConfig (registers @UseCase beans, adds transactions), ClockConfig
└── rest
    ├── api                      generated from api.yaml (never edited)
    ├── exception                ApiExceptionHandler (ProblemDetail with traceId)
    └── filter                   RequestLoggingFilter (one log line per request)
```

There are no bounded contexts yet. The first one is added as `com.CHANGEME.<context>` with the
layout described in `architecture.md`. A complete fictional example (`orders`) is in the
references folder (see `references/INDEX.md` in `.claude/` or `.github/`).

## Tests (`src/test/java/com/CHANGEME`)

| Class | Purpose |
|---|---|
| `ArchitectureTest` | ArchUnit rules: dependencies, names and packages (`architecture.md`) |
| `IntegrationTest`, `TestcontainersConfig` | Spring + real database for integration tests |
| `TestApplication` | Local run with Testcontainers: `mvn spring-boot:test-run` |
| `ApplicationTest` | The context starts and use case transactions are configured |
| `infrastructure/rest/exception/ApiExceptionHandlerTest` | Error translation to `ProblemDetail` |
