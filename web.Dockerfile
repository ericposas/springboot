#Build stage
FROM gradle:latest AS BUILD
WORKDIR /usr/app
COPY . . 
RUN gradle build

# Package stage
FROM openjdk:latest
WORKDIR /usr/app
COPY --from=BUILD /usr/app .
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/usr/app/build/libs/api-0.0.1-SNAPSHOT.jar" ]
