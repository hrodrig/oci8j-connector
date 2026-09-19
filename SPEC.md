# Spec — HTTP API and ops contract

Normative operator contracts for **oci8j-connector** as of **v1.3.1**.  
Narrative install/env: **[README.md](README.md)**. Agent / release rules: **[AGENTS.md](AGENTS.md)**.

> **Stability:** Documented routes, JSON fields, and environment variable names are **stable** within the 1.x line — no rename/removal without a SemVer major bump. **Additive** fields, routes, and env vars are allowed in minor releases.

This document describes **current** behavior. Client-breaking changes must bump SemVer and update this file + CHANGELOG.

---

## 1. Deployment model

| Constraint | Rule |
|------------|------|
| Runtime | Java 8+; Spring Boot **2.7.18** fat JAR |
| Oracle driver | `lib/classes12.jar` (system-scoped Maven dependency) |
| Config | YAML (`config.yaml` / packaged resources) overridden by env vars |
| Auth | Optional HTTP Basic Auth (`BASIC_AUTH_*` / `basic_auth.*`) |
| Container | Multi-stage Eclipse Temurin 8 image; non-root `appuser`; port **8080** |
| Host builds | **`make`** (test/lint/package/docker-*); plain `mvn` also supported |
| Published image | `ghcr.io/hrodrig/oci8j-connector:v<VERSION>` — **linux/amd64 only** |

---

## 2. HTTP surface overview

Base path: **`/api/v1/oci8j-connector`**

| Route | Method | Auth | Notes |
|-------|--------|------|--------|
| `/` | `GET` | **Always public** | Minimal **404** JSON: `{"code":404,"message":"Not found"}` |
| `/query` | `POST` | Basic Auth if enabled | Execute SQL; JSON body |
| `/healthz` | `GET` | Public by default† | Liveness; **200** even if DB down |
| `/ready` | `GET` | Public by default† | Readiness; **200** only if DB connected |
| `/readyz` | `GET` | Public by default† | Alias of `/ready` (k8s-style) |
| `/info` | `GET` | Basic Auth if enabled | App/build/git/auth/**endpoints** discovery |
| `/v3/api-docs` | `GET` | Same as `/info` when docs on | OpenAPI JSON (gated; §7.1) |
| `/swagger-ui.html` | `GET` | Same as `/info` when docs on | Swagger UI (gated; §7.1) |

† Probes default public (`edge.probes_public` / `PROBES_PUBLIC=true`). May require Basic Auth and/or `edge.probes_allowed_cidrs`.

Unknown paths return the same minimal JSON 404 (no Whitelabel HTML).

CORS: `hardening.cors_origins` / `CORS_ORIGINS` (empty = `*`). See §7.3.

When Basic Auth is enabled, **`/`** stays public; probes follow `PROBES_PUBLIC`; other API and OpenAPI routes require credentials.

§7 A/B/C implemented on the **v1.4.0** line (this branch); released **v1.3.1** did not ship these gates.

---

## 3. API contracts

### 3.0 `GET /`

- **404** `application/json` → `{"code":404,"message":"Not found"}`
- Always public. API discovery: **`GET /api/v1/oci8j-connector/info`**.

### 3.0.1 Unknown paths

- Same minimal **404** JSON via `/error` (no Whitelabel HTML).

### 3.1 `POST /api/v1/oci8j-connector/query`

**Request** (`application/json`):

```json
{
  "query": "SELECT * FROM emp WHERE deptno = ?",
  "parameters": [10]
}
```

| Field | Type | Required | Notes |
|-------|------|----------|--------|
| `query` | string | yes | SQL statement |
| `parameters` | array | no | Positional bind values |

**Success (SELECT)** — **200**:

```json
{
  "success": true,
  "message": "...",
  "data": [ { "...": "..." } ],
  "rowCount": 1,
  "columns": ["..."]
}
```

**Success (DML)** — **200**: same envelope with affected-row semantics in `rowCount` / message.

**Client / SQL error** — **400** with `success: false` and `message`.  
**Unexpected failure** — **500** with `success: false`.

### 3.2 `GET /api/v1/oci8j-connector/healthz`

- **200** always if the process is up (Kubernetes liveness).
- Body includes at least: `status` (`UP`), `database` (`CONNECTED`|`DISCONNECTED`), `timestamp`, `uptime`, `version`, `build`.

### 3.3 `GET /api/v1/oci8j-connector/ready`

- **200** when Oracle is reachable (`status: READY`, `database: CONNECTED`).
- **503** when DB unreachable (`status: NOT_READY`, `database: DISCONNECTED`).

### 3.4 `GET /api/v1/oci8j-connector/info`

- **200** JSON with `name`, `version`, `description`, `build`, `git`, `authentication`, `endpoints`.

---

## 4. Configuration (env vars)

Env vars override YAML defaults.

| Variable | Purpose |
|----------|---------|
| `ORACLE_HOST` | DB host |
| `ORACLE_PORT` | DB port (default `1521`) |
| `ORACLE_SID` | Oracle SID |
| `ORACLE_USERNAME` | DB user |
| `ORACLE_PASSWORD` | DB password |
| `ORACLE_QUERY_TIMEOUT` | Query timeout (ms) |
| `BASIC_AUTH_ENABLED` | `true`/`false` |
| `BASIC_AUTH_USERNAME` | Basic Auth user |
| `BASIC_AUTH_PASSWORD` | Basic Auth password |

Sensitive values must come from env / secrets managers — never commit real compose credentials.

Query keyword blocking: `security.forbidden_keywords` in YAML.

**v1.4 YAML nesting** (see `config.example.yaml`): `edge.trusted_proxies`, `edge.allowed_cidrs`, `edge.probes_public`, `edge.probes_allowed_cidrs`, `hardening.rate_limit_max`, `hardening.rate_limit_window_seconds`, `hardening.cors_origins`, `openapi.enabled`. Env names in §7.4 still override via placeholders in packaged `config.yaml`.

---

## 5. Build and release artifacts

| Artifact | Source of truth |
|----------|-----------------|
| Semver string | **`VERSION`** (no `v` prefix) |
| Maven coordinates | `pom.xml` `<version>` must match `VERSION` |
| Local image tags | `oci8j-connector:<VERSION>`, `oci8j-connector:latest` via `make docker-build` |
| GHCR image | `ghcr.io/hrodrig/oci8j-connector:v<VERSION>` and `:latest` — **linux/amd64 only** (Release workflow on tag `v*`) |
| Fat JAR | `target/oracle8i-connector-<VERSION>.jar` (attached to GitHub Release) |
| SBOM | Syft SPDX JSON under `dist/` (attached to GitHub Release) |
| Quality gate | `make release-check` = lint + test + package + docker-scan (Grype `--fail-on critical`) |

**Note:** Runtime image is **Eclipse Temurin 8** (required for `classes12.jar` / Oracle 8i). Java 8 base images typically report **High** OS/JRE CVEs; the release gate fails only on **Critical**. Override locally: `make docker-scan GRYPE_FAIL_ON=high`.


Git flow and bump checklist: **[AGENTS.md](AGENTS.md)**.

---

## 6. Probes (Kubernetes)

| Probe | Path | Expect |
|-------|------|--------|
| Liveness | `GET /api/v1/oci8j-connector/healthz` | **200** |
| Readiness | `GET /api/v1/oci8j-connector/ready` | **200** |

Example manifest: `kubernetes/k8s-deployment.yaml`.

---

## 7. Target: OpenAPI, edge identity, and hardening (A/B/C)

> **Status:** Implemented on the **v1.4.0** line (unreleased until VERSION bump). Normative for operators once **1.4.0** is tagged. Env/YAML names below are frozen.

Scope locked by operator decision **A + B + C**:

| Track | Scope |
|-------|--------|
| **A** | OpenAPI / Swagger UI, gated |
| **B** | IP/CIDR allow-list + trusted proxies + client IP from forwarded headers |
| **C** | Rate limit + CORS allow-list + baseline security headers |

### 7.1 OpenAPI / Swagger (A)

| Rule | Detail |
|------|--------|
| Stack | springdoc-openapi compatible with Spring Boot **2.7** / Java **8** |
| Default | **Off** when `SPRING_PROFILES_ACTIVE` contains `prod` **or** when unset and not explicitly enabled |
| Enable | `OPENAPI_ENABLED=true` **or** profile `dev` / `local` (exact profile names frozen at implement time; document in §4 when shipped) |
| Paths | OpenAPI JSON **`/v3/api-docs`** + Swagger UI **`/swagger-ui.html`** (springdoc 1.7 defaults) |
| Auth | Same Basic Auth gate as `/info` when Basic Auth is enabled; if OpenAPI is on and Basic Auth is off, log a **startup warn** |
| Prod safety | OpenAPI **must not** be on by default in production images/compose examples |
| Gate | `OpenApiEnvironmentPostProcessor`: off by default; on if `openapi.enabled`/`OPENAPI_ENABLED=true` or profile `dev`/`local`; `prod` off unless forced true |

### 7.2 Client IP, trusted proxies, allow-list (B)

| Rule | Detail |
|------|--------|
| Peer | TCP remote address is always known (`RemoteAddr`) |
| `TRUSTED_PROXIES` | Comma-separated CIDRs/IPs of reverse proxies. **Empty (default) = trust no proxy headers** |
| Forwarded headers | Only if peer ∈ `TRUSTED_PROXIES`: honor `X-Forwarded-For` (left-most client hop after stripping trusted proxies) and/or `X-Real-IP` |
| Non-standard | Do **not** trust `X-Remote-IP` / similar unless explicitly added later to this SPEC |
| Untrusted peer | Ignore all forwarded client headers; identity = peer `RemoteAddr` |
| `ALLOWED_CIDRS` | Comma-separated CIDRs for **API** routes (`/query`, `/info`, OpenAPI when on, etc.). **Empty (default) = no API IP allow-list** |
| Allow-list check | Uses **resolved client IP** (after trusted-proxy rules) |
| Deny (API allow-list) | **403** JSON minimal body when `ALLOWED_CIDRS` is set and client IP is outside it |
| `/` | Remains public 404; not subject to API allow-list |

### 7.2.1 Probe access: `/healthz`, `/ready`, `/readyz`

Paths: existing **`/api/v1/oci8j-connector/healthz`** and **`/ready`**; add **`/readyz`** as an identical readiness alias (same handlers/status semantics as `/ready`).

| Rule | Detail |
|------|--------|
| Default | Probes are **public**: no Basic Auth; not gated by `ALLOWED_CIDRS` |
| `PROBES_PUBLIC` | `true` (default) / `false`. When `false` **and** Basic Auth is enabled, probes require valid Basic Auth (same credentials as API). When Basic Auth is disabled, `PROBES_PUBLIC=false` has no auth effect (still open unless IP allow-list applies) |
| `PROBES_ALLOWED_CIDRS` | Comma-separated CIDRs. **Empty (default) = no probe-specific IP filter**. When set, only resolved client IPs in this list may call probe routes; others get **403** |
| Interaction with `ALLOWED_CIDRS` | Probe routes use **`PROBES_ALLOWED_CIDRS` only** (not the API `ALLOWED_CIDRS`), so cluster kubelet/probe sources can be allow-listed without opening `/query` |
| Rate limit | Probes remain exempt from rate limit (§7.3) unless a future flag opts them in |
| Deny body | Minimal JSON, e.g. `{"code":403,"message":"Forbidden"}` |

### 7.3 Rate limit, CORS, security headers (C)

| Rule | Detail |
|------|--------|
| Rate limit | Per resolved client IP. `RATE_LIMIT_MAX` (default **0** = off), `RATE_LIMIT_WINDOW_SECONDS` (default **60**). Exempt: probe routes (§7.2.1) |
| Exceeded | **429** with `{"code":429,"message":"Too Many Requests"}` |
| CORS | `CORS_ORIGINS` comma list. **Empty (default) = `*`** with startup **warn** when Basic Auth is off |
| Allow-list CORS | Echo matching `Origin` via Spring `CorsFilter`; mismatch → no allow-origin |
| Security headers | On all HTTP responses: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, `Permissions-Policy: camera=(), microphone=(), geolocation=()` |

### 7.4 Env vars reserved for §7 (names stable once shipped)

| Variable | Track | Purpose |
|----------|-------|---------|
| `OPENAPI_ENABLED` | A | `true`/`false` force OpenAPI on/off |
| `TRUSTED_PROXIES` | B | Proxy CIDRs trusted for `X-Forwarded-For` / `X-Real-IP` |
| `ALLOWED_CIDRS` | B | Client IP allow-list for API routes |
| `PROBES_PUBLIC` | B | `true` (default) / `false` — skip Basic Auth on probes when true |
| `PROBES_ALLOWED_CIDRS` | B | Optional IP allow-list for `/healthz`, `/ready`, `/readyz` only |
| `RATE_LIMIT_MAX` | C | Max requests per window per IP; **0** = disabled |
| `RATE_LIMIT_WINDOW_SECONDS` | C | Window length (default **60**) |
| `CORS_ORIGINS` | C | Allowed browser origins CSV; empty = `*` |

Until **1.4.0** is tagged, released binaries remain **v1.3.1** behavior for operators who have not upgraded.
