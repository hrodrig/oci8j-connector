# Phase 1: Edge identity (B core)

**Goal:** Trusted proxies + resolved client IP + API `ALLOWED_CIDRS` with 403 deny.  
**Requirements:** EDGE-01..05  
**Process:** Frequent atomic commits. PR only after full milestone local gates (`make test` / lint; release-check before ship).

## Success criteria

- [ ] Empty `TRUSTED_PROXIES` → ignore XFF / X-Real-IP (spoof rejected)
- [ ] Peer in `TRUSTED_PROXIES` → resolve client from XFF (strip trusted) or X-Real-IP
- [ ] Empty `ALLOWED_CIDRS` → no API IP filter
- [ ] Non-empty → API routes outside list get `{"code":403,"message":"Forbidden"}`
- [ ] Probes (`/healthz`, `/ready`) exempt from API allow-list
- [ ] `GET /` unaffected (filter not on `/`)
- [ ] Unit tests cover matcher + resolver + filter deny/allow
- [ ] `make test` green

## Tasks

### T1 — PLAN + STATE
Commit planning artifacts; note PR-at-end + local validation gate.

### T2 — CIDR + ClientIpResolver (pure)
- `CidrMatcher` IPv4 CIDR + exact IP (IPv6 exact optional)
- `ClientIpResolver.resolve(HttpServletRequest, trustedCidrs)`
- Unit tests EDGE-01/02

### T3 — Allow-list filter
- `EdgeAccessConfig` (`TRUSTED_PROXIES`, `ALLOWED_CIDRS`)
- `IpAllowListFilter` order 0; register `/api/v1/oci8j-connector/*`
- Skip healthz/ready; 403 JSON
- Wire in `SecurityConfig`; defaults in `application.yml`
- Unit/filter tests EDGE-03..05

### T4 — Verify
`make test` (and `make lint` if quick)

## Out of scope this phase

Probes config (`PROBES_*`, `/readyz`), rate limit, CORS, OpenAPI.
