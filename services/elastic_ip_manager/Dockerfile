# Dockerfile for Elastic Ip Manager

FROM openjdk:11

MAINTAINER Wei Yuan <dakuaiera@sina.com>

EXPOSE 9000

# Generate container image and run container
COPY ./target/elasticipmanager-0.1.0-SNAPSHOT.jar /app/AlcorElasticIpManager-0.1.0.jar
#COPY ./config/machine.json /app/config/machine.json

CMD ["java", "-jar", "/app/AlcorElasticipManager-0.1.0.jar"]

