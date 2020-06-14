# Dockerfile for Security Group Manager

FROM openjdk:8

MAINTAINER Piaoping Chen <chenpiaoping@yeah.net>

EXPOSE 8080

# Generate container image and run container
COPY ./target/securitygroupmanager-0.0.1-SNAPSHOT.jar /app/AlcorSecurityGroupManager-0.0.1-SNAPSHOT.jar
#COPY ./config/machine.json /app/config/machine.json

CMD ["java", "-jar", "/app/AlcorSecurityGroupManager-0.0.1-SNAPSHOT.jar"]

