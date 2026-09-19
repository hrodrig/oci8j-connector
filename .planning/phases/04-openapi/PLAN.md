# Phase 4: OpenAPI gated

**Goal:** OAPI-01..05 — springdoc off by default; Basic Auth + allow-list when on.

## Success criteria

- [ ] springdoc-openapi-ui on Boot 2.7 / Java 8
- [ ] Off by default; on via `openapi.enabled` / `OPENAPI_ENABLED` or profile `dev`/`local`; off on `prod` unless forced true
- [ ] Paths: `/v3/api-docs`, `/swagger-ui.html` (springdoc defaults)
- [ ] Basic Auth + ALLOWED_CIDRS cover OpenAPI paths when those gates apply
- [ ] Startup warn if OpenAPI on and Basic Auth off
- [ ] `make test` green
