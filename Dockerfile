# Dockerfile for Alioth Controller

FROM openjdk:8

MAINTAINER Liguang Xie <lxie@futurewei.com>

# Alioth Controller process
EXPOSE 8080
# Alioth Controller admin process

# Generate container image and run container
COPY ./target/AliothController-0.1.0-SNAPSHOT.jar /app/AliothController-0.1.0.jar
WORKDIR /app
CMD ["java", "-jar", "AliothController-0.1.0.jar"]

# Alioth entry points
