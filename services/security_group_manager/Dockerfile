# Dockerfile for Security Group Manager

FROM jboss/base-jdk:11

MAINTAINER Piaoping Chen <chenpiaoping@yeah.net>

EXPOSE 9008

# Generate container image and run container
COPY ./target/securitygroupmanager-0.0.1-SNAPSHOT.jar /app/AlcorSecurityGroupManager-0.0.1-SNAPSHOT.jar

CMD java -jar /app/AlcorSecurityGroupManager-0.1.0.jar \
    --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --illegal-access=permit