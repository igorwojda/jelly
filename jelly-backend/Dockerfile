FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
# Separate layer so dependencies are cached until build files change
RUN ./gradlew dependencies --no-daemon > /dev/null
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home jelly
COPY --from=build /workspace/build/libs/jellybackend.jar app.jar
USER jelly
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
