# Phase 2: Probe access

**Goal:** `/readyz` + `PROBES_PUBLIC` + `PROBES_ALLOWED_CIDRS`  
**Requirements:** PROB-01..05  
**Process:** Atomic commits; PR only after full milestone local gates.

## Success criteria

- [x] `/readyz` same semantics as `/ready`
- [x] Default `PROBES_PUBLIC=true` — skip Basic Auth on probes
- [x] `PROBES_PUBLIC=false` + Basic Auth on → probes need credentials
- [x] `PROBES_ALLOWED_CIDRS` gates probes only; independent of `ALLOWED_CIDRS`
- [x] Empty probe CIDR → no probe IP filter
- [x] Outside probe CIDR → 403 JSON
- [x] Probes still exempt from API `ALLOWED_CIDRS`
- [x] Unit tests + `make test` green (20 tests)

## Status

**Complete** 2026-09-19 — commits `fa3f83b`, `6f464ff`.

Note: PROB-05 (rate-limit exempt) holds vacuously until Phase 3 adds rate limiting.

## Tasks

1. PLAN + `/readyz` controller + `/info` discovery
2. `PROBES_*` on `EdgeAccessConfig`; probe IP logic in `IpAllowListFilter`
3. `BasicAuthFilter` respects `PROBES_PUBLIC` + `/readyz`
4. Tests; CHANGELOG/REQUIREMENTS/STATE
