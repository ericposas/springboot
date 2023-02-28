FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
RUN ./gradlew --stop

ENTRYPOINT [ "./gradlew", "--no-daemon", "bootRun" ]
