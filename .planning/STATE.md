---
milestone: v1.4.0
milestone_name: OpenAPI and edge hardening
status: planning
progress:
  phases_total: 5
  phases_done: 0
  percent: 0
last_activity: 2026-09-19 — Milestone v1.4.0 started (frequent atomic commits)
---

# STATE

## Current Position

Phase: Not started (defining requirements / roadmap)
Plan: —
Status: Defining requirements → roadmap
Last activity: 2026-09-19 — Milestone v1.4.0 started

## Process

- **Commits:** frecuentes y atómicos (1 cambio lógico / task por commit).
- **Ship:** VERSION bump + tag solo con OK explícito.
- **Branch:** `feat/v1.4.0-hardening` → PR → `develop`.

## Accumulated Context

- SPEC §7 A+B+C locked; probes `PROBES_PUBLIC` + `PROBES_ALLOWED_CIDRS`; `/readyz` alias.
- Baseline shipped: v1.3.1.
- Research skipped for milestone start: contract already normative in SPEC.

## Blockers

(None)

## Todos

- [ ] Approve ROADMAP phases
- [ ] `/gsd-discuss-phase` or `/gsd-plan-phase 1` when roadmap locked
