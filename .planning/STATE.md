---
milestone: v1.4.0
milestone_name: OpenAPI and edge hardening
status: executing
progress:
  phases_total: 5
  phases_done: 1
  percent: 20
last_activity: 2026-09-19 — Phase 1 done (13 tests); next phase 2 probes
---

# STATE

## Current Position

Phase: 2 — Probe access (next)
Plan: Phase 1 complete; phase 2 not planned yet
Status: Phase 1 done
Last activity: 2026-09-19 — EDGE-* shipped; `make test` 13/13

## Process

- **Commits:** frecuentes y atómicos.
- **PR:** solo al **final**, tras validar local (`make test`, `make lint`; `release-check` antes de ship).
- **Branch:** `feat/v1.4.0-hardening`.

## Accumulated Context

- Phase 1: `CidrMatcher`, `ClientIpResolver`, `IpAllowListFilter`, `TRUSTED_PROXIES` / `ALLOWED_CIDRS`.
- Roadmap confirmed; PR deferred until local gates green.

## Blockers

(None)

## Todos

- [x] Phase 1 EDGE-*
- [ ] Phase 2 PROB-*
- [ ] Phases 3–5
- [ ] Local gates green before PR
