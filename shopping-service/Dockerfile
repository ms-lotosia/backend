# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:17-jdk-jammy@sha256:3b43b9984c60dbf042f0d921850ec1ddfb062ac1741e67f2b4904e336d84e4f0 AS build

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle build.gradle ./

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew \
    --no-daemon \
    --build-cache \
    tasks

COPY shopping-service/ shopping-service/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :shopping-service:bootJar \
    --no-daemon \
    --build-cache \
    --parallel \
    -x test

FROM gcr.io/distroless/java17-debian12@sha256:fd925ba431f3a6c1f1c8114ce1999ca38803220baf0fdf25a4c71b38db8af67f AS runtime

WORKDIR /app

COPY --from=build \
    /workspace/app/shopping-service/build/libs/shopping-service.jar \
    app.jar

EXPOSE 8080

ENTRYPOINT [ \
  "java", \
  "-XX:MaxRAMPercentage=70.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/urandom", \
  "-jar", \
  "app.jar" \
]

