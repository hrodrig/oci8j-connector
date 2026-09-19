# Agent Guidelines (oci8j-connector)

- Use **English** for all project artifacts (code, docs, commit messages, UI/API messages).
- Do not commit without first showing the proposed commit message and getting explicit user approval.
- Keep `VERSION`, `pom.xml` `<version>`, README badges, Dockerfile `LABEL version` / JAR path, and image tags synchronized.
- Do **not** commit secrets: real `docker/docker-compose.yml`, credential-filled configs, `.env`. Use `*.example` / env vars only.
- Shared agent policy lives in tracked files (`AGENTS.md`, `SPEC.md`, `README.md`, `CONTRIBUTING.md`, `Makefile`). Do not rely on untracked local agent config as source of truth.
- Prefer **`make`** for all quality gates and Docker ops (`make help`). Windows: Git Bash / WSL Make, or plain `mvn` + Docker CLI.

## Git flow

```
topic branch → PR → develop → PR → main → annotated tag vX.Y.Z → Release workflow → sync main → develop
```

| Rule | Detail |
|------|--------|
| Topic work | Open topic branches from **`develop`**. Merge via **PR into `develop`**. |
| Protected | **Never** commit or push directly to **`develop`** or **`main`**. |
| Release merge | Open **PR `develop` → `main`**, merge on GitHub (never local merge / direct push to `main`). |
| Tag | Annotated tag **`v<semver>`** on **`main` only**, matching root **`VERSION`**. |
| Image | Published **only** by the Release workflow on tag push (not on every `develop` merge). |
| Sync | After every merge into `main`, sync **`main` → `develop`** so the next release PR is not stale. |
| Approval | **Never** merge to **`main`**, create/push a release tag, or trigger a release **without explicit user approval** in the current conversation — even if `make release-check` is green. |

## Scope

| Area | In this repo |
|------|----------------|
| App | Spring Boot 2.7 / Java 8 REST connector (`classes12.jar`) |
| Build | Maven (`pom.xml`), Docker multi-stage (`docker/Dockerfile`) |
| Gates / ops | **`Makefile`** (`test`, `lint`, `package`, `docker-build`, `docker-scan`, `release-check`, compose) |
| Deploy examples | `docker/docker-compose.example.yml`, `kubernetes/k8s-deployment.yaml` |
| CI | `.github/workflows/ci.yml` (PR/push), `.github/workflows/release.yml` (tag `v*`) |

Normative HTTP contract: **[SPEC.md](SPEC.md)**. Narrative how-to: **[README.md](README.md)**.

## Version bump (on `develop`, before merge/tag)

Do the **VERSION bump as a dedicated commit on `develop`** (via PR) after feature/docs work is in, **before** proposing `make release-check` → merge to `main` / annotated tag. Never bump only on `main`. **Stop and ask** before ship steps.

| # | Artifact | Action |
|---|----------|--------|
| 1 | **`VERSION`** | New semver without `v` (e.g. `1.2.9`) |
| 2 | **`pom.xml`** | `<version>` matches `VERSION` |
| 3 | **`README.md`** | Static Version badge + any hard-coded version strings |
| 4 | **`docker/Dockerfile`** | `LABEL version` + JAR copy path if versioned |
| 5 | **`SPEC.md`** | Header “as of **v\<semver\>**” when the contract matches that release |
| 6 | **`CHANGELOG.md`** | Move `[Unreleased]` into `## [<semver>] - YYYY-MM-DD` |
| 7 | **Gate** | `make release-check` — run only after user asks |
| 8 | **Ship** | PR `develop` → `main`, annotated tag `v<semver>`, push tag — **only after user explicitly approves**. Then sync `main` → `develop`. |

**Release publishes:** `ghcr.io/hrodrig/oci8j-connector:v<semver>` (**linux/amd64**), `:latest`, fat JAR + Syft SBOMs on the GitHub Release. Grype runs in `make docker-scan` / `release-check`.

## Make targets (quick)

| Target | Purpose |
|--------|---------|
| `make help` | List targets / current `VERSION` / GHCR image |
| `make test` | `mvn test` (host `mvn`, else container `DOCKER_MVN=1`) |
| `make lint` | VERSION ↔ pom ↔ Dockerfile sync + `mvn validate` |
| `make package` | Fat JAR (`-DskipTests`) |
| `make clean` | `mvn clean` + remove `dist/` |
| `make server` | Build image if missing; `docker compose up --build` (foreground) |
| `make docker-build` | Local image `oci8j-connector:<VERSION>` (**linux/amd64**; runs `mvn test package` in Dockerfile) |
| `make docker-scan` | Build + Grype (`--fail-on critical`; High expected on Temurin 8) |
| `make sbom` | Syft SPDX JSON under `dist/` |
| `make release-check` | lint + test + package + docker-scan |
| `make compose-up` / `compose-down` / `logs` / `health` | Local compose stack (`docker/docker-compose.yml`) |

Force Maven-in-container: `make test DOCKER_MVN=1`. Compose file required for `server` / compose targets (copy from `docker/docker-compose.example.yml`).


## License note

Project license is **MIT**. `lib/classes12.jar` remains under **Oracle** terms and is not re-licensed by this repository.
