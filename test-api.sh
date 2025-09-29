#!/bin/bash

# Script to test Oracle 8i Connector API

BASE_URL="http://localhost:8080"

# Basic Auth credentials (if enabled)
BASIC_AUTH_USERNAME="admin"
BASIC_AUTH_PASSWORD="secretpassword"

# Function to make API calls with optional Basic Auth
api_call() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local auth_header=""
    
    # Check if Basic Auth is enabled by testing the info endpoint
    local auth_status=$(curl -s "$BASE_URL/api/v1/oci8j-connector/info" | jq -r '.authentication.enabled // false' 2>/dev/null)
    
    if [ "$auth_status" = "true" ]; then
        auth_header="-u $BASIC_AUTH_USERNAME:$BASIC_AUTH_PASSWORD"
        echo "🔐 Using Basic Authentication"
    else
        echo "🔓 No authentication required"
    fi
    
    if [ -n "$data" ]; then
        curl -s -X "$method" $auth_header "$BASE_URL$endpoint" \
             -H "Content-Type: application/json" \
             -d "$data"
    else
        curl -s -X "$method" $auth_header "$BASE_URL$endpoint"
    fi
}

echo "🧪 Testing Oracle 8i Connector API"
echo "=================================="

# Check if the application is running
echo "1. Checking application status..."
api_call "GET" "/api/v1/oci8j-connector/healthz" | jq '.' || echo "❌ Error: Application is not running at $BASE_URL"

echo -e "\n2. Testing readiness check..."
api_call "GET" "/api/v1/oci8j-connector/ready" | jq '.' || echo "❌ Error: Readiness check failed"

echo -e "\n3. Getting application information..."
api_call "GET" "/api/v1/oci8j-connector/info" | jq '.' || echo "❌ Error: Could not get information"

echo -e "\n4. Testing simple query..."
api_call "POST" "/api/v1/oci8j-connector/query" '{"query": "SELECT 1 as test_value FROM DUAL"}' | jq '.' || echo "❌ Error: Could not execute simple query"

echo -e "\n5. Testing query with parameters..."
api_call "POST" "/api/v1/oci8j-connector/query" '{"query": "SELECT ? as param_value FROM DUAL", "parameters": ["Hello Oracle 8i"]}' | jq '.' || echo "❌ Error: Could not execute query with parameters"

echo -e "\n6. Testing system information query..."
api_call "POST" "/api/v1/oci8j-connector/query" '{"query": "SELECT USER as current_user, SYSDATE as current_date FROM DUAL"}' | jq '.' || echo "❌ Error: Could not execute system query"

echo -e "\n7. Testing INSERT query..."
api_call "POST" "/api/v1/oci8j-connector/query" '{"query": "INSERT INTO emp (empno, ename, job, deptno) VALUES (?, ?, ?, ?)", "parameters": [9999, "TEST", "CLERK", 10]}' | jq '.' || echo "❌ Error: Could not execute INSERT query"

echo -e "\n8. Testing UPDATE query..."
api_call "POST" "/api/v1/oci8j-connector/query" '{"query": "UPDATE emp SET sal = ? WHERE empno = ?", "parameters": [5000, 9999]}' | jq '.' || echo "❌ Error: Could not execute UPDATE query"

echo -e "\n9. Testing DELETE query..."
api_call "POST" "/api/v1/oci8j-connector/query" '{"query": "DELETE FROM emp WHERE empno = ?", "parameters": [9999]}' | jq '.' || echo "❌ Error: Could not execute DELETE query"

echo -e "\n✅ Tests completed"
