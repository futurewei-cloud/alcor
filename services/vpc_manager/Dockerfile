# Dockerfile for VPC Manager

FROM openjdk:11

MAINTAINER Zhonghao Lyu <zlyu@futurewei.com>

#ENV FOLDER "./target/swagger"

#ENV API_URL "http://localhost:9001/swagger-ui.html#!"
#ENV API_KEY "**None**"
#ENV SWAGGER_JSON "/app/swagger.json"
#ENV PORT 801

# VPC Manager process
EXPOSE 8080
# VPC Manager admin process

# Generate container image and run container
#COPY ./target/swagger/swagger.json /app/swagger.json
COPY ./target/vpcmanager-0.1.0-SNAPSHOT.jar /app/AlcorVpcManager-0.1.0.jar

CMD ["java", "-jar", "/app/AlcorVpcManager-0.1.0.jar"]
