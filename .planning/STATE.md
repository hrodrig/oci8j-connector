---
milestone: v1.4.0
milestone_name: OpenAPI and edge hardening
status: executing
progress:
  phases_total: 5
  phases_done: 2
  percent: 40
last_activity: 2026-09-19 — Phase 2 done (20 tests); next phase 3 hardening
---

# STATE

## Current Position

Phase: 3 — Rate limit / CORS / headers (next)
Plan: Phase 2 complete
Status: Phase 2 done
Last activity: 2026-09-19 — PROB-* shipped; `make test` 20/20

## Process

- **Commits:** frecuentes y atómicos.
- **PR:** solo al **final**, tras validar local (`make test`, `make lint`; `release-check` antes de ship).
- **Branch:** `feat/v1.4.0-hardening`.

## Accumulated Context

- Phase 1: edge identity filters.
- Phase 2: `/readyz`, `PROBES_PUBLIC`, `PROBES_ALLOWED_CIDRS`.

## Blockers

(None)

## Todos

- [x] Phase 1 EDGE-*
- [x] Phase 2 PROB-*
- [ ] Phase 3 HARD-*
- [ ] Phases 4–5
- [ ] Local gates green before PR
