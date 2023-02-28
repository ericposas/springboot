FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

ENTRYPOINT [ "java", "-jar", "./build/libs/api-0.0.1-SNAPSHOT.jar" ]
