#!/bin/bash

# Test script for Oracle 8i Connector Security Features
# This script demonstrates the forbidden keywords functionality

echo "🔒 Testing Oracle 8i Connector Security Features"
echo "================================================"
echo ""

# Configuration
BASE_URL="http://localhost:8080/api/v1/oci8j-connector"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to make API calls
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo -e "${BLUE}Testing:${NC} $description"
    echo -e "${YELLOW}Request:${NC} $method $endpoint"
    if [ ! -z "$data" ]; then
        echo -e "${YELLOW}Data:${NC} $data"
    fi
    echo ""
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" -H "$CONTENT_TYPE" -d "$data" "$BASE_URL$endpoint")
    fi
    
    # Split response and status code
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq 200 ]; then
        echo -e "${GREEN}✅ SUCCESS${NC} (HTTP $http_code)"
    else
        echo -e "${RED}❌ FAILED${NC} (HTTP $http_code)"
    fi
    
    echo -e "${YELLOW}Response:${NC} $body"
    echo ""
    echo "----------------------------------------"
    echo ""
}

# Check if application is running
echo "🔍 Checking if application is running..."
health_response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/healthz")

if [ "$health_response" -ne 200 ]; then
    echo -e "${RED}❌ Application is not running!${NC}"
    echo "Please start the application first:"
    echo "  mvn spring-boot:run"
    exit 1
fi

echo -e "${GREEN}✅ Application is running${NC}"
echo ""

# Test 1: Allowed query (should work with empty forbidden list)
make_request "POST" "/query" '{"query": "SELECT 1 as test_value FROM DUAL"}' "Allowed SELECT query"

# Test 2: Test with forbidden keywords (if configured)
echo -e "${BLUE}Testing forbidden keywords (if configured in config.yaml):${NC}"
echo ""

# Test DROP command
make_request "POST" "/query" '{"query": "DROP TABLE test_table"}' "DROP command (should be blocked if DROP is in forbidden list)"

# Test DELETE command
make_request "POST" "/query" '{"query": "DELETE FROM test_table WHERE id = 1"}' "DELETE command (should be blocked if DELETE is in forbidden list)"

# Test UPDATE command
make_request "POST" "/query" '{"query": "UPDATE test_table SET name = '\''hacked'\'' WHERE id = 1"}' "UPDATE command (should be blocked if UPDATE is in forbidden list)"

# Test INSERT command
make_request "POST" "/query" '{"query": "INSERT INTO test_table (id, name) VALUES (999, '\''test'\'')"}' "INSERT command (should be blocked if INSERT is in forbidden list)"

# Test ALTER command
make_request "POST" "/query" '{"query": "ALTER TABLE test_table ADD COLUMN new_column VARCHAR(50)"}' "ALTER command (should be blocked if ALTER is in forbidden list)"

# Test CREATE command
make_request "POST" "/query" '{"query": "CREATE TABLE malicious_table (id NUMBER, data VARCHAR(255))"}' "CREATE command (should be blocked if CREATE is in forbidden list)"

# Test GRANT command
make_request "POST" "/query" '{"query": "GRANT ALL PRIVILEGES ON test_table TO public"}' "GRANT command (should be blocked if GRANT is in forbidden list)"

# Test system commands
make_request "POST" "/query" '{"query": "SHUTDOWN IMMEDIATE"}' "SHUTDOWN command (should be blocked if SHUTDOWN is in forbidden list)"

# Test stored procedure execution
make_request "POST" "/query" '{"query": "EXEC malicious_procedure"}' "EXEC command (should be blocked if EXEC is in forbidden list)"

# Test complex malicious query
make_request "POST" "/query" '{"query": "SELECT * FROM users; DROP TABLE users; --"}' "Complex malicious query (should be blocked if DROP is in forbidden list)"

echo -e "${GREEN}🎉 Security testing completed!${NC}"
echo ""
echo -e "${YELLOW}Note:${NC} If you see 'SUCCESS' for forbidden commands, it means:"
echo "1. The forbidden keywords list is empty in your config.yaml, OR"
echo "2. The specific keyword is not in your forbidden list"
echo ""
echo -e "${YELLOW}To test with restrictions, add keywords to config.yaml:${NC}"
echo "security:"
echo "  forbidden_keywords:"
echo "    - \"DROP\""
echo "    - \"DELETE\""
echo "    - \"UPDATE\""
echo "    - \"INSERT\""
echo "    - \"ALTER\""
echo "    - \"CREATE\""
echo "    - \"GRANT\""
echo "    - \"SHUTDOWN\""
echo "    - \"EXEC\""
