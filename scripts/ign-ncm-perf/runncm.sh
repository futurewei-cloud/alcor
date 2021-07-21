#! /bin/sh
OPTS="\
-Djava.net.preferIPv4Stack=true \
-Xms40G \
-XX:MaxMetaspaceSize=1G 
-Xmx40G \
-XX:NewSize=512m \
-XX:SurvivorRatio=6 \
-XX:+AlwaysPreTouch \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=2000 \
-XX:GCTimeRatio=4 \
-XX:InitiatingHeapOccupancyPercent=30 \
-XX:G1HeapRegionSize=33554432 \
-XX:ParallelGCThreads=8 \
-XX:ConcGCThreads=8 \
-XX:G1HeapWastePercent=10 \
-XX:+UseTLAB \
-XX:+ScavengeBeforeFullGC \
-XX:+DisableExplicitGC \
-XX:+PrintGCDetails \
-XX:+FlightRecorder \
-XX:+UnlockDiagnosticVMOptions \
-XX:+DebugNonSafepoints \
-Xloggc:/tmp/alcor-gc-logs/ncm-11839-gc.log \
--class-path=/home/user/apache-ignite-2.9.1/libs \
--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
--add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
--add-exports=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
--add-exports=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
--illegal-access=permit \
--add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED
"

MSDIR="/home/user/alcor-rio/services"
MSJAR="network_config_manager/target/networkconfigmanager-0.1.0-SNAPSHOT.jar"


java ${OPTS} -jar ${MSDIR}/${MSJAR}
