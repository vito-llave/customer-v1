## --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Use quiet mode; skip tests to speed up image builds
RUN mvn -q -DskipTests clean package

## --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_NAME
# Copy the bootable jar produced by Spring Boot repackage
COPY --from=build /app/target/${JAR_NAME:-customer-v1-*.jar} app.jar

# Default profile; change or override at runtime
ENV SPRING_PROFILES_ACTIVE=crud-repo
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
