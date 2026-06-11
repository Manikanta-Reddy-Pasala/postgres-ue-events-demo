FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Ensure we have access to the parent dir's proto files
# Docker build context will be the root directory
COPY backend/gradle backend/gradle
COPY backend/gradlew backend/
COPY backend/settings.gradle backend/
COPY backend/build.gradle backend/
COPY backend/src backend/src
COPY proto proto

WORKDIR /app/backend
RUN chmod +x gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
