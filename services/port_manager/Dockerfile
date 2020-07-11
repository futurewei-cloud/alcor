# Dockerfile for Port Manager

FROM openjdk:11

MAINTAINER Piaoping Chen <chenpiaoping@yeah.net>

EXPOSE 8080

# Generate container image and run container
COPY ./target/portmanager-0.0.1-SNAPSHOT.jar /app/AlcorPortManager-0.0.1-SNAPSHOT.jar
#COPY ./config/machine.json /app/config/machine.json

CMD ["java", "-jar", "/app/AlcorPortManager-0.0.1-SNAPSHOT.jar"]

