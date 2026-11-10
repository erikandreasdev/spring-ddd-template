#!/usr/bin/env bash
# Claude Code "Stop" hook: runs the ArchUnit rules when Claude finishes a turn.
# If they fail (or the code does not compile), it returns the error to Claude with exit 2 so it fixes it.

input=$(cat)

# Avoid loops: if Claude is already continuing because of this hook, let it finish.
if printf '%s' "$input" | grep -Eq '"stop_hook_active"[[:space:]]*:[[:space:]]*true'; then
  exit 0
fi

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

# With no pending changes in Java files or the pom there is nothing to check.
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
  echo "architecture-check: Maven not found; architecture rules not checked." >&2
  exit 1
fi

if output=$("$mvn_cmd" -q test -Dtest=ArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false 2>&1); then
  exit 0
fi

{
  echo "ArchitectureTest failed or the project does not compile."
  echo "Fix the code following CLAUDE.md; do not modify or disable ArchitectureTest."
  echo
  printf '%s\n' "$output" | tail -n 80
} >&2
exit 2
