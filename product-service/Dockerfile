FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY product-service/ product-service/

RUN chmod +x gradlew

RUN ./gradlew :product-service:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /workspace/app

COPY --from=build /workspace/app/product-service/build/libs/product-service-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

FROM runtime AS ci-build

WORKDIR /workspace/app
