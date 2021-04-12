pulsar-admin functions create \
--jar target/dataplanemanager-0.1.0-SNAPSHOT.jar \
--classname com.futurewei.alcor.dataplane.client.pulsar.function.UnicastFunction \
--tenant public --namespace default \
--name unicast-function \
--inputs persistent://public/default/unicast-topic1 \
--output persistent://public/default/group-topic1

pulsar-admin functions create \
--jar target/dataplanemanager-0.1.0-SNAPSHOT.jar \
--classname com.futurewei.alcor.dataplane.client.pulsar.MulticastFunction \
--tenant public --namespace default \
--name multicast-function \
--inputs persistent://public/default/multicast-topic1 \
--output persistent://public/default/group-topic1