FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY support-service/ support-service/

RUN chmod +x gradlew

RUN ./gradlew :support-service:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy

WORKDIR /workspace/app

COPY --from=build /workspace/app/support-service/build/libs/support-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
