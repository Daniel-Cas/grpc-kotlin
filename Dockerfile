FROM amazoncorretto:21-alpine-jdk as build
WORKDIR /app

RUN apk add --no-cache protobuf-dev

COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradlew /app/
COPY src src

RUN ./gradlew generateProto
RUN ./gradlew build

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*-shadow.jar app.jar
RUN chmod +x /app/app.jar

ENTRYPOINT ["java", "-jar", \
    "-Dnetworkaddress.cache.ttl=60", \
    "-Dnetworkaddress.cache.negative.ttl=10", \
    "-Duser.timezone=UTC", \
    "-Xms256m", "-Xmx900m", \
    "-XX:MinHeapFreeRatio=15", \
    "-XX:MaxHeapFreeRatio=30", \
    "--enable-preview", \
    "-Dkotlinx.coroutines.virtual.threads=true", \
    "/app/app.jar", \
    "app:grpc", "-instances", "1"]