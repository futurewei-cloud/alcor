# Dockerfile for VPC Manager

FROM openjdk:8

MAINTAINER Zhonghao Lyu <zlyu@futurewei.com>

# VPC Manager process
EXPOSE 9000
# VPC Manager admin process

# Generate container image and run container
COPY ./target/vpcmanager-0.1.0-SNAPSHOT.jar /app/AlcorVpcManager-0.1.0.jar
#COPY ./config/machine.json /app/config/machine.json

CMD ["java", "-jar", "/app/AlcorVpcManager-0.1.0.jar"]

