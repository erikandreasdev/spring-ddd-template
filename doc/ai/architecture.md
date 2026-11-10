# Architecture rules

- Domain Driven Design (tactical), with ports and adapters.
- Clean architecture: dependencies only point inwards, `infrastructure → application → domain`.
- Vertical slicing: one package per bounded context.

These rules are mandatory. If a request conflicts with any of them, say so before writing code
instead of skipping the rule.

## Base package architecture

- Base package `com.CHANGEME` is the root of all bounded contexts and slices.
- Cross-cutting code lives in `common` (framework-agnostic helpers: no Spring, no MyBatis, no
  Jakarta).
- Top-level `infrastructure` holds technical code not owned by any context: `config`,
  `rest.api` (generated), `rest.exception`, `rest.filter`.
- Each bounded context is an independent package under the base package:
  `com.CHANGEME.<context>`. A context never imports classes from another context; it references
  other aggregates by ID only.
- Each bounded context is organized in three layers: `domain`, `application` and
  `infrastructure`.

```
com.CHANGEME
├── common
│   ├── domain/exception         DomainException, NotFoundException, ConcurrentUpdateException
│   ├── domain                   shared value objects (Money)
│   └── application/dto/results  shared result records (PageResult); UseCase annotation
├── infrastructure
│   ├── config
│   └── rest/{api,exception,filter}
└── <context>                    e.g. orders
    ├── domain
    │   ├── exception  repository  services  policies  guards
    ├── application
    │   ├── usecases
    │   ├── dto/{commands,queries,results}
    │   └── mapper
    └── infrastructure
        ├── persistence/mybatis/{row,mapper,mapping}
        ├── rest/{controller,mapper,exception}
        └── <purpose>            other adapters, e.g. payment
```

## application layer

- Imports only `java.*`, `domain` and `application` classes (including `common`).
- Every use case is located under `usecases`.
- Every dto used in this layer is located under `dto` in its respective `commands`, `queries` or
  `results` package.
- Every mapper used in this layer (aggregate to result record) is located under `mapper`.
- Ports and the exceptions of their contract are located directly under `application`:
  read ports (`OrderSummaries`) and output ports (`PaymentGateway`).

## infrastructure layer

### persistence

- Every persistence adapter (repository or read-port implementation) is located under
  `persistence.mybatis`.
- Every persistence row dto (`*Row`) is located under `persistence.mybatis.row`.
- Every MyBatis persistence mapper is located under `persistence.mybatis.mapper`.
- Every persistence mapping that maps from persistence rows into domain and viceversa is
  located under `persistence.mybatis.mapping`.

### rest

- Every rest controller is located under `rest.controller`.
- Every rest mapper (`*ApiConversion`) is located under `rest.mapper`.
- Every rest exception handler is located under `rest.exception`.

### other adapters

- Every adapter to an external service is located under `infrastructure.<purpose>` (for example
  `payment`), together with its properties, configuration, request bodies and technical
  exceptions.

## domain layer

- Imports only `java.*` and other `domain` classes.
- Every domain entity, value object and aggregate (and its typed IDs) is located under `domain`.
- Every domain exception is located under `domain.exception`.
- Every domain service is located under `domain.services`: stateless logic that involves more
  than one aggregate and belongs to none.
- Every domain repository (interface) is located under `domain.repository`.
- Every domain policy is located under `domain.policies`: a business decision that changes
  independently of the aggregate.
- Every domain guard is located under `domain.guards`: a precondition check that throws a domain
  exception.

## Keeping the structure

- Before creating a class, find its kind in this file and put it in the package it names. Tests
  go in the same package as the class under test.
- If no package fits, stop and ask. Changing the structure means updating, in the same task, this
  file, `project-map.md`, the references and `ArchitectureTest`.
- `ArchitectureTest` enforces the dependency rule, the names and these packages. It is not
  modified or disabled to make code pass: the code is fixed.
- A new context copies the layout above; nothing outside `domain`, `application` and
  `infrastructure` goes inside a context.

## References

- Code style per layer: `doc/ai/code-conventions.md`
- Enforcement: `.claude/rules/{domain,application,infrastructure}.md` (Claude Code),
  `.github/instructions/{domain,application,infrastructure}.instructions.md` (Copilot) and
  `ArchitectureTest`.
