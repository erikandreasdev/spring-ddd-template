---
paths:
  - "docs/**"
---

# Documentación

Se escribe en español. Los nombres de clases, tablas y endpoints se mantienen tal cual.

## Diagramas (PlantUML)
- Un diagrama de clases por contexto: `docs/diagrams/<contexto>.puml`.
- Contiene agregados (`<<Aggregate Root>>`), entidades (`<<Entity>>`) y value objects
  (`<<Value Object>>`), con sus atributos de negocio y métodos públicos de negocio. Sin
  repositorios, casos de uso ni detalles técnicos.
- Las referencias por ID entre agregados se dibujan como dependencia discontinua con el nombre
  del ID (`Order ..> Customer : customerId`). La composición, con rombo relleno.
- Con dos o más contextos, además `docs/diagrams/context-map.puml`: un componente por contexto
  y una flecha por relación, con la dirección de la dependencia y qué se intercambia.
- Se actualizan en la misma tarea que cambia el modelo.

## ADRs (formato de Michael Nygard)
- Uno por decisión: `docs/adr/NNNN-titulo-en-minusculas-con-guiones.md`, numerados en orden.
- Se crean cuando se acepta una excepción a las reglas del proyecto, se elige entre
  alternativas reales (una librería, una forma de integrar un servicio) o cambia una regla.
  No para cada tarea.
- Estructura fija: título, estado, contexto, decisión y consecuencias. Plantilla en
  `docs/adr/template.md`.
- Un ADR aceptado no se reescribe: si cambia la decisión, se crea otro y el antiguo pasa a
  `Sustituido por NNNN`.
