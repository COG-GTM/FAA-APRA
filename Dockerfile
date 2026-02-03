# FAA Aeronautical Product Release API (APRA) - Modernized Container Image
# Multi-stage build for optimized production image

# Stage 1: Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml first for better layer caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds, tests run in CI)
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Production runtime image
FROM eclipse-temurin:17-jre-alpine

# Security: Run as non-root user
RUN addgroup -g 1001 -S apra && \
    adduser -u 1001 -S apra -G apra

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/apra.jar /app/apra.jar

# Create directories for logs and config
RUN mkdir -p /var/log/apra /opt/apra/conf && \
    chown -R apra:apra /app /var/log/apra /opt/apra

# Switch to non-root user
USER apra

# Expose application port
EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/apra/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=production"

# Application entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/apra.jar"]
