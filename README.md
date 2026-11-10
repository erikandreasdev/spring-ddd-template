# Plantilla Spring Boot + DDD para Claude Code

Proyecto mínimo de Spring Boot 4 (Java 21, Maven, MyBatis, Flyway, API contract-first) con la
configuración de Claude Code ya incluida. No trae código de negocio: solo el arranque, las
piezas compartidas de la arquitectura y los tests que la verifican.

## Antes del primer arranque

Todo lo que hay que tocar está marcado con la palabra **CAMBIAME**. Hay dos tipos de marca:

- `CAMBIAME:` (con dos puntos) señala una decisión que hay que revisar a mano.
- `CAMBIAME` a secas es el nombre del proyecto y se sustituye de golpe.

### 1. Revisa las decisiones

```
grep -rn "CAMBIAME:" --exclude=README.md .
```

Salen ocho puntos, todos menos uno sobre la base de datos. La plantilla funciona con
PostgreSQL sin cambiar nada; si usas esa base de datos, basta con borrar los comentarios.

| Fichero | Qué decidir |
|---|---|
| `pom.xml` (dos comentarios) | Driver JDBC, módulo de Flyway y módulo de Testcontainers de tu base de datos |
| `src/main/resources/application.yml` | Prefijo de la URL JDBC |
| `src/test/java/com/CAMBIAME/TestcontainersConfig.java` | Clase e imagen del contenedor |
| `CLAUDE.local.md` (dos comentarios) | Nombre de la base de datos y comando de formato |
| `.claude/rules/persistence.md` | Sintaxis de las secuencias |
| `.claude/references/INDEX.md` | Sintaxis SQL de las referencias |

Borra cada comentario `CAMBIAME:` al resolverlo, para que el paso siguiente no lo altere.

### 2. Pon el nombre del proyecto

Elige un nombre en minúsculas, sin espacios ni guiones (por ejemplo `tienda`). Se usa como
paquete (`com.tienda`), `artifactId`, nombre de la aplicación, título de la API y nombre de la
base de datos.

```
NOMBRE=tienda
grep -rl "CAMBIAME" --exclude=README.md . | xargs sed -i "s/CAMBIAME/$NOMBRE/g"
find . -depth -type d -name CAMBIAME -execdir mv CAMBIAME "$NOMBRE" \;
```

El `find` renombra las cuatro carpetas de paquete: las de `src/` y las de `.claude/references/`.

En macOS, `sed -i` necesita un argumento vacío: usa `sed -i ''` en lugar de `sed -i`.

Si tu paquete no empieza por `com`, cambia también ese prefijo en el `groupId` y en los
paquetes del generador del `pom.xml`, en las declaraciones `package` e `import` y en las
carpetas.

Comprueba que no queda ninguna marca:

```
grep -rn "CAMBIAME" --exclude=README.md .
```

### 3. Deja la configuración de Claude Code fuera del repositorio

Es configuración personal. Después de `git init`, añade a `.git/info/exclude`:

```
CLAUDE.local.md
.claude/
```

`docs/` sí forma parte del proyecto.

### 4. Primer arranque

Hace falta Java 21, Maven y Docker en marcha.

```
mvn -q test -Dtest=ArchitectureTest   # reglas de arquitectura, sin Docker
mvn verify                            # todos los tests y cobertura
mvn spring-boot:test-run              # arranca la aplicación con la base de datos en Docker
```

Con la aplicación arrancada, `http://localhost:8080/actuator/health` debe responder `UP`.

Después abre Claude Code (versión con soporte de `defaultMode` y reglas con `paths`) en la
carpeta del proyecto. Empieza en modo plan: no edita nada hasta que apruebas el plan.

## Qué incluye

### Claude Code

| Fichero | Función |
|---|---|
| `CLAUDE.local.md` | Arquitectura y forma de trabajar; se carga en cada sesión |
| `.claude/rules/java-style.md` | Estilo Java; se carga al leer ficheros `.java` |
| `.claude/rules/spring-style.md` | Configuración, logs y observabilidad; al leer infraestructura o recursos |
| `.claude/rules/api.md` | Diseño de la API y controladores; al leer `api.yaml` o la capa web |
| `.claude/rules/persistence.md` | Repositorios, SQL y migraciones; al leer persistencia o `db/` |
| `.claude/rules/testing.md` | Estrategia y estilo de tests; al leer `src/test` |
| `.claude/rules/docs.md` | Diagramas y ADRs; al leer `docs/` |
| `.claude/skills/add-feature/` | Procedimiento para añadir funcionalidad |
| `.claude/references/` | Un ejemplo de cada tipo de clase (contexto `orders`), con índice en `INDEX.md`. No se compila |
| `.claude/settings.local.json` | Modo plan por defecto, permisos y el hook |
| `.claude/hooks/architecture-check.sh` | Ejecuta `ArchitectureTest` al terminar cada turno |

Permisos configurados: `mvn` y los comandos de lectura de `git` sin preguntar; `git commit` y
`git push` bloqueados; pregunta antes de editar `CLAUDE.local.md`, `.claude/`,
`ArchitectureTest` y el `pom.xml`.

### Código

| Fichero | Función |
|---|---|
| `src/main/resources/openapi/api.yaml` | Especificación de la API; genera interfaces y modelos al compilar |
| `shared/application/UseCase.java` | Anotación propia, sin Spring, para marcar casos de uso |
| `shared/infrastructure/UseCaseConfig.java` | Registra las clases `@UseCase` como beans transaccionales |
| `shared/infrastructure/ClockConfig.java` | Bean `Clock` en UTC |
| `shared/domain/*Exception.java` | Bases de las excepciones de dominio y conflicto de versión |
| `shared/infrastructure/web/ApiExceptionHandler.java` | Traduce excepciones a `ProblemDetail` con `traceId` |
| `shared/infrastructure/web/RequestLoggingFilter.java` | Una línea de log por petición |
| `ArchitectureTest.java` | Reglas de ArchUnit |
| `ApiExceptionHandlerTest.java` | Tests del manejo de errores |
| `IntegrationTest.java`, `TestcontainersConfig.java` | Tests de integración con base de datos real |
| `TestApplication.java` | Arranque local con Testcontainers |
| `ApplicationTest.java` | Comprueba que el contexto arranca |
| `docs/adr/` | Plantilla y primer ADR |
| `docs/diagrams/` | Diagramas PlantUML por contexto (vacío al empezar) |

## Configuración por entorno

Todo por variables de entorno, sin perfiles:

| Variable | Para qué |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Base de datos real |
| `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` | Logs en JSON |
| `MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT` | Envío de trazas a un colector OTLP |

## Versiones

Spring Boot 4.1.2, MyBatis Spring Boot Starter 4.1.0, OpenAPI Generator 7.27.0, ArchUnit
1.5.1, Instancio 6.2.1 y JaCoCo 0.8.16. Están en las propiedades del `pom.xml`.
