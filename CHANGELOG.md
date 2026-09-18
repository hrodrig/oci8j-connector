# Changelog

All notable changes to this project are documented in this file.

Format based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
This project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Family docs: `VERSION`, `AGENTS.md`, `SPEC.md`, `docs/README.md`, this `CHANGELOG.md`.
- Windows helper: `oci8jctl.cmd` and `scripts/generate-build-info.cmd` (parity with bash `oci8jctl`).

### Changed

- Replace `Makefile` with `oci8jctl` / `oci8jctl.cmd` for cross-platform Docker builds (Linux/macOS/Windows hosts).
- Relocate ops assets: `docker/Dockerfile`, `docker/docker-compose.example.yml`, `kubernetes/k8s-deployment.yaml`, `scripts/*`.
- Image helpers read semver from `VERSION` first (fallback: `pom.xml`).

### Fixed

- Restore project license to **MIT** (WIP GPL-3 rewrite reverted). Align headers, badge, and `COPYRIGHT_*` docs. Clarify that `classes12.jar` remains under Oracle terms.

## [1.2.8] - 2025-12-14

### Added

- Kubernetes readiness probe endpoint: `GET /api/v1/oci8j-connector/ready`.

### Fixed

- Remove dead code in `Oracle8iConfig`.

## [1.2.7] - 2025-12-14

### Added

- Makefile-based Docker build helpers (`build`, `build-arm64`, `build-amd64`) and env-var priority for Oracle settings.

### Fixed

- Oracle 8i connection path and Docker build (`pom.xml` no longer excluded via `.dockerignore`).

## [1.0.0] - 2025-09-28

### Added

- Initial Spring Boot Oracle 8i connector using `classes12.jar`.
- REST API: `POST /query`, `GET /healthz`, `GET /info`.
- Optional Basic Authentication; Docker and security hardening; CONTRIBUTING / LICENSE.

[Unreleased]: https://github.com/hrodrig/oci8j-connector/compare/v1.2.8...HEAD
[1.2.8]: https://github.com/hrodrig/oci8j-connector/compare/v1.2.7...v1.2.8
[1.2.7]: https://github.com/hrodrig/oci8j-connector/compare/v1.0.0...v1.2.7
[1.0.0]: https://github.com/hrodrig/oci8j-connector/releases/tag/v1.0.0
