# Oracle 8i Connector

Spring Boot application to connect to Oracle 8i using the `classes12.jar` driver. Allows executing SQL queries through a REST API.

## Features

- ✅ Oracle 8i connection using `classes12.jar`
- ✅ Configuration through `config.yaml`
- ✅ Override with environment variables
- ✅ REST API for executing SQL queries
- ✅ Support for all SQL query types (SELECT, INSERT, UPDATE, DELETE, etc.)
- ✅ JSON format responses
- ✅ Health check endpoint
- ✅ Optional Basic Authentication

## Configuration

### config.yaml file

Copy `config.example.yaml` to `config.yaml` and update with your actual Oracle connection details:

```yaml
oracle8i:
  host: 1.2.3.4                    # Replace with your Oracle server IP
  port: 1521                       # Replace with your Oracle port
  sid: orcl                        # Replace with your Oracle SID
  username: username               # Replace with your Oracle username
  password: password               # Replace with your Oracle password
  driver: oracle.jdbc.driver.OracleDriver
  # Timeout configurations (in milliseconds)
  connection_timeout: 30000        # Connection timeout (30 seconds)
  socket_timeout: 60000            # Socket timeout (60 seconds)
  query_timeout: 120000            # Query execution timeout (120 seconds)
```

### Environment Variables

You can override the configuration using environment variables:

```bash
export ORACLE_HOST=1.2.3.4
export ORACLE_PORT=1521
export ORACLE_SID=orcl
export ORACLE_USERNAME=username
export ORACLE_PASSWORD=password
export ORACLE_CONNECTION_TIMEOUT=30000
export ORACLE_SOCKET_TIMEOUT=60000
export ORACLE_QUERY_TIMEOUT=120000
```

### Basic Authentication (Optional)

The application supports optional Basic Authentication. It's only enabled if both username and password are provided:

```yaml
basic_auth:
  enabled: true                        # Set to true to enable Basic Auth
  username: admin                      # Your desired username
  password: secretpassword             # Your desired password
```

Or using environment variables:

```bash
export BASIC_AUTH_ENABLED=true
export BASIC_AUTH_USERNAME=admin
export BASIC_AUTH_PASSWORD=secretpassword
```

**Note:** If Basic Auth is enabled, all API endpoints will require authentication. The `/healthz` endpoint remains accessible without authentication for monitoring purposes.

## Compilation and Execution

### Compile

```bash
mvn clean compile
```

### Run

```bash
mvn spring-boot:run
```

### Create JAR

```bash
mvn clean package
java -jar target/oracle8i-connector-1.0.0.jar
```

## API Endpoints

### POST /api/v1/oci8j-connector/query

Executes a SQL query and returns the results in JSON format.

**Request Body:**
```json
{
  "query": "SELECT * FROM emp WHERE deptno = ?",
  "parameters": [10]
}
```

**Response (SELECT query):**
```json
{
  "success": true,
  "message": "Query executed successfully",
  "data": [
    {
      "EMPNO": 7782,
      "ENAME": "CLARK",
      "JOB": "MANAGER",
      "DEPTNO": 10
    }
  ],
  "rowCount": 1,
  "executionTimeMs": 45
}
```

**Response (INSERT/UPDATE/DELETE query):**
```json
{
  "success": true,
  "message": "Query executed successfully. 1 rows affected.",
  "data": null,
  "rowCount": 1,
  "executionTimeMs": 23
}
```

### GET /api/v1/oci8j-connector/healthz

**Liveness Probe** - Verifies the application is running (always returns 200 OK).

**Response:**
```json
{
  "status": "UP",
  "database": "CONNECTED",
  "timestamp": 1640995200000,
  "uptime": 45000
}
```

### GET /api/v1/oci8j-connector/ready

**Readiness Probe** - Verifies the application is ready to serve traffic (includes database connectivity).

**Response:**
```json
{
  "status": "READY",
  "database": "CONNECTED",
  "timestamp": 1640995200000
}
```

**Note:** For Kubernetes deployments, use `/healthz` for liveness probes and `/ready` for readiness probes.

### GET /api/v1/oci8j-connector/info

Gets application information.

**Response:**
```json
{
  "name": "Oracle 8i Connector",
  "version": "1.0.0",
  "description": "Oracle 8i connector using classes12.jar",
  "endpoints": {
    "query": "POST /api/v1/oci8j-connector/query",
    "healthz": "GET /api/v1/oci8j-connector/healthz",
    "info": "GET /api/v1/oci8j-connector/info"
  }
}
```

## Usage Examples

### Simple query

```bash
curl -X POST http://localhost:8080/api/v1/oci8j-connector/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM emp LIMIT 5"}'
```

### Query with parameters

```bash
curl -X POST http://localhost:8080/api/v1/oci8j-connector/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM emp WHERE deptno = ?", "parameters": [10]}'
```

### INSERT query

```bash
curl -X POST http://localhost:8080/api/v1/oci8j-connector/query \
  -H "Content-Type: application/json" \
  -d '{"query": "INSERT INTO emp (empno, ename, job, deptno) VALUES (?, ?, ?, ?)", "parameters": [9999, "TEST", "CLERK", 10]}'
```

### UPDATE query

```bash
curl -X POST http://localhost:8080/api/v1/oci8j-connector/query \
  -H "Content-Type: application/json" \
  -d '{"query": "UPDATE emp SET sal = ? WHERE empno = ?", "parameters": [5000, 9999]}'
```

### DELETE query

```bash
curl -X POST http://localhost:8080/api/v1/oci8j-connector/query \
  -H "Content-Type: application/json" \
  -d '{"query": "DELETE FROM emp WHERE empno = ?", "parameters": [9999]}'
```

### Check status

```bash
curl http://localhost:8080/api/v1/oci8j-connector/healthz
```

## Security

### Query Security Configuration

The application supports configurable query security through a forbidden keywords list. This allows you to block specific SQL keywords to prevent potentially dangerous operations.

#### Configuration

Add the security section to your `config.yaml`:

```yaml
# Security configuration
security:
  # List of forbidden keywords in SQL queries (empty by default)
  # Configure according to your security requirements
  forbidden_keywords: []
```

#### Example Restrictive Configuration

```yaml
security:
  forbidden_keywords:
    - "DROP"
    - "DELETE"
    - "UPDATE"
    - "INSERT"
    - "TRUNCATE"
    - "ALTER"
    - "CREATE"
    - "GRANT"
    - "REVOKE"
```

#### Behavior

- **Empty list**: No restrictions applied (default behavior)
- **Keywords configured**: Queries containing forbidden keywords are blocked
- **Case insensitive**: Validation is case-insensitive
- **Error response**: Blocked queries return a 400 error with details

#### Example Usage

```bash
# Allowed query (if no restrictions)
curl -X POST http://localhost:8080/api/v1/oci8j-connector/query \
  -H "Content-Type: application/json" \
  -d '{"query": "SELECT * FROM users WHERE id = 1"}'

# Blocked query (if DROP is in forbidden list)
curl -X POST http://localhost:8080/api/v1/oci8j-connector/query \
  -H "Content-Type: application/json" \
  -d '{"query": "DROP TABLE users"}'
# Response: {"success": false, "message": "Query blocked due to forbidden keyword: DROP"}
```

#### Recommended Keywords to Block

##### DDL Commands (Data Definition Language)
- `DROP` - Delete database objects
- `CREATE` - Create database objects
- `ALTER` - Modify object structure
- `TRUNCATE` - Empty tables
- `RENAME` - Rename objects
- `COMMENT` - Add comments

##### DML Commands (Data Manipulation Language)
- `DELETE` - Delete records
- `UPDATE` - Modify records
- `INSERT` - Insert records
- `MERGE` - Combine data
- `REPLACE` - Replace data

##### Transaction Control Commands
- `COMMIT` - Commit transactions
- `ROLLBACK` - Rollback transactions
- `SAVEPOINT` - Create savepoints
- `LOCK` - Lock resources
- `UNLOCK` - Unlock resources

##### System Control Commands
- `GRANT` - Grant privileges
- `REVOKE` - Revoke privileges
- `AUDIT` - Audit actions
- `NOAUDIT` - Disable auditing
- `SHUTDOWN` - Shutdown database
- `STARTUP` - Startup database

##### Stored Procedure Commands
- `EXEC` - Execute procedures
- `EXECUTE` - Execute procedures
- `CALL` - Call procedures
- `BEGIN` - Start PL/SQL block
- `END` - End PL/SQL block
- `DECLARE` - Declare variables
- `VARIABLE` - Define variables
- `CONSTANT` - Define constants

##### Flow Control Commands
- `IF` - Conditional statements
- `CASE` - Decision structures
- `WHEN` - Conditional clauses
- `THEN` - Action clauses
- `ELSE` - Alternative clauses
- `WHILE` - Conditional loops
- `FOR` - Count loops
- `LOOP` - Infinite loops
- `REPEAT` - Loops with end condition
- `RETRY` - Retry operations
- `WAIT` - Wait operations
- `SLEEP` - Pause execution
- `DELAY` - Delay execution
- `TIMEOUT` - Time limits
- `CANCEL` - Cancel operations
- `ABORT` - Abort operations
- `KILL` - Terminate processes
- `TERMINATE` - End processes
- `SUSPEND` - Suspend processes
- `RESUME` - Resume processes

##### Optimization and Performance Commands
- `ANALYZE` - Analyze objects
- `EXPLAIN` - Explain execution plans
- `OPTIMIZE` - Optimize queries
- `TUNE` - Tune performance
- `ADVISE` - Advise optimization
- `RECOMMEND` - Recommend changes
- `SUGGEST` - Suggest improvements
- `HINT` - Optimization hints
- `PARALLEL` - Parallel execution
- `NOPARALLEL` - Disable parallelism

##### Utility Commands
- `DESCRIBE` - Describe objects
- `SHOW` - Show information
- `HELP` - Help
- `QUIT` - Quit
- `EXIT` - Exit
- `CONNECT` - Connect
- `DISCONNECT` - Disconnect
- `SET` - Set variables
- `RESET` - Reset variables
- `CLEAR` - Clear screen
- `SPOOL` - Redirect output
- `START` - Start script
- `STOP` - Stop script
- `PAUSE` - Pause execution
- `RESUME` - Resume execution

##### Recovery and Backup Commands
- `RECOVER` - Recover database
- `FLASHBACK` - Time travel
- `PURGE` - Clean obsolete data
- `BACKUP` - Backup data
- `RESTORE` - Restore data
- `LOAD` - Load data
- `COPY` - Copy data
- `IMPORT` - Import data
- `EXPORT` - Export data

##### Validation and Maintenance Commands
- `VALIDATE` - Validate data
- `INVALIDATE` - Invalidate objects
- `REFRESH` - Refresh views
- `REBUILD` - Rebuild objects
- `REORGANIZE` - Reorganize data
- `COMPILE` - Compile objects
- `DEBUG` - Debug code
- `TRACE` - Trace execution
- `PROFILE` - Profile performance

#### Security Recommendations

1. **Development**: Keep list empty for maximum flexibility
2. **Testing**: Add destructive commands (`DROP`, `DELETE`, `UPDATE`)
3. **Production**: Restrictive list with read-only commands
4. **Auditing**: Review logs for unauthorized access attempts
5. **Monitoring**: Set up alerts when forbidden keywords are detected

### General Security Notes

- All SQL query types are allowed by default (SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, etc.)
- Basic input validation (empty query check)
- Configurable keyword blocking for enhanced security
- **Warning**: This allows full database access - use with caution in production
- **Recommendation**: Configure forbidden keywords based on your security requirements

## Requirements

- Java 8+
- Maven 3.6+
- Oracle 8i with network access
- `classes12.jar` driver (included in the project)

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/connectors/oracle8i/
│   │       ├── Oracle8iConnectorApplication.java
│   │       ├── config/
│   │       │   └── Oracle8iConfig.java
│   │       ├── controller/
│   │       │   └── Oracle8iController.java
│   │       ├── model/
│   │       │   ├── QueryRequest.java
│   │       │   └── QueryResponse.java
│   │       └── service/
│   │           └── Oracle8iService.java
│   └── resources/
│       ├── application.yml
│       └── config.yaml
├── lib/
│   └── classes12.jar
├── config.example.yaml
├── examples.sql
├── start.sh
├── test-api.sh
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```
