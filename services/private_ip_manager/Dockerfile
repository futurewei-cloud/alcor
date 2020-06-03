# Dockerfile for Private Ip Manager

FROM openjdk:8

MAINTAINER Zhonghao Lyu <zlyu@futurewei.com>

EXPOSE 8080

# Generate container image and run container
COPY ./target/privateipmanager-0.1.0-SNAPSHOT.jar /app/AlcorPrivateIpManager-0.1.0.jar
#COPY ./config/machine.json /app/config/machine.json

CMD ["java", "-jar", "/app/AlcorPrivateIpManager-0.1.0.jar"]

