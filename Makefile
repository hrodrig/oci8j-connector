.PHONY: build build-arm64 build-amd64 generate-build-info up down logs health query help

# Get version from pom.xml or use VERSION env variable
VERSION ?= $(shell grep -m1 '<version>' pom.xml | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')

help: ## Show this help message
	@echo "Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "Current version: $(VERSION)"
	@echo "Override with: make build VERSION=x.y.z"

build: ## Build Docker image (uses host platform)
	@echo "🐳 Building oci8j-connector image v$(VERSION) (host platform)..."
	docker build --no-cache -t oci8j-connector:$(VERSION) -t oci8j-connector:latest .
	@echo "✅ Image built successfully: oci8j-connector:$(VERSION) and oci8j-connector:latest"

build-arm64: ## Build Docker image for linux/arm64 platform
	@echo "🐳 Building oci8j-connector image v$(VERSION) (linux/arm64)..."
	docker build --no-cache --platform linux/arm64 -t oci8j-connector:$(VERSION)-arm64 -t oci8j-connector:latest-arm64 .
	@echo "✅ Image built successfully: oci8j-connector:$(VERSION)-arm64 and oci8j-connector:latest-arm64"

build-amd64: ## Build Docker image for linux/amd64 platform
	@echo "🐳 Building oci8j-connector image v$(VERSION) (linux/amd64)..."
	docker build --no-cache --platform linux/amd64 -t oci8j-connector:$(VERSION)-amd64 -t oci8j-connector:latest-amd64 .
	@echo "✅ Image built successfully: oci8j-connector:$(VERSION)-amd64 and oci8j-connector:latest-amd64"

generate-build-info: ## Generate build-info.properties file
	@echo "🔧 Generating build information..."
	@chmod +x generate-build-info.sh
	@./generate-build-info.sh

up: ## Start the service
	@echo "🚀 Starting oci8j-connector..."
	docker compose -f docker-compose.yml up -d

down: ## Stop the service
	@echo "🛑 Stopping oci8j-connector..."
	docker compose -f docker-compose.yml down

logs: ## View service logs
	docker compose -f docker-compose.yml logs -f

health: ## Check health status
	@echo "🏥 Checking health status..."
	@curl -f http://localhost:8080/api/v1/oci8j-connector/healthz || echo "❌ Health check failed"

query: ## Execute test query (requires query argument: make query QUERY="SELECT 1 FROM DUAL")
	@if [ -z "$(QUERY)" ]; then \
		echo "❌ Error: You must provide a query"; \
		echo "   Usage: make query QUERY=\"SELECT 1 FROM DUAL\""; \
		exit 1; \
	fi
	@echo "📊 Executing query: $(QUERY)"
	@curl -X POST http://localhost:8080/api/v1/oci8j-connector/query \
		-H "Content-Type: application/json" \
		-d "{\"query\": \"$(QUERY)\"}" | jq .

