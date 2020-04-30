# Dockerfile for VPC Manager

FROM openjdk:8

MAINTAINER xzhang2 <xzhang2@futurewei.com>

# Data Plane Manager process
EXPOSE 8080

# Generate container image and run container
COPY ./target/DataPlaneManager-0.1.0-SNAPSHOT.jar /app/DataPlaneManager-0.1.0-SNAPSHOT.jar
COPY ./src/main/resources/machine.json /app/config/machine.json

CMD ["java", "-jar", "/app/DataPlaneManager-0.1.0-SNAPSHOT.jar"]

