# Use official JDK 17 image
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set work directory
WORKDIR /app

# Copy Maven wrapper & pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# ==========================
# Final lightweight image
# ==========================
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
