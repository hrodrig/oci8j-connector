# oci8j-connector — build, quality and release workflow

IMAGE_NAME  := oci8j-connector
GHCR_IMAGE  := ghcr.io/hrodrig/oci8j-connector
DIST        := dist
DOCKERFILE  := docker/Dockerfile
COMPOSE_FILE := docker/docker-compose.yml

# Single source of truth: VERSION file at repo root.
VERSION_RAW ?= $(shell cat VERSION 2>/dev/null | tr -d '\n\r')
VERSION     := $(patsubst v%,%,$(VERSION_RAW))
TAG         := v$(VERSION)
COMMIT      := $(shell git rev-parse --short HEAD 2>/dev/null || echo unknown)
BUILDDATE   := $(shell date -u +%Y-%m-%dT%H:%M:%SZ)

# Release / local image builds are linux/amd64 only (plan decision).
DOCKER_PLATFORM ?= linux/amd64
GRYPE_FAIL_ON   ?= high
GRYPE_DIR_EXCLUDES ?= --exclude './target/**' --exclude './dist/**' --exclude './.git/**'

check-docker = @docker info >/dev/null 2>&1 || { echo "Error: Docker is not running. Start Docker and try again."; exit 1; }

.DEFAULT_GOAL := help

.PHONY: help clean test package lint docker-build docker-scan security release-check \
	compose-up compose-down logs health sbom

GREEN  := \033[0;32m
YELLOW := \033[0;33m
RESET  := \033[0m

help:
	@echo "$(GREEN)oci8j-connector$(RESET) — Oracle 8i REST API"
	@echo ""
	@echo "Usage: make [target]"
	@echo ""
	@echo "$(YELLOW)Build:$(RESET)"
	@echo "  $(GREEN)package$(RESET)           Maven package (skip tests)"
	@echo "  $(GREEN)clean$(RESET)             Maven clean + remove dist/"
	@echo ""
	@echo "$(YELLOW)Quality:$(RESET)"
	@echo "  $(GREEN)test$(RESET)              Maven tests"
	@echo "  $(GREEN)lint$(RESET)              VERSION/pom/Dockerfile sync + mvn validate"
	@echo "  $(GREEN)security$(RESET)          Directory grype + docker-scan"
	@echo "  $(GREEN)release-check$(RESET)     lint, test, package, docker-scan (requires Docker)"
	@echo ""
	@echo "$(YELLOW)Docker:$(RESET)"
	@echo "  $(GREEN)docker-build$(RESET)      Build $(IMAGE_NAME):$(VERSION) (platform $(DOCKER_PLATFORM))"
	@echo "  $(GREEN)docker-scan$(RESET)       Build and scan image with Grype"
	@echo "  $(GREEN)sbom$(RESET)              Generate Syft SBOM for local image into dist/"
	@echo "  $(GREEN)compose-up$(RESET)        Start stack (docker/docker-compose.yml)"
	@echo "  $(GREEN)compose-down$(RESET)      Stop stack"
	@echo "  $(GREEN)logs$(RESET)              Follow compose logs"
	@echo "  $(GREEN)health$(RESET)            Hit /healthz"
	@echo ""
	@echo "Current version: $(VERSION) (tag: $(TAG))"
	@echo "GHCR image: $(GHCR_IMAGE):$(TAG) (linux/amd64)"
	@echo ""
	@echo "Examples:"
	@echo "  make release-check"
	@echo "  make docker-build"

clean:
	mvn -B clean
	rm -rf $(DIST)

test:
	mvn -B test

package:
	mvn -B -DskipTests package

lint:
	@test -f VERSION || (echo "VERSION file is required"; exit 1)
	@echo "$(VERSION)" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$$' || (echo "VERSION must be semver (e.g. 1.2.8)"; exit 1)
	@pom_ver=$$(grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' '); \
	  test "$$pom_ver" = "$(VERSION)" || (echo "pom.xml version ($$pom_ver) != VERSION ($(VERSION))"; exit 1)
	@grep -q 'LABEL version="$(VERSION)"' $(DOCKERFILE) || (echo "Dockerfile LABEL version must be $(VERSION)"; exit 1)
	@grep -q "oracle8i-connector-$(VERSION).jar" $(DOCKERFILE) || (echo "Dockerfile JAR copy must use oracle8i-connector-$(VERSION).jar"; exit 1)
	mvn -B -DskipTests validate
	@echo "lint OK (VERSION=$(VERSION))"

docker-build:
	$(check-docker)
	DOCKER_BUILDKIT=1 docker build \
		--platform $(DOCKER_PLATFORM) \
		-f $(DOCKERFILE) \
		-t $(IMAGE_NAME):$(VERSION) \
		-t $(IMAGE_NAME):latest \
		.

docker-scan: docker-build
	@if command -v grype >/dev/null 2>&1; then \
		grype $(IMAGE_NAME):$(VERSION) --fail-on $(GRYPE_FAIL_ON) ; \
	else \
		echo "grype not found locally, using container image..."; \
		docker run --rm --pull=always -v /var/run/docker.sock:/var/run/docker.sock \
			anchore/grype:latest $(IMAGE_NAME):$(VERSION) --fail-on $(GRYPE_FAIL_ON) ; \
	fi

security: docker-scan
	@if command -v grype >/dev/null 2>&1; then \
		grype dir:. $(GRYPE_DIR_EXCLUDES) ; \
	else \
		echo "grype not found locally, using container image..."; \
		docker run --rm --pull=always -v "$(CURDIR):/workspace" -w /workspace \
			anchore/grype:latest dir:. $(GRYPE_DIR_EXCLUDES) ; \
	fi

sbom: docker-build
	@mkdir -p $(DIST)
	@if command -v syft >/dev/null 2>&1; then \
		syft $(IMAGE_NAME):$(VERSION) -o spdx-json > $(DIST)/$(IMAGE_NAME)-$(VERSION).spdx.json ; \
	else \
		echo "syft not found locally, using container image..."; \
		docker run --rm --pull=always -v /var/run/docker.sock:/var/run/docker.sock \
			-v "$(CURDIR)/$(DIST):/out" \
			anchore/syft:latest $(IMAGE_NAME):$(VERSION) -o spdx-json=/out/$(IMAGE_NAME)-$(VERSION).spdx.json ; \
	fi
	@echo "Wrote $(DIST)/$(IMAGE_NAME)-$(VERSION).spdx.json"

release-check:
	$(check-docker)
	@test -f VERSION || (echo "VERSION file is required"; exit 1)
	@echo "Release version: $(VERSION) (tag: $(TAG))"
	@echo "$(VERSION)" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$$' || (echo "VERSION must be semantic version"; exit 1)
	@echo "Running release checks (lint, test, package, docker-scan)..."
	@$(MAKE) lint
	@$(MAKE) test
	@$(MAKE) package
	@$(MAKE) docker-scan
	@echo "release-check OK"

compose-up:
	$(check-docker)
	docker compose -f $(COMPOSE_FILE) up -d

compose-down:
	$(check-docker)
	docker compose -f $(COMPOSE_FILE) down

logs:
	docker compose -f $(COMPOSE_FILE) logs -f

health:
	@curl -fsS http://localhost:8080/api/v1/oci8j-connector/healthz || (echo "Health check failed"; exit 1)
