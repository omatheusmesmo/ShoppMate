# Stage 1: Build environment
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for better security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built artifact from the build stage
# We use a wildcard to capture the jar, but ensure only one is copied if possible
COPY --from=build /app/target/*.jar app.jar

# Runtime configuration
ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    SERVER_PORT=8080

EXPOSE 8080

# Execute the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]