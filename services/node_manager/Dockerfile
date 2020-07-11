# Dockerfile for VPC Manager

FROM openjdk:11

MAINTAINER Liguang Xie <lxie@futurewei.com>

# Route Manager process
EXPOSE 8080
# ROute Manager admin process

# Generate container image and run container
COPY ./target/nodemanager-0.1.0-SNAPSHOT.jar /app/AlcorNodeManager-0.1.0.jar

CMD ["java", "-jar", "/app/AlcorNodeManager-0.1.0.jar"]

