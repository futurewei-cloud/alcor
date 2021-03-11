# Dockerfile for API Gateway

FROM jboss/base-jdk:11

MAINTAINER Zhonghao Lyu <zlyu@futurewei.com>

# Gateway Manager process
EXPOSE 9015
# Gateway Manager admin process

# Generate container image and run container
COPY ./target/gatewaymanager-0.1.0-SNAPSHOT.jar /app/AlcorGatewayManager-0.1.0.jar

CMD java -jar /app/AlcorGatewayManager-0.1.0.jar \
    --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --illegal-access=permit

