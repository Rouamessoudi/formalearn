FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline || true
COPY src src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --retries=8 CMD curl -fsS http://127.0.0.1:8080/api/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
