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
| `/readyz` | `GET` | Public by default† | **Target §7:** alias of `/ready` (k8s-style name) — **not** shipped in v1.3.1 |
| `/info` | `GET` | Basic Auth if enabled | App/build/git/auth/**endpoints** discovery |

† **v1.3.1 (current):** probes are always public (Basic Auth skipped). **v1.4 §7.2.1:** probes stay public by default but operators may require Basic Auth and/or a probe IP allow-list.

Unknown paths return the same minimal JSON 404 (no Whitelabel HTML).

CORS: controller allows `origins = "*"` (**current** behavior; see §7 for planned allow-list).

When Basic Auth is enabled (v1.3.1), **`/`**, **`/healthz`**, and **`/ready`** remain accessible without credentials. Other routes under the API base path require valid credentials.

**Not in v1.3.1:** OpenAPI/Swagger UI, IP/CIDR allow-list, trusted-proxy / `X-Forwarded-*` client identity, rate limit, security headers, configurable probe access, `/readyz`. Target contract: **§7**.

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

Query keyword blocking (forbidden SQL tokens) exists in application config today (`security.forbidden_keywords` / related YAML) but is **not yet** fully enumerated as a stable env-var contract here; treat as operator-facing YAML until §7 ships a frozen env surface if needed.

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

> **Status:** **Not implemented** in **v1.3.1**. This section is the **normative target** for the next minor (planned **v1.4.x**). Env names and rules below are reserved; shipping them is additive (SemVer minor). Implementation should follow a GSD milestone / phase plan (see **AGENTS.md**), not ad-hoc commits on `main`.

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
| Paths | OpenAPI JSON + Swagger UI under fixed paths (e.g. `/v3/api-docs`, `/swagger-ui.html`) — freeze paths in this section when implemented |
| Auth | Same Basic Auth gate as `/info` when Basic Auth is enabled; if OpenAPI is on and Basic Auth is off, log a **startup warn** |
| Prod safety | OpenAPI **must not** be on by default in production images/compose examples |

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
| Rate limit | Per resolved client IP; configurable requests/window via env (names frozen at implement). Exempt: probe routes (§7.2.1) |
| Exceeded | **429** with minimal JSON |
| CORS | `CORS_ORIGINS` comma list. Empty → current `*` behavior for compatibility **or** fail-closed — **decide at implement** and freeze here (prefer: empty = `*` with startup warn when Basic Auth off) |
| Allow-list CORS | Echo matching `Origin`; omit `Access-Control-Allow-Origin` on mismatch |
| Security headers | On all HTTP responses at least: `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, and a conservative `Permissions-Policy` (no camera/mic/geolocation) |

### 7.4 Env vars reserved for §7 (names stable once shipped)

| Variable | Track | Purpose |
|----------|-------|---------|
| `OPENAPI_ENABLED` | A | `true`/`false` force OpenAPI on/off |
| `TRUSTED_PROXIES` | B | Proxy CIDRs trusted for `X-Forwarded-For` / `X-Real-IP` |
| `ALLOWED_CIDRS` | B | Client IP allow-list for API routes |
| `PROBES_PUBLIC` | B | `true` (default) / `false` — skip Basic Auth on probes when true |
| `PROBES_ALLOWED_CIDRS` | B | Optional IP allow-list for `/healthz`, `/ready`, `/readyz` only |
| `RATE_LIMIT_*` | C | Window/max (exact suffixes frozen at implement) |
| `CORS_ORIGINS` | C | Allowed browser origins |

Until §7 ships, operators must assume: **no** Swagger, **no** IP allow-list, **no** trusted XFF, CORS `*`, no rate limit, no security headers; probes are **always public** (v1.3.1 behavior).
