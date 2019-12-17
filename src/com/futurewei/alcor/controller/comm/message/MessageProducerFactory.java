package com.futurewei.alcor.controller.comm.message;

import com.futurewei.alcor.controller.interfaces.AbstractFactory;
import com.futurewei.alcor.controller.comm.config.IKafkaConfiguration;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;

import java.util.Properties;

public abstract class MessageProducerFactory implements AbstractFactory<Producer> {

    @Value("${apache.kafka.address}")
    private String kafkaAddress;

    public Producer Create() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaAddress);
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
