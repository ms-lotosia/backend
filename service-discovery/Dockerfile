# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle build.gradle ./

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies \
    --no-daemon \
    --build-cache \
    --scan=false

COPY service-discovery/ service-discovery/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :service-discovery:bootJar \
    --no-daemon \
    --build-cache \
    --parallel \
    --scan=false \
    -x test

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN useradd -r -u 1001 appuser
USER appuser

COPY --from=build /workspace/app/service-discovery/build/libs/service-discovery-*.jar app.jar

EXPOSE 8761

ENTRYPOINT [ \
  "java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", \
  "app.jar" \
]

FROM build AS ci-build

ARG JAR_PATH=jars

COPY ${JAR_PATH}/*.jar /workspace/app/service-discovery/build/libs/ || true
