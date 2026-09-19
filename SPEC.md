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
| `/healthz` | `GET` | **Always public** | Liveness; **200** even if DB down |
| `/ready` | `GET` | Basic Auth if enabled | Readiness; **200** only if DB connected |
| `/info` | `GET` | Basic Auth if enabled | App/build/git/auth/**endpoints** discovery |

Unknown paths return the same minimal JSON 404 (no Whitelabel HTML).

CORS: controller allows `origins = "*"` (current behavior).

When Basic Auth is enabled, **`/`**, **`/healthz`**, and **`/ready`** remain accessible without credentials. Other routes under the API base path require valid credentials.

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
