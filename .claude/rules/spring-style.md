---
paths:
  - "src/main/java/**/infrastructure/**/*.java"
  - "src/main/resources/**"
---

# Estilo Spring

Spring solo se usa en `infrastructure`.

## Configuración y cableado
- Inyección por constructor, con un único constructor y sin `@Autowired`. Nunca en campos ni
  en setters. Nunca `ApplicationContext.getBean(...)`.
- Los adaptadores se registran con estereotipos (`@RestController`, `@Repository`,
  `@Component`) y son package-private.
- Propiedades: un record `@ConfigurationProperties` con `@Validated` por adaptador
  (`PaymentProperties`). Nunca `@Value`.
- Sin `@Profile` en código de producción. Lo que cambia entre entornos son valores de
  `application.yml` resueltos con variables de entorno. Los secretos, solo por variables de
  entorno.
- Configuración siempre en `application.yml`, nunca en `.properties`.
- Clases de configuración con `@Configuration(proxyBeanMethods = false)`.
- Nunca `@Transactional`: la transacción la pone `UseCaseConfig` en los casos de uso.
- La hora la da el bean `Clock` de `ClockConfig`.

## Servicios HTTP externos
- `RestClient` dentro del adaptador que implementa el puerto, con URL base y timeouts en su
  record de propiedades. Nunca `RestTemplate` ni Feign.

## Logs y observabilidad
- Solo se registra en infraestructura. Ya existen el log de peticiones (`RequestLoggingFilter`)
  y el de errores (`ApiExceptionHandler`); no se añaden logs de entrada o salida de métodos.
- Una excepción se registra una sola vez, en `ApiExceptionHandler`: los 5xx como `ERROR` con
  traza; los 4xx no se registran.
- Los adaptadores a servicios externos pueden registrar `WARN` en reintentos o fallos de
  integración. `ERROR` solo para lo que requiere intervención. `DEBUG` libre.
- Nunca se registran contraseñas, tokens, datos de tarjetas ni datos personales (emails,
  nombres, direcciones). Se registran IDs.
- Mensajes en inglés, con placeholders de SLF4J y sin concatenar cadenas.
- El `traceId` lo pone Micrometer Tracing en cada línea de log y en las respuestas de error.
  No se gestiona a mano.
- Actuator expone solo `health` (con `liveness` y `readiness`) e `info`.
