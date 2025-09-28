# Multi-stage build for security and smaller image size
# Stage 1: Build stage with Maven
FROM eclipse-temurin:8-jdk AS builder

# Maintainer information
LABEL maintainer="Oracle 8i Connector Team"
LABEL description="Oracle 8i Connector with classes12.jar"
LABEL version="1.2.0"

# Create working directory
WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy configuration files
COPY pom.xml .
COPY lib/classes12.jar lib/

# Copy source code
COPY src/ src/

# Compile the application
RUN mvn clean package -DskipTests

# Verify that the JAR was created
RUN ls -la target/

# Stage 2: Runtime stage with non-root user
FROM eclipse-temurin:8-jre

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Create application directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/oracle8i-connector-1.2.0.jar app.jar

# Copy Oracle driver
COPY --from=builder /app/lib/classes12.jar lib/

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/oci8j-connector/healthz || exit 1

# Startup command
CMD ["java", "-jar", "/app/app.jar"]
