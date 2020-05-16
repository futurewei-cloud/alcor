# Dockerfile for API Gateway

FROM openjdk:8

MAINTAINER Zhonghao Lyu <zlyu@futurewei.com>

# API Gateway process
EXPOSE 8080
# API Gateway admin process

# Generate container image and run container
COPY ./target/apigateway-0.1.0-SNAPSHOT.jar /app/AlcorApiGateway-0.1.0.jar
#COPY ./config/machine.json /app/config/machine.json

CMD ["java", "-jar", "/app/AlcorApiGateway-0.1.0.jar"]

