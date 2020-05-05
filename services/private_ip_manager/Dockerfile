# Dockerfile for VPC Manager

FROM openjdk:8

MAINTAINER Zhonghao Lyu <zlyu@futurewei.com>

# Route Manager process
EXPOSE 9000

# ROute Manager admin process

# Generate container image and run container
COPY ./target/AlcorPrivateIpManager-0.1.0-SNAPSHOT.jar /app/AlcorPrivateIpManager-0.1.0.jar
#COPY ./config/machine.json /app/config/machine.json

CMD ["java", "-jar", "/app/AlcorPrivateIpManager-0.1.0.jar"]

