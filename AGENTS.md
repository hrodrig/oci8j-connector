# Agent Guidelines (oci8j-connector)

- Use **English** for all project artifacts (code, docs, commit messages, UI/API messages).
- Follow git flow: work on **topic branches** (opened from `develop`, merged to `develop` via PR); releases from `main`. **Never commit directly to `develop` or `main`.**
- **Never** merge to **`main`**, create/push a release tag, or publish a release image **without explicit user approval in the current conversation.** Ask first.
- Do not commit without first showing the proposed commit message and getting explicit user approval.
- Prefer **`oci8jctl`** (Linux/macOS) or **`oci8jctl.cmd`** (Windows) for Docker image builds and compose ops. Maven (`mvn clean package`) is the portable no-Docker path on both platforms.
- Keep `VERSION`, `pom.xml` `<version>`, README badges, Dockerfile `LABEL version`, and image tags synchronized.
- Do **not** commit secrets: real `docker/docker-compose.yml`, credential-filled configs, `.env`. Use `*.example` / env vars only.
- Shared agent policy lives in tracked files (`AGENTS.md`, `SPEC.md`, `README.md`, `CONTRIBUTING.md`). Do not rely on untracked local agent config as source of truth.

## Scope

| Area | In this repo |
|------|----------------|
| App | Spring Boot 2.7 / Java 8 REST connector (`classes12.jar`) |
| Build | Maven (`pom.xml`), Docker multi-stage (`docker/Dockerfile`) |
| Ops helpers | `oci8jctl` / `oci8jctl.cmd`, `scripts/` |
| Deploy examples | `docker/docker-compose.example.yml`, `kubernetes/k8s-deployment.yaml` |

Normative HTTP contract: **[SPEC.md](SPEC.md)**. Narrative how-to: **[README.md](README.md)**.

## Version bump (on `develop`, before merge/tag)

Do the **VERSION bump as a dedicated commit on `develop`** after feature/docs work is in, **before** proposing merge to `main` / annotated tag. Never bump only on `main`. **Stop and ask** before ship steps.

| # | Artifact | Action |
|---|----------|--------|
| 1 | **`VERSION`** | New semver without `v` (e.g. `1.2.9`) |
| 2 | **`pom.xml`** | `<version>` matches `VERSION` |
| 3 | **`README.md`** | Static Version badge + any hard-coded version strings |
| 4 | **`docker/Dockerfile`** | `LABEL version` + JAR copy path if versioned |
| 5 | **`SPEC.md`** | Header “as of **v\<semver\>**” when the contract matches that release |
| 6 | **`CHANGELOG.md`** | Move `[Unreleased]` into `## [<semver>] - YYYY-MM-DD` |
| 7 | **Gate** | `mvn clean package` (+ Docker build via `oci8jctl` / `oci8jctl.cmd` when shipping images) — run only after user asks |
| 8 | **Ship** | PR `develop` → `main`, annotated tag `v<semver>`, push tag — **only after user explicitly approves** |

## Cross-platform builds

| Host | Helper | Notes |
|------|--------|-------|
| Linux / macOS | `./oci8jctl <cmd>` | Bash; `chmod +x` once |
| Windows | `oci8jctl.cmd <cmd>` | cmd.exe / PowerShell |
| Any (no Docker) | `mvn clean package` | Requires JDK 8+ and Maven |

Image builds always produce **Linux** container images (`linux/amd64`, `linux/arm64`) regardless of host OS.
