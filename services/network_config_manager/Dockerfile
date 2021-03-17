# Dockerfile for Port Manager

FROM jboss/base-jdk:11

MAINTAINER Liguang Xie <lxie@futurewei.com>

EXPOSE 9014 9016 9017

# Generate container image and run container
COPY ./target/networkconfigmanager-0.1.0-SNAPSHOT.jar /app/AlcorNetworkConfigManager-0.1.0.jar

CMD java -jar /app/AlcorNetworkConfigManager-0.1.0.jar \
    --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --illegal-access=permit

