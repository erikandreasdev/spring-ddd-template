#!/usr/bin/env bash
# Hook "Stop" de Claude Code: ejecuta las reglas de ArchUnit cuando Claude termina un turno.
# Si fallan (o el código no compila), devuelve el error a Claude con exit 2 para que lo corrija.

input=$(cat)

# Evita bucles: si Claude ya está continuando por este hook, se le deja terminar.
if printf '%s' "$input" | grep -Eq '"stop_hook_active"[[:space:]]*:[[:space:]]*true'; then
  exit 0
fi

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

# Sin cambios pendientes en Java o en el pom no hay nada que comprobar.
if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  if [ -z "$(git status --porcelain --untracked-files=all -- '*.java' 'pom.xml')" ]; then
    exit 0
  fi
fi

if [ -x ./mvnw ]; then
  mvn_cmd=./mvnw
elif command -v mvn >/dev/null 2>&1; then
  mvn_cmd=mvn
else
  echo "architecture-check: no se encuentra Maven; reglas de arquitectura sin comprobar." >&2
  exit 1
fi

if output=$("$mvn_cmd" -q test -Dtest=ArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1); then
  exit 0
fi

{
  echo "ArchitectureTest ha fallado o el proyecto no compila."
  echo "Corrige el código siguiendo CLAUDE.local.md; no modifiques ni desactives ArchitectureTest."
  echo
  printf '%s\n' "$output" | tail -n 80
} >&2
exit 2
