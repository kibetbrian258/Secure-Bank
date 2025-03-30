# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add app user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built jar from build stage
COPY --from=build /app/target/SecureBank-0.0.1-SNAPSHOT.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Define entrypoint
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

# Expose port 8080
EXPOSE 8080