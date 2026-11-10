# 0002. Use English for all project text

## Status

Accepted

## Context

The template was written in Spanish: `CLAUDE.md`, `.claude/` (rules, skill, references,
hook), README, ADRs, comments and Javadoc, while identifiers, log messages and exception
messages were already in English. Mixing two languages makes the project harder to share and
forces readers and Claude to switch between them. The `CAMBIAME` placeholder was also Spanish.

## Decision

We will write all project text in English: code, comments, documentation, ADRs and Claude Code
instructions. The placeholder for the project name and for pending decisions is `CHANGEME`.

## Consequences

- One language across code, documentation and Claude instructions.
- ADR file names use English slugs (`0001-record-architecture-decisions.md`).
- New documentation and comments are written in English (`.claude/rules/docs.md`).
