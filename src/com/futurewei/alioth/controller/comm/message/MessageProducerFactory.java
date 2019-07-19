package com.futurewei.alioth.controller.comm.message;

import com.futurewei.alioth.controller.comm.config.IKafkaConfiguration;
import com.futurewei.alioth.controller.interfaces.AbstractFactory;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Properties;

public abstract class MessageProducerFactory implements AbstractFactory<Producer> {

    public Producer Create() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, IKafkaConfiguration.KAFKA_BROKERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, IKafkaConfiguration.PRODUCER_CLIENT_ID);

        // Key is set as long and Value is given by concrete implementation
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());

        Serializer serializer = getSerializer();
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serializer.getClass().getName());

        //TODO: Optimizing partition
        // props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());

        return new KafkaProducer<>(props);
    }

    public abstract Serializer getSerializer();
}
