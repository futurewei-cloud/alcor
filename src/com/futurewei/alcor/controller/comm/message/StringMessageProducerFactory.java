package com.futurewei.alcor.controller.comm.message;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class StringMessageProducerFactory extends MessageProducerFactory {
    @Override
    public Serializer getSerializer() {
        return new StringSerializer();
    }
}
