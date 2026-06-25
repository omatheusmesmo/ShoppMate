# Stage 1: Build environment
FROM maven:3.9.6-eclipse-temurin-17@sha256:29a1658b1f3078e07c2b17f7b519b45eb47f65d9628e887eac45a8c5c8f939d4 AS build
WORKDIR /app

# Copy pom.xml first and download dependencies to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment
FROM eclipse-temurin:17-jre@sha256:d226a3132511d057267330beeca73804275899aacdea331b7bd8313bb1506a7a
WORKDIR /app

# Create a non-root user for better runtime security
RUN groupadd --system spring && useradd --system --gid spring spring

# Copy only runtime artifacts into the final image
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
