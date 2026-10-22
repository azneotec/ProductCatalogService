# Builds and runs ProductCatalogService as a container: docker build -t product-catalog-service .
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/ProductCatalogService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8002
ENTRYPOINT ["java", "-jar", "app.jar"]
