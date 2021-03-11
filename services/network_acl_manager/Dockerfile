# Dockerfile for Elastic Ip Manager

FROM jboss/base-jdk:11

MAINTAINER Wei Yuan <dakuaiera@sina.com>

EXPOSE 9013

# Generate container image and run container
COPY ./target/network-acl-manager-0.0.1-SNAPSHOT.jar /app/AlcorNetworkAclManager-0.0.1.jar

CMD java -jar /app/AlcorNetworkAclManager-0.0.1.jar \
    --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --illegal-access=permit
