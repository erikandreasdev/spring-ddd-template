---
name: add-feature
description: Añade o modifica una funcionalidad (caso de uso, endpoint, agregado, repositorio) siguiendo la arquitectura del proyecto. Úsala siempre que haya que crear o cambiar código de negocio.
---

# Añadir o cambiar una funcionalidad

## 1. Plan (siempre, y esperar aprobación)

Antes de escribir nada, presenta un plan con:

- Qué ficheros se crean o cambian, por capa.
- Cambios en `api.yaml` (endpoints, esquemas, códigos de respuesta).
- Cambios en el modelo de dominio y en el esquema de base de datos. Las migraciones
  destructivas, señaladas aparte.
- Dependencias nuevas, con el motivo.
- Si alguna parte se aparta de las reglas o elige entre alternativas: propón el ADR.

Revisa antes los ADRs de `docs/adr/` para no contradecir decisiones ya tomadas.

## 2. Implementación

En cada paso, escribe primero el test, comprueba que falla y después implementa. Antes de
crear cada clase, abre su referencia en `.claude/references/INDEX.md` y sigue su forma.

1. API: cambia `api.yaml` y ejecuta `mvn generate-sources`.
2. Dominio: test unitario de la regla; después el agregado, value object o método de negocio.
3. Persistencia: migración de Flyway (tabla, secuencia, restricciones); interfaz del
   repositorio en `domain`; implementación y mapper en `infrastructure.persistence`.
4. Caso de uso: test de integración; después el `*Command` o `*Query` y la clase `@UseCase`.
5. Web: test del controlador; después la clase `*ApiConversion` y el controlador que
   implementa la interfaz generada.
6. Flujo: añade o amplía el `<Agregado>FlowTest` si cambia el flujo principal.
7. Documentación: actualiza `docs/diagrams/<contexto>.puml` si cambió el modelo (y el mapa de
   contextos si hay varios). Escribe el ADR si se aprobó en el plan.
8. Formatea el código y ejecuta `mvn verify`.

## 3. Cierre

- No hagas commit.
- Resume qué cambió, el resultado de `mvn verify` y cualquier desviación de las reglas.

## Referencias

`.claude/references/INDEX.md` tiene un contexto de ejemplo completo (`orders`) con un fichero por
cada tipo de clase: agregado, value objects, casos de uso, puertos, controlador, conversión web,
mappers, repositorio, migración, adaptador HTTP, tests de cada nivel, diagramas y ADR.
