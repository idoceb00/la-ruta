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

# Let the heap use most of the container memory (the JVM default is 25%)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]