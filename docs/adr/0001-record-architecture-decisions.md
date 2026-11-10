# 0001. Record architecture decisions

## Status

Accepted

## Context

The project follows a set of architecture and style rules (`CLAUDE.md` and
`.claude/rules/`). Over time there will be exceptions to those rules and choices between
alternatives they do not cover. If they are not written down with their reason, they get
lost or undone by mistake.

## Decision

We will record those decisions as ADRs in Michael Nygard's format in `docs/adr/`, numbered in
order and using the `docs/adr/template.md` template. An accepted ADR is never rewritten: if
the decision changes, a new one is created that supersedes it.

## Consequences

Before proposing a relevant change, the existing ADRs must be reviewed. The project rules are
still the norm; an ADR only documents what departs from them or what they do not cover.
