FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY . .
# CMD [ "./gradlew", "build", "--continuous", "-xtest" ]
CMD [ "./gradlew", "--stop" ]
