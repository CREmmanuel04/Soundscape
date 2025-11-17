# Multi-stage build for Spring Boot application
FROM eclipse-temurin:23-jdk AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./

# Make mvnw executable (for Linux)
RUN chmod +x mvnw || true

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:23-jre

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (Render uses PORT env variable)
EXPOSE ${PORT:-8080}

# Run the application with production profile
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
