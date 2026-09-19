---
milestone: v1.4.0
milestone_name: OpenAPI and edge hardening
status: executing
progress:
  phases_total: 5
  phases_done: 0
  percent: 0
last_activity: 2026-09-19 — Phase 1 executing; PR after local gates
---

# STATE

## Current Position

Phase: 1 — Edge identity (executing)
Plan: `.planning/phases/01-edge-identity/PLAN.md`
Status: Executing phase 1
Last activity: 2026-09-19 — Roadmap OK; PR deferred until local gates green

## Process

- **Commits:** frecuentes y atómicos (1 cambio lógico / task por commit).
- **PR:** solo al **final** del milestone, **después** de validar en local que pasen las pruebas (`make test`, `make lint`; `make release-check` antes de ship).
- **Ship:** VERSION bump + tag solo con OK explícito.
- **Branch:** `feat/v1.4.0-hardening` → PR → `develop`.

## Accumulated Context

- SPEC §7 A+B+C locked; probes `PROBES_PUBLIC` + `PROBES_ALLOWED_CIDRS`; `/readyz` alias.
- Baseline shipped: v1.3.1.
- Research skipped for milestone start: contract already normative in SPEC.
- Roadmap confirmed 2026-09-19.

## Blockers

(None)

## Todos

- [x] Approve ROADMAP phases
- [ ] Complete phase 1 (EDGE-*)
- [ ] Phases 2–5
- [ ] Local `make test` (+ lint) green before PR
