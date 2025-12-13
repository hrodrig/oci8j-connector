# Oracle 8i Connector v1.2.8

![Version](https://img.shields.io/badge/version-1.2.8-blue.svg)
![Java](https://img.shields.io/badge/Java-8-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Maven](https://img.shields.io/badge/Maven-3.9+-red.svg)
![Docker](https://img.shields.io/badge/Docker-ready-blue.svg)

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

### Security Note

**Important**: This project uses a secure configuration approach:
- **Sensitive files** (like `docker-compose.yml` with real credentials) are excluded from Git
- **Example files** (like `docker-compose.example.yml`) are included for reference
- **Configuration files** use environment variables for sensitive data

### config.yaml file

The application uses `src/main/resources/config.yaml` which is safe to commit because it uses environment variables for sensitive data. For reference, you can also check `config.example.yaml`:

```yaml
oracle8i:
  host: 1.2.3.4                    # Replace with your Oracle server IP
  port: 1521                       # Replace with your Oracle port
  sid: orcl                        # Replace with your Oracle SID
  username: username               # Replace with your Oracle username
  password: password               # Replace with your Oracle password
  driver: oracle.jdbc.driver.OracleDriver
  # Query timeout (in milliseconds)
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

## Quick Start with Makefile

The easiest way to build and run the application is using the included `Makefile`:

```bash
# Show all available commands
make help

# Build the Docker image
make build

# Start the service
make up

# Check health status
make health

# Execute a test query
make query QUERY="SELECT 1 FROM DUAL"
```

## Using Makefile

The project includes a comprehensive `Makefile` with convenient commands for building and managing the application.

### Available Commands

#### Build Commands

```bash
# Show help and current version
make help

# Build Docker image (uses host platform - recommended for local development)
# This will build for your current architecture (ARM64 on Apple Silicon, AMD64 on Intel)
make build

# Build for specific platforms (useful for cross-platform builds)
make build-arm64    # Build for linux/arm64 (Apple Silicon, ARM servers)
make build-amd64    # Build for linux/amd64 (Intel/AMD x86_64 servers)

# Generate build-info.properties manually (usually done automatically by Maven)
make generate-build-info
```

**When to use each build command:**
- `make build`: Use this for local development. It builds for your current platform.
- `make build-amd64`: Use this when you need to build for AMD64/x86_64 servers (most common for production).
- `make build-arm64`: Use this when you need to build for ARM64 servers (Apple Silicon, AWS Graviton, etc.).

All build commands use `--no-cache` to ensure a fresh build and tag images with both version and `latest`.

#### Service Management Commands

```bash
# Start the service
make up

# Stop the service
make down

# View service logs (follow mode)
make logs

# Check health status
make health
```

#### Query Execution

```bash
# Execute a test query
make query QUERY="SELECT 1 FROM DUAL"

# Execute a more complex query
make query QUERY="SELECT * FROM users WHERE id = 1"
```

### Version Management

The Makefile automatically reads the version from `pom.xml`. You can override it:

```bash
make build VERSION=1.2.8
```

Images are tagged with both the version and `latest`:
- `oci8j-connector:1.2.8` and `oci8j-connector:latest`
- `oci8j-connector:1.2.8-arm64` and `oci8j-connector:latest-arm64`
- `oci8j-connector:1.2.8-amd64` and `oci8j-connector:latest-amd64`

### Example Workflow

```bash
# 1. Build the image
make build

# 2. Start the service
make up

# 3. Check if it's running
make health

# 4. Execute a query
make query QUERY="SELECT 1 FROM DUAL"

# 5. View logs if needed
make logs

# 6. Stop when done
make down
```

## Compilation and Execution (Maven)

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
java -jar target/oracle8i-connector-1.2.8.jar
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

## Docker

### Docker Compose Setup

**Security Note**: The `docker-compose.yml` file with real credentials is excluded from Git for security reasons.

#### Quick Start (Using Makefile)

1. **Copy the example file:**
   ```bash
   cp docker-compose.example.yml docker-compose.yml
   ```

2. **Edit with your credentials:**
   ```bash
   # Edit docker-compose.yml with your actual Oracle connection details
   nano docker-compose.yml
   ```

3. **Build and start the service:**
   ```bash
   make build    # Build the Docker image
   make up        # Start the service
   ```

#### Quick Start (Using Docker Compose directly)

1. **Copy the example file:**
   ```bash
   cp docker-compose.example.yml docker-compose.yml
   ```

2. **Edit with your credentials:**
   ```bash
   # Edit docker-compose.yml with your actual Oracle connection details
   nano docker-compose.yml
   ```

3. **Start the service:**
   ```bash
   docker compose up -d
   ```

#### Environment Variables

The Docker Compose file supports the following environment variables:

```yaml
environment:
  # Oracle Database Configuration
  - ORACLE_HOST=your-oracle-host
  - ORACLE_PORT=1521
  - ORACLE_SID=your-sid
  - ORACLE_USERNAME=your-username
  - ORACLE_PASSWORD=your-password
  
  # Query timeout (in milliseconds)
  - ORACLE_QUERY_TIMEOUT=120000
  
  # Basic Authentication (optional)
  - BASIC_AUTH_ENABLED=false
  - BASIC_AUTH_USERNAME=admin
  - BASIC_AUTH_PASSWORD=your-secret-password
```

#### Health Check

The container includes a health check that verifies the API is responding:

```bash
# Using Makefile
make health        # Check health status
make logs          # View logs

# Using Docker Compose directly
docker compose ps
docker compose logs -f oracle8i-connector
```

### Important Notes

- **Oracle 8i Compatibility**: The connection URL uses the simple format `jdbc:oracle:thin:@host:port:sid` without timeout parameters, as Oracle 8i's `classes12.jar` driver doesn't support them in the URL.
- **Query Timeout**: Only `query_timeout` is configurable and is applied at the JDBC template level, not in the connection URL.

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
│   │       │   ├── Oracle8iConfig.java
│   │       │   ├── SecurityConfig.java
│   │       │   └── QuerySecurityConfig.java
│   │       ├── controller/
│   │       │   └── Oracle8iController.java
│   │       ├── model/
│   │       │   ├── QueryRequest.java
│   │       │   └── QueryResponse.java
│   │       ├── service/
│   │       │   ├── Oracle8iService.java
│   │       │   └── BuildInfoService.java
│   │       └── security/
│   │           └── BasicAuthFilter.java
│   └── resources/
│       ├── application.yml
│       ├── application-docker.yml
│       └── config.yaml
├── lib/
│   └── classes12.jar
├── config.example.yaml
├── docker-compose.example.yml
├── Dockerfile
├── .dockerignore
├── .gitignore
├── .gitattributes
├── k8s-deployment.yaml
├── generate-build-info.sh
├── test-api.sh
├── test-security.sh
├── cleanup-dsstore.sh
└── pom.xml
```

**Note**: Sensitive files like `docker-compose.yml` (with real credentials) are excluded from Git for security reasons.

## Disclaimer

**THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.**

This software is provided for use at your own risk. The authors and contributors shall not be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.

**By using this software, you acknowledge that you have read this disclaimer, understand it, and agree to be bound by its terms. You assume full responsibility for any consequences that may result from the use of this software, including but not limited to:**
- Data loss or corruption
- Security breaches
- Infrastructure failures
- Service interruptions
- Any other damages or losses

It is your responsibility to:
- Test the software thoroughly in a non-production environment
- Review and understand the code before deployment
- Implement appropriate security measures
- Maintain backups of your data
- Monitor the software in production

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
