package com.futurewei.alioth.controller.comm.message;

import com.futurewei.alioth.controller.comm.config.IKafkaConfiguration;
import com.futurewei.alioth.controller.interfaces.AbstractFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Properties;

public abstract class MessageConsumerFactory implements AbstractFactory<Consumer> {

    public Consumer Create() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, IKafkaConfiguration.KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, IKafkaConfiguration.CONSUMER_GROUP_ID);

        // Key is set as long
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());

        Deserializer deserializer = getDeserializer();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass().getName());

        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, IKafkaConfiguration.MAX_POLL_RECORDS);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, IKafkaConfiguration.OFFSET_RESET_EARLIER);

        Consumer<Long, String> consumer = new KafkaConsumer<>(props);
        return consumer;
    }

    public abstract Deserializer getDeserializer();
}
