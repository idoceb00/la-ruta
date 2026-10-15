# ---- Build stage: compile the jar with the Maven wrapper ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Dependencies first, so this layer is cached until pom.xml changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Tests already run in CI and need Docker (Testcontainers), not available here
COPY src/ src/
RUN ./mvnw -B package -DskipTests

# ---- Runtime stage: only the JRE and the jar ----
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/target/*.jar app.jar
USER app

# Small CPU instances: faster startup and less memory over peak throughput
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:TieredStopAtLevel=1 -XX:+UseSerialGC"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]