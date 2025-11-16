FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace/app

COPY build.gradle settings.gradle* ./
COPY gradlew ./
COPY gradle gradle
RUN chmod +x ./gradlew

COPY content-service/build.gradle content-service/
RUN ./gradlew :content-service:dependencies --no-daemon || true

COPY content-service/src content-service/src
ENV GRADLE_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m -Dorg.gradle.caching=true -Dorg.gradle.parallel=true"
RUN --mount=type=cache,target=/root/.gradle/caches     --mount=type=cache,target=/root/.gradle/wrapper     ./gradlew :content-service:build -x test --no-daemon --build-cache --parallel

FROM eclipse-temurin:17-jre-jammy
VOLUME /tmp
ARG JAR_FILE=content-service/build/libs/*.jar
COPY --from=build /workspace/app/${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]