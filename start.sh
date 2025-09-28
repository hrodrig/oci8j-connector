#!/bin/bash

# Startup script for Oracle 8i Connector

echo "🚀 Starting Oracle 8i Connector..."
echo "=================================="

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed"
    exit 1
fi

# Check if the driver JAR is present
if [ ! -f "lib/classes12.jar" ]; then
    echo "❌ Error: lib/classes12.jar not found"
    exit 1
fi

echo "✅ Verifications completed"

# Compile the application
echo "🔨 Compiling application..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "❌ Error: Compilation failed"
    exit 1
fi

echo "✅ Compilation successful"

# Run the application
echo "🏃 Running application..."
echo "🌐 Application will be available at: http://localhost:8080"
echo "📝 Query endpoint: POST /api/v1/oci8j-connector/query"
echo "🔍 Health check: GET /api/v1/oci8j-connector/healthz"
echo "ℹ️  Information: GET /api/v1/oci8j-connector/info"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

mvn spring-boot:run
