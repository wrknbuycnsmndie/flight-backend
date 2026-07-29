FROM maven:3.9.16-eclipse-temurin-17 AS build

WORKDIR /build

COPY pom.xml .
COPY src/main ./src/main
RUN mvn -B package -Dmaven.test.skip=true && \
    java -Djarmode=tools -jar target/*.jar extract \
        --layers --destination extracted && \
    mv extracted/application/*.jar extracted/application/application.jar

FROM gcr.io/distroless/java17-debian12:nonroot

WORKDIR /app

COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]