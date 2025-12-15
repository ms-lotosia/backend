FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

COPY service-discovery/ service-discovery/

RUN chmod +x gradlew

RUN ./gradlew :service-discovery:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy

WORKDIR /workspace/app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/app/service-discovery/build/libs/service-discovery-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
