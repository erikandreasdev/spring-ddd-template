---
paths:
  - "src/main/java/**/infrastructure/**/*.java"
  - "src/main/resources/**"
---

# Spring style

Spring is only used in `infrastructure`.

## Configuration and wiring
- Constructor injection, with a single constructor and no `@Autowired`. Never on fields or
  setters. Never `ApplicationContext.getBean(...)`.
- Adapters are registered with stereotypes (`@RestController`, `@Repository`, `@Component`)
  and are package-private.
- Properties: one `@ConfigurationProperties` record with `@Validated` per adapter
  (`PaymentProperties`). Never `@Value`.
- No `@Profile` in production code. What changes between environments are values in
  `application.yml` resolved from environment variables. Secrets only through environment
  variables.
- Configuration always in `application.yml`, never in `.properties`.
- Configuration classes with `@Configuration(proxyBeanMethods = false)`.
- Never `@Transactional`: `UseCaseConfig` applies the transaction to use cases.
- The time comes from the `Clock` bean in `ClockConfig`.

## External HTTP services
- `RestClient` inside the adapter that implements the port, with base URL and timeouts in its
  properties record. Never `RestTemplate` or Feign.

## Logging and observability
- Logging only happens in infrastructure. Request logging (`RequestLoggingFilter`) and error
  logging (`ApiExceptionHandler`) already exist; do not add method entry or exit logs.
- An exception is logged only once, in `ApiExceptionHandler`: 5xx as `ERROR` with stack trace;
  4xx are not logged.
- Adapters to external services may log `WARN` on retries or integration failures. `ERROR`
  only for what requires intervention. `DEBUG` freely.
- Never log passwords, tokens, card data or personal data (emails, names, addresses). Log IDs.
- Messages in English, with SLF4J placeholders and no string concatenation.
- Micrometer Tracing puts the `traceId` on every log line and in error responses. It is not
  handled by hand.
- Actuator exposes only `health` (with `liveness` and `readiness`) and `info`.
