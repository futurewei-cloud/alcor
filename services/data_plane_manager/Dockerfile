# Dockerfile for Data Plane Manager

FROM openjdk:11

MAINTAINER xzhang2 <xzhang2@futurewei.com>

# Data Plane Manager process
EXPOSE 8080

# Generate container image and run container
COPY ./target/AlcorDataPlaneManager-0.1.0-SNAPSHOT.jar /app/AlcorDataPlaneManager-0.1.0-SNAPSHOT.jar


CMD ["java", "-jar", "/app/AlcorDataPlaneManager-0.1.0-SNAPSHOT.jar"]

