FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY api-gateway/ api-gateway/

RUN chmod +x gradlew

RUN ./gradlew :api-gateway:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /workspace/app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/app/api-gateway/build/libs/api-gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

FROM runtime AS ci-build

WORKDIR /workspace/app
