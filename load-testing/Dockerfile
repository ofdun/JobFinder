FROM gradle:9.1-jdk25 AS build
WORKDIR /source
COPY JobFinder/gradle gradle
COPY JobFinder/gradlew JobFinder/build.gradle.kts JobFinder/settings.gradle.kts ./
COPY JobFinder/src src
COPY ci/testing.gradle /ci/testing.gradle
RUN --mount=type=cache,target=/home/gradle/.gradle gradle --no-daemon --console=plain bootJar -x test

FROM gradle:9.1-jdk25
WORKDIR /app
COPY --from=build /source/build/libs/JobFinder-0.0.1-SNAPSHOT.jar /app/application.jar
COPY JobFinder/src/test/resources/db/migration-test /fixtures
ENTRYPOINT ["java", "-Xms128m", "-Xmx384m", "-XX:ActiveProcessorCount=1", "-jar", "/app/application.jar"]
