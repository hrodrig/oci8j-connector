# Requirements: oci8j-connector v1.4.0

**Defined:** 2026-09-19  
**Core Value:** Safe, operable REST access to legacy Oracle 8i without upgrading the database stack.  
**Contract:** [SPEC.md](../SPEC.md) §7

## v1.4.0 Requirements

### OpenAPI (A)

- [ ] **OAPI-01**: springdoc-openapi runs on Spring Boot 2.7 / Java 8
- [ ] **OAPI-02**: OpenAPI off by default when profile contains `prod` or when not explicitly enabled
- [ ] **OAPI-03**: `OPENAPI_ENABLED=true` or profile `dev`/`local` enables docs + UI; paths frozen in SPEC
- [ ] **OAPI-04**: When Basic Auth enabled, OpenAPI paths require same credentials as `/info`
- [ ] **OAPI-05**: If OpenAPI on and Basic Auth off → startup warn; prod compose/examples keep OpenAPI off

### Edge identity & allow-lists (B)

- [x] **EDGE-01**: Resolve client IP from peer `RemoteAddr`; honor XFF/X-Real-IP only if peer ∈ `TRUSTED_PROXIES`
- [x] **EDGE-02**: Empty `TRUSTED_PROXIES` (default) ignores all forwarded client headers
- [x] **EDGE-03**: `ALLOWED_CIDRS` gates API routes (`/query`, `/info`, OpenAPI when on); empty = no API IP filter
- [x] **EDGE-04**: Outside allow-list → **403** minimal JSON
- [x] **EDGE-05**: `GET /` public 404 remains outside API allow-list

### Probes (B)

- [ ] **PROB-01**: `/readyz` alias identical to `/ready`
- [ ] **PROB-02**: Default `PROBES_PUBLIC=true` — no Basic Auth; not gated by `ALLOWED_CIDRS`
- [ ] **PROB-03**: `PROBES_PUBLIC=false` + Basic Auth on → probes require Basic Auth
- [ ] **PROB-04**: Non-empty `PROBES_ALLOWED_CIDRS` → only those IPs; else **403**; independent of `ALLOWED_CIDRS`
- [ ] **PROB-05**: Probes exempt from rate limit

### Hardening (C)

- [ ] **HARD-01**: Per resolved-client-IP rate limit; env names frozen in SPEC; **429** minimal JSON
- [ ] **HARD-02**: `CORS_ORIGINS` allow-list; empty = `*` with startup warn when Basic Auth off (freeze at implement)
- [ ] **HARD-03**: Security headers on all responses: `nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, conservative `Permissions-Policy`

### Docs & release hygiene

- [ ] **DOCS-01**: SPEC §4/§7 env table matches shipped names; README + compose examples updated
- [ ] **DOCS-02**: CHANGELOG Unreleased → 1.4.0; VERSION/pom/Dockerfile/README badges synced on bump PR
- [ ] **TEST-01**: Automated tests for 403/429, probe defaults, trusted-proxy spoof rejection

## Deferred (post-1.4.0)

- Rate-limit opt-in for probes
- Fail-closed CORS default
- OAuth / mTLS

## Out of Scope

| Feature | Reason |
|---------|--------|
| Oracle / JDBC upgrade | Product premise is 8i + classes12 |
| Non-amd64 GHCR | Existing release contract |
| Drive-by main commits | AGENTS gitflow |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| EDGE-01..05 | 1 | Done |
| PROB-01..05 | 2 | Pending |
| HARD-01..03 | 3 | Pending |
| OAPI-01..05 | 4 | Pending |
| DOCS-01..02, TEST-01 | 5 | Pending |

**Coverage:** 20 v1.4 reqs mapped; 0 unmapped.

---
*Requirements defined: 2026-09-19*  
*Last updated: 2026-09-19 after milestone start*
