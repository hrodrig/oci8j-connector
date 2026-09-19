# Phase 3: Rate limit, CORS, security headers

**Goal:** HARD-01..03 per SPEC §7.3  
**Freeze:** `RATE_LIMIT_MAX` (0=off), `RATE_LIMIT_WINDOW_SECONDS` (default 60), `CORS_ORIGINS` (empty=`*`)

## Success criteria

- [ ] Per resolved-IP rate limit; probes exempt; 429 JSON
- [ ] CORS_ORIGINS allow-list; empty = `*` + warn if Basic Auth off
- [ ] Security headers on responses
- [ ] SPEC §7.3/§7.4 env names frozen
- [ ] Tests + `make test` green
