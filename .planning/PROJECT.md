# oci8j-connector

## What This Is

Spring Boot 2.7 / Java 8 REST bridge to Oracle 8i via `classes12.jar`. Operators run it as JAR or GHCR image (`linux/amd64`) behind Basic Auth, with SQL keyword blocking and health/readiness probes for orchestration.

## Core Value

Safe, operable REST access to legacy Oracle 8i without upgrading the database stack.

## Current Milestone: v1.4.0 OpenAPI and edge hardening

**Goal:** Ship SPEC §7 tracks **A + B + C** — gated OpenAPI, trusted-proxy client identity + allow-lists (API + probes), rate limit / CORS allow-list / security headers — as additive SemVer minor **1.4.0**.

**Target features:**
- OpenAPI / Swagger UI, off by default in prod, Basic Auth gated when auth on
- `TRUSTED_PROXIES` + resolved client IP; `ALLOWED_CIDRS` for API routes
- Probe policy: `/healthz`, `/ready`, `/readyz`; `PROBES_PUBLIC` (default true) + `PROBES_ALLOWED_CIDRS`
- Rate limit per client IP; `CORS_ORIGINS`; baseline security headers
- Docs / env contract frozen in SPEC §4–§7; VERSION bump as dedicated develop PR before tag

**Process:** Frequent atomic commits (one logical change / task per commit). GSD discuss → plan → execute → verify per phase. No drive-by commits on `main`/`develop`.

## Requirements

### Validated

- REST `/query`, `/info`, `/healthz`, `/ready` under `/api/v1/oci8j-connector`
- Basic Auth (optional), SQL forbidden keywords
- Makefile gates, GHCR release, root minimal 404 JSON
- Released **v1.3.1** (Grype fail-on critical)

### Active

- [ ] SPEC §7.1 OpenAPI gated (A)
- [ ] SPEC §7.2 Client IP / trusted proxies / API allow-list (B)
- [ ] SPEC §7.2.1 Configurable probe access + `/readyz` (B)
- [ ] SPEC §7.3 Rate limit, CORS allow-list, security headers (C)
- [ ] Env names frozen in SPEC; README/compose examples updated
- [ ] Tests covering deny paths (403/429) and probe defaults

### Out of Scope

- Replacing `classes12` / Oracle upgrade path — product premise
- Mutual TLS / OAuth — Basic Auth remains; revisit later
- Fail-closed CORS by default — prefer empty = `*` + warn when Basic Auth off (SPEC)
- Rate-limiting probes — exempt unless future flag
- Windows-native GHCR images — amd64 Linux only

## Context

Normative contract: **[SPEC.md](../SPEC.md)** §7 (locked A+B+C). Shipped baseline: **v1.3.1**. Planning started while PR docs for §7 may land on `develop` via parallel PR; this branch carries the same SPEC §7 text.

## Constraints

- **Tech stack**: Spring Boot **2.7**, Java **8** — springdoc and filters must stay compatible
- **Git flow**: topic → PR → `develop` → PR → `main` → annotated tag; never push protected branches
- **Commits**: frequent / atomic; show message and get approval when AGENTS requires it for ship steps
- **Security**: no secrets in git; deny bodies minimal JSON
- **Release**: VERSION bump dedicated commit on `develop` before `make release-check` / tag

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Scope A+B+C in one minor | Operator lock; additive env surface | — Pending ship |
| Probes public by default | K8s/compose DX; optional lock-down | — Pending |
| `PROBES_ALLOWED_CIDRS` ≠ `ALLOWED_CIDRS` | Probe sources ≠ API clients | — Pending |
| Empty `TRUSTED_PROXIES` = ignore XFF | Avoid spoofing | — Pending |
| Frequent atomic commits | Reviewability; GSD execute discipline | ✓ Active |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition:**
1. Requirements invalidated? → Out of Scope with reason
2. Requirements validated? → Validated with phase reference
3. New requirements emerged? → Active
4. Decisions to log? → Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone:**
1. Full review of all sections
2. Core Value check
3. Audit Out of Scope
4. Update Context

---
*Last updated: 2026-09-19 after milestone v1.4.0 start*
