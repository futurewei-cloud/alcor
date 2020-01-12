# Dockerfile for Alcor Controller

FROM openjdk:8

MAINTAINER Liguang Xie <lxie@futurewei.com>

# Alcor Controller process
EXPOSE 8080
# Alcor Controller admin process

# Generate container image and run container
COPY ./target/AlcorController-0.1.0-SNAPSHOT.jar /app/AlcorController-0.1.0.jar
COPY ./config/machine.json /app/config/machine.json
WORKDIR /app
CMD ["java", "-Dspring.profiles.active=k8s", "-jar", "AlcorController-0.1.0.jar"]

# Alcor entry points
