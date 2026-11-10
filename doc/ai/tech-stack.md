# Tech stack

- Java 21, Spring Boot 4, Maven.
- Persistence with MyBatis (annotation-based mappers) and migrations with Flyway.
- Database: PostgreSQL. <!-- CHANGEME: database -->
- Contract-first API: `src/main/resources/openapi/api.yaml` generates interfaces and models.
- No Lombok or MapStruct: records, constructors and hand-written mapping.
- Formatting: google-java-format. <!-- CHANGEME: format command, or delete this comment if you format from the IDE -->
- Tests: JUnit 5, AssertJ, Instancio, Testcontainers and ArchUnit; JaCoCo enforces 80% branch coverage.
- Base package: `com.CHANGEME`.
