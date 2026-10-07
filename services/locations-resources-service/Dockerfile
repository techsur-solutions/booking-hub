# Multi-stage build: Maven builder + JRE runtime

# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
# Download dependencies for caching
RUN mvn dependency:go-offline -B
COPY src ./src
# Build the application
RUN mvn package -DskipTests -B

# Stage 2: Runtime with JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from builder stage
COPY --from=builder /app/target/locations-resources-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
