# Dockerfile for Port Manager

FROM jboss/base-jdk:11

MAINTAINER Piaoping Chen <chenpiaoping@yeah.net>

EXPOSE 9006

# Generate container image and run container
COPY ./target/portmanager-0.1.0-SNAPSHOT.jar /app/AlcorPortManager-0.1.0.jar

CMD java -jar /app/AlcorPortManager-0.1.0.jar \
    --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --illegal-access=permit

