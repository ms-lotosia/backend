FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY payment-service/ payment-service/

RUN chmod +x gradlew

RUN ./gradlew :payment-service:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /workspace/app

COPY --from=build /workspace/app/payment-service/build/libs/payment-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

FROM runtime AS ci-build

WORKDIR /workspace/app
