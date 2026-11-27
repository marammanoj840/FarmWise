# Multi-stage build for FarmWise Spring Boot Backend
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S farmwise && adduser -S farmwise -G farmwise
COPY --from=build /workspace/target/*.jar app.jar
RUN chown -R farmwise:farmwise /app

USER farmwise
ENV PORT=8080
ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-8080}/api/v1/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
