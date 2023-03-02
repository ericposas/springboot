FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .

ENTRYPOINT [ "java", "-jar", "/app/build/libs/api-0.0.1-SNAPSHOT.jar" ]
