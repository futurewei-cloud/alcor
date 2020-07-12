# Dockerfile for VPC Manager

FROM jboss/base-jdk:11

MAINTAINER Zhonghao Lyu <zlyu@futurewei.com>

#ENV FOLDER "./target/swagger"

#ENV API_URL "http://localhost:9001/swagger-ui.html#!"
#ENV API_KEY "**None**"
#ENV SWAGGER_JSON "/app/swagger.json"
#ENV PORT 801

EXPOSE 9001

# Generate container image and run container
#COPY ./target/swagger/swagger.json /app/swagger.json
COPY ./target/vpcmanager-0.1.0-SNAPSHOT.jar /app/AlcorVpcManager-0.1.0.jar

CMD java -jar /app/AlcorVpcManager-0.1.0.jar \
    --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --illegal-access=permit