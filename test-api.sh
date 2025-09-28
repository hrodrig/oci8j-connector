#!/bin/bash

# Script to test Oracle 8i Connector API

BASE_URL="http://localhost:8080"

echo "🧪 Testing Oracle 8i Connector API"
echo "=================================="

# Check if the application is running
echo "1. Checking application status..."
curl -s "$BASE_URL/api/v1/oci8j-connector/healthz" | jq '.' || echo "❌ Error: Application is not running at $BASE_URL"

echo -e "\n2. Testing readiness check..."
curl -s "$BASE_URL/api/v1/oci8j-connector/ready" | jq '.' || echo "❌ Error: Readiness check failed"

echo -e "\n3. Getting application information..."
curl -s "$BASE_URL/api/v1/oci8j-connector/info" | jq '.' || echo "❌ Error: Could not get information"

echo -e "\n3. Testing simple query..."
curl -s -X POST "$BASE_URL/api/v1/oci8j-connector/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT 1 as test_value FROM DUAL"}' | jq '.' || echo "❌ Error: Could not execute simple query"

echo -e "\n4. Testing query with parameters..."
curl -s -X POST "$BASE_URL/api/v1/oci8j-connector/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT ? as param_value FROM DUAL", "parameters": ["Hello Oracle 8i"]}' | jq '.' || echo "❌ Error: Could not execute query with parameters"

echo -e "\n5. Testing system information query..."
curl -s -X POST "$BASE_URL/api/v1/oci8j-connector/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT USER as current_user, SYSDATE as current_date FROM DUAL"}' | jq '.' || echo "❌ Error: Could not execute system query"

echo -e "\n6. Testing INSERT query..."
curl -s -X POST "$BASE_URL/api/v1/oci8j-connector/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "INSERT INTO emp (empno, ename, job, deptno) VALUES (?, ?, ?, ?)", "parameters": [9999, "TEST", "CLERK", 10]}' | jq '.' || echo "❌ Error: Could not execute INSERT query"

echo -e "\n7. Testing UPDATE query..."
curl -s -X POST "$BASE_URL/api/v1/oci8j-connector/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "UPDATE emp SET sal = ? WHERE empno = ?", "parameters": [5000, 9999]}' | jq '.' || echo "❌ Error: Could not execute UPDATE query"

echo -e "\n8. Testing DELETE query..."
curl -s -X POST "$BASE_URL/api/v1/oci8j-connector/query" \
  -H "Content-Type: application/json" \
  -d '{"query": "DELETE FROM emp WHERE empno = ?", "parameters": [9999]}' | jq '.' || echo "❌ Error: Could not execute DELETE query"

echo -e "\n✅ Tests completed"
