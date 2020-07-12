# Dockerfile for Data Plane Manager

FROM jboss/base-jdk:11

MAINTAINER xzhang2 <xzhang2@futurewei.com>

# Data Plane Manager process
EXPOSE 9010

# Generate container image and run container
COPY ./target/dataplanemanager-0.1.0-SNAPSHOT.jar /app/AlcorDataPlaneManager-0.1.0.jar

CMD java -jar /app/AlcorDataPlaneManager-0.1.0.jar \
    --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --illegal-access=permit
