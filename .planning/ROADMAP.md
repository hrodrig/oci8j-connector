# Roadmap: oci8j-connector v1.4.0

**Milestone:** v1.4.0 — OpenAPI and edge hardening  
**Process:** Frequent atomic commits per task. Discuss → plan → execute → verify per phase.

## Phases

### Phase 1 — Edge identity (B core)

**Goal:** Trusted proxies + resolved client IP + API `ALLOWED_CIDRS` with 403 deny.

**Requirements:** EDGE-01, EDGE-02, EDGE-03, EDGE-04, EDGE-05

**Success:**
- Empty `TRUSTED_PROXIES` ignores XFF spoofing
- API allow-list denies outsiders with minimal 403 JSON
- `/` stays public

### Phase 2 — Probe access (B probes)

**Goal:** `/readyz` + `PROBES_PUBLIC` + `PROBES_ALLOWED_CIDRS`.

**Requirements:** PROB-01..05

**Success:**
- Defaults match v1.3.1 public probes
- Probe CIDR independent of API allow-list
- Auth toggle works when Basic Auth enabled

### Phase 3 — Rate limit, CORS, headers (C)

**Goal:** Per-IP rate limit, CORS allow-list, baseline security headers.

**Requirements:** HARD-01, HARD-02, HARD-03

**Success:**
- 429 on exceed; probes exempt
- CORS empty/`*` behavior frozen in SPEC
- Headers present on success and error responses

### Phase 4 — OpenAPI gated (A)

**Goal:** springdoc off-by-default in prod; Basic Auth when auth on.

**Requirements:** OAPI-01..05

**Success:**
- Prod profile / default: no Swagger UI
- Enable path documented; startup warn if open without auth

### Phase 5 — Docs, tests freeze, release prep

**Goal:** SPEC/README/compose aligned; TEST-01 coverage; VERSION bump ready (separate bump PR).

**Requirements:** DOCS-01, DOCS-02, TEST-01

**Success:**
- `make test` / `make lint` green
- Env table frozen; Unreleased notes ready for 1.4.0
- Stop before tag unless user explicitly approves ship

## Dependency order

```
1 Edge identity → 2 Probes → 3 Hardening
                ↘         ↗
                  4 OpenAPI (can start after 1 for IP gate on docs)
                              → 5 Docs/tests/release prep
```

OpenAPI (4) may proceed after Phase 1 (needs allow-list/auth integration); prefer after 2–3 if capacity serial.

## Next

1. Confirm this roadmap (reply **OK** / adjustments)
2. `/gsd-plan-phase 1` (or discuss-phase 1)

---
*Roadmap created: 2026-09-19*
