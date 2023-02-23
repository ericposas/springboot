FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
CMD [ "./gradlew", "--stop" ]
