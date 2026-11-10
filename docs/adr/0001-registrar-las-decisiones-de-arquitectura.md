# 0001. Registrar las decisiones de arquitectura

## Estado

Aceptado

## Contexto

El proyecto sigue un conjunto de reglas de arquitectura y estilo (`CLAUDE.local.md` y
`.claude/rules/`). Con el tiempo aparecerán excepciones a esas reglas y elecciones entre
alternativas que no están cubiertas por ellas. Si no quedan escritas con su motivo, se
pierden o se deshacen por error.

## Decisión

Registraremos esas decisiones como ADRs con el formato de Michael Nygard en `docs/adr/`,
numerados en orden y con la plantilla `docs/adr/template.md`. Un ADR aceptado no se
reescribe: si la decisión cambia, se crea uno nuevo que lo sustituye.

## Consecuencias

Antes de proponer un cambio relevante hay que revisar los ADRs existentes. Las reglas del
proyecto siguen siendo la norma; un ADR solo documenta lo que se aparta de ellas o lo que no
cubren.
