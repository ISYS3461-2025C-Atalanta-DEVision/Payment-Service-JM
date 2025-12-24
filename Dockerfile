# Multi-module Dockerfile for Render Deployment
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy all pom.xml files first (for dependency caching)
COPY pom.xml ./pom.xml
COPY payment-api/pom.xml ./payment-api/pom.xml
COPY payment-core/pom.xml ./payment-core/pom.xml
COPY payment-app/pom.xml ./payment-app/pom.xml

# Create source directories
RUN mkdir -p payment-api/src payment-core/src payment-app/src

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B || true

# Copy source code for all modules
COPY payment-api/src ./payment-api/src
COPY payment-core/src ./payment-core/src
COPY payment-app/src ./payment-app/src

# Build all modules (payment-app depends on payment-api and payment-core)
RUN mvn clean package -DskipTests -B -pl payment-app -am

# Runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Add curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy the built jar from payment-app module
COPY --from=builder /app/payment-app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port (Render will override with PORT env var)
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8083}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
