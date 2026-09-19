---
milestone: v1.4.0
milestone_name: OpenAPI and edge hardening
status: executing
progress:
  phases_total: 5
  phases_done: 4
  percent: 80
last_activity: 2026-09-19 — Phase 4 done (29 tests); next docs/release prep
---

# STATE

## Current Position

Phase: 5 — Docs, tests freeze, release prep
Plan: Phase 4 complete
Status: Phase 4 done
Last activity: 2026-09-19 — OAPI-* shipped; `make test` 29/29

## Process

- **Commits:** frecuentes y atómicos.
- **PR:** solo al **final**, tras `make test` + `make lint` (y `release-check` antes de ship).
- **Branch:** `feat/v1.4.0-hardening`.
- **VERSION bump:** dedicated later — not in this feature PR unless asked.

## Todos

- [x] Phases 1–4
- [ ] Phase 5 README/SPEC polish + lint
- [ ] Local gates then PR
