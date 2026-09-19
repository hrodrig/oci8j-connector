# Changelog

All notable changes to this project are documented in this file.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
This project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.4.0] - 2026-09-19

### Security

- Override managed Tomcat **9.0.122** and Spring Framework **5.3.39** on Boot 2.7.18 (clear Tomcat Criticals in Grype).
- Document Grype ignore for **GHSA-4wrc-f8pq-fpqp** (`spring-web`): fix requires Spring Framework 6 / Java 17; stack stays Boot 2.7 / Java 8 (`.grype.yaml`).

### Added

- Edge identity: `edge.trusted_proxies` / `TRUSTED_PROXIES` + resolved client IP; `edge.allowed_cidrs` / `ALLOWED_CIDRS` API allow-list with **403** JSON.
- Probe access: `/readyz` alias; `edge.probes_public` / `PROBES_PUBLIC` (default true); `edge.probes_allowed_cidrs` independent of API list.
- Hardening: `hardening.rate_limit_max` / `RATE_LIMIT_MAX` (0=off), window seconds, `hardening.cors_origins` / `CORS_ORIGINS`, baseline security headers.
- OpenAPI: springdoc-openapi-ui **1.7** gated (`openapi.enabled` / `OPENAPI_ENABLED` or profile `dev`|`local`); paths `/v3/api-docs`, `/swagger-ui.html`.
- Nested YAML under `edge` / `hardening` / `openapi` in `config.example.yaml` (env placeholders in packaged `config.yaml`).

### Documentation

- Compose examples pull **`ghcr.io/hrodrig/oci8j-connector:v1.4.0`** by default (`OCI8J_IMAGE` override); `make server` / `compose-up` run `docker compose pull` instead of `--build`.
- Docker TLS edge examples: Traefik v3 (**Let's Encrypt** + file certs), Caddy 2, and `nginxinc/nginx-unprivileged` under `docker/tls/` (app HTTP; `TRUSTED_PROXIES` for XFF).
- README opens with **problem → solution** (Oracle 8i / `classes12` gap vs REST bridge).
- SPEC §7 A/B/C frozen and marked shipped for **v1.4.0**; README env/YAML section for edge/probes/hardening/OpenAPI.
- Compose example documents §7 environment variables (defaults off / public probes).

## [1.3.1] - 2026-09-19

### Fixed

- Release Grype gate: `--fail-on critical` (not `high`). Temurin 8 / Java 8 images cannot stay High-clean; scan still runs and reports High.

### Documentation

- Explicit README disclaimer: Java 8 / Temurin CVE posture, Oracle `classes12.jar` license, SQL-over-HTTP risk, and operator responsibilities (plus top-of-README warning callout).
- README hero image: `assets/oci8j-connector-hero.png`.
- README table of contents.

## [1.3.0] - 2026-09-18

### Added

- Family **Makefile** gates: `lint`, `test`, `package`, `docker-build`, `docker-scan`, `sbom`, `release-check`, compose helpers, `server`.
- GitHub Actions: `.github/workflows/ci.yml` (PR/push) and `release.yml` (tag `v*` → GHCR **linux/amd64** + JAR + Syft SBOMs).
- Gitflow docs in **AGENTS.md**; deprecate/remove `oci8jctl`.

[Unreleased]: https://github.com/hrodrig/oci8j-connector/compare/v1.4.0...HEAD
[1.4.0]: https://github.com/hrodrig/oci8j-connector/compare/v1.3.1...v1.4.0
[1.3.1]: https://github.com/hrodrig/oci8j-connector/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/hrodrig/oci8j-connector/compare/v1.2.8...v1.3.0
[1.2.8]: https://github.com/hrodrig/oci8j-connector/compare/v1.2.7...v1.2.8
[1.2.7]: https://github.com/hrodrig/oci8j-connector/compare/v1.0.0...v1.2.7
[1.0.0]: https://github.com/hrodrig/oci8j-connector/releases/tag/v1.0.0
