FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY content-service/ content-service/

RUN chmod +x gradlew

RUN ./gradlew :content-service:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /workspace/app

COPY --from=build /workspace/app/content-service/build/libs/content-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

FROM runtime AS ci-build

WORKDIR /workspace/app
