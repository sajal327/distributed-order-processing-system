FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml ./pom.xml

COPY order-service/pom.xml ./order-service/pom.xml
COPY inventory-service/pom.xml ./inventory-service/pom.xml
COPY payment-service/pom.xml ./payment-service/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml

COPY inventory-service/src ./inventory-service/src

RUN mvn -q -pl inventory-service -am -DskipTests package


#FROM eclipse-temurin:21-jre-alpine
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

COPY --from=build /build/inventory-service/target/inventory-service-0.1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]