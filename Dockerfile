FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src
COPY frontend frontend

RUN mkdir -p src/main/resources/static \
    && cp -R frontend/. src/main/resources/static/

RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
