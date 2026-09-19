# Phase 2: Probe access

**Goal:** `/readyz` + `PROBES_PUBLIC` + `PROBES_ALLOWED_CIDRS`  
**Requirements:** PROB-01..05  
**Process:** Atomic commits; PR only after full milestone local gates.

## Success criteria

- [ ] `/readyz` same semantics as `/ready`
- [ ] Default `PROBES_PUBLIC=true` — skip Basic Auth on probes
- [ ] `PROBES_PUBLIC=false` + Basic Auth on → probes need credentials
- [ ] `PROBES_ALLOWED_CIDRS` gates probes only; independent of `ALLOWED_CIDRS`
- [ ] Empty probe CIDR → no probe IP filter
- [ ] Outside probe CIDR → 403 JSON
- [ ] Probes still exempt from API `ALLOWED_CIDRS`
- [ ] Unit tests + `make test` green

## Tasks

1. PLAN + `/readyz` controller + `/info` discovery
2. `PROBES_*` on `EdgeAccessConfig`; probe IP logic in `IpAllowListFilter`
3. `BasicAuthFilter` respects `PROBES_PUBLIC` + `/readyz`
4. Tests; CHANGELOG/REQUIREMENTS/STATE
