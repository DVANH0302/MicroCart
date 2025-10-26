# Multi-stage Dockerfile for Bank Service

# Stage 1: Build
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app

# Copy gradle files for dependency caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (cached if no changes)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application (skip tests for faster Docker builds)
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/api/bank/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
