FROM maven:3.9.5-eclipse-temurin-17 AS build

COPY pom.xml /app/
COPY src /app/src
COPY *.properties /app/*.properties

RUN mvn -f /app/pom.xml clean package

FROM --platform=linux/amd64 openjdk:17-alpine3.14

COPY --from=build /app/target/*.jar /app/biolink-api.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app/biolink-api.jar"]