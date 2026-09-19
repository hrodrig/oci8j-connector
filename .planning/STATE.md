---
milestone: v1.4.0
milestone_name: OpenAPI and edge hardening
status: executing
progress:
  phases_total: 5
  phases_done: 3
  percent: 60
last_activity: 2026-09-19 — Phase 3 done (24 tests); next OpenAPI
---

# STATE

## Current Position

Phase: 4 — OpenAPI gated (next)
Plan: Phase 3 complete
Status: Phase 3 done
Last activity: 2026-09-19 — HARD-* shipped; `make test` 24/24

## Process

- **Commits:** frecuentes y atómicos.
- **PR:** solo al **final**, tras validar local (`make test`, `make lint`; `release-check` antes de ship).
- **Branch:** `feat/v1.4.0-hardening`.

## Accumulated Context

- Phases 1–3: edge identity, probes, rate/CORS/headers.
- Config: nested `edge.*` / `hardening.*` / `openapi.*` in YAML; env overrides.

## Blockers

(None)

## Todos

- [x] Phase 1–3
- [ ] Phase 4 OpenAPI
- [ ] Phase 5 docs/tests/release prep
- [ ] Local gates green before PR
