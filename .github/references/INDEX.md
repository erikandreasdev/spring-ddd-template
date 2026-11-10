# Code references

An example `orders` context. Copy the shape (names, visibility, validation, `final`, structure), not the business: the domain is fictional.
The `//` comments explain the pattern for you; they are not copied into project code, which only carries comments about the why.
The code blocks are not compiled or run. The source of truth is the rules; if anything conflicts, the rules win.

Before creating a class, open the file for its layer and copy the shape of the section for its kind.

- The SQL is PostgreSQL; the marker for adapting it to your database is in `infrastructure.md`.
- Some pieces need a dependency the template does not include: `spring-boot-starter-restclient` for the HTTP adapter and `org.wiremock:wiremock-standalone` (test) for `WireMockConfig`. Include them in the plan when the first external service appears.

| Layer | File |
|---|---|
| Domain | `domain.md` |
| Application | `application.md` |
| Infrastructure (web, persistence, external adapters, API, migrations) | `infrastructure.md` |
| Tests | `tests.md` |
| Diagrams | `docs.md` |

## Sections by kind

| Kind | File |
|---|---|
| Aggregate root | `domain.md` |
| Aggregate state for persistence (more than 4 values) | `domain.md` |
| Typed identifier | `domain.md` |
| ID of an aggregate from another context | `domain.md` |
| Collection element (value object without ID) | `domain.md` |
| Shared value object | `domain.md` |
| Variants with a sealed interface | `domain.md` |
| Persisted enum | `domain.md` |
| Business rule exceptions (409) | `domain.md` |
| "Not found" exception (404) | `domain.md` |
| Repository interface | `domain.md` |
| Command | `application.md` |
| Creation use case (sequence ID, `Clock`) | `application.md` |
| Modification use case | `application.md` |
| Use case with an external service (ordering and rollback) | `application.md` |
| Query | `application.md` |
| Result record (and conversion from the aggregate) | `application.md` |
| Read-by-ID use case | `application.md` |
| Paginated query | `application.md` |
| Listing use case | `application.md` |
| Read port | `application.md` |
| Paginated result | `application.md` |
| Output port to an external service | `application.md` |
| Exception in a port's contract | `application.md` |
| Controller | `infrastructure.md` |
| Web conversion | `infrastructure.md` |
| API specification | `infrastructure.md` |
| Flyway migration | `infrastructure.md` |
| Aggregate mapper | `infrastructure.md` |
| Rows (`*Row`) | `infrastructure.md` |
| Repository (version, collections, sealed types) | `infrastructure.md` |
| Read mapper | `infrastructure.md` |
| Read port implementation | `infrastructure.md` |
| Adapter properties | `infrastructure.md` |
| HTTP adapter with `RestClient` | `infrastructure.md` |
| External request body | `infrastructure.md` |
| Technical integration exception | `infrastructure.md` |
| Object Mother | `tests.md` |
| Aggregate test (includes a parameterized one) | `tests.md` |
| Value object test | `tests.md` |
| Use case test | `tests.md` |
| Use case test with an external service (WireMock) | `tests.md` |
| Read port test | `tests.md` |
| Repository concurrency test | `tests.md` |
| Controller test | `tests.md` |
| Full flow test | `tests.md` |
| Shared WireMock server | `tests.md` |
| Context diagram | `docs.md` |
| Context map | `docs.md` |
