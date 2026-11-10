# Arquitectura del proyecto

Todo el código sigue DDD táctico con puertos y adaptadores y la regla de dependencia de Clean
Architecture. Estas reglas son obligatorias. Si una petición choca con alguna, dilo antes de
escribir código en lugar de saltártela.

## Stack

- Java 21, Spring Boot 4, Maven.
- Persistencia con MyBatis (mappers con anotaciones) y migraciones con Flyway.
- Base de datos: PostgreSQL. <!-- CAMBIAME: base de datos -->
- API contract-first: `src/main/resources/openapi/api.yaml` genera interfaces y modelos.
- Sin Lombok ni MapStruct: records, constructores y mapeo escritos a mano.
- Formato: google-java-format. <!-- CAMBIAME: comando de formato, o borra este comentario si formateas desde el IDE -->
- Paquete base: `com.CAMBIAME`.

## Estructura de paquetes

Primero por contexto, después por capa:

```
com.CAMBIAME
├── shared
│   ├── domain           DomainException, NotFoundException, ConcurrentUpdateException
│   ├── application      UseCase (anotación propia, sin Spring)
│   └── infrastructure   UseCaseConfig, ClockConfig; web/ (manejo de errores, log de peticiones)
│       └── web/api      código generado desde api.yaml (no se edita)
└── <contexto>           p. ej. orders
    ├── domain           agregados, value objects, IDs, excepciones, interfaces *Repository
    ├── application      casos de uso, comandos, consultas, records de resultado, puertos
    └── infrastructure
        ├── web          controladores y clases de conversión *ApiConversion
        └── persistence  mappers MyBatis, records *Row, implementaciones de repositorio
```

Las dependencias solo apuntan hacia dentro: `infrastructure → application → domain`.

## Reglas por capa

### domain
- Solo importa `java.*` y otras clases de `domain`. Nada de Spring, MyBatis, Jakarta ni Jackson.
- Los agregados protegen sus invariantes con métodos de negocio. Sin setters.
- Los agregados tienen constructor privado y factorías estáticas: una de creación con nombre
  del lenguaje de negocio (`Order.place(id, ...)`) y `restore(...)` para persistencia.
- Identificadores tipados `record OrderId(long value)`, positivos. Los genera una secuencia de
  la base de datos: el caso de uso los pide con `repository.nextId()` antes de crear el
  agregado, que nace siempre con ID. Nunca `long` sueltos en firmas de dominio o aplicación.
- Cada agregado lleva `long version` (bloqueo optimista): 0 al crearse, la recibe en
  `restore(...)`.
- Un agregado referencia a otro solo por su ID, nunca por objeto.
- Los elementos de una colección dentro de un agregado no tienen ID propio.
- Value objects como records inmutables que se validan en el constructor compacto.
- Un repositorio por agregado, como interfaz en `domain`: `OrderRepository`.
- La hora actual se recibe de un `java.time.Clock` inyectado; nunca `Instant.now()` directo.
- Sin eventos de dominio. Si hace falta reaccionar a algo, el caso de uso llama a otro puerto.

### application
- Solo importa `java.*`, `domain` y `application`. Nada de Spring, Jakarta ni logging.
- Una clase por caso de uso: pública, no final, anotada con `@UseCase`, con nombre
  `<Verbo><Sustantivo>Service` y un único método público.
- Sin interfaz de entrada: el controlador depende de la clase del caso de uso.
- Entrada: un record `*Command` (modifica) o `*Query` (consulta), validado en su constructor
  compacto. Sin anotaciones de Bean Validation.
- Salida: `void` o el ID en comandos; un record de resultado (`OrderSummary`) en consultas.
  Nunca se devuelve el agregado. Sin presenters ni output boundaries.
- El caso de uso orquesta (cargar, llamar al agregado, guardar). Las reglas de negocio viven
  en el dominio.
- No lleva `@Service` ni `@Transactional`: `UseCaseConfig` registra como bean toda clase
  `@UseCase` y envuelve sus métodos públicos en una transacción.
- Lectura de un agregado por su ID: repositorio y conversión al record de resultado.
- Listados, búsquedas y paginación: puerto de lectura en `application` (`OrderSummaries`) que
  devuelve records de resultado directamente desde SQL.
- Los demás puertos de salida son interfaces en `application` nombradas por su propósito
  (`PaymentGateway`) e implementadas en `infrastructure`.

### infrastructure
- Único lugar donde se usa Spring, MyBatis, Jakarta y logging.
- `web`: los controladores implementan la interfaz generada y solo traducen entre HTTP y casos
  de uso. Nunca usan repositorios ni contienen lógica.
- `persistence`: implementaciones de repositorios y puertos de lectura. El agregado nunca se
  usa como tipo de MyBatis.

## Reglas detalladas

| Para | Lee |
|---|---|
| Cualquier código Java | `.claude/rules/java-style.md` |
| Infraestructura y configuración | `.claude/rules/spring-style.md` |
| API (`api.yaml`) y controladores | `.claude/rules/api.md` |
| Persistencia y migraciones | `.claude/rules/persistence.md` |
| Tests | `.claude/rules/testing.md` |
| Diagramas y ADRs | `.claude/rules/docs.md` |
| Añadir o cambiar una funcionalidad | skill `add-feature` (procedimiento) |
| Ejemplo de cada tipo de clase | `.claude/references/INDEX.md` |

Las reglas se cargan solas al leer un fichero de su zona. Si vas a escribir código en una zona
de la que todavía no has leído ningún fichero, lee antes la regla correspondiente. Antes de crear
una clase de un tipo que todavía no existe en el proyecto, abre su referencia.

## Forma de trabajar

- Siempre un plan antes de tocar código, y se espera a que lo apruebe. El plan dice qué
  ficheros cambian, si cambia la API, el modelo o el esquema, y si hace falta un ADR.
- Nunca hagas commits ni `push`: los cambios quedan sin confirmar para que los revise.
- Nunca, sin que lo pida: modificar `ArchitectureTest`, `CLAUDE.local.md` o `.claude/`, editar
  código generado, desactivar o borrar tests.
- No añadas dependencias al `pom.xml` sin decir cuál y por qué en el plan.
- Migraciones destructivas (borrar columnas o tablas, cambiar tipos): siempre explícitas en el
  plan, aunque el cambio parezca pequeño.
- Si cambia el modelo de dominio, actualiza su diagrama en la misma tarea.
- Si acepto una excepción a estas reglas o elegimos entre alternativas reales, regístralo en
  un ADR en `docs/adr/`. Antes de proponer algo, revisa los ADRs existentes.
- Al terminar: qué cambió, resultado de `mvn verify` y cualquier desviación de las reglas.

## Verificación

- Se trabaja con TDD: primero el test, después el código (detalle en `.claude/rules/testing.md`).
- `ArchitectureTest` (ArchUnit) hace cumplir las dependencias y los nombres. No se modifica ni
  se desactiva para que pase el código; se arregla el código. Un hook lo ejecuta al terminar
  cada turno: si falla, corrige antes de dar el trabajo por terminado.
- Una funcionalidad está terminada cuando `mvn verify` pasa (tests y 80 % de cobertura de
  ramas). Necesita Docker en marcha.
