---
paths:
  - "src/test/**"
---

# Tests

## Orden de trabajo (TDD)
- Primero el test. Se escribe, se ejecuta y se comprueba que falla por el motivo esperado.
  Después, el código mínimo que lo hace pasar. Después, refactorizar con los tests en verde.
- Un test no se modifica para que pase, salvo que el propio test estuviera mal.

## Qué tests y cuántos

| Nivel | Tipo | Cuántos |
|---|---|---|
| Dominio | Unitario, sin Spring | Uno por regla de negocio y por caso límite de cada value object |
| Caso de uso | `@IntegrationTest` | Tres como máximo: camino feliz, "no existe" y un fallo de regla que demuestre el rollback |
| Puerto de lectura | `@IntegrationTest` | Uno por cada filtro, orden o paginación |
| Controlador | `@WebMvcTest` | Uno por código HTTP distinto de cada endpoint |
| Flujo completo | `@IntegrationTest` por HTTP | Uno por agregado, con su flujo principal (`OrderFlowTest`) |

- Las reglas de negocio se prueban en el dominio, no se repiten en los casos de uso.
- Los repositorios no tienen tests propios: los cubren los tests de casos de uso. La única
  excepción es un test por agregado que guarda una versión antigua y espera
  `ConcurrentUpdateException` (`MyBatisOrderRepositoryTest`).

## Forma
- Nombres `givenX_whenY_thenZ` y cuerpo separado con `// given`, `// when`, `// then`.
- Un comportamiento por test, con todas las aserciones que lo describan.
- La unidad es el comportamiento, no la clase: los agregados se prueban con sus value objects
  reales. Sin mocks dentro del dominio.
- AssertJ para todo; excepciones con `assertThatThrownBy`. Nada de aserciones de JUnit.
- Se comprueba el resultado observable (estado, valor devuelto, excepción), no las llamadas.
- Sin lógica en los tests: ni `if`, ni bucles, ni cálculo del valor esperado.
- `@ParameterizedTest` solo para valores límite de un mismo comportamiento, con `name` legible.
- `<Clase>Test` en el mismo paquete que la clase probada. Clases y métodos package-private.
- `@BeforeEach` solo para preparación técnica, nunca para datos de negocio.

## Datos de prueba
- Un `*Mother` por agregado (clase pública en `src/test`, en el paquete del agregado) con una
  factoría estática por escenario: `OrderMother.draftWithOneLine()`.
- Los agregados se construyen siempre con las factorías y métodos de negocio del dominio.
  Nunca con `restore(...)`, reflexión ni rellenando sus campos con Instancio.
- Todo valor que el test no comprueba (IDs, nombres, importes, cantidades) se genera con
  Instancio, tanto en los Mothers como en los tests.
- Se usa un literal solo cuando el test comprueba ese valor o un resultado que depende de él.
- Los valores aleatorios deben ser válidos para el dominio: rangos acotados
  (`Instancio.gen().ints().range(1, 20).get()`), IDs positivos y coherencia entre ellos
  (misma divisa).
- En tests de dominio los IDs los genera Instancio; en tests de integración se piden al
  repositorio con `nextId()`, para que coincidan con la secuencia.
- La hora se fija con `Clock.fixed(...)` cuando el resultado depende de ella.
- Toda clase de test que use Instancio lleva `@ExtendWith(InstancioExtension.class)`
  (`@IntegrationTest` ya lo incluye). Si un test falla, se reproduce con la semilla que
  imprime (`@Seed`) y se corrige el código o el generador; `@Seed` no se deja en el código.

## Tests con Spring
- Integración siempre con `@IntegrationTest` (Spring y base de datos real con Testcontainers).
  Nunca `@Testcontainers` ni `@DynamicPropertySource` por clase.
- Los tests de casos de uso usan el repositorio real. Sin fakes ni mocks de repositorios. Cada
  test crea sus datos con IDs nuevos y comprueba el estado leyendo del repositorio.
- Sin `@Transactional` ni `@DirtiesContext` en los tests.
- Controladores: `@WebMvcTest`, casos de uso sustituidos con `@MockitoBean` y aserciones con
  `MockMvcTester`. Solo se comprueba la traducción HTTP, con las rutas completas (`/v1/...`).
- Servicios externos: el adaptador real contra WireMock, con un único servidor compartido y la
  URL base del adaptador apuntando a él. Nunca `@MockitoBean` ni fakes sobre puertos.

## Cobertura
- JaCoCo exige un 80 % de ramas en todo el proyecto; `mvn verify` falla si no se alcanza.
- Antes de dar por terminada una funcionalidad se ejecuta `mvn verify`.
- No se escriben tests sin aserciones ni se añaden exclusiones para subir el porcentaje.

## Referencias

Hay un test de referencia de cada nivel en `.claude/references/test/` (índice en
`.claude/references/INDEX.md`): Mother, dominio, value object, caso de uso, caso de uso con
WireMock, puerto de lectura, concurrencia, controlador y flujo completo.
